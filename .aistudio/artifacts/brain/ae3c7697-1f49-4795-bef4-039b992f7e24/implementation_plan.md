# Production-Grade Cognitive Reels Agent & Autonomous Engagement Engine

An intelligent, senior-engineered background companion for Instagram, YouTube Shorts, and TikTok that deeply perceives reels (captions, creators, likes, shares, comments, and on-screen text) using Gemini 3.5 Flash with offline fallback, autonomously posts genuine contextual comments, adaptively watches captivating videos in full, and maintains a movable, expressive floating presence.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural decisions were confirmed via interactive user requirements:
> - **Autonomous Comment Posting**: Fully autonomous comment typing into the social app's comment input node followed by instant auto-submission via accessibility actions (`ACTION_SET_TEXT` + send button click).
> - **Adaptive Watch Time**: Dynamic viewing duration based on reel substance: captivating or high-engagement reels are watched fully (18–35 seconds), while low-signal reels are skipped in 5–8 seconds.
> - **Intelligence Brain**: Online Gemini AI (`gemini-3.5-flash`) for nuanced reel context comprehension, with an ultra-fast on-device heuristic fallback ensuring continuous operation even offline or with zero latency.
> - **Movable UI**: Both the circular avatar bubble and the compact cyber control card are fully draggable anywhere across the screen without restrictive edge snapping.

---

## 1. Overview & Core Concept

- **What It Does**: As the user browses short-form video feeds (or lets the bot run autonomous watch sessions), the agent reads all on-screen UI nodes in the background (creator name, like count, comment count, share count, full captions, on-screen text, and audio cues). It feeds this context to an intelligent reasoning loop that produces authentic human reactions, decides whether to double-tap like (~1 in 10 reels), chooses when to watch a reel to completion, and autonomously opens comments, reads discussions, types an insightful comment, and submits it.
- **Target Audience**: Power users and creators seeking an intelligent, lively co-pilot that simulates authentic human engagement and curation.
- **Key Value**: Replaces mechanical scrolling with intelligent comprehension and true autonomous interaction.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Background Perception & Covert Comprehension**:
   - The user opens Instagram Reels, YouTube Shorts, or TikTok.
   - The bot inspects all visible text, metrics (like count, share count, comment count), author identity, and audio title.
   - Background reasoning processes the context without displaying raw metadata to the user.

2. **Lively Human Reaction**:
   - The bot displays expressive pop-out comic thought bubbles with context-specific reflections (e.g. *"Bro she is stunning... algorithm definitely locked in on my taste 🤩✨"*, *"Wait this compiler optimization workflow is genius 💻🧠"*, or *"LMAOO the top comment got me crying 💀😭"*).
   - Animated emoji badges bounce on the avatar in sync with mood states (`HAPPY`, `CURIOUS`, `THINKING`, `SPEAKING`).

3. **Autonomous Comment Posting Sequence**:
   - Every ~10–14 reels (or upon user trigger from the overlay card), the bot enters comment mode:
     1. Taps comment button node on the right rail.
     2. Pauses 1.2s to scan top community comments.
     3. Synthesizes a natural, high-IQ comment tailored to the video topic and personality.
     4. Focuses the comment input field (`EditText` node) and sets the text via `ACTION_SET_TEXT`.
     5. Clicks the send/submit button (`button_post`, `send_button`, or contentDescription "Post").
     6. Delays 1.0s, then closes the comment sheet via back gesture.
     7. Pops a thought bubble: *"Dropped a comment on that! Moving to next reel 🚀"*.

4. **Adaptive Pacing (Full Reel vs Quick Skip)**:
   - Deep/captivating reels (long educational captions, high engagement, or aesthetic beauty): Watched for 18–35 seconds.
   - Standard reels: Watched for 6–10 seconds.
   - Low-quality or unengaging content: Skipped after 4–6 seconds.

5. **Movable Free-Floating Overlay**:
   - Both the circular companion bubble and the compact popup card can be dragged freely to any screen position.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Dual-Brain Intelligence (Gemini 3.5 Flash + Heuristic Fallback)**
  - *Chosen Approach*: When internet is available and an API key is present, queries `gemini-3.5-flash` for deep semantic comprehension and comment drafting; if offline or during rapid scrolling, immediately falls back to local heuristic reasoning without delaying gestures.
  - *Why*: Delivers high-IQ, human-level commentary while guaranteeing zero freeze or latency in gesture dispatching.

