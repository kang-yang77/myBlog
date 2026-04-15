import { defineConfig } from 'vitepress'

export default defineConfig({
  // ==========================================
  // 1. 站点核心配置
  // ==========================================
  title: "YK's log", // 浏览器标签页标题
  description: "YK's log，记录后端工程、AI 应用与长期写作",
  base: '/',
  appearance: true,

  // 配置浏览器标签页的小图标 (Favicon)
  // 请确保 docs/public/favicon.ico 存在
  head: [
    ['link', { rel: 'icon', type: 'image/x-icon', href: '/favicon.ico?v=20260415' }],
    ['link', { rel: 'shortcut icon', type: 'image/x-icon', href: '/favicon.ico?v=20260415' }]
  ],

  // ==========================================
  // 2. 主题配置 (Theme Config)
  // ==========================================
  themeConfig: {
    // 左上角 Logo 和标题配置
    siteTitle: "YK's log", // 网站左上角文字
    // logo: '/logo.png',      // 网站左上角图标 (请确保 docs/public/logo.png 存在)

    // 顶部导航栏
    nav: [
      { text: 'Posts', link: '/' },
      { text: 'Archive', link: '/archive/index' },
      { text: 'Tags', link: '/tags/index' },
      { text: 'About', link: '/about/index' }
    ],
    // 社交链接
    socialLinks: [
      { icon: 'github', link: 'https://github.com/kang-yang77' }
    ],

    // 本地搜索
    search: { provider: 'local' },
    
    // 页脚
    footer: {
      message: 'Writing as a way to think.',
      // 在 copyright 中使用 HTML 标签添加备案号并链接到工信部
      copyright: 'Copyright © 2026 Yang Kang | <a href="https://beian.miit.gov.cn/" target="_blank" rel="noreferrer">蜀ICP备2026002075号</a>'
    }
  },
  
  // Markdown 显示行号
  markdown: { lineNumbers: true }
})
