package com.trevo.core.engine.crenca

import java.time.LocalDate

// RF-03.2 — não existe fórmula de referência em nenhum doc (no protótipo é
// um valor mockado). Puramente decorativo, mesmo espírito de tradição
// popular do resto do app: determinístico (mesmo dia + mesmo nome sempre
// dão o mesmo número), nunca usado como dado de origem de nenhuma crença de
// RF-02 nem exibido como se alterasse a probabilidade (regra inviolável 2).
fun indiceDeSorteDoDia(
    nome: String?,
    hoje: LocalDate,
): Int {
    val baseDoNome = if (nome.isNullOrBlank()) 0 else dezenasDoNome(nome).sum()
    val baseDoDia = hoje.dayOfYear * 7 + hoje.year
    return (baseDoDia + baseDoNome * 3) % 100
}
