# Security Policy

Reel Library takes security, local privacy, and user data safety very seriously. This document outlines our policy for responsible disclosure, reporting security vulnerabilities, and supported versions.

---

## 🔒 Supported Versions

Only the latest release receives active security patches. We recommend all users keep their installations up to date via the GitHub Releases page.

| Version | Supported | Notes |
| :--- | :---: | :--- |
| `1.2.x` | ✅ Yes | Current active release branch |
| `< 1.2.0` | ❌ No | Deprecated legacy builds |

---

## 🛡️ Reporting a Vulnerability

If you discover a security vulnerability in Reel Library, please do **NOT** open a public GitHub issue. Public issues expose users to risk before a fix can be verified and published.

Instead, please report security issues through one of the following private channels:

1. **GitHub Private Security Advisory**: Use the [GitHub Security Advisory submission](https://github.com/pratham1659/reel-library/security/advisories/new) tool on the repository.
2. **Direct Security Email**: Send an encrypted report to **pratham1659@gmail.com** with the subject line:
   `[SECURITY] Vulnerability Report - Reel Library`.

### What to Include in Your Report
To help us triage and resolve the issue quickly, please provide:
- A clear description of the vulnerability and its potential impact.
- Exact reproduction steps, including sample payload, URL, or device environment (e.g., Android version, device model).
- Proof-of-concept (PoC) code or screen recording if applicable.
- Any suggested fixes or mitigations.

### Response Timeline
- **Initial Acknowledgment**: Within **48 hours** of receiving the report.
- **Triage & Assessment**: Within **5 business days**, detailing confirmed severity and remediation plan.
- **Release & Patch**: A patched release APK will be assembled, tested, and published to GitHub Releases alongside public attribution (unless anonymity is requested).

---

## 🏛️ Security Architecture & In-Scope Areas

Reel Library operates under a **local-first, zero-telemetry** security model:

- **Local Storage Security**: Database files and notes are stored strictly in private app storage (`/data/data/com.reel_library.app/`).
- **Hardware-Backed Key Storage**: Cloud AI API keys (Groq, OpenAI, Claude) are encrypted using AES-256-GCM via AndroidX Security Crypto (`MasterKeys`) backed by the Android Keystore.
- **Zero Cleartext HTTP**: Cleartext network communication is blocked at the OS layer via `network_security_config.xml` (`cleartextTrafficPermitted="false"`).
- **Zero Third-Party Trackers**: The codebase contains zero analytics SDKs, ad trackers, or third-party mediation libraries.
