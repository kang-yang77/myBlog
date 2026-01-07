---
layout: home

hero:
  name: "杨康的开发笔记"
  text: "Java 后端与 AI 的融合"
  tagline: 专注于高并发架构、AI 应用落地与算法精进
  actions:
    - theme: brand
      text: 🚀 查看实战项目
      link: /projects/short-link/index
    - theme: alt
      text: ⚡ 浏览算法心得
      link: /algorithm/index

features:
  - title: 🔗 短链接系统 (SaaS)
    details: 基于 Spring Boot + Redis + MySQL 构建的高性能短链平台。解决了海量数据分库分表与布隆过滤器防穿透问题。
    icon: 🚀
    link: /projects/short-link/index

  - title: 🤖 Text2SQL AI 工具
    details: 探索 LLM 在后端业务中的落地。实现了自然语言转 SQL 的自动化执行，集成 LangChain 思想的 Java 实现。
    icon: 🧠
    link: /projects/text2sql/index

  - title: ⚔️ 算法心得
    details: LeetCode 刷题记录与核心算法归纳。包含动态规划、回溯算法、图论等高频考点深度解析。
    icon: 💎
    link: /algorithm/index

  - title: 📚 基础知识体系
    details: 沉淀 Java 并发编程、JVM 底层原理、MySQL 索引优化与计算机网络核心知识。
    icon: 📖
    link: /basics/index

  - title: 博客建立
    details: 沉淀 Java 并发编程、JVM 底层原理、MySQL 索引优化与计算机网络核心知识。
    icon: 📖
    link: /blog/index
---

<style>
:root {
  /* 给首页标题加个更具科技感的蓝紫渐变 */
  --vp-home-hero-name-background: -webkit-linear-gradient(120deg, #35495e 30%, #42b883);
}
</style>