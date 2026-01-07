import { defineConfig } from 'vitepress'

export default defineConfig({
  title: "杨康的技术博客", // 换成你的名字
  description: "Java后端 | AI落地 | 算法心得",
  base: '/',

  themeConfig: {
    // 1. 顶部导航栏 (关键修改点)
    nav: [
      { text: '首页', link: '/' },
      { 
        text: '🚀 实战项目', 
        items: [
          { text: '🔗 短链接系统 (Java)', link: '/projects/short-link/index' },
          { text: '🤖 Text2SQL 工具 (AI)', link: '/projects/text2sql/index' }
        ]
      },
      { text: '⚡ 算法心得', link: '/algorithm/index' },
      { text: '📚 基础知识', link: '/basics/index' },
      { text: '博客构建', link: '/blog/index' },
    ],

    // 2. 侧边栏 (为每个模块单独定制)
    sidebar: {
      // 短链接项目的侧边栏
      '/projects/short-link/': [
        {
          text: '短链接系统实战',
          items: [
            { text: '项目介绍与架构', link: '/projects/short-link/index' },
            { text: '海量数据存储设计', link: '/projects/short-link/storage' },
            { text: '布隆过滤器优化', link: '/projects/short-link/bloom-filter' }
          ]
        }
      ],
      // Text2SQL 项目的侧边栏
      '/projects/text2sql/': [
        {
          text: 'Text2SQL 研发笔记',
          items: [
            { text: '设计思路与演示', link: '/projects/text2sql/index' },
            { text: 'Prompt 工程实践', link: '/projects/text2sql/prompt' },
            { text: 'Java 对接 LLM 实录', link: '/projects/text2sql/java-llm' }
          ]
        }
      ],
      // 算法模块侧边栏
      '/algorithm/': [
        {
          text: '算法修炼',
          items: [
            { text: '刷题指南', link: '/algorithm/index' },
            { text: '动态规划专题', link: '/algorithm/dp' },
            { text: '图论精讲', link: '/algorithm/graph' }
          ]
        }
      ],
      // 基础知识侧边栏
      '/basics/': [
        {
          text: '夯实基础',
          items: [
            { text: 'Java 核心', link: '/basics/index' },
            { text: 'JVM 深度调优', link: '/basics/jvm' },
            { text: 'MySQL 高级特性', link: '/basics/mysql' }
          ]
        }
      ]
    },

    socialLinks: [
      { icon: 'github', link: 'https://github.com/kang-yang77' }
    ],

    search: { provider: 'local' },
    
    footer: {
      message: 'Talk is cheap, show me the code.',
      copyright: 'Copyright © 2024 YangKang'
    }
  },
  
  markdown: { lineNumbers: true }
})