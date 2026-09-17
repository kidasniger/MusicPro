package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [AudioTrackEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MusicProDatabase : RoomDatabase() {

    abstract fun audioTrackDao(): AudioTrackDao

    companion object {
        @Volatile
        private var INSTANCE: MusicProDatabase? = null

        fun getInstance(context: Context): MusicProDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MusicProDatabase::class.java,
                    "musicpro_database"
                ).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
