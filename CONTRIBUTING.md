# Contributing to Reel Library

Thank you for your interest in contributing to **Reel Library**! We welcome contributions from developers, designers, and security researchers who want to help keep Reel Library private, fast, and reliable.

---

## 🛠️ Local Environment & Setup

### Prerequisites
- **Android Studio**: Ladybug (2024.2.1+) or newer.
- **Java Development Kit**: JDK 17 or JDK 21.
- **Android SDK**: Compile SDK `35`, Min SDK `26`.
- **Command Line**: Git and `./gradlew`.

### Getting Started
1. **Fork and Clone**:
   ```bash
   git clone https://github.com/pratham1659/reel-library.git
   cd reel-library
   ```
2. **Verify Local Environment**:
   Run the full unit test suite:
   ```bash
   ./gradlew test
   ```
3. **Assemble Debug Build**:
   ```bash
   ./gradlew assembleDebug
   ```

---

## 🚨 Mandatory Architectural Guardrails

Before submitting code, please ensure your changes respect these non-negotiable core guardrails:

1. **Brand Identity & Protected Assets**:
   - The brand design is finalized as **Tonal Motif B (Tonal Shade + Smart Note Sheet)**.
   - Background gradient is strictly Tonal Raspberry Rose (`#F03850` to `#C81466`, middle stop `#E22058`).
   - **Never modify** protected icon files:
     - `app/src/main/res/drawable/ic_launcher_background.xml`
     - `app/src/main/res/drawable/ic_launcher_foreground.xml`
     - `app/src/main/res/mipmap/`
     - `website/public/app-icon.svg`
     - `website/public/favicon.svg`
2. **Permission Minimization**:
   - Keep permissions strictly minimal. Do **NOT** add high-heuristic or broad permissions (`QUERY_ALL_PACKAGES`, `REQUEST_INSTALL_PACKAGES`, `SYSTEM_ALERT_WINDOW`, `BIND_ACCESSIBILITY_SERVICE`).
3. **Zero Telemetry**:
   - Do **NOT** introduce analytics, tracking, or ad mediation SDKs. All user vault notes and indices must stay 100% on the local device.
4. **Reproducible Builds**:
   - Gradle builds must execute without hardcoded developer credentials or local paths.

---

## 🌿 Development & Pull Request Workflow

1. **Create a Feature Branch**:
   ```bash
   git checkout -b feature/your-feature-name
   # or fix/your-bugfix-name
   ```
2. **Code Style & Linting**:
   - Write idiomatic Kotlin and Compose code.
   - Run unit tests and ensure zero regressions:
     ```bash
     ./gradlew test
     ```
3. **Commit Messages**:
   Follow [Conventional Commits](https://www.conventionalcommits.org/):
   - `feat: add new feature`
   - `fix: resolve a bug`
   - `refactor: clean up code structure`
   - `security: harden permissions or build configs`
4. **Open a Pull Request**:
   - Describe what changed and why.
   - Attach screenshot or screen recording for UI changes.
   - Verify all GitHub Actions checks pass.
