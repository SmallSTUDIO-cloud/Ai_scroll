# AI BREAK — The Fully Local AI Social Experiment

> **Tagline:** "Your AI works hard. Let it scroll."

A 100% offline, privacy-first native Android application where an autonomous AI agent explores a vertical-video feed, makes real-time decisions, develops evolving topic preferences through on-device memory, and generates detailed analytical session reports—without any internet connection, cloud APIs, or external tracking.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following core design decisions have been established based on your clarification responses:

- **Confirmed Decision 1 (Dual-Engine Architecture):** The app operates with a primary deterministic rule-based heuristic brain alongside a dedicated Local AI Model import and validation subsystem (supporting local GGUF/LiteRT/TFLite model files imported via Android's Storage Access Framework), ensuring instant offline functionality with zero fake promises.
- **Confirmed Decision 2 (Full-Screen Visual Experience):** The simulator showcases full-screen vertical video cards with rich vector scene illustrations, engagement stats, topic tags, and a floating semi-transparent "Agent Brain" HUD displaying live thoughts, decision reasons, and action timers.
- **Confirmed Decision 3 (Dynamic Personality Evolution):** Users can choose from preset archetypes (The Tech Nerd, The Curious Explorer, The Comedian, The Analyst, or Custom), and the agent's personality traits dynamically adapt and evolve based on watched reels, human reel likes, or organically drift towards becoming a dedicated "Coder Agent."
- **Confirmed Decision 4 (Zero-Cloud Privacy Guarantee):** Zero network permissions requested. No Gemini, OpenAI, or remote API calls. All preferences, session history, and metrics persist exclusively in device-local Room SQLite storage.

---

## 1. Overview & Core Concept

- **What It Does:** AI BREAK turns the tables on algorithmic scrolling: instead of humans endlessly watching feeds, you grant your local AI agent a "break" to browse a rich vertical feed of 30+ bundled topics. The agent watches, pauses, likes, saves, or skips videos based on its personality. As it scrolls, its preference matrix shifts, its interests deepen, and it generates a comprehensive post-break debrief.
- **Target Audience:** Tech enthusiasts, AI experimenters, privacy advocates, and digital wellbeing seekers curious about autonomous agent behavior, machine decision-making, and on-device computing.
- **Key Value:** An entertaining, visually stunning behavioral experiment that runs completely in airplane mode with full transparency, zero data leakage, and rigorous honesty about on-device AI capabilities.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Onboarding & Lab Entrance:**
   - Welcome intro presenting the privacy pledge (100% local, no internet, airplane-mode ready).
   - Instant permission-free access to the Home Dashboard.
2. **Dashboard & Agent Setup:**
   - Laboratory dashboard showing agent readiness status (`Simulation Engine Active`, `Local Model Slot`, `Offline Integrity`).
   - Quick personality picker or deep editor with trait sliders (Exploration rate, Skip threshold, Watch duration, Coder affinity).
3. **Active AI Break Session (The Feed):**
   - Full-bleed vertical video feed with animated canvas-rendered scene graphics, creator badges, hashtags, and sound-wave visualizers.
   - Floating Agent Thought HUD with pulse indicator, current internal monologue, decision countdown, and immediate **STOP** / **PAUSE** controls.
   - Interactive dual control: user can manually like/save/swipe alongside the agent to influence its emerging tastes!
4. **Debriefing & Session Report:**
   - Automatic session breakdown upon stopping: total time spent, videos seen vs. skipped, top explored genres, taste-drift delta chart, and witty behavioral observations.
   - Option to save report, compare with previous sessions, or export via native Android share sheet.
5. **Model Lab & Real-Device Readiness:**
   - Model file picker (Storage Access Framework) to inspect local weights, check device RAM limits, and review text/multimodal capabilities.
   - Controlled Real-Device Mode gateway explaining system AccessibilityService requirements with safe default guards.

### Visual Identity & Theme

- **Aesthetic Direction:** Futuristic Cyber-Laboratory / High-tech Observatory. Restrained, clean, dark-mode first with precision typography and glowing accents.
- **Color Palette & Atmosphere:**
  - *Primary Dark Surface:* Deep Midnight Navy (`#080C14`) & Pure Obsidian (`#020408`)
  - *Card Surface:* Elevated Cyber Slate (`#0F172A`) with subtle cyan border glow (`#1E293B`)
  - *Electric Accents:* Neon Cyan (`#00F0FF`), Electric Blue (`#3B82F6`), High-vis Amber (`#F59E0B`), and Terminal Green (`#10B981`)
  - *Typography:* High-legibility crisp sans-serif with monospace accents for telemetry and thought strings.
- **Interactive Feedback & Motion:**
  - Smooth vertical snapping transitions (`rememberPagerState`).
  - Spring-animated like heart bursts and glowing thought-bubble pulses.
  - Tactile M3 ripple states and clear visual badges distinguishing heuristic decisions from local LLM outputs.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Bundled Procedural Vector Video Cards vs. Heavy Media Files**
  - *Chosen Approach:* Bundle 32 rich, diverse content items featuring category-specific animated Canvas compositions, dynamic gradient backdrops, creator handles, descriptions, and metadata.
  - *Why:* Keeps APK size under 15MB, ensures instant 60fps scrolling without network/codec issues, and provides rich semantic metadata for the agent brain to evaluate.
- **Decision 2: Room SQLite for Persistence vs. In-Memory or DataStore**
  - *Chosen Approach:* Room Database with separate DAOs for Session History, Personality Profiles, Dynamic Topic Preferences, and Saved Reel Collections.
  - *Why:* Guarantees session persistence across app restarts, supports relational queries for multi-session trend analysis, and allows complete local memory wipes.
- **Decision 3: Transparent Dual-Brain Architecture (`AgentBrain` abstraction)**
  - *Chosen Approach:* A unified interface with two distinct implementations: `HeuristicRuleBrain` (deterministic, configurable, instant) and `LocalModelBrain` (SAF model loader, memory bounds verification, structured JSON parser).
  - *Why:* Never lies about model status. If no local model is loaded, the app runs as a clearly marked, rich "Heuristic Behavioral Simulator", while gracefully supporting local model weights when imported.
- **Decision 4: Accessibility Foundation with Non-Destructive Scoping**
  - *Chosen Approach:* Complete Android `AccessibilityService` structure with explicit user authorization flow, target app picker, and basic scroll dispatch, strictly gated behind user confirmation.

---

## 4. Technical Architecture & Data Strategy

### System Architecture Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                              AI BREAK UI                               │
│  ┌──────────────────┐ ┌──────────────────┐ ┌────────────────────────┐  │
│  │  Home Dashboard  │ │  Vertical Feed   │ │ Agent Thought Overlay  │  │
│  └────────┬─────────┘ └────────┬─────────┘ └───────────┬────────────┘  │
│           │                    │                       │               │
│  ┌────────┴─────────┐ ┌────────┴─────────┐ ┌───────────┴────────────┐  │
│  │ Personality View │ │  Session Report  │ │   Model Management     │  │
│  └────────┬─────────┘ └────────┬─────────┘ └───────────┬────────────┘  │
└───────────┼────────────────────┼───────────────────────┼───────────────┘
            ▼                    ▼                       ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        VIEWMODELS & ORCHESTRATION                      │
│   • FeedSessionViewModel (Timer, Decision Loop, Gesture Dispatches)    │
│   • PersonalityViewModel (Trait Sliders, Evolution, Presets)           │
│   • ModelManagerViewModel (SAF Import, File Parsing, Memory Check)     │
│   • ReportHistoryViewModel (Analytics, Trend Deltas, Local Export)     │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                             AGENT ENGINE                               │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                      AgentBrain (Interface)                      │  │
│  │        + decideNextAction(context: ReelContext): AgentDecision   │  │
│  └───────────────────▲──────────────────────────────▲───────────────┘  │
│                      │                              │                  │
│       ┌──────────────┴──────────────┐ ┌─────────────┴────────────┐     │
│       │ HeuristicRuleBrain          │ │ LocalModelBrain          │     │
│       │ (Taste scores, RNG balance, │ │ (Imported GGUF/LiteRT    │     │
│       │  Coder drift, Trait weights)│ │  validation & parser)    │     │
│       └─────────────────────────────┘ └──────────────────────────┘     │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                     LOCAL STORAGE & ACCESSIBILITY                      │
│   ┌────────────────────────────────────────────────────────────────┐   │
│   │ Room Database (Entities: Session, Personality, History, Prefs) │   │
│   └────────────────────────────────────────────────────────────────┘   │
│   ┌────────────────────────────────────────────────────────────────┐   │
│   │ Accessibility Controller (Controlled scrolling for real apps)  │   │
│   └────────────────────────────────────────────────────────────────┘   │
└────────────────────────────────────────────────────────────────────────┘
```

### Core Entities & Database Schema

1. **`AgentProfileEntity`**:
   - `id`: String (e.g. `"tech_nerd"`, `"comedian"`, `"custom_coder"`)
   - `name`, `description`, `avatarType`
   - `explorationRate`: Float (0.0 - 1.0)
   - `skipThresholdSec`: Float (1.0 - 15.0)
   - `coderAffinity`: Float (dynamic bias towards programming/tech reels)
   - `isCustom`: Boolean
2. **`TopicPreferenceEntity`**:
   - `profileId`: String (FK)
   - `topic`: String (`"technology"`, `"coding"`, `"ai"`, `"comedy"`, `"gaming"`, `"science"`, `"cooking"`, `"animals"`, etc.)
   - `score`: Float (-1.0 to +1.0)
   - `watches`: Int, `skips`: Int, `likes`: Int
3. **`SessionRecordEntity`**:
   - `sessionId`: String (UUID)
   - `profileId`: String
   - `startTime`: Long, `durationSeconds`: Long
   - `engineType`: String (`"HEURISTIC_SIM"` vs `"LOCAL_MODEL"`)
   - `totalSeen`: Int, `totalLiked`: Int, `totalSaved`: Int, `totalSkipped`: Int
   - `topTopic`: String, `driftSummary`: String
4. **`FeedItem`**:
   - Rich bundled dataset of 32 items with title, creator, category, description, duration, tags, initial engagement metrics, and visual generative theme.

---

## 5. Execution Phases & Testing Plan

- **Phase A (Core Offline Framework):**
  - Setup Room schema, DAOs, repositories, and bundled dataset.
  - Implement M3 dark cyber-observatory theme with custom color tokens.
  - Build Full-Screen Vertical Feed (`HorizontalPager`/`VerticalPager`) with fluid gesture navigation, animated canvas backgrounds, and metadata HUD.
- **Phase B (Agent Decision Engine & Evolution):**
  - Implement `AgentBrain`, `HeuristicRuleBrain`, and action queue (`SWIPE_UP`, `LIKE`, `SAVE`, `WAIT`, `STOP`).
  - Implement dynamic preference evolution: watching or liking programming/tech videos boosts `coderAffinity` and unlocks the specialized "Coder Agent" title!
  - Floating thought inspector with animated typing effect and live decision reasons.
- **Phase C (Analytics & Reports):**
  - Polished AI Break debrief screen with category bar charts, preference drift delta, and behavioral summary.
  - Session history list with detailed drill-down and full local data wipe/reset tools.
- **Phase D (Model Import Lab & Accessibility Gateway):**
  - Storage Access Framework model importer with file metadata inspection, memory safety calculation, and capability detection.
  - Real-app AccessibilityService foundation with user confirmation dialog, explicit app selection, and safe scrolling dispatch.
- **Verification & Acceptance:**
  - Compile with Gradle and verify 100% offline compliance (zero network calls, no INTERNET permission in Manifest).
  - Run unit and Robolectric tests for decision engine and preference updating.

---

# 6. Feature Addendum: Floating AI BREAK Companion

> **This addendum extends the original plan. Where it conflicts with the original strict offline-only statements, the mode/build-variant rules in Section 8 supersede them. The local-only experience must remain available.**

## 6.1 Product Goal

Add a small, movable, animated AI companion that can remain on top of other apps (with explicit Android permission), including Instagram. The companion is a quick entry point for voice/text questions, screen-context explanations, and user-approved Reel reply drafting. It must not pretend to understand a Reel when the available UI metadata or captured frames are insufficient.

Use the user-provided pixel-creature image as the initial avatar reference. The artwork should be configurable, and the UI must not falsely label a custom avatar as an official Claude/OpenAI character. Provider names should be shown as the selected brain/provider, separate from the avatar art.

## 6.2 Floating Bubble & Companion Panel

- Implement `FloatingCompanionService` and a `WindowManager` application overlay, gated behind a clear explanation and the user's explicit `SYSTEM_ALERT_WINDOW` grant.
- Bubble behaviours: drag, snap to left/right screen edge, minimize/expand, adjustable size/opacity, persistent dismiss/stop control, and tap-to-open.
- The companion can display: avatar, current brain/provider, listening/thinking/speaking status, short answer preview, privacy/capture status, and Stop/Pause controls.
- Add a compact panel with conversation history, text input, microphone button, stop speaking, clear conversation, and a way to switch brain modes.
- Avoid covering key Instagram controls. Support edge snapping and user repositioning; provide a one-tap hide/close control.
- Use lifecycle-safe foreground-service behavior only where required by the relevant Android function. Overlay visibility must not be used to disguise recording or to evade Android background-execution restrictions.

## 6.3 Avatar States and Animation

Implement a lightweight state-driven animation system. Required states:

- `IDLE`: subtle breathing/blinking or small idle motion.
- `LISTENING`: microphone/listening indicator while the user has deliberately activated voice input.
- `THINKING`: processing animation while the selected brain is working.
- `ANALYZING_SCREEN`: visible indicator while screen context is being read.
- `SPEAKING`: mouth/body animation synchronized approximately to spoken response.
- `HAPPY`, `CURIOUS`, `CONFUSED`, `NEUTRAL`: optional conversational reactions; never allow the expression to imply confidence when the result is uncertain.
- `OFFLINE` / `ERROR`: clearly indicate when the selected provider is unavailable and offer the local fallback where supported.

Prefer vector or sprite-sheet animations to keep the app small and smooth. Expressions are presentation only; the app must not claim the model literally feels emotions.

## 6.4 Voice Conversation

- Push-to-talk is the default: the user taps the microphone to begin and taps again or presses Stop to end. Do not add always-listening or wake-word operation in the first release.
- Request microphone permission only when the user activates voice input and explain its purpose.
- Provide speech-to-text, text correction before sending (optional), and spoken responses through Android text-to-speech or a configured speech service.
- Clearly distinguish offline speech support from services that may require network access; speech recognition availability varies by device and installed language packs.
- Include mute, stop-speaking, and text-only controls. Never transmit microphone audio to a remote provider in Offline Mode.

## 6.5 Screen Context: “Explain This Reel”

Add a user-triggered action such as `Explain this screen` / `Summarize this Reel`.

Context collection order:
1. Read relevant, accessible UI text and labels where available.
2. If that is insufficient, use an explicitly started Android MediaProjection session to obtain selected screen frames, respecting Android's system consent flow and foreground-service requirements.
3. Sample only a limited number of frames needed for a question, rather than continuously recording by default.
4. If Connected Mode is active, display a clear disclosure and require the user's setting/consent before screen frames or extracted text can leave the device. Offline Mode must keep this processing on-device.
5. Return a summary with uncertainty labels. If the Reel content is obscured, moving too quickly, or visually ambiguous, ask the user to provide context instead of inventing details.

The app must visibly indicate when screen capture/analysis is active, stop promptly when requested, and handle Android's projection-stop callback. Screen capture must not run secretly. Each Android 14+ MediaProjection session requires user consent; see the Android developer documentation linked in Section 10.

## 6.6 Reel Assistance and Replies

- Detect Reel transitions using accessibility/UI state and, when user-authorized, limited screen-frame comparison.
- Offer commands like `Explain`, `Summarize`, `What is the joke?`, `Explain the coding technique`, and `Draft a reply`.
- `Draft a reply` generates a comment based on the available Reel context, displays it for review, and lets the user edit/copy it.
- Posting a comment, sending a reply, following an account, liking, saving, or any other consequential account action must require an explicit user confirmation in the initial release. Never publish comments in the background without the user seeing the final text.
- Keep manual Pause/Stop visible. Do not implement spammy repeated comments, engagement farming, evasion of platform rate limits, or covert account actions.
- Real-device scrolling/automation is a separate controller. Before enabling any automation, review the applicable Google Play Accessibility API policy. Google's current policy prohibits apps from using AccessibilityService to autonomously initiate, plan, and execute actions or decisions unless the app genuinely qualifies as an accessibility tool whose core purpose is disability assistance. Do not falsely label AI BREAK as an accessibility tool. See Section 10.

## 6.7 Agent/Provider Architecture

Keep the existing `AgentBrain` abstraction and add a separate conversational/provider layer instead of tying the UI to one company:

```text
AgentBrain
├── HeuristicRuleBrain
├── LocalModelBrain
└── RemoteModelBrain (Connected build only)
    ├── OpenAI API adapter
    └── Anthropic API adapter

Companion UI
├── FloatingCompanionService
├── CompanionOverlayController
├── AvatarStateAnimator
├── VoiceInputController
├── SpeechOutputController
└── ScreenContextController

ScreenContextController
├── AccessibilityContextReader
└── UserConsentedMediaProjectionReader
```

Provider integration requirements:
- The floating Android companion must call a supported model API (or another explicitly supported integration) for remote responses. A ChatGPT/Claude plugin or MCP connector is not, by itself, a mechanism that grants an Android app access to the user's logged-in ChatGPT/Claude consumer app or subscription.
- Keep separate adapters for OpenAI and Anthropic so the app can switch providers without rewriting the overlay or agent engine.
- Do not embed a developer's secret API key in the APK. Use a properly secured backend for shared credentials or user-supplied credentials stored using Android secure storage with clear risk disclosure. Never write secrets to logs or analytics.
- Provider API access, consumer-app subscriptions, and custom MCP connections are distinct products with distinct availability and billing. Verify the selected provider's current terms, API capabilities, pricing, image input support, and retention controls before enabling each adapter.
- An optional MCP server can later expose selected AI BREAK functions to supported external ChatGPT/Claude clients. That is a separate integration direction from using an LLM as the phone's companion brain, and it must have authenticated, scoped tools with read-only defaults.
- Use capability-specific interfaces such as `ConversationBrain`, `ScreenUnderstandingProvider`, and `SpeechProvider`; do not assume every model supports images, audio, or tool calls.

## 7. Privacy, Permission, and Safety Requirements

- Request overlay, microphone, and screen-capture permissions only when the associated feature is first used, with plain-language explanations.
- The user must be able to see which brain is active, whether the app is offline/connected, and whether screen capture, microphone input, or remote transmission is active.
- Include a global Pause/Stop switch in the overlay and a persistent indicator while capture or a long-running user-started task is active.
- Never send captured frames, extracted UI text, microphone audio, or conversation history to a remote provider without clear user-facing disclosure and an explicit opt-in to Connected Mode.
- Provide conversation deletion, local history deletion, and model/provider disconnect/reset controls. Keep local session preferences in Room as planned.
- Do not read unrelated apps by default. Use an app allowlist and make target-app selection explicit.
- Do not capture or transmit password fields, payment credentials, authentication codes, private notifications, or other unrelated sensitive material. If reliable exclusion is not possible, pause analysis and ask the user to manually select/share content.
- Avoid background microphone recording. Push-to-talk is the default for version 1.
- Clearly label model output as AI-generated and give the user a way to inspect or edit suggested replies before posting.

## 8. Build Variants and Offline Guarantee

The original plan promises no `INTERNET` permission and zero network calls. A remote ChatGPT/Claude brain cannot satisfy that same APK-level guarantee, even if network use is disabled in the UI. Preserve honest privacy guarantees by separating builds:

1. **`offline` build**: no `INTERNET` permission; uses `HeuristicRuleBrain` and imported local models only. No remote provider adapters. All text/image/audio processing must remain local to the extent supported by the selected on-device components.
2. **`connected` build**: declares network access for explicitly enabled remote provider adapters. Local mode remains usable, but the UI must explain which actions send data remotely and what data is sent. Connected Mode starts disabled until the user configures a provider and consents.

Use Gradle product flavors/source sets so the offline manifest can be verified to have no `android.permission.INTERNET`. Do not claim the Connected build is “100% offline.”

The overlay, voice, and screen-context features must degrade gracefully when permissions are denied. The simulator must remain fully usable without any of these permissions.

## 9. Implementation Phases and Acceptance Tests

- **Phase E — Floating companion:** implement bubble lifecycle, drag/edge snap, expandable panel, avatar states, Stop/Pause, and permission onboarding. Test over multiple apps, rotation/configuration changes, process recreation, and system memory pressure.
- **Phase F — Conversation & voice:** implement text chat first, then push-to-talk and spoken output. Verify permission denial, mute, cancel, network loss, and local/remote status labels.
- **Phase G — Screen context:** implement accessibility text extraction and an explicit “Explain this Reel” flow. Then add consent-based MediaProjection frame sampling and on-device/connected visual understanding through capability-based interfaces.
- **Phase H — Model providers:** implement provider adapters behind interfaces; add connectivity, quota, timeout, privacy, and redaction handling. Never put API secrets in source control or the APK.
- **Phase I — Reel reply assistance:** add context-grounded drafts, edit/copy, and explicit confirmation before account-changing actions. Real-device automated action support is gated on policy review and must not be presumed Play Store compliant.
- **Tests:** unit-test provider selection, privacy-mode routing, overlay state transitions, conversation cancellation, redaction rules, and reply confirmation. Instrumented tests should verify that Offline Mode makes no network requests and that the Offline APK manifest contains no Internet permission.
- **Release gate:** do not describe the application as ready for fully autonomous Instagram control or Google Play release until Android permission flows, platform terms, Accessibility API policy, provider policies, real-device tests, and privacy disclosures have been reviewed.

## 10. Official Technical References

- [Android application overlays (`SYSTEM_ALERT_WINDOW`)](https://developer.android.com/reference/android/Manifest.permission#SYSTEM_ALERT_WINDOW)
- [Android MediaProjection / screen capture](https://developer.android.com/media/grow/media-projection)
- [Google Play: Use of the AccessibilityService API](https://support.google.com/googleplay/android-developer/answer/10964491)
- [OpenAI Apps SDK](https://help.openai.com/en/articles/12515353-build-with-the-apps-sdk)
- [OpenAI custom MCP server availability and connection model](https://help.openai.com/en/articles/12584461-connecting-custom-mcp-servers-in-chatgpt)
- [Anthropic Model Context Protocol (MCP)](https://docs.anthropic.com/en/docs/mcp)

