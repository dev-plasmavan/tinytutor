package com.plasmavan.tinytutor.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [SavedQuestions::class], version = 1)
abstract class AppDataBase : RoomDatabase() {
    abstract fun SavedQuestionsDao(): SavedQuestionsDao

    companion object {
        fun buildDataBase(context: Context): AppDataBase {
            return Room.databaseBuilder(
                context,
                AppDataBase::class.java, "questions_db"
            ).build()
        }
    }
}