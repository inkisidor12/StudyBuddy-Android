package com.example.hito4.data.dao


import androidx.room.*
import com.example.hito4.data.entity.SubjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects WHERE uid = :uid ORDER BY name")
    fun observeAll(uid: String): Flow<List<SubjectEntity>>

    @Insert
    suspend fun insert(subject: SubjectEntity)

    @Delete
    suspend fun delete(subject: SubjectEntity)
}