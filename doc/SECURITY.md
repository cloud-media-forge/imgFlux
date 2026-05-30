# Security Policy

[English](SECURITY.md) | [中文](SECURITY.zh-CN.md)

## Supported Versions

| Version | Supported          |
|---------|--------------------|
| 0.0.1   | :white_check_mark: |

## Reporting a Vulnerability

If you discover a security vulnerability in this project, please report it responsibly.

### How to Report

**Do not** open a public issue for security vulnerabilities.


Include the following information in your report:
- Description of the vulnerability
- Steps to reproduce the issue
- Potential impact of the vulnerability
- Any suggested fixes or mitigations (if available)

### Response Timeline

We will acknowledge receipt of your report within 48 hours and provide a detailed response within 7 days, including:
- Confirmation of the vulnerability
- Expected timeline for a fix
- Coordination on disclosure if needed

### Disclosure Policy

Once a vulnerability is fixed, we will:
1. Release a security update as soon as possible
2. Publish security advisories with details
3. Credit the reporter (if desired)
4. Update the CHANGELOG with security fixes

## Security Best Practices

### For Users

- Keep dependencies updated
- Use strong JWT secret keys
- Enable HTTPS in production
- Regularly review access controls
- Monitor logs for suspicious activity

### For Deployments

- Never expose storage credentials in code
- Use environment variables for sensitive configuration
- Implement rate limiting on API endpoints
- Keep GraphicsMagick updated
- Use secure storage configurations

### Configuration Security

- Change default credentials immediately
- Use strong passwords for storage services
- Restrict network access to storage endpoints
- Enable authentication for Admin UI
- Use secure JWT token expiration settings

## Dependency Security

This project uses the following security-focused practices:
- Regular dependency updates
- Vulnerability scanning in CI/CD
- Signed releases
- Verified checksums for downloads

## Contact

For security-related questions not related to vulnerabilities, please open an issue with the `security` label.
