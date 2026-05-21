import autoprefixer from 'autoprefixer'
import pxToViewport from 'postcss-px-to-viewport'

export default {
  plugins: [
    autoprefixer,
    pxToViewport({
      viewportWidth: 375,
      viewportHeight: 667,
      unitPrecision: 3,
      viewportUnit: 'vw',
      selectorBlackList: ['.ignore', '.hairlines'],
      minPixelValue: 1,
      mediaQuery: false,
    }),
  ],
}
