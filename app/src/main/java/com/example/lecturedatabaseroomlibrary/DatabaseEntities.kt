package com.example.lecturedatabaseroomlibrary

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.text.DateFormat
import java.util.Date

@Entity(tableName = "T_Lection")
data class T_Lection ( // таблица лекций
    @PrimaryKey(autoGenerate = true)
    var lectionId: Int? = null,
    @ColumnInfo("F_Title")
    var title: String,
    @ColumnInfo("F_Description")
    var description: String)
