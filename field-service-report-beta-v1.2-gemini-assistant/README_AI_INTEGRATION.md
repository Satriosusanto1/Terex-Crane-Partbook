# Field Service Report Beta — AI Assistant Integration

## What changed

- Added an **AI Assistant** action to the report editor.
- Added four actions: improve finding wording, structure notes, check report completeness, and summarize for a supervisor.
- Results are shown in an editable preview and only written into a selected field after the user presses **Masukkan**.
- Added a client service that calls a configurable HTTPS backend endpoint. It does **not** put a Gemini API key in the APK.
- Added a Node.js server-side Gemini starter in `backend/`.

## Important: not yet connected to a live Gemini service

The beta ZIP did not include a deployed backend URL, `google-services.json`, Gemini server secret, or hosting credentials. Therefore, the AI UI is implemented, but requests will show a clear configuration message until a real HTTPS backend is configured and deployed. No live Gemini call or APK build is claimed.

## Configure Android app endpoint

Provide a public HTTPS URL for the backend when building:

```bash
./gradlew assembleDebug -PFSR_AI_ENDPOINT=https://YOUR-HOST/api/fsr-ai
```

Alternatively set the `FSR_AI_ENDPOINT` environment variable. The endpoint URL is not a secret; the Gemini API key must remain server-side.

## Backend starter

In `backend/`:

```bash
npm install
GEMINI_API_KEY="set-this-in-your-host-secret-manager" npm start
```

Deploy behind HTTPS and configure `FSR_AI_ENDPOINT` to the `/api/fsr-ai` route. Do not commit real credentials or `.env` files.

### Production security requirements

The sample backend includes request-size limits, per-IP rate limiting, response minimization, and avoids logging report content. Before public deployment, add a strong caller-verification layer such as Firebase App Check verified server-side or an authenticated gateway. IP rate limits alone do not prevent abuse. Configure hosting-level quotas, budget alerts, monitoring, and HTTPS. Do not expose the endpoint publicly without caller verification.

## Data and safety behavior

The client sends selected report text and a limited set of report fields. Photos, signatures, names, email addresses, and parts list are not sent by default. The AI output is a draft; the technician must review it. AI does not save/approve a report automatically. The existing save, PDF, email, WhatsApp, and local database paths were not intentionally changed.

## Build status and limitations

This environment did not have a Gradle executable or Gradle wrapper JAR in the supplied ZIP. The Kotlin/Gradle changes have been statically inspected, but a successful Gradle compile, device test, and APK signing were not possible here. Open the project in Android Studio or add the standard Gradle wrapper, configure the endpoint, then run unit/build/device tests before distribution.

## Deploy the Gemini backend on Render (starter)

This repository includes `render.yaml` as a Render Blueprint. Push the project to a private GitHub repository, then in Render choose **New → Blueprint**, select that repository, and deploy the `fsr-gemini-backend` service. When Render asks for `GEMINI_API_KEY`, enter the key as a secret in Render's environment settings; do not put it in GitHub, `.env` in the Android project, or the APK. Keep billing/usage limits enabled for the Google AI project. The free Render service may sleep when idle and the first request can be slow.

After deployment:

1. Open `https://YOUR-SERVICE.onrender.com/health`; expect `{"ok":true}`.
2. Set the Android build property to `FSR_AI_ENDPOINT=https://YOUR-SERVICE.onrender.com/api/fsr-ai`.
3. Build and install the APK, then test each of the four actions using non-sensitive sample text.
4. Confirm that AI output is only a draft and does not automatically approve or save the report.

**Security status:** this is a deployment starter, not a production-secured public API. Before using it with real work reports, add server-verified caller authentication (for example Firebase App Check verification or an authenticated gateway), configure provider quotas/budget alerts, and test rate limits. A public endpoint protected only by IP rate limiting can be abused and may incur Gemini costs. Do not send confidential customer/site information until the caller-verification layer is in place.

## Manual build configuration

In Android Studio, open the project and use Gradle argument `-PFSR_AI_ENDPOINT=https://YOUR-SERVICE.onrender.com/api/fsr-ai` for the build, or set the `FSR_AI_ENDPOINT` environment variable before launching Gradle. For a debug build:

```bash
./gradlew assembleDebug -PFSR_AI_ENDPOINT=https://YOUR-SERVICE.onrender.com/api/fsr-ai
```

The supplied ZIP still requires a standard Gradle wrapper script/JAR to be restored if Android Studio does not generate one automatically. This environment has not compiled or run the Android app, so a successful APK build must be confirmed in Android Studio before field use.
