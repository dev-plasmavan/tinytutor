package com.plasmavan.tinytutor.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppViewModel(application: Application): AndroidViewModel(application) {
    private val savedQuestionsDao: SavedQuestionsDao

    init {
        val dataBaseBuilder = AppDataBase.buildDataBase(application)
        savedQuestionsDao = dataBaseBuilder.SavedQuestionsDao()
    }

    fun insertTheQuestion(
        date: String,
        title: String,
        content: String,
        field: String,
        level: String,
        difficulty: String,
        certification: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            savedQuestionsDao.insertObject(
                SavedQuestions(
                    id = 0,
                    createdDate = date,
                    contentTitle = title,
                    savedContent = content,
                    contentField = field,
                    contentLevel = level,
                    contentDifficulty = difficulty,
                    contentCertification = certification
                )
            )
        }
    }

    fun selectAllQuestions(): LiveData<List<SavedQuestions>> {
        return savedQuestionsDao.selectAll()
    }

    fun findQuestionById(objectId: Int): LiveData<List<SavedQuestions>> {
        return savedQuestionsDao.loadAllByIds(objectId)
    }

    fun deleteOne(objectId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            savedQuestionsDao.deleteObject(objectId)
        }
    }
}