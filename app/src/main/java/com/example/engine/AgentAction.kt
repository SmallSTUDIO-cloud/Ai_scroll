package com.example.engine

enum class AgentAction(val displayName: String, val iconDescription: String) {
    SWIPE_UP("Swipe Up", "Next video in feed"),
    SWIPE_DOWN("Swipe Down", "Previous video"),
    WAIT("Dwell & Watch", "Watching the video with interest"),
    LIKE("Like", "Tap heart on video"),
    UNLIKE("Unlike", "Remove heart"),
    SAVE("Save Bookmark", "Save to local favorites"),
    UNSAVE("Unsave", "Remove from saved items"),
    STOP("End Break", "Agent decides its break is finished")
}
