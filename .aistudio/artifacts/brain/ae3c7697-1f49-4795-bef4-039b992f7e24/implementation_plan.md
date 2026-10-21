# AI BREAK — Floating System Companion & External App Overlay Plan

> **Tagline:** "Your AI works hard. Let it scroll."

This plan specifies the implementation of the **AI BREAK System Floating Companion**: a persistent, free-floating avatar bubble that hovers above other apps (Instagram, YouTube Shorts, Reddit, TikTok, Home Screen) using Android's `WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY`. It introduces a compact floating in-place popup, human-like variable scrolling dynamics, playful procrastination quirks ("just one more reel!"), and simulated like/comment drafting.

---

## Confirmed User Preferences & Decisions

- **Confirmed Decision 1 (Compact In-Place Popup):** Tapping the floating avatar over other apps opens a compact, floating popup dialog right above the current app—never kicking the user out of Instagram or Shorts.
- **Confirmed Decision 2 (Human-Like Scrolling & Quirks):**
  - **Dynamic Human Dwell:** Instead of robotic fixed intervals, the agent pauses for variable realistic intervals (3s–12s) based on perceived interest.
  - **Procrastination Reflex:** When issued a "Stop" command, the companion playfully protests with a human-like reaction: *"Wait, just ONE more reel! It's too interesting..."* before gracefully halting.
  - **Emotional / Attracted Quips:** Entertaining internal monologue and reactions for cute/aesthetic reels, viral dances, and tech breakthroughs, with draft comment proposals.
  - **Simulated Engagement Gestures:** Double-tap center gesture for likes via `AccessibilityService`.
- **Confirmed Decision 3 (Free-Floating Placement):** The companion avatar bubble can be dragged and positioned anywhere on screen without automatic edge snapping, staying wherever the user leaves it.

---

## 1. System Architecture

```
┌────────────────────────────────────────────────────────────────────────┐
│                   ANDROID SYSTEM WINDOW MANAGER                        │
│                                                                        │
│   ┌────────────────────────────────┐  ┌─────────────────────────────┐  │
│   │ Floating Avatar Bubble View    │  │ Compact Overlay Popup View  │  │
│   │  • TYPE_APPLICATION_OVERLAY   │  │  • Sleek Cyber Card          │  │
│   │  • Free-floating drag listener │  │  • Quick Actions:            │  │
│   │  • Animated state glowing halo │  │    - Next Reel (Swipe Up)   │  │
│   │  • Pixel Creature artwork      │  │    - Like Reel (Double-Tap) │  │
│   │  • Status badge (Thinking...)  │  │    - Explain On-Screen      │  │
│   └────────────────────────────────┘  │    - Draft Comment / Copy   │  │
│                                       │    - Stop ("One more reel!")│  │
│                                       └─────────────────────────────┘  │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                    FLOATING COMPANION SERVICE                          │
│   • Manages WindowManager life cycle (add/update/remove views)         │
│   • Dispatches gestures via AiBreakAccessibilityService               │
│   • Coordinates with ConversationBrain & HeuristicRuleBrain            │
│   • Runs human-like scroll coroutine with variable jitter delays       │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Key Components & Implementation Details

### Component A: `FloatingCompanionService` (WindowManager Native Overlay)
- **Overlay Window Management:**
  - Create and attach avatar view to `WindowManager` using `TYPE_APPLICATION_OVERLAY`.
  - Handle `FLAG_NOT_FOCUSABLE` for smooth touch passthrough to background apps while dragging.
  - Touch event tracking (`ACTION_DOWN`, `ACTION_MOVE`, `ACTION_UP`) allowing 100% free-floating placement anywhere on screen.
- **Compact Overlay Popup:**
  - Tapping avatar inflates a lightweight Compose-based or custom M3 overlay window.
  - Compact size (width ~280dp, max-height ~380dp) positioned comfortably on top of external apps.
  - Provides quick action chips:
    - 🔄 **Scroll Next** (Human-like curved swipe up)
    - ❤️ **Like Reel** (Double-tap center of screen)
    - 💬 **Draft Comment** (Human-like quirky comment with 1-tap clipboard copy)
    - 🧠 **Explain Reel** (Breaks down visual tags & humor)
    - 🛑 **Stop Break** (Triggers quirky "one more reel!" procrastination dialogue)
    - ✕ **Close Popup** (Leaves avatar floating)

### Component B: Human-Like Scrolling & Procrastination Engine
- **Variable Humanized Dwell Times:**
  - 3s to 5s for uninteresting/skipped content.
  - 7s to 14s for high-interest topics (coding, cute aesthetics, space science).
  - Occasional micro-pauses mimicking human reading/listening behavior.
- **The "One More Reel!" Reflex:**
  - When the user presses Stop or timer elapses, an animated thought bubble pops up: *"Wait, just one more reel! This one looks promising..."*
  - Scrolls one final time before completing the session debrief.
- **Interactive Comment & Like Automation:**
  - Pre-generates realistic, witty, and contextual comments based on current detected tags.
  - Copies to Android clipboard so user can paste into comments with 1 tap.

### Component C: In-App & Overlay Permission Handling
- Robust `canDrawOverlays(context)` check.
- If permission missing, presents a helpful Material 3 guide sheet and opens Android's "Display over other apps" settings directly with user package targeted.
- Fallback in-app draggable bubble for instant emulator previewing.

---

## 3. Verification & Safety

1. Verify `compile_applet` succeeds without syntax or lifecycle errors.
2. Verify touch dispatch: dragging bubble across Instagram, YouTube Shorts, Reddit, or Home Screen moves freely without crashing.
3. Test double-tap like gesture and curved swipe gesture via `AiBreakAccessibilityService`.
4. Ensure zero internet permission usage (100% offline).
5. Compile debug APK and update `/public/app-debug.apk` and `.build-outputs/app-debug.apk`.
