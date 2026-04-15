export interface BlogCatalogEntry {
  title: string
  href: string
  date: string
  summary: string
  category: string
  tags: string[]
  readingTime: string
  featured?: boolean
}

export const blogCatalog: BlogCatalogEntry[] = [
  {
    title: '博客内容平台设计',
    href: '/blogCreate/content-platform',
    date: '2026-04-14',
    summary:
      '把文章状态机、时间轴、多级分类、标签系统和背景配置整合成一套真正可落地的个人博客后端模型。',
    category: 'Blog Engineering',
    tags: ['Spring Boot', 'Architecture', 'CMS'],
    readingTime: '9 min',
    featured: true
  },
  {
    title: 'VitePress + Docker + Nginx + GitHub Actions 全自动化部署指南',
    href: '/blogCreate/index',
    date: '2026-04-10',
    summary:
      '记录如何把个人博客从本地开发推到线上部署，包括容器化、Nginx、SSH 免密和自动发布链路。',
    category: 'DevOps',
    tags: ['VitePress', 'Docker', 'GitHub Actions'],
    readingTime: '8 min'
  },
  {
    title: 'Smart SQL Pilot - 基于 RAG 的智能数据库助手',
    href: '/projects/text2sql/index',
    date: '2026-03-26',
    summary:
      '一个把自然语言查询、数据库 Schema 检索和 SQL 安全校验串起来的 Java AI 应用实践。',
    category: 'AI Systems',
    tags: ['RAG', 'LangChain4j', 'Java'],
    readingTime: '7 min'
  },
  {
    title: '短链接 SaaS 系统',
    href: '/projects/short-link/index',
    date: '2026-03-18',
    summary:
      '围绕高并发读写、缓存一致性和网关跳转设计的短链接平台实践，重点放在真实工程约束上。',
    category: 'Backend',
    tags: ['Spring Cloud', 'Redis', 'MySQL'],
    readingTime: '11 min'
  },
  {
    title: 'LIS 算法总结',
    href: '/algorithm/lis_dp',
    date: '2026-02-28',
    summary:
      '从动态规划到贪心 + 二分，把最长上升子序列的核心套路、变种题型和常见易错点系统梳理一遍。',
    category: 'Algorithms',
    tags: ['DP', 'Binary Search', 'LeetCode'],
    readingTime: '6 min'
  },
  {
    title: 'Java 后端相关知识总结',
    href: '/basics/index',
    date: '2026-02-16',
    summary:
      '沉淀 Spring、Spring Boot、MySQL 和并发基础知识，把零散知识点整理成后端工程的长期底座。',
    category: 'Notes',
    tags: ['Spring', 'MySQL', 'Backend Notes'],
    readingTime: '10 min'
  }
].sort((left, right) => right.date.localeCompare(left.date))

export const catalogTags = Array.from(
  new Set(blogCatalog.flatMap((entry) => entry.tags))
).sort((left, right) => left.localeCompare(right))
