# Reel Library App Update Service (Cloudflare Worker)

This Cloudflare Worker powers the **in-app "Check for updates"** feature on Android.

---

## ⚡ How It Works

1. **Android App**: Sends `GET /check` to your Cloudflare Worker URL.
2. **Cloudflare Worker**:
   - Queries GitHub's latest release for `pratham1659/reel-library`.
   - Parses the latest version (`versionName` & `versionCode`), release notes, and the `.apk` download link.
   - Caches the response at the edge for 10 minutes (to avoid GitHub API rate limits).
   - If an update is found, returns:
     ```json
     {
       "versionCode": 2,
       "versionName": "1.2.0",
       "apkUrl": "https://<worker-domain>/download",
       "releaseNotes": "• Multi-provider AI\n• Bug fixes",
       "forceUpdate": false
     }
     ```
3. **Android Client UI**:
   - Compares the server version against its own `BuildConfig.VERSION_CODE`.
   - Shows the release notes & a **"Download & Install Update"** button.
   - Downloads the APK with a live progress bar.
   - Automatically opens Android's package installer to install the update.

---

## 🚀 Deployment (2 Methods)

### Method 1: Cloudflare Dashboard (Quickest & No CLI Required)

1. Log in to [Cloudflare Dashboard](https://dash.cloudflare.com/).
2. In the left sidebar, navigate to **Compute (Workers & Pages)** > **Workers**.
3. Click **Create Application** > **Create Worker**.
4. Name the worker `reel-library-update` and click **Deploy**.
5. Click **Edit Code** (Quick Edit) in the top-right corner.
6. Copy and paste the entire code from [`index.js`](./index.js) into `worker.js`.
7. Click **Deploy**.
8. Your worker is live at `https://reel-library-update.<your-subdomain>.workers.dev`!

### Method 2: Wrangler CLI

Run from this folder:

```bash
cd cloudflare-worker
npx wrangler login
npx wrangler deploy
```

---

## 🔗 Endpoints

| Endpoint | Method | Description |
| :--- | :--- | :--- |
| `/check` or `/version` | GET | Returns latest release metadata JSON. |
| `/download` | GET | Redirects (302) directly to the latest `.apk` asset on GitHub Releases. |
| `/health` | GET | Health check (`{"status": "ok"}`). |

---

## ⚙️ Optional: Custom Version Overrides

By default, the Worker automatically detects releases from GitHub. If you ever want to manually specify the version without using GitHub Releases, set these Environment Variables in **Worker Settings > Variables**:

- `LATEST_VERSION_CODE`: e.g. `3`
- `LATEST_VERSION_NAME`: e.g. `1.3.0`
- `APK_DOWNLOAD_URL`: Direct link to your APK (GitHub or Cloudflare R2).
- `RELEASE_NOTES`: Changelog text for the dialog.

