package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "admin_posts")
@JsonClass(generateAdapter = true)
data class AdminPost(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val category: String = "Trending Update",
    val badge: String = "NEW",
    val templateJson: String? = null,
    val bannerText: String? = null,
    val isFeatured: Boolean = true,
    val author: String = "PixCraft Admin",
    val createdAt: Long = System.currentTimeMillis()
)
