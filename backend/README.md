# Blog Backend

这个模块是给当前博客站补上的 Spring Boot 后端，负责文章状态机、分类标签、时间线聚合和图片上传。

## 已实现接口

- `POST /api/admin/articles/drafts`
- `PUT /api/admin/articles/{id}/draft`
- `PUT /api/admin/articles/{id}/status`
- `GET /api/admin/articles`
- `GET /api/admin/articles/{id}`
- `GET /api/admin/categories`
- `GET /api/admin/categories/tree`
- `POST /api/admin/categories`
- `PUT /api/admin/categories/{id}`
- `GET /api/admin/tags`
- `POST /api/admin/tags`
- `PUT /api/admin/tags/{id}`
- `GET /api/admin/visual-configs`
- `POST /api/admin/visual-configs`
- `PUT /api/admin/visual-configs/{id}`
- `PUT /api/admin/visual-configs/{id}/activate`
- `POST /api/admin/assets/upload`
- `GET /api/timeline`

## 本地启动

在仓库根目录执行：

```bash
mvn -f backend/pom.xml spring-boot:run
```

默认配置：

- 端口：`8081`
- 数据库：H2 文件库 `backend/data/blogdb`
- 上传目录：`backend/uploads`
- H2 控制台：`/h2-console`

## 切换到 MySQL

```bash
mvn -f backend/pom.xml spring-boot:run -Dspring-boot.run.profiles=mysql
```

也可以通过环境变量覆盖：

- `BLOG_DB_URL`
- `BLOG_DB_USERNAME`
- `BLOG_DB_PASSWORD`
