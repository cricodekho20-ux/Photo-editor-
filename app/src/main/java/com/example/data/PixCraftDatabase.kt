package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.model.AdminPost

@Database(entities = [ProjectEntity::class, AdminPost::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class PixCraftDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun adminPostDao(): AdminPostDao

    companion object {
        @Volatile
        private var INSTANCE: PixCraftDatabase? = null

        fun getDatabase(context: Context): PixCraftDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PixCraftDatabase::class.java,
                    "pixcraft_pro.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
