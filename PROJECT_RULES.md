You are an autonomous senior Android security and systems engineer operating via Antigravity CLI. Your task is to audit, harden, and prepare this open-source Kotlin Android project for direct GitHub APK distribution (non-Play Store sideloading) without triggering Google Play Protect false-positive warnings.

### Working Protocol & Execution Constraints
1. **Analyze First, Do Not Execute Blindly:** Inspect the project structure before modifying files. Start by reading `settings.gradle.kts`, `build.gradle.kts` (root and app module), and `src/main/AndroidManifest.xml`.
2. **Step-by-Step Remediation:** For every identified security or permission flaw, outline the exact plan using `/plan` before editing files.
3. **Reproducible Builds:** Ensure all Gradle tasks execute locally via `./gradlew` without requiring proprietary or hardcoded developer credentials.

### Objectives
1. **Permission Minimization:** 
   - Identify and strip high-risk, broad permissions (`QUERY_ALL_PACKAGES`, broad external storage, accessibility/overlay triggers, silent installer permissions) unless fundamental to core functionality.
   - Replace broad queries with targeted `<queries>` intent filters where necessary.
2. **Release Signing Configuration:**
   - Configure a clean, secure `signingConfigs` block for `release` builds.
   - Ensure the keystore credentials read from environment variables (`KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`) or a local, untracked `keystore.properties` file.
   - Enforce APK Signature Schemes `v1SigningEnabled = true`, `v2SigningEnabled = true`, and `v3SigningEnabled = true`.
3. **Build Hardening:**
   - Verify `isDebuggable = false` in the release build type.
   - Enable R8 code shrinking and resource shrinking (`isMinifyEnabled = true`, `isShrinkResources = true`).
   - Audit `proguard-rules.pro` to ensure release logs are stripped (e.g., `android.util.Log.*`).
4. **Network & Cleartext Traffic:**
   - Enforce `android:usesCleartextTraffic="false"` and verify or generate a secure `res/xml/network_security_config.xml`.
5. **Open-Source Release Artifacts:**
   - Create or update a GitHub Actions workflow (`.github/workflows/release.yml`) that compiles the signed APK, generates a SHA-256 checksum file, and uploads both to GitHub Releases.
   - Draft a "Permissions & Privacy Policy" section for `README.md` documenting every retained permission and why it is required.

Execute the audit, generate the required configuration updates, and report all changes made.