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
    // RF-11.13/RF-11.10 — `null`/"" pros palpites salvos antes do RF-11
    // (MIGRATION_2_3 preenche com esse mesmo padrão neutro nas linhas já existentes).
    val modo: String? = null,
    val ritual: String = "",
)
