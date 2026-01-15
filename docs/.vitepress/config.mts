import { defineConfig } from 'vitepress'

export default defineConfig({
  // ==========================================
  // 1. 站点核心配置
  // ==========================================
  title: "ExitZero_", // 浏览器标签页标题
  description: "Java 后端与 AI 的融合之路",
  base: '/',

  // 配置浏览器标签页的小图标 (Favicon)
  // 请确保 docs/public/favicon.ico 存在
  head: [
    ['link', { rel: 'icon', href: '/favicon.ico' }]
  ],

  // ==========================================
  // 2. 主题配置 (Theme Config)
  // ==========================================
  themeConfig: {
    // 左上角 Logo 和标题配置
    siteTitle: 'ExitZero_', // 网站左上角文字
    // logo: '/logo.png',      // 网站左上角图标 (请确保 docs/public/logo.png 存在)

    // 顶部导航栏
    nav: [
      { text: '首页', link: '/' },
      { 
        text: '实战项目', 
        items: [
          { text: '短链接系统', link: '/projects/short-link/index' },
          { text: 'Text2SQL 助手', link: '/projects/text2sql/index' }
        ]
      },
      { text: '算法心得', link: '/algorithm/index' },
      { text: '基础知识', link: '/basics/index' },
      { text: '博客搭建', link: '/blogCreate/index' }
    ],
    // 社交链接
    socialLinks: [
      { icon: 'github', link: 'https://github.com/kang-yang77' }
    ],

    // 本地搜索
    search: { provider: 'local' },
    
    // 页脚
    footer: {
      message: 'Hello World!!.',
      // 在 copyright 中使用 HTML 标签添加备案号并链接到工信部
      copyright: `
        <div style="display: flex; justify-content: center; align-items: center; gap: 5px; flex-wrap: wrap;">
          <span>Copyright © 2026 ExitZero</span>
          <span>|</span>
          <img src="https://beian.miit.gov.cn/favicon.ico" style="width: 15px; height: 15px; display: inline-block;" />
          <a href="https://beian.miit.gov.cn/" target="_blank" rel="noreferrer" style="text-decoration: none; color: inherit;">
            蜀ICP备2026002075号
          </a>
        </div>
      `
    }
  },
  
  // Markdown 显示行号
  markdown: { lineNumbers: true }
})