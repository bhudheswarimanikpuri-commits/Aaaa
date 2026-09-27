package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val aspectRatio: String, // AspectRatioType.name
    val resolution: String,
    val fps: Int,
    val durationMs: Long,
    val updatedAt: Long,
    val thumbnailResName: String?,
    val thumbnailUri: String?,
    val clipCount: Int,
    val clipsJson: String,
    val audioTracksJson: String,
    val textOverlaysJson: String,
    val stickersJson: String
)
