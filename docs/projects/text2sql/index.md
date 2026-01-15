# 🚀 Smart SQL Pilot - 基于 RAG 的智能数据库助手

> **让数据查询像聊天一样简单。**
>
> 这是一个基于 **Java Spring Boot 3** + **LangChain4j** + **通义千问 (Qwen)** 构建的 AIGC 应用。它利用 **RAG (检索增强生成)** 技术，能够理解用户的自然语言问题，结合实时数据库 Schema，自动生成精准的 SQL 语句并执行查询。

---

## 在线体验地址

<!-- - [项目地址](https://exitzero.tech/text2sql/) -->



---

##  项目简介

传统的数据库查询需要专业人员编写复杂的 SQL 语句。**Smart SQL Pilot** 旨在打破这一壁垒。通过连接您的业务数据库，它能自动提取表结构信息（Schema），并结合大语言模型的推理能力，实现“对话即查询”。

**应用场景：**
* 非技术人员的数据报表提取。
* 开发人员快速验证数据逻辑。
* BI (商业智能) 系统的自然语言接口。

---

##  核心特性

* **动态数据源连接**：支持运行时动态连接任意 MySQL 数据库，无需重启服务。
* **RAG 上下文增强**：自动提取数据库表结构、字段类型及**注释**，作为上下文喂给 AI，大幅提升 SQL 生成准确率。
* **安全防御机制**：
    * 内置 SQL 行为检测，禁止执行 `DROP`、`DELETE`、`UPDATE` 等高危操作。
    * 支持数据库连接只读模式 (`Read-Only`) 设置。
* **高性能连接池**：引入 `HikariCP` 并配合 `ConcurrentHashMap` 缓存机制，优化高并发下的数据库连接性能。
---

## 🛠 技术栈

| 模块 | 技术选型 | 说明 |
| :--- | :--- | :--- |
| **后端框架** | Spring Boot 3.x | 核心业务逻辑 |
| **开发语言** | Java 21 | 利用新特性 (Record, Virtual Threads等) |
| **AI 框架** | LangChain4j | 统一接入 LLM 能力，简化 RAG 流程 |
| **大模型** | Alibaba Cloud DashScope | 接入通义千问 (Qwen-Turbo/Max) |
| **数据库** | MySQL 8.0 | 业务数据存储 (Docker 部署) |
| **连接池** | HikariCP | 高性能 JDBC 连接池管理 |

---

## 🏗 系统架构

![系统架构图](/images/fig1.png)

**处理流程：**
1.  **User Input**: 用户输入自然语言（例如：“查询消费超过 1000 元的用户”）。
2.  **Schema Retriever**: 后端工具类动态连接数据库，提取 `Table` 和 `Column` 的元数据及注释。
3.  **Prompt Engineering**: 将用户问题 + 数据库 Schema + 预设指令 组装成 Prompt。
4.  **LLM Inference**: 调用通义千问 API，获取生成的 SQL。
5.  **Safety Check**: 校验 SQL 是否包含恶意操作。
6.  **Execution**: 在目标数据库执行 SQL，将 `ResultSet` 转换为 JSON 返回前端。

---

## 💾 数据库设计 (示例)

本项目默认支持电商场景的演示，包含以下核心表结构：

* **`t_users` (用户表)**: 存储用户基本信息。
* **`t_orders` (订单表)**: 存储订单主信息，通过 `user_id` 关联用户 (1:N)。
* **`t_products` (商品表)**: 存储商品单价、名称。
---
