package com.example.lecturedatabaseroomlibrary

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [T_Lection::class], version = 2) // создание бд
abstract class MainDb : RoomDatabase() {
    abstract fun getLectionDao(): LectionDao

    companion object{
        fun getDb(context: Context): MainDb{
            return Room.databaseBuilder(
                context.applicationContext,
                MainDb::class.java,
                "AppDatabase.db"
            ).build()
        }
    }
}