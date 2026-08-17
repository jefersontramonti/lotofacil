package com.trevo.core.data.palpite

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "palpites")
data class PalpiteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dezenas: String,
    val dezenasFixas: String,
    val contribuicoes: String,
    val forca: Int,
    val criadoEmEpochMillis: Long,
)
