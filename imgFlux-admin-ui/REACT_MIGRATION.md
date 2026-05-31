# React Migration Guide

## 概述

已成功将 videoFlux-admin-ui 的前端框架从 Thymeleaf 迁移到 ReactJS。

## 主要变更

### 1. 删除 Thymeleaf 依赖

- 从 `pom.xml` 中移除了 `spring-boot-starter-thymeleaf` 依赖

### 2. 创建 React 项目结构

在 `frontend/` 目录下创建了完整的 React 项目：

- `package.json` - React 项目配置
- `public/index.html` - HTML 入口文件
- `src/index.js` - React 入口文件
- `src/App.js` - 主应用组件（包含路由）
- `src/components/` - React 组件目录

### 3. React 组件

重构了所有原有的 Thymeleaf 模板为 React 组件：

- `Navbar.js` - 导航栏组件
- `Login.js` - 登录页面
- `Register.js` - 注册页面
- `Dashboard.js` - 仪表板
- `ImageList.js` - 视频/图片列表
- `ImageView.js` - 视频/图片详情
- `Error.js` - 错误页面

### 4. Maven 配置更新

在 `pom.xml` 中添加了前端构建配置：

- 使用 `frontend-maven-plugin` 插件
- 自动安装 Node.js 和 npm
- 执行 `npm install` 和 `npm run build`
- 使用 `maven-resources-plugin` 将构建结果复制到 `target/classes/static`

### 5. Docker 构建流程更新

更新了 `Dockerfile`：

- 采用多阶段构建
- 构建阶段：安装 Node.js、Maven，构建前端和后端
- 运行阶段：仅包含运行时依赖
- 前端构建产物被打包到 JAR 文件的 `static` 目录中

### 6. Controller 更新

将 `AdminUIController` 从 `@Controller` 改为 `@RestController`：

- 所有端点现在返回 JSON 而非 HTML 模板
- 添加了 `/admin/user` 端点用于获取用户信息
- 修改了所有方法返回 `ResponseEntity` 对象
- 移除了 `Model` 参数的使用

### 7. 新增 WebController

创建了 `WebController` 用于：

- 服务 React 应用
- 处理客户端路由
- 将所有前端路由转发到 `index.html`

## 构建和部署

### 本地开发

```bash
# 前端开发
cd frontend
npm install
npm start

# 后端开发
mvn spring-boot:run
```

### 生产构建

```bash
# Maven 会自动构建前端和后端
mvn clean package

# 或者使用 Docker
docker build -t videoflux-admin-ui .
```

### Docker 运行

```bash
docker run -p 8080:8080 videoflux-admin-ui
```

## API 端点变更

### 新的 JSON API

- `GET /admin/user` - 获取当前用户信息
- `GET /admin/dashboard` - 获取仪表板数据
- `GET /admin/video?path=xxx` - 获取视频列表
- `GET /admin/video/{path}` - 获取视频详情
- `POST /admin/video/create-folder` - 创建文件夹
- `DELETE /admin/video/delete` - 删除对象
- `GET /admin/video/**` - 直接访问视频文件

### 前端路由

React 应用使用客户端路由：

- `/` - 重定向到 `/dashboard`
- `/dashboard` - 仪表板
- `/video` - 视频列表
- `/video/:path` - 带路径的视频列表
- `/video/view/:id` - 视频详情
- `/login` - 登录页面
- `/register` - 注册页面
- `/error` - 错误页面

## 注意事项

1. **认证**：前端使用 Cookie 进行认证，需要确保后端正确配置 CORS
2. **静态资源**：React 构建产物会自动打包到 Spring Boot 的 `static` 目录
3. **API 代理**：开发时，React 通过 `proxy` 配置代理 API 请求到后端
4. **构建顺序**：Maven 会按顺序执行：前端构建 -> 复制静态资源 -> 后端打包

## 故障排除

### 构建失败

- 确保 Node.js 版本匹配（v18.16.0）
- 检查网络连接，npm install 需要下载依赖
- 查看 Maven 构建日志中的错误信息

### 运行时问题

- 检查后端日志确认 API 端点正常
- 确认静态资源正确加载
- 验证认证配置正确

### Docker 问题

- 确保 Docker 镜像构建成功
- 检查端口映射（8080）
- 查看容器日志排查问题

## 下一步优化建议

1. 添加环境变量配置（API 地址等）
2. 实现更完善的错误处理
3. 添加加载状态和用户体验优化
4. 考虑添加 TypeScript 支持
5. 实现更复杂的认证流程（JWT 等）
6. 添加单元测试和集成测试