- **Decision 2: Direct Accessibility Typing & Submission**
  - *Chosen Approach*: Uses Android's `AccessibilityNodeInfo.ACTION_SET_TEXT` with `Bundle.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, comment)` on the comment input node, followed by finding and clicking the submit button or dispatching a tap coordinate.
  - *Why*: Works natively across Instagram, YouTube Shorts, and TikTok without needing clipboard pasting or user intervention.

- **Decision 3: Non-Intrusive Floating Window Hierarchy**
  - *Chosen Approach*: Single `ComposeView` with `FLAG_NOT_FOCUSABLE` during idle viewing, toggling `FLAG_NOT_TOUCH_MODAL` only when the user opens the keyboard in the popup.
  - *Why*: Allows touch events outside the companion to pass directly to the underlying video player (swiping, pausing) uninterrupted.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Background Reels Agent                          │
│                                                                        │
│   ┌──────────────────────────────────────────────────────────────┐     │
│   │                 AiBreakAccessibilityService                  │     │
│   │  - parseReelMetrics(): likes, comments, shares, views       │     │
│   │  - autoTypeAndSubmitComment(text): native comment posting    │     │
│   │  - performDoubleTapLike(): center viewport gesture           │     │
│   │  - performHumanScrollUp(): organic thumb swipe               │     │
│   └──────────────────────────────┬───────────────────────────────┘     │
│                                  │ Metric & Node Stream                │
│                                  ▼                                     │
│   ┌──────────────────────────────────────────────────────────────┐     │
│   │                 FloatingCompanionController                  │     │
│   │  - Gemini 3.5 Flash + Offline Brain Integration              │     │
│   │  - Adaptive Watch Timer (18-35s captivating vs 5-8s skip)    │     │
│   │  - Autonomous Like (~1 in 10) & Comment Auto-Poster          │     │
│   └──────────────────────────────┬───────────────────────────────┘     │
│                                  │ State & Comic Thought               │
│                                  ▼                                     │
│   ┌──────────────────────────────────────────────────────────────┐     │
│   │                    FloatingCompanionService                  │     │
│   │  - Free-floating draggable overlay view                      │     │
│   │  - PopOutComicBubble with animated expressions               │     │
│   │  - CompactOverlayPopup with manual action triggers           │     │
│   └──────────────────────────────────────────────────────────────┘     │
└────────────────────────────────────────────────────────────────────────┘
```

### Component Implementations

1. **`AiBreakAccessibilityService.kt`**:
   - Enhanced `extractVisibleReelContent()`: Extracts numeric engagement metrics (likes string like "1.2M", comment count like "4.5K", shares) and video text chunks.
   - `postAutonomousComment(commentText: String): Boolean`:
     - Opens comment drawer if not open.
     - Locates comment input field node (`EditText` / `android.widget.EditText`).
     - Injects text via `ACTION_SET_TEXT`.
     - Finds and clicks submit button (`Post`, `Send`, or coordinate tap).
     - Closes drawer cleanly.

2. **`GeminiReelBrain.kt`**:
   - Direct REST integration with `gemini-3.5-flash` using `BuildConfig.GEMINI_API_KEY`.
   - Generates contextual, witty comments and thoughtful internal reactions based on reel title, caption, creator, and metrics.
   - Falls back gracefully to `HeuristicConversationBrain` when offline.

3. **`RealDeviceController.kt` & `FloatingCompanionController.kt`**:
   - Adaptive watch time algorithm: computes reel interest score -> determines delay (18–35s for full watch vs 6–10s standard).
   - Frequency manager: ~1 in 10 like dispatch, ~1 in 12 comment auto-post.

4. **`FloatingCompanionService.kt` & `CompactOverlayPopup.kt`**:
   - Make the entire popup card draggable anywhere on screen, giving the user full flexibility over placement.

---

## 5. Verification & Testing Plan

1. **Compilation Check**:
   - Run `compile_applet` to verify clean build with zero errors.
2. **Local Robolectric & Unit Tests**:
   - Run `gradle :app:testDebugUnitTest` to test metric extraction, adaptive watch time calculation, and comment generation.
3. **APK Assembly**:
   - Run `gradle assembleDebug` and update `.build-outputs/app-debug.apk` and `public/app-debug.apk`.
