package com.plasmavan.tinytutor.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SavedQuestionsDao {
    @Query("SELECT * FROM savedquestions")
    fun selectAll(): LiveData<List<SavedQuestions>>

    @Query("SELECT * FROM savedquestions WHERE id IN (:loadIds)")
    fun loadAllByIds(loadIds: Int): LiveData<List<SavedQuestions>>

    @Query("SELECT * FROM savedquestions WHERE created_date LIKE :searchDate AND " +
            "saved_content LIKE :searchContent LIMIT 1")
    fun findByDate(searchDate: String, searchContent: String): SavedQuestions

    @Query("DELETE FROM savedquestions WHERE id = :objectId")
    fun deleteObject(objectId: Int)

    @Insert
    fun insertObject(vararg questions: SavedQuestions)

    @Delete
    fun deleteAll(questions: SavedQuestions)
}