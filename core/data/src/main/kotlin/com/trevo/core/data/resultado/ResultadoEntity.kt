package com.trevo.core.data.resultado

import androidx.room.Entity
import androidx.room.PrimaryKey

// `numero` é nulo pra resultado inserido manualmente (RF-05.10) — o app
// nunca calcula número de concurso offline (CLAUDE.md §8), então um
// resultado manual não tem um. `id` autoincrementa e decide "o mais
// recente" — evita depender de `numero` como chave estável.
@Entity(tableName = "resultados")
data class ResultadoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val numero: Int?,
    val dataApuracaoIso: String,
    val dezenasSorteadas: String,
    val faixas: String,
    val acumulado: Boolean,
    val origem: String,
)
