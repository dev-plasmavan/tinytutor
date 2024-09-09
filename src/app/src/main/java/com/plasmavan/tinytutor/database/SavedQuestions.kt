package com.plasmavan.tinytutor.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savedquestions")
data class SavedQuestions(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "created_date") val createdDate: String?,
    @ColumnInfo(name = "content_title") val contentTitle: String?,
    @ColumnInfo(name = "saved_content") val savedContent: String?,
    @ColumnInfo(name = "content_field") val contentField: String?,
    @ColumnInfo(name = "content_level") val contentLevel: String?,
    @ColumnInfo(name = "content_difficulty") val contentDifficulty: String?,
    @ColumnInfo(name = "content_certification") val contentCertification: String?
)
