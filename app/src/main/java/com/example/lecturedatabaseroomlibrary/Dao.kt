package com.example.lecturedatabaseroomlibrary

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update


@Dao
interface LectionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(lection: T_Lection)

    @Update
    suspend fun update(lection: T_Lection)

    @Delete
    suspend fun delete(lection: T_Lection)

    @Query("SELECT * FROM T_Lection")
    fun getAllLections(): List<T_Lection>

    @Query("SELECT * FROM T_Lection WHERE lectionId = :lectionId")
    fun getLectionById(lectionId: Int): T_Lection?
}
