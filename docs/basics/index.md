# Java 后端相关知识总结

## Spring相关
### 一、 Web 容器 (Servlet Containers)
* **Tomcat**
    * **定位**：Spring Boot 默认容器，成熟稳定，文档丰富。
    * **适用**：大多数传统的企业级 Web 应用。
* **Undertow**
    * **定位**：性能怪兽，基于 NIO（非阻塞），启动快，内存占用低。
    * **适用**：高并发场景，微服务架构。
* **Jetty**
    * **定位**：长连接专家，架构灵活。
    * **适用**：WebSocket、即时通讯 (IM)、聊天系统。

---

### 二、 Spring Boot 核心原理
#### 1. 启动类注解 `@SpringBootApplication`
它是一个复合注解，包含三个核心：
* **`@SpringBootConfiguration`**：标识这是主配置类。
* **`@ComponentScan`**：自动扫描当前包及其子包下的组件（Bean）。
* **`@EnableAutoConfiguration`**：**核心**。开启自动配置机制。

#### 2. 自动配置原理 (SPI 机制)
* **加载**：启动时扫描所有 jar 包下的 `META-INF/spring.factories` 文件。
* **筛选**：利用 `@Conditional` 系列注解（如 `@ConditionalOnClass`）进行判断。
    * *只有当 classpath 下存在指定的类（依赖）且用户未手动配置 Bean 时，自动配置类才生效。*
* **注册**：将符合条件的默认配置加载到 Spring 容器中（如自动配置 DataSource）。

#### 3. 配置文件优先级
遵循 **“外层覆盖内层，具体覆盖通用”** 原则：
1.  **命令行参数** (`--server.port=8888`) [优先级最高]
2.  **Jar 包外部** 的配置文件 (`config/application.yml`)
3.  **Jar 包内部** 的配置文件 (`src/main/resources/application.yml`)
4.  **带环境后缀** 的配置 (`application-dev.yml`) > **通用** 配置 (`application.yml`)

#### 4. 多环境管理 (Profiles)
* **最佳实践**：使用多文件隔离 (`application-dev.yml`, `application-prod.yml`)。
* **切换方式**：
    * 开发时：在 `application.yml` 设置 `spring.profiles.active=dev`。
    * **生产时**：启动命令指定 `java -jar app.jar --spring.profiles.active=prod`。

---

### 三、 Spring 常用注解体系
#### 1. Bean 定义 (类级别)
* **`@Component`**：通用组件（打杂的）。
* **`@Repository`**：持久层/DAO（管仓库的），支持数据库异常翻译。
* **`@Service`**：业务层（经理），通常在此处开启事务。
* **`@Controller`**：MVC 控制层（传统前台），返回页面。
* **`@RestController`**：`@Controller` + `@ResponseBody`，直接返回数据 (JSON)，适用于前后端分离。

#### 2. 依赖注入
* **`@Autowired`**：Spring 提供，自动按类型注入 Bean。

#### 3. HTTP 请求传参
* **`@RequestParam`**：获取 URL 查询参数 (`?name=tom`)。
* **`@PathVariable`**：获取 URL 路径参数 (`/user/{id}`)。
* **`@RequestBody`**：获取 POST 请求体中的 JSON 数据，并自动映射为 Java 对象。

---

### 四、 Spring 核心思想 (IOC & AOP)
#### 1. IOC (控制反转) & DI (依赖注入)
* **概念**：将对象的创建权和管理权从代码移交给 Spring 容器。
* **目的**：**解耦**。修改依赖实现时无需修改业务代码，只需调整配置。

#### 2. AOP (面向切面编程)
* **概念**：将横切关注点（日志、事务、权限）与核心业务逻辑分离。
* **实现**：基于动态代理（JDK 代理或 CGLIB）。
* **应用**：声明式事务 (`@Transactional`) 是最典型的应用。

---

