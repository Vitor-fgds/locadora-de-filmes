import postcssGlobalData from '@csstools/postcss-global-data'
import postcssCustomMedia from 'postcss-custom-media'

// Os breakpoints (@custom-media) ficam em um só arquivo e valem em todo CSS Module,
// sem precisar de @import em cada um.
export default {
  plugins: [
    postcssGlobalData({ files: ['./src/styles/breakpoints.css'] }),
    postcssCustomMedia(),
  ],
}
