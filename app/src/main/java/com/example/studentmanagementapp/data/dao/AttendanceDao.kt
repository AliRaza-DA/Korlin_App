package com.example.studentmanagementapp.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.studentmanagementapp.data.entity.Attendance

@Dao
interface AttendanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attendance: Attendance): Long

    @Query("SELECT * FROM attendance WHERE courseOwnerId = :courseId")
    fun getAttendanceForCourse(courseId: Long): LiveData<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE courseOwnerId = :courseId")
    suspend fun getAttendanceListForCourse(courseId: Long): List<Attendance>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(attendance: List<Attendance>)

    @Query("DELETE FROM attendance WHERE studentOwnerId = :studentId")
    suspend fun deleteAttendanceByStudentId(studentId: Long)
}
