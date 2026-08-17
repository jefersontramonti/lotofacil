package com.trevo.core.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.trevo.core.data.palpite.PalpiteDao
import com.trevo.core.data.palpite.PalpiteEntity

@Database(entities = [PalpiteEntity::class], version = 1)
abstract class TrevoDatabase : RoomDatabase() {
    abstract fun palpiteDao(): PalpiteDao
}
