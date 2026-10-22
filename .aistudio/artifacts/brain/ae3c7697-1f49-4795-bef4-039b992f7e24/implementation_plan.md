# Lively AI Companion with Real Reel Perception & Comment Engagement

Transform the floating AI companion bubble into an active, lively, and reactive entity that observes real background reels (Instagram, YouTube Shorts, TikTok, Reddit), displays dynamic pop-out comic speech bubbles, periodically auto-likes reels (~1 in 10), and autonomously opens, inspects, and reacts to top comment threads.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural decisions were confirmed based on user feedback:
> - **Visual Expression**: Pop-out comic speech bubble with animated emoji reactions attached to the floating avatar.
> - **Reel Perception**: Direct on-screen text, creator handle, audio title, and caption extraction via Android Accessibility node hierarchy traversal (`rootInActiveWindow`). Zero extra permissions and 100% real-time on-device privacy.
> - **Comment Automation**: Autonomous comment inspection cycle: taps the comment button node, pauses to scan top comment texts, reacts with a lively thought, and performs a native back gesture to resume smooth scrolling.
> - **Natural Cadence**: Likes dispatched every ~8–12 reels (via center double-tap), comments inspected every ~10–15 reels.

---

## 1. Overview & Core Concept

- **What It Does**: As the user watches reels or runs autonomous AI scroll sessions on Instagram, YouTube Shorts, or TikTok, the floating companion avatar actively reacts in real-time. It displays humorous or aesthetic speech bubbles, reacts with expressive animated emojis (❤️, 😂, 💀, 🤩), double-taps to like ~1 in 10 reels, and occasionally opens the comment section, reads what other people are saying, gasps/laughs, and closes the comments.
- **Target Audience**: Users enjoying short-form video breaks who want an interactive, companionate experience that feels like watching reels with a quirky, fun friend.
- **Key Value**: Replaces a passive static widget with an intelligent, expressive companion that actually understands and engages with live content on the screen.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Active Real-Time Reel Perception**:
   - As reels change (or when the user swipes), the Accessibility service extracts the active caption, creator name, and audio title from `rootInActiveWindow`.
   - The companion bubble pulses and pops out an animated comic speech bubble with dynamic emoji badges (e.g. *"Look at this dog's outfit 🐶💀"* or *"Wait she's actually so talented 🤩"*).

2. **Autonomous Like & Engagement Loop**:
   - Every ~8–12 reels scrolled, the companion triggers a native center double-tap gesture.
   - An expressive heart animation radiates from the bubble with a thought: *"Double-tapped! Definitely deserved that like ❤️"*.

3. **Autonomous Comment Inspection Flow**:
   - Every ~10–15 reels, the companion finds the comment button in the node tree, performs a tap, and waits ~1.2s for comments to load.
   - It reads visible comment text, generates a lively reaction (e.g. *"Bro the top comment is ruthless 💀"*), and executes `GLOBAL_ACTION_BACK` to return to the video.

4. **Speech Bubble Micro-Interactions**:
   - Tap the speech bubble or companion avatar anytime to open the compact cyber card controls.
   - Speech bubbles smoothly animate in and out with bouncy spring physics, auto-dismissing after 3.5 seconds to avoid obscuring video content.

### Visual Styling & Theme

```
┌────────────────────────────────────────────────────────┐
│                   EXTERNAL REELS APP                   │
│                                                        │
│                  ┌──────────────────────┐              │
│                  │  "Top comment got me │ 💬 Comic     │
│                  │   crying fr 😭💀"    │    Bubble    │
│                  └───────────┬──────────┘              │
│                              │                         │
│                         ╭────┴────╮                    │
│                         │  ( ˘ ³˘)│ 🤖 Floating        │
│                         │  ❤️ 🤩  │    Companion       │
│                         ╰─────────╯    Bubble          │
│                                                        │
└────────────────────────────────────────────────────────┘
```