### 五、 事务管理 (Transaction)
#### 1. 本地事务失效场景 (`@Transactional`)
* **自调用**：同一类内部方法直接调用 (`this.method()`)，绕过了代理对象。
* **异常被吞**：业务代码手动 `try-catch` 了异常且未抛出，AOP 无法捕获异常。
* **异常类型不匹配**：默认只回滚 `RuntimeException`，若抛出 Checked Exception (如 `IOException`) 需配置 `rollbackFor = Exception.class`。
* **权限限制**：方法非 `public`。

#### 2. 分布式事务解决方案
* **2PC (两阶段提交)**：强一致性，但性能差，资源锁定时间长。
* **TCC (Try-Confirm-Cancel)**：性能好，但代码侵入性高（需实现三个方法）。
* **Seata (AT模式)**：阿里开源，对业务无侵入，利用 Undo Log 实现回滚，适合大多数企业场景。
* **最终一致性 (基于 MQ)**：高并发首选。允许短暂数据不一致，通过消息队列异步重试，保证最终结果正确。

## Mysql

### 1. SQL 性能优化案例：深度分页 (Deep Pagination)

#### 1.1 问题场景
当使用 `LIMIT 100000, 20` 这种深分页查询时，性能极差。
**原因**：MySQL 需要扫描前 100,020 行数据，回表读取完整数据，然后丢弃前 100,000 行。大量的随机 I/O 和 CPU 计算被浪费。

#### 1.2 优化方案：延迟关联 (Deferred Join)
```sql
-- 优化前：回表 100020 次
SELECT * FROM table_a WHERE type = 1 LIMIT 100000, 20;

-- 优化后：利用子查询只回表 20 次
SELECT t1.* FROM table_a as t1, 
     (SELECT id FROM table_a WHERE type = 1 LIMIT 100000, 20) as t2 
WHERE t1.id = t2.id;
```

#### 1.3 核心原理
1.  **子查询 (覆盖索引)**：内层查询只查 `id`，利用**覆盖索引**技术，直接在索引树上遍历，**无需回表**，速度极快。
2.  **精准回表**：拿到 20 个目标 ID 后，外层查询只需要做 20 次回表操作，极大降低 I/O 开销。

---

### 2. 索引核心概念：回表 vs 覆盖索引

| 概念 | 描述 | 比喻 | 性能 |
| :--- | :--- | :--- | :--- |
| **聚簇索引** (Clustered Index) | 叶子节点存储**整行数据**。一张表只有一棵（通常是主键）。 | 图书馆的书架（正文） | - |
| **二级索引** (Secondary Index) | 叶子节点存储**索引列 + 主键 ID**。 | 书后的关键词目录 | - |
| **回表** (Table Lookup) | 先查二级索引拿到 ID，再回聚簇索引查完整数据。**“跑两趟”**。 | 查了目录还得去书架拿书 | 较慢 (随机 I/O) |
| **覆盖索引** (Covering Index) | 查询的列完全包含在二级索引中，**无需回表**。**“一趟搞定”**。 | 目录上直接写了答案 | 极快 (内存操作) |

---

### 3. 多表 JOIN 策略 (阿里巴巴开发手册)

#### 3.1 强制规范
* **禁止超过 3 张表 JOIN**。
* 被关联字段必须有**索引**。
* 字段数据类型保持绝对一致。

#### 3.2 为什么 JOIN 慢？
* **Nested-Loop Join (嵌套循环)**：本质是多层 `for` 循环。
* **复杂度爆炸**：如果是 3 张大表关联，复杂度近似 $O(A \times B \times C)$。如果不走索引（Simple Nested-Loop），性能是灾难级的。

#### 3.3 替代方案：应用层组装 (单表查询 + 内存关联)
**推荐做法**：
1.  **查询驱动表**：`SELECT * FROM order LIMIT 10` -> 得到 `userIds`.
2.  **批量查询被驱动表**：`SELECT * FROM user WHERE id IN (userIds)`.
3.  **内存组装**：在 Java 代码中利用 `Map` 进行数据拼装。

