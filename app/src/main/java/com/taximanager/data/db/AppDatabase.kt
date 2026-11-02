package com.taximanager.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.taximanager.data.entity.Order
import com.taximanager.data.entity.Shift

@Database(entities = [Shift::class, Order::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shiftDao(): ShiftDao
    abstract fun orderDao(): OrderDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "taxi_manager.db"
                )
                .fallbackToDestructiveMigration() // Очистит старую БД (v2 -> v3)
                .build().also { INSTANCE = it }
            }
    }
}