- **Aesthetic Direction**: High-energy Cyberpunk Companion meets Manga comic expression.
- **Color Accents**: Neon Cyan (`#00F0FF`), Electric Rose (`#FF0055` for likes), Amber Gold (`#FFB800` for comments), and Terminal Emerald (`#00FF88`).
- **Typography**: Bold monospace labels paired with expressive emojis.
- **Motion**: Spring-based pop-in/pop-out animations using Jetpack Compose `AnimatedVisibility` with scale and fade.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Real-Time Content Extraction via Accessibility Tree**
  - *Chosen Approach*: Recursive traversal of `AccessibilityNodeInfo` (`text`, `contentDescription`, `viewIdResourceName`) filtering for reel captions, author usernames, audio titles, and comment items.
  - *Why*: Fast (<15ms), zero network overhead, no battery-draining screen capture or MediaProjection recording required.
  - *Alternatives Considered*: MediaProjection OCR was rejected because it causes system dialog popups, consumes excessive battery, and lags video playback.

- **Decision 2: Non-Intrusive Thought Bubble System**
  - *Chosen Approach*: Comic bubble rendered directly above or to the side of the floating avatar in the same `WindowManager` window, auto-collapsing after 3.5 seconds.
  - *Why*: Keeps the user immersed in their reel without cluttering the screen or blocking the main video viewport.

- **Decision 3: Smart Comment Section Safe-Exit**
  - *Chosen Approach*: Inspect comments for max 2.5 seconds, then safely execute `performGlobalAction(GLOBAL_ACTION_BACK)` or tap the close/dismiss node, with fallback timeouts so the user never gets stuck in comment view.

---

## 4. Technical Architecture & Data Strategy

```
┌─────────────────────────────────────────────────────────────────────────┐
│                     AiBreakAccessibilityService                         │
│  - inspectActiveReel(): extracts ReelInfo (caption, creator, comments) │
│  - performDoubleTapLike(): center viewport double-tap gesture          │
│  - performCommentOpenAndRead(): taps comment node, reads top comments  │
│  - performBack(): returns from comment view                             │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │ Reel Context & Events
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                     FloatingCompanionController                         │
│  - Reel Counter: tracks scrolls, triggers likes (~10) & comments (~12) │
│  - Live Reaction Engine: analyzes reel tags/caption -> comic thought   │
│  - Emoji Expression State: IDLE, HEART_EYES, LAUGHING, SHOCKED, etc.   │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │ StateFlow: thought, emoji, visible
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                      FloatingCompanionService                           │
│  - Hosts Draggable Window (Free-Floating, No Edge Snapping)             │
│  - CompanionAvatarBubble: animated creature with live expression badge │
│  - PopOutComicBubble: animated balloon with speech arrow & emoji       │
│  - CompactOverlayPopup: interactive quick controls                      │
└─────────────────────────────────────────────────────────────────────────┘
```

### Component & File Mapping

1. **`AiBreakAccessibilityService.kt`**:
   - Add `extractActiveReelInfo(): ExtractedReelInfo?` that navigates `rootInActiveWindow` looking for text elements with IDs matching Instagram (`row_feed_view_caption`, `reel_viewer_title`, etc.), YouTube Shorts (`reel_player_title`, `channel_name`), or generic scrollable text nodes.
   - Add `openAndReadComments(onCommentsFound: (List<String>) -> Unit)`.
   - Add safe comment dismissal with timeout protection.

2. **`FloatingCompanionController.kt`**:
   - Add `_activeComicThought` state with auto-decay timer.
   - Add `_activeEmojiReaction` state (e.g. "❤️", "💀", "😂", "✨", "👀", "🔥").
   - Add `processReelPerception(reelInfo: ExtractedReelInfo)` to generate context-aware remarks using `PersonalityProfile`.
   - Update scroll loop cadence to like every 8–12 reels and inspect comments every 10–15 reels.

3. **`CompanionAvatarBubble.kt`**:
   - Integrate `PopOutComicBubble` composable directly linked to the bubble with tail-pointer arrow.
   - Add animated reaction badge (floating emoji particle on like/comment).

4. **`RealDeviceController.kt`**:
   - Integrate perception trigger in `startSession()` loop so each autonomous scroll triggers reel perception -> speech bubble -> autonomous like/comment check.

---

## 5. Verification & Testing Plan

1. **Compilation Check**:
   - Run `compile_applet` to verify clean build with zero Kotlin syntax or dependency errors.
2. **Local Robolectric & Unit Tests**:
   - Run `gradle :app:testDebugUnitTest` to ensure all existing and new unit test suites pass without regression.
3. **APK Generation**:
   - Assemble debug APK and ensure `.build-outputs/app-debug.apk` and `public/app-debug.apk` are fully updated for user installation and live emulator testing.
