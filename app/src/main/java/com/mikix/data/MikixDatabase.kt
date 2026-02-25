package com.mikix.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Exercise::class, WorkoutTemplate::class, TemplateExercise::class, WorkoutSession::class, SessionExercise::class, SetEntry::class],
    version = 1
)
abstract class MikixDatabase : RoomDatabase() {
    abstract fun dao(): MikixDao
}
