export const LEVEL_RULES = [
  { level: 1, title: '旅行新手', min: 0, max: 99 },
  { level: 2, title: '城市探索者', min: 100, max: 299 },
  { level: 3, title: '旅途记录者', min: 300, max: 599 },
  { level: 4, title: '风景猎人', min: 600, max: 999 },
  { level: 5, title: '路线达人', min: 1000, max: 1599 },
  { level: 6, title: '目的地玩家', min: 1600, max: 2399 },
  { level: 7, title: '远行探险家', min: 2400, max: 3499 },
  { level: 8, title: '旅行领航员', min: 3500, max: 4999 },
  { level: 9, title: '星途旅者', min: 5000, max: 6999 },
  { level: 10, title: '传奇旅行家', min: 7000, max: Infinity },
]

export const getLevelInfo = (points = 0) => {
  const safePoints = Number(points) || 0
  const currentLevel = LEVEL_RULES.find(rule => safePoints >= rule.min && safePoints <= rule.max) || LEVEL_RULES[0]
  const nextLevel = LEVEL_RULES.find(rule => rule.level === currentLevel.level + 1) || null

  const pointsToNextLevel = nextLevel
    ? Math.max(nextLevel.min - safePoints, 0)
    : 0

  const levelProgress = nextLevel
    ? Math.max(
        0,
        Math.min(
          100,
          Math.round(((safePoints - currentLevel.min) / (nextLevel.min - currentLevel.min)) * 100)
        )
      )
    : 100

  return {
    currentLevel: {
      ...currentLevel,
      maxText: Number.isFinite(currentLevel.max) ? currentLevel.max : '∞',
    },
    nextLevel,
    pointsToNextLevel,
    levelProgress,
  }
}
