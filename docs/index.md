---
layout: home

hero:
  name: "Yang Kang's Code"
  text: "Keep Coding, Keep Evolving."
  tagline: "while(alive) { learn(); build(); share(); }"
  # image:
  #   # src: /logo.png # 建议放一个透明底的 Logo 或 AI 相关的 3D 图标，如果没有图片，可以先注释掉这行
  #   alt: Logo
  actions:
    - theme: brand
      text:  实战项目
      link: /projects/short-link/index
    - theme: alt
      text:  算法心得
      link: /algorithm/index

features:
  - title: 短链接系统 (SaaS)
    details: 基于 Spring Boot + Redis + MySQL 构建的高性能短链平台。深度解决海量数据分库分表与布隆过滤器防穿透实战难题。
    icon: 🚀
    link: /projects/short-link/index

  - title:  Text2SQL AI 助手
    details: 探索 LLM 在后端业务中的落地。基于 RAG 技术实现自然语言转 SQL 的自动化执行，集成 LangChain 思想的 Java 极简实现。
    icon: 🧠
    link: /projects/text2sql/index

  - title:  算法通关之路
    details: LeetCode 刷题沉淀与核心算法归纳。包含动态规划、回溯算法、图论等高频考点深度解析与模板总结。
    icon: 💎
    link: /algorithm/index

  - title:  知识体系构建
    details: 沉淀 Java 并发编程、JVM 底层原理、MySQL 索引优化与计算机网络核心知识，构建扎实的后端护城河。
    icon: 📖
    link: /basics/index

  - title:  博客搭建实录
    details: 记录本博客从零搭建的过程，VitePress 配置优化、GitHub Actions 自动化部署与 SEO 优化全记录。
    icon: 🛠️
    link: /blog/index
---
<style>
:root {
  /* ⚡️ 核心变量 */
  --vp-home-hero-name-color: transparent;
  --vp-home-hero-name-background: -webkit-linear-gradient(120deg, #bd34fe 30%, #41d1ff);
  --vp-home-hero-image-background-image: linear-gradient(-45deg, #bd34fe 50%, #47caff 50%);
  --vp-home-hero-image-filter: blur(40px);
}

.vp-doc h1 { 
    font-weight: 800; 
    letter-spacing: -1.5px;
}

.VPContent {
    background-image: 
        radial-gradient(transparent 1px, var(--vp-c-bg) 1px),
        linear-gradient(to right, rgba(255, 255, 255, 0.05) 1px, transparent 1px),
        linear-gradient(to bottom, rgba(255, 255, 255, 0.05) 1px, transparent 1px);
    background-size: 40px 40px, 40px 40px, 40px 40px;
    background-position: center top;
}

.dark .VPContent {
    background-image: 
        radial-gradient(rgba(255,255,255,0.1) 1px, transparent 1px),
        linear-gradient(to right, rgba(255, 255, 255, 0.02) 1px, transparent 1px),
        linear-gradient(to bottom, rgba(255, 255, 255, 0.02) 1px, transparent 1px);
}

/* Hero 图片悬浮呼吸动画 */
.image-src {
    animation: float 6s ease-in-out infinite;
}

@keyframes float {
    0% { transform: translateY(0px); }
    50% { transform: translateY(-15px); }
    100% { transform: translateY(0px); }
}

.VPFeature {
    transition: all 0.3s ease-in-out !important;
    border: 1px solid rgba(189, 52, 254, 0.1);
    background: rgba(255, 255, 255, 0.02);
    backdrop-filter: blur(10px);
    border-radius: 12px;
}

.VPFeature:hover {
    transform: translateY(-5px);
    border-color: #bd34fe;
    box-shadow: 0 8px 20px -5px rgba(189, 52, 254, 0.3);
    background: linear-gradient(145deg, rgba(255,255,255,0.05) 0%, rgba(189,52,254,0.05) 100%);
}

.VPFeature .icon {
    background-color: rgba(189, 52, 254, 0.1);
    border-radius: 8px;
    padding: 8px;
    font-size: 24px;
}


/* 主按钮 (brand)：实心渐变 */
.action .brand {
    background: linear-gradient(to right, #bd34fe, #41d1ff) !important;
    border: none !important;
    color: white !important; /* 强制白字 */
    box-shadow: 0 4px 15px rgba(65, 209, 255, 0.4);
    transition: all 0.3s;
}
.action .brand:hover {
    box-shadow: 0 6px 20px rgba(189, 52, 254, 0.6);
    transform: scale(1.05);
}

/* 次按钮 (alt)：磨砂玻璃 + 霓虹边框 */
.action .alt {
    background: rgba(255, 255, 255, 0.05) !important; /* 微透明背景 */
    border: 1px solid rgba(189, 52, 254, 0.5) !important; /* 紫色半透明边框 */
    color: var(--vp-c-text-1) !important; /* 跟随主题文字颜色 */
    backdrop-filter: blur(5px); /* 毛玻璃 */
    transition: all 0.3s;
}

.action .alt:hover {
    border-color: #41d1ff !important; /* 悬停变蓝 */
    background: rgba(189, 52, 254, 0.1) !important; /* 背景微微变紫 */
    box-shadow: 0 0 15px rgba(189, 52, 254, 0.3); /* 发光 */
    transform: scale(1.05);
}
</style>