# 贡献 Image Flux

感谢你对 Image Flux 项目的关注！我们欢迎任何形式的贡献。

[English](CONTRIBUTING.md) | [中文](CONTRIBUTING.zh-CN.md)

## 如何贡献

### 报告问题

如果你发现了 bug 或有功能建议，请在 [GitHub Issues](https://github.com/cloud-media-forge/imgFlux/issues) 中提交。

**提交 Issue 时请包含：**
- 清晰的标题和描述
- 复现步骤
- 预期行为和实际行为
- 环境信息（操作系统、Java 版本等）
- 相关日志或截图

### 提交代码

1. **Fork 本仓库**
2. **创建你的特性分支**
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. **提交你的更改**
   ```bash
   git commit -m 'Add some feature'
   ```
4. **推送到分支**
   ```bash
   git push origin feature/your-feature-name
   ```
5. **创建 Pull Request**

## 代码规范

- 遵循现有的代码风格
- 添加必要的注释
- 编写单元测试
- 确保所有测试通过
- 更新相关文档

## 提交信息规范

使用清晰的提交信息格式：

```
<type>: <subject>

<body>
```

**类型：**
- `feat`: 新功能
- `fix`: 修复 bug
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 重构
- `test`: 测试相关
- `chore`: 构建/工具相关

**示例：**
```
feat: add support for WebP image format

- Add WebP encoding/decoding support
- Update documentation with WebP examples
- Add tests for WebP conversion
```

## 开发环境设置

```bash
# 克隆仓库
git clone 
cd imgFlux

# 构建项目
mvn clean install

# 运行测试
mvn test

# 启动服务
mvn spring-boot:run -pl imgFlux-upload-api
mvn spring-boot:run -pl imgFlux-download-api
```

## 许可证

通过贡献代码，你同意你的贡献将根据项目的 MIT 许可证进行许可。

## 联系方式

如有问题，请通过 [GitHub Issues](https://github.com/cloud-media-forge/imgFlux/issues) 联系我们。
