package com.yogesh.videoplayer.model

data class VideoResponse(
    var id: String,
    val title: String,
    val displayName: String,
    val size: String,
    val duration: String,
    val path: String,
    val dateAdded: String
)