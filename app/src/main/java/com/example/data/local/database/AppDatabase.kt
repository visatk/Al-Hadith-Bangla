package com.example.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.HadithDao
import com.example.data.local.entity.AyahEntity
import com.example.data.local.entity.BookEntity
import com.example.data.local.entity.BookmarkCollectionEntity
import com.example.data.local.entity.BookmarkEntity
import com.example.data.local.entity.ChapterEntity
import com.example.data.local.entity.HadithEntity
import com.example.data.local.entity.ReadingProgressEntity
import com.example.data.local.entity.SectionEntity
import com.example.data.local.entity.SurahEntity

@Database(
    entities = [
        BookEntity::class,
        ChapterEntity::class,
        SectionEntity::class,
        HadithEntity::class,
        SurahEntity::class,
        AyahEntity::class,
        BookmarkCollectionEntity::class,
        BookmarkEntity::class,
        ReadingProgressEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun hadithDao(): HadithDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "al_hadith_room_engine.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
