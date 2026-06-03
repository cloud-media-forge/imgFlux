# Contributing to Image Flux

[English](CONTRIBUTING.md) | [中文](CONTRIBUTING.zh-CN.md)

Thank you for your interest in contributing to Image Flux! We welcome contributions in any form.

## How to Contribute

### Reporting Issues

When reporting a bug or suggesting a feature, please open an issue
on [GitHub Issues](https://github.com/cloud-media-forge/imgFlux/issues).

**Please include when submitting an issue:**
- Clear title and description
- Steps to reproduce
- Expected and actual behavior
- Environment info (OS, Java version, etc.)
- Relevant logs or screenshots

### Submitting Code

1. **Fork this repository**
2. **Create your feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. **Commit your changes**
   ```bash
   git commit -m 'Add some feature'
   ```
4. **Push to the branch**
   ```bash
   git push origin feature/your-feature-name
   ```
5. **Create a Pull Request**

## Code Style

- Follow existing code style
- Add necessary comments
- Write unit tests
- Ensure all tests pass
- Update relevant documentation

## Commit Message Convention

Use clear commit message format:

```
<type>: <subject>

<body>
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation update
- `style`: Code style change
- `refactor`: Refactoring
- `test`: Test related
- `chore`: Build/tool related

**Example:**
```
feat: add support for WebP image format

- Add WebP encoding/decoding support
- Update documentation with WebP examples
- Add tests for WebP conversion
```

## Development Setup

```bash
# Clone repository
git clone 
cd imgFlux

# Build project
mvn clean install

# Run tests
mvn test

# Start services
mvn spring-boot:run -pl imgFlux-upload-api
mvn spring-boot:run -pl imgFlux-download-api
```

## License

By contributing code, you agree that your contributions will be licensed under the project's GPL License.

## Contact

If you have questions, please contact us through [GitHub Issues](https://github.com/cloud-media-forge/imgFlux/issues).
