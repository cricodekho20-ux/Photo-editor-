package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.model.AdminPost
import kotlinx.coroutines.flow.Flow

@Dao
interface AdminPostDao {
    @Query("SELECT * FROM admin_posts ORDER BY createdAt DESC")
    fun getAllPosts(): Flow<List<AdminPost>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: AdminPost): Long

    @Query("DELETE FROM admin_posts WHERE id = :id")
    suspend fun deletePostById(id: Long)

    @Query("SELECT COUNT(*) FROM admin_posts")
    suspend fun getPostsCount(): Int
}
