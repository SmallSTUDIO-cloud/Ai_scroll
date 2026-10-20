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
