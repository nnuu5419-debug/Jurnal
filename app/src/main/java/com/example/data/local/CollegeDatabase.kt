package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.AttendanceRecord
import com.example.data.model.CollegeGroup
import com.example.data.model.Student
import com.example.data.model.UserAccount

@Database(
    entities = [
        UserAccount::class,
        CollegeGroup::class,
        Student::class,
        AttendanceRecord::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CollegeDatabase : RoomDatabase() {
    abstract fun collegeDao(): CollegeDao

    companion object {
        @Volatile
        private var INSTANCE: CollegeDatabase? = null

        fun getInstance(context: Context): CollegeDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CollegeDatabase::class.java,
                    "jalal_abad_college_attendance.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
