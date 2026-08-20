package com.trevo.core.engine.palpite

import com.trevo.core.engine.crenca.Crenca
import com.trevo.core.engine.crenca.ModoDeGeracao
import com.trevo.core.engine.crenca.RevelacaoDoAmuleto

data class Palpite(
    val dezenas: List<Int>,
    val dezenasFixas: List<Int>,
    val contribuicoes: Map<Crenca, List<Int>>,
    val forca: Int,
    // RF-11.13 — `null` pros palpites gerados antes do RF-11 existir (sem
    // conceito de modo ainda) ou pelo onboarding, que não passa por um
    // seletor de modo.
    val modo: ModoDeGeracao? = null,
    // RF-11.10 — as revelações do ritual dos amuletos que forçaram dezenas
    // neste palpite (suas dezenas já estão em `dezenasFixas`); vazio fora
    // do modo Destino.
    val ritual: List<RevelacaoDoAmuleto> = emptyList(),
)
