import type { Theme } from 'vitepress'
import DefaultTheme from 'vitepress/theme'
import Layout from './Layout.vue'
import BlogShowcase from './components/BlogShowcase.vue'
import BlogStudio from './components/BlogStudio.vue'
import SiteTimeline from './components/SiteTimeline.vue'
import StatusBadge from './components/StatusBadge.vue'
import StudioLanding from './components/StudioLanding.vue'
import PublicBlogHome from './components/PublicBlogHome.vue'
import PublicArchive from './components/PublicArchive.vue'
import PublicTags from './components/PublicTags.vue'
import './styles/custom.css'

export default {
  extends: DefaultTheme,
  Layout,
  enhanceApp({ app }) {
    app.component('BlogStudio', BlogStudio)
    app.component('BlogShowcase', BlogShowcase)
    app.component('SiteTimeline', SiteTimeline)
    app.component('StatusBadge', StatusBadge)
    app.component('StudioLanding', StudioLanding)
    app.component('PublicBlogHome', PublicBlogHome)
    app.component('PublicArchive', PublicArchive)
    app.component('PublicTags', PublicTags)
  }
} satisfies Theme