**优势**：
* **缓存友好** (User 数据可缓存 Redis)。
* **支持分库分表** (跨库无法 JOIN)。
* **降低数据库压力** (计算逻辑上移到应用服务器)。

---

### 4. 事务隔离级别 (Isolation Levels)

核心目的是平衡 **并发性能** 与 **数据一致性**。

| 级别 | 名称 | 脏读 | 不可重复读 | 幻读 | 说明 |
| :--- | :--- | :---: | :---: | :---: | :--- |
| **RU** | Read Uncommitted (读未提交) | ✅ | ✅ | ✅ | 极其不安全，基本不用。 |
| **RC** | Read Committed (读已提交) | ❌ | ✅ | ✅ | **Oracle/PG 默认**。解决脏读。 |
| **RR** | Repeatable Read (可重复读) | ❌ | ❌ | ❌(大半) | **MySQL InnoDB 默认**。快照读。 |
| **Serial** | Serializable (串行化) | ❌ | ❌ | ❌ | 即使读也加锁，性能极差。 |

> * **脏读**：读到别人未提交的数据。
> * **不可重复读**：同一事务内，两次读同一行数据不一样（被修改）。
> * **幻读**：同一事务内，两次统计行数不一样（被新增/删除）。

---

### 5. MVCC (多版本并发控制)

**核心作用**：实现**不加锁**的非阻塞读（快照读），解决读写冲突。

#### 5.1 实现原理
1.  **隐藏字段**：
    * `trx_id` (最后修改的事务 ID)。
    * `roll_pointer` (指向 Undo Log 的回滚指针)。
2.  **Undo Log (版本链)**：
    * 保存数据的历史版本链表。
3.  **Read View (一致性视图)**：
    * 可见性判断的“滤镜”，决定当前事务能看到版本链中的哪一个版本。

#### 5.2 RC 与 RR 的区别 (高频考点)
* **RC (读已提交)**：每一次 `SELECT` **都会生成**新的 Read View。-> 能读到最新提交的数据。
* **RR (可重复读)**：只在事务开启后的第一次 `SELECT` **生成一次** Read View，后续复用。-> 保证整个事务期间看到的数据一致（快照）。

---

### 6. MySQL 锁机制 (Locks)

当 MVCC 搞不定（需要**当前读**或**写操作**）时，就需要锁。

#### 6.1 锁的维度
* **表锁**：开销小，并发低（MyISAM）。
* **行锁**：开销大，并发高（InnoDB）。**注意：InnoDB 的行锁是加在索引上的！如果不走索引，会退化为表锁。**

#### 6.2 锁的算法 (InnoDB 三大神器)
1.  **Record Lock (记录锁)**：锁住索引记录本身。
2.  **Gap Lock (间隙锁)**：锁住索引之间的**空隙**。
    * **作用**：防止其他事务在这个范围内插入数据。
    * **意义**：配合 MVCC 解决了 **RR 级别下的幻读问题**。
3.  **Next-Key Lock (临键锁)**：Record Lock + Gap Lock。锁住“数据+间隙”。

#### 6.3 锁类型
* **共享锁 (S锁)**：`SELECT ... LOCK IN SHARE MODE` (读锁)。
* **排他锁 (X锁)**：`SELECT ... FOR UPDATE` / `UPDATE` / `DELETE` (写锁)。

---

### 7. 总结

1.  **深度分页怎么优？** -> 延迟关联（子查询利用覆盖索引只查 ID，再 Inner Join 回表）。
2.  **为什么不推荐 JOIN？** -> 避免笛卡尔积，方便分库分表，减轻 DB CPU 压力。
3.  **MySQL 默认隔离级别？** -> RR（可重复读）。
4.  **MVCC 是什么？** -> 通过 Undo Log 版本链和 Read View 实现的无锁并发读。
5.  **幻读怎么解决的？** -> 快照读靠 MVCC，当前读靠 Next-Key Lock (间隙锁)。
6.  **没走索引会怎样？** -> 行锁退化为表锁，系统可能由于死锁或等待而崩溃。