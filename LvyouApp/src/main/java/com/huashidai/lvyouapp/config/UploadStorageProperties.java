package com.huashidai.lvyouapp.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * 统一管理上传文件根目录，避免运行目录变化导致资源丢失。
 */
@Component
@Slf4j
public class UploadStorageProperties {

    private static final String DEFAULT_RELATIVE_UPLOAD_DIR = ".lvyou-app/uploads";

    private static volatile Path uploadRootPath;

    @Value("${app.upload-dir:${user.home}/" + DEFAULT_RELATIVE_UPLOAD_DIR + "}")
    private String configuredUploadDir;

    @PostConstruct
    public void init() {
        Path targetRoot = Paths.get(configuredUploadDir).toAbsolutePath().normalize();
        uploadRootPath = targetRoot;
        ensureBaseDirectories(targetRoot);
        migrateLegacyUploadsIfNeeded(targetRoot);
        log.info("文件上传根目录: {}", targetRoot);
    }

    public static Path getUploadRootPath() {
        if (uploadRootPath == null) {
            Path fallback = Paths.get(System.getProperty("user.home"), ".lvyou-app", "uploads")
                    .toAbsolutePath()
                    .normalize();
            ensureBaseDirectories(fallback);
            uploadRootPath = fallback;
        }
        return uploadRootPath;
    }

    public static Path resolveFolderPath(String folder) {
        Path folderPath = getUploadRootPath().resolve(folder).normalize();
        if (!folderPath.startsWith(getUploadRootPath())) {
            throw new IllegalArgumentException("非法上传目录: " + folder);
        }
        return folderPath;
    }

    public static Path resolveResourcePath(String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) {
            return null;
        }

        String normalized = resourcePath.trim().replace("\\", "/");
        if (normalized.startsWith("http://") || normalized.startsWith("https://")) {
            return null;
        }

        String relativePath;
        if (normalized.startsWith("/uploads/")) {
            relativePath = normalized.substring("/uploads/".length());
        } else if (normalized.startsWith("/avatars/")) {
            relativePath = "avatars/" + normalized.substring("/avatars/".length());
        } else if (normalized.startsWith("/activities/")) {
            relativePath = "activities/" + normalized.substring("/activities/".length());
        } else if (normalized.startsWith("/guides/")) {
            relativePath = "guides/" + normalized.substring("/guides/".length());
        } else if (normalized.startsWith("/posts/")) {
            relativePath = "posts/" + normalized.substring("/posts/".length());
        } else {
            relativePath = normalized.startsWith("/") ? normalized.substring(1) : normalized;
        }

        Path resolved = getUploadRootPath().resolve(relativePath).normalize();
        if (!resolved.startsWith(getUploadRootPath())) {
            throw new IllegalArgumentException("非法文件路径: " + resourcePath);
        }
        return resolved;
    }

    private static void ensureBaseDirectories(Path uploadRoot) {
        try {
            Files.createDirectories(uploadRoot);
            Files.createDirectories(uploadRoot.resolve("avatars"));
            Files.createDirectories(uploadRoot.resolve("posts"));
            Files.createDirectories(uploadRoot.resolve("guides"));
            Files.createDirectories(uploadRoot.resolve("activities"));
            Files.createDirectories(uploadRoot.resolve("attractions"));
        } catch (IOException e) {
            throw new IllegalStateException("初始化上传目录失败: " + uploadRoot, e);
        }
    }

    private void migrateLegacyUploadsIfNeeded(Path targetRoot) {
        Path legacyRoot = Paths.get(System.getProperty("user.dir"), "src", "main", "webapp", "uploads")
                .toAbsolutePath()
                .normalize();

        if (targetRoot.equals(legacyRoot) || !Files.exists(legacyRoot)) {
            return;
        }

        try (Stream<Path> pathStream = Files.walk(legacyRoot)) {
            pathStream.forEach(source -> copyIfMissing(legacyRoot, targetRoot, source));
        } catch (IOException e) {
            log.warn("迁移旧上传目录失败: {}", e.getMessage());
        }
    }

    private void copyIfMissing(Path legacyRoot, Path targetRoot, Path source) {
        try {
            Path relative = legacyRoot.relativize(source);
            Path target = targetRoot.resolve(relative).normalize();
            if (!target.startsWith(targetRoot)) {
                return;
            }

            if (Files.isDirectory(source)) {
                Files.createDirectories(target);
                return;
            }

            if (Files.notExists(target)) {
                Files.createDirectories(target.getParent());
                Files.copy(source, target);
            }
        } catch (IOException e) {
            log.warn("迁移文件失败: {}", source, e);
        }
    }
}
