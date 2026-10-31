/**
 * Cloudflare Worker for Reel Library App Updates
 * 
 * Endpoints:
 * - GET /check or /version: Returns latest release metadata JSON
 * - GET /download: Redirects (302) to the latest APK asset
 * - GET /health: Health check
 */

// GitHub repo settings
const GITHUB_OWNER = "pratham1659";
const GITHUB_REPO = "reel-library";

// Fallback configuration if GitHub API is unreachable or manual mode is preferred
const FALLBACK_CONFIG = {
  versionCode: 2,
  versionName: "1.2.0",
  apkUrl: `https://github.com/${GITHUB_OWNER}/${GITHUB_REPO}/releases/latest/download/ReelLibrary-release-v1.2.0.apk`,
  releaseNotes: "• Multi-provider AI support (Gemini, Claude, OpenAI, Groq)\n• Real error propagation on transcription failures\n• Background processing improvements\n• Bug fixes and UI optimizations",
  forceUpdate: false,
  minSupportedVersionCode: 1
};

export default {
  async fetch(request, env, ctx) {
    const url = new URL(request.url);
    const path = url.pathname.replace(/\/+$/, "");

    // CORS headers for web/in-app cross-origin compatibility
    const corsHeaders = {
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Methods": "GET, HEAD, OPTIONS",
      "Access-Control-Allow-Headers": "Content-Type, User-Agent, Accept",
      "Content-Type": "application/json"
    };

    if (request.method === "OPTIONS") {
      return new Response(null, { headers: corsHeaders });
    }

    try {
      // 1. Health check
      if (path === "/health" || path === "") {
        return new Response(
          JSON.stringify({ status: "ok", service: "Reel Library Update API" }),
          { headers: corsHeaders }
        );
      }

      // 2. Fetch latest release info (from cache or GitHub API)
      const releaseInfo = await getLatestRelease(env, ctx);

      // 3. /download - direct redirect to APK
      if (path === "/download") {
        return Response.redirect(releaseInfo.apkUrl, 302);
      }

      // 4. /check or /version - return update metadata
      if (path === "/check" || path === "/version") {
        return new Response(
          JSON.stringify(releaseInfo, null, 2),
          {
            headers: {
              ...corsHeaders,
              "Cache-Control": "public, max-age=300, s-maxage=600"
            }
          }
        );
      }

      return new Response(
        JSON.stringify({ error: "Not Found", availableEndpoints: ["/check", "/version", "/download", "/health"] }),
        { status: 404, headers: corsHeaders }
      );
    } catch (err) {
      // Fallback gracefully on any error
      return new Response(
        JSON.stringify(FALLBACK_CONFIG, null, 2),
        { headers: corsHeaders }
      );
    }
  }
};

/**
 * Fetches latest release from GitHub API with edge caching
 */
async function getLatestRelease(env, ctx) {
  // If manual environment variables are provided in Cloudflare, use them
  if (env?.LATEST_VERSION_CODE && env?.LATEST_VERSION_NAME) {
    return {
      versionCode: parseInt(env.LATEST_VERSION_CODE, 10),
      versionName: env.LATEST_VERSION_NAME,
      apkUrl: env.APK_DOWNLOAD_URL || FALLBACK_CONFIG.apkUrl,
      releaseNotes: env.RELEASE_NOTES || FALLBACK_CONFIG.releaseNotes,
      forceUpdate: env.FORCE_UPDATE === "true",
      minSupportedVersionCode: parseInt(env.MIN_SUPPORTED_VERSION_CODE || "1", 10)
    };
  }

  const cacheKey = `https://cache.local/github-release-${GITHUB_OWNER}-${GITHUB_REPO}`;
  const cache = caches.default;
  let response = await cache.match(cacheKey);

  if (!response) {
    const apiUrl = `https://api.github.com/repos/${GITHUB_OWNER}/${GITHUB_REPO}/releases/latest`;
    const headers = {
      "User-Agent": "ReelLibrary-UpdateWorker",
      "Accept": "application/vnd.github.v3+json"
    };

    if (env?.GITHUB_TOKEN) {
      headers["Authorization"] = `Bearer ${env.GITHUB_TOKEN}`;
    }

    const apiRes = await fetch(apiUrl, { headers });
    if (!apiRes.ok) {
      return FALLBACK_CONFIG;
    }

    const release = await apiRes.json();
    
    // Extract version name (e.g., "v1.2.0" -> "1.2.0")
    const tagName = release.tag_name || "1.2.0";
    const cleanVersionName = tagName.replace(/^v/, "");

    // Extract or calculate versionCode:
    // Format: "1.2.0" -> 1*1000 + 2*100 + 0 = 1200 or custom mapping
    // We can also extract "versionCode: 2" from release body if present
    let versionCode = 2;
    const codeMatch = release.body?.match(/`?versionCode`?[:=\s]+(\d+)/i);
    if (codeMatch) {
      versionCode = parseInt(codeMatch[1], 10);
    } else {
      const parts = cleanVersionName.split(".").map(Number);
      if (parts.length >= 2 && !isNaN(parts[0]) && !isNaN(parts[1])) {
        versionCode = (parts[0] * 10) + parts[1]; // 1.2 -> 12, 1.0 -> 10, or custom
      }
    }

    // Find APK asset
    let apkUrl = "";
    if (release.assets && release.assets.length > 0) {
      const apkAsset = release.assets.find(a => a.name.endsWith(".apk"));
      if (apkAsset) {
        apkUrl = apkAsset.browser_download_url;
      }
    }

    if (!apkUrl) {
      apkUrl = `https://github.com/${GITHUB_OWNER}/${GITHUB_REPO}/releases/download/${tagName}/ReelLibrary-release-v${cleanVersionName}.apk`;
    }

    const releaseData = {
      versionCode: versionCode,
      versionName: cleanVersionName,
      apkUrl: apkUrl,
      releaseNotes: release.body || "Bug fixes and performance enhancements.",
      forceUpdate: false,
      minSupportedVersionCode: 1
    };

    response = new Response(JSON.stringify(releaseData), {
      headers: {
        "Content-Type": "application/json",
        "Cache-Control": "public, max-age=600" // Cache for 10 minutes
      }
    });

    ctx.waitUntil(cache.put(cacheKey, response.clone()));
    return releaseData;
  }

  return await response.json();
}

