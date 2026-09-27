package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [ProjectEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class CineProDatabase : RoomDatabase() {
    abstract fun projectDao(): CineProDao

    companion object {
        @Volatile
        private var INSTANCE: CineProDatabase? = null

        fun getInstance(context: Context): CineProDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CineProDatabase::class.java,
                    "cinepro_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
