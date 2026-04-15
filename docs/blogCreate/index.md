# VitePress + Docker + Nginx + GitHub Actions 全自动化部署指南

## 这次博客升级新增了什么

- [博客工作台](/studio/index)：站内完成 Markdown 编辑、草稿自动保存、封面图上传和状态机流转。
- [发布看板](/journal/index)：展示已发布文章、分类聚合与视觉背景效果。
- [发布时间线](/timeline/index)：汇总建站历程和文章发布动态。
- [内容平台设计](/blogCreate/content-platform)：后端表结构、状态机和 API 设计。

本文档总结了从零开始搭建博客、配置服务器环境、解决 SSH 密钥认证以及最终自动化部署的完整流程。

---

## 📋 架构概览
* **前端框架**: VitePress
* **服务器系统**: CentOS 7 / Ubuntu
* **容器化管理**: Docker + Docker Compose
* **Web 服务器**: Nginx (Alpine 镜像)
* **CI/CD 工具**: GitHub Actions (使用 `rsync` 协议同步文件)

---

## 第一阶段：服务器基础环境准备

### 1. 安装系统依赖
GitHub Actions 需要使用 `rsync` 进行文件传输，务必在服务器安装。
```bash
# CentOS
yum install rsync -y

# Ubuntu/Debian
apt-get install rsync -y
```
### 2. 创建目录结构
我们需要在服务器上规划好文件存放的位置。
```bash
mkdir -p /home/docker/nginx/conf.d
mkdir -p /home/docker/nginx/logs
# 注意：blog 目录是 GitHub Actions 上传的目标路径
mkdir -p /home/docker/nginx/html/blog
```
## 第二阶段：配置 SSH 免密登录
GitHub Actions 需要一把“钥匙”才能自动进入服务器。
### 1. 生成专用密钥对 (在本地电脑生成)

重要提示：必须生成无密码 (-N "") 的密钥，否则自动化脚本会卡住。

生成 ed25519 类型的密钥（比 RSA 更安全且短）
```bash
ssh-keygen -t ed25519 -f github_deploy_key -C "github-actions" -N ""
执行后会生成两个文件：
github_deploy_key (私钥 -> 存入 GitHub Secrets)
github_deploy_key.pub (公钥 -> 放入服务器)
```
### 2. 配置服务器公钥 (白名单)

将 github_deploy_key.pub 的内容追加到服务器的授权文件中。
```bash
# 在服务器执行
mkdir -p ~/.ssh
# 替换下方内容为你生成的公钥内容
echo "ssh-ed25519 AAAA..." >> ~/.ssh/authorized_keys
```

### 3. 修复 SSH 权限 (权限过宽 SSH 会拒绝连接)
chmod 700 ~/.ssh
chmod 600 ~/.ssh/authorized_keys
chown -R root:root ~/.ssh
### 4. 配置 GitHub Secrets
进入 GitHub 仓库 -> Settings -> Secrets and variables -> Actions -> New repository secret。

Name: SSH_PRIVATE_KEY

Value: 粘贴 github_deploy_key (私钥) 的全部内容（包含 BEGIN 和 END 行）。

## 第三阶段：Nginx 与 Docker 配置
### 1. 编写 Nginx 配置文件
文件路径：/home/docker/nginx/conf.d/blog.conf 重点：防止重定向死循环，简化 try_files 逻辑。
```bash
server {
    listen 80;
    server_name _; # 后续可改为你的域名 exitzero.tech

    # 容器内部路径 (由 docker-compose 映射而来)
    root /usr/share/nginx/html;
    index index.html;

    # 错误日志
    error_log /var/log/nginx/error.log warn;

    location / {
        # 尝试直接查找文件，找不到则返回首页（适用于 SPA 单页应用）
        try_files $uri /index.html;
    }

    # 静态资源缓存配置
    location ~ .*\.(gif|jpg|jpeg|png|bmp|swf|ico|svg|js|css)$ {
        root /usr/share/nginx/html;
        expires 30d;
    }
}
```
### 2. 编写 Docker Compose 文件
文件路径：/home/docker/docker-compose.yml (或 /home/docker/nginx/docker-compose.yml，视具体操作而定) 重点：挂载路径必须精准对应 GitHub 上传的目录。
```bash
version: '3'
services:
  nginx:
    image: nginx:alpine
    container_name: blog-nginx
    restart: always
    ports:
      - "80:80"
      - "443:443"
    volumes:
      # 【核心】将宿主机上传文件的目录，挂载到容器的根目录
      # 左边：宿主机实际路径 | 右边：容器内 Nginx 默认读取路径
      - /home/docker/nginx/html/blog:/usr/share/nginx/html
      
      # 挂载配置
      - ./conf.d:/etc/nginx/conf.d
      # 挂载日志
      - ./logs:/var/log/nginx
    environment:
      - TZ=Asia/Shanghai
```
## 第四阶段：GitHub Actions 自动化脚本
文件路径：项目根目录 .github/workflows/deploy.yml
```bash
name: Deploy to Server

on:
  push:
    branches:
      - main  # 监听 main 分支的变动

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout code
        uses: actions/checkout@v3

      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: 18

      - name: Install and Build
        run: |
          npm install
          npm run docs:build  # 确保打包命令正确

      - name: Deploy to Server via Rsync
        uses: easingthemes/ssh-deploy@main
        with:
          SSH_PRIVATE_KEY: ${{ secrets.SSH_PRIVATE_KEY }}
          ARGS: "-rltgoDzvO --delete"
          # 本地打包后的产物路径 (VitePress 默认为 docs/.vitepress/dist/)
          SOURCE: "docs/.vitepress/dist/"
          # 服务器目标路径 (宿主机路径)
          TARGET: "/home/docker/nginx/html/blog/"
          REMOTE_HOST: "117.72.41.145" # 你的服务器 IP
          REMOTE_USER: "root"
```
根据我们的 .yml 配置 (on: push)，只要你把代码推送到 main 分支，部署就会自动开始。
在你的本地电脑终端执行：
```bash
# 1. 添加所有更改
git add .

# 2. 提交更改
git commit -m "deploy: init blog"

# 3. 推送到 GitHub (这一步会瞬间触发 Actions)
git push origin main
```
代码推送后，打开 GitHub 仓库页面。

点击顶部的 Actions 标签。

你应该能看到一个正在运行的任务（黄色转圈）。

点击进去，查看 Deploy 步骤的日志。

✅ 绿色对勾：部署成功！

❌ 红色叉号：部署失败，
## 第五阶段：启动
```bash
cd /home/docker/nginx
# 强制重建容器以应用最新挂载配置
docker-compose up -d --force-recreate
```
