package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.AudioTrack
import com.example.data.model.ClipData
import com.example.data.model.StickerOverlay
import com.example.data.model.TextOverlay
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val clipListType = Types.newParameterizedType(List::class.java, ClipData::class.java)
    private val clipAdapter = moshi.adapter<List<ClipData>>(clipListType)

    private val audioListType = Types.newParameterizedType(List::class.java, AudioTrack::class.java)
    private val audioAdapter = moshi.adapter<List<AudioTrack>>(audioListType)

    private val textListType = Types.newParameterizedType(List::class.java, TextOverlay::class.java)
    private val textAdapter = moshi.adapter<List<TextOverlay>>(textListType)

    private val stickerListType = Types.newParameterizedType(List::class.java, StickerOverlay::class.java)
    private val stickerAdapter = moshi.adapter<List<StickerOverlay>>(stickerListType)

    @TypeConverter
    fun fromClipList(list: List<ClipData>?): String {
        return clipAdapter.toJson(list ?: emptyList())
    }

    @TypeConverter
    fun toClipList(json: String?): List<ClipData> {
        if (json.isNullOrEmpty()) return emptyList()
        return try {
            clipAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromAudioList(list: List<AudioTrack>?): String {
        return audioAdapter.toJson(list ?: emptyList())
    }

    @TypeConverter
    fun toAudioList(json: String?): List<AudioTrack> {
        if (json.isNullOrEmpty()) return emptyList()
        return try {
            audioAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromTextList(list: List<TextOverlay>?): String {
        return textAdapter.toJson(list ?: emptyList())
    }

    @TypeConverter
    fun toTextList(json: String?): List<TextOverlay> {
        if (json.isNullOrEmpty()) return emptyList()
        return try {
            textAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromStickerList(list: List<StickerOverlay>?): String {
        return stickerAdapter.toJson(list ?: emptyList())
    }

    @TypeConverter
    fun toStickerList(json: String?): List<StickerOverlay> {
        if (json.isNullOrEmpty()) return emptyList()
        return try {
            stickerAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
