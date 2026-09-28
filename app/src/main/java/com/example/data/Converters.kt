package com.example.data

import androidx.room.TypeConverter
import com.example.model.BackgroundConfig
import com.example.model.CanvasLayer
import com.example.model.DrawStroke
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val backgroundAdapter = moshi.adapter(BackgroundConfig::class.java)

    private val layersListType = Types.newParameterizedType(List::class.java, CanvasLayer::class.java)
    private val layersAdapter = moshi.adapter<List<CanvasLayer>>(layersListType)

    private val strokesListType = Types.newParameterizedType(List::class.java, DrawStroke::class.java)
    private val strokesAdapter = moshi.adapter<List<DrawStroke>>(strokesListType)

    @TypeConverter
    fun fromBackgroundConfig(config: BackgroundConfig?): String {
        return if (config != null) backgroundAdapter.toJson(config) else ""
    }

    @TypeConverter
    fun toBackgroundConfig(json: String?): BackgroundConfig {
        return if (!json.isNullOrBlank()) {
            try {
                backgroundAdapter.fromJson(json) ?: BackgroundConfig()
            } catch (e: Exception) {
                BackgroundConfig()
            }
        } else {
            BackgroundConfig()
        }
    }

    @TypeConverter
    fun fromLayersList(layers: List<CanvasLayer>?): String {
        return if (layers != null) layersAdapter.toJson(layers) else "[]"
    }

    @TypeConverter
    fun toLayersList(json: String?): List<CanvasLayer> {
        return if (!json.isNullOrBlank()) {
            try {
                layersAdapter.fromJson(json) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    @TypeConverter
    fun fromStrokesList(strokes: List<DrawStroke>?): String {
        return if (strokes != null) strokesAdapter.toJson(strokes) else "[]"
    }

    @TypeConverter
    fun toStrokesList(json: String?): List<DrawStroke> {
        return if (!json.isNullOrBlank()) {
            try {
                strokesAdapter.fromJson(json) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
}
