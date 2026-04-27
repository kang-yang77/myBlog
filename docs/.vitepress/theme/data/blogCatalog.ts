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
    title: 'KeePassXC：本地密码保险柜',
    href: '/posts/2026-04-27-untitled',
    date: '2026-04-26',
    summary: 'KeePassXC 是一款免费开源的本地密码管理软件，可将 API Key、数据库密码等敏感信息高强度加密存储为 .kdbx 文件，完全离线、不联网，从物理层面杜绝泄露。本文介绍其核心优势与三步使用流程。',
    category: 'DevOps',
    tags: ['KeePassXC', '密码管理', '安全', 'API Key', '开源工具', '本地加密'],
    readingTime: '5 min',
  },
  {
    title: 'Harness Engineering 总结',
    href: '/posts/agent_260420',
    date: '2026-04-20',
    summary:
      '尝试总结了Harness Engineering相关概念',
    category: 'DevOps',
    tags: ['Harness', 'Spec Coding', '跨Agent同步'],
    readingTime: '5 min',
    featured: true
  },
  {
    title: 'Agent基础',
    href: '/posts/agent_2604',
    date: '2026-04-14',
    summary:
      '尝试并总结了智能体Agent的相关理论',
    category: 'DevOps',
    tags: ['Agent', 'Agent设计模式', 'Agent评估','任务分解'],
    readingTime: '5 min',
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
  },
  {
    title: 'Docker 容器化部署实践',
    href: '/posts/2026-04-26-docker-containerization-best-practices',
    date: '2026-04-26',
    summary: '总结 Docker 部署应用时的关键最佳实践，包括镜像优化、多阶段构建、数据持久化和单进程原则。',
    category: 'DevOps',
    tags: ['Docker', '容器化', '部署', '最佳实践'],
    readingTime: '4 min',
  },
].sort((left, right) => right.date.localeCompare(left.date))

export const catalogTags = Array.from(
  new Set(blogCatalog.flatMap((entry) => entry.tags))
).sort((left, right) => left.localeCompare(right))
