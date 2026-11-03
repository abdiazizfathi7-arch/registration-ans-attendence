package com.example.ardayapp.data

import android.content.Context
import androidx.room.*

@Entity(tableName = "classes")
data class SchoolClass(@PrimaryKey(autoGenerate = true) val id: Long = 0, val name: String)

@Entity(tableName = "students", indices = [Index(value = ["studentCode"], unique = true)])
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentCode: String,
    val fullName: String,
    val phone: String,
    val classId: Long
)

@Entity(tableName = "attendance", indices = [Index(value = ["studentId", "date"], unique = true)])
data class Attendance(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val date: String,
    val status: String
)

@Dao
interface SchoolDao {
    @Query("SELECT * FROM classes ORDER BY name") suspend fun getClasses(): List<SchoolClass>
    @Insert suspend fun addClass(item: SchoolClass): Long
    @Query("SELECT * FROM students ORDER BY fullName") suspend fun getStudents(): List<Student>
    @Query("SELECT * FROM students WHERE classId = :classId ORDER BY fullName") suspend fun studentsInClass(classId: Long): List<Student>
    @Insert suspend fun addStudent(item: Student): Long
    @Query("SELECT * FROM attendance WHERE date = :date") suspend fun attendanceForDate(date: String): List<Attendance>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun saveAttendance(item: Attendance)
    @Query("SELECT * FROM attendance WHERE studentId = :studentId AND date = :date LIMIT 1") suspend fun attendance(studentId: Long, date: String): Attendance?
    @Query("SELECT COUNT(*) FROM attendance WHERE date = :date AND status = 'Jooga'") suspend fun presentCount(date: String): Int
    @Query("SELECT COUNT(*) FROM attendance WHERE date = :date AND status = 'Maqan'") suspend fun absentCount(date: String): Int
}

@Database(entities = [SchoolClass::class, Student::class, Attendance::class], version = 1, exportSchema = false)
abstract class SchoolDatabase : RoomDatabase() {
    abstract fun dao(): SchoolDao
    companion object {
    @Volatile private var INSTANCE: SchoolDatabase? = null
    fun get(context: Context): SchoolDatabase = INSTANCE ?: synchronized(this) {
        INSTANCE ?: Room.databaseBuilder(context.applicationContext, SchoolDatabase::class.java, "arday_database").build().also { INSTANCE = it }
    }
    }
}
