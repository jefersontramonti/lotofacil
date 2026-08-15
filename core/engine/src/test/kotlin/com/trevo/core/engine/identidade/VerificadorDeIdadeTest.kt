package com.trevo.core.engine.identidade

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * RF-01.4 — "Bloquear o cadastro de menores de 18 anos, com mensagem
 * explícita." Implementa a regra inviolável 5 do `CLAUDE.md` ("Cadastro
 * exige 18 anos completos").
 *
 * Cobre a tabela E1–E14 do plano em `.claude/handoff.md` (seção "Plano de
 * implementação — RF-01.4", "6. Tabela para o test-engineer"), com valores
 * conferidos executando `java.time` de verdade, não estimados.
 *
 * [idadeEmAnosCompletos], [VerificadorDeIdade] e
 * [IDADE_MINIMA_PARA_CADASTRO] ainda não existem em produção: este arquivo
 * deve falhar a compilação (`:core:engine:test`) até que o
 * `trevo-developer` os implemente em
 * `core/engine/src/main/kotlin/com/trevo/core/engine/identidade/VerificadorDeIdade.kt`.
 *
 * `idadeEmAnosCompletos(nascimento, hoje)` é pura — recebe `hoje` como
 * `LocalDate`, sem `Clock` — para que os casos de calendário afirmem o
 * valor exato da idade (17 vs 18), não só o booleano.
 * `VerificadorDeIdade(clock).ehMaiorDeIdade(nascimento)` é o ponto de
 * injeção do `Clock`, espelhando `ValidadorDataNascimento`.
 */
class VerificadorDeIdadeTest {
    /**
     * `Instant.parse("$data" + "T12:00:00Z")` em America/Sao_Paulo (UTC-3,
     * sem horário de verão desde 2019) cai sempre no mesmo dia calendário
     * de `data` — mesmo padrão de `ValidadorDataNascimentoTest`.
     */
    private fun relogioEm(data: LocalDate): Clock =
        Clock.fixed(Instant.parse("${data}T12:00:00Z"), ZoneId.of("America/Sao_Paulo"))

    private data class Caso(
        val id: String,
        val nascimento: LocalDate,
        val hoje: LocalDate,
        val idadeEsperada: Int,
        val maiorEsperado: Boolean,
        val motivo: String,
    )

    private val casos =
        listOf(
            Caso(
                "E1",
                LocalDate.of(2008, 8, 13),
                LocalDate.of(2026, 8, 13),
                18,
                true,
                "completa 18 exatamente hoje — >=, não >",
            ),
            Caso(
                "E2",
                LocalDate.of(2008, 8, 14),
                LocalDate.of(2026, 8, 13),
                17,
                false,
                "completa amanhã (17a 11m 30d = 17 anos e 364 dias)",
            ),
            Caso(
                "E3",
                LocalDate.of(2008, 8, 12),
                LocalDate.of(2026, 8, 13),
                18,
                true,
                "18 anos e 1 dia",
            ),
            Caso(
                "E4",
                LocalDate.of(2009, 8, 13),
                LocalDate.of(2026, 8, 13),
                17,
                false,
                "17 anos cravados",
            ),
            Caso(
                "E5",
                LocalDate.of(2008, 2, 29),
                LocalDate.of(2026, 2, 27),
                17,
                false,
                "bissexto, dois dias antes",
            ),
            Caso(
                "E6",
                LocalDate.of(2008, 2, 29),
                LocalDate.of(2026, 2, 28),
                17,
                false,
                "decisão de calendário: 28/02 ainda é menor",
            ),
            Caso(
                "E7",
                LocalDate.of(2008, 2, 29),
                LocalDate.of(2026, 3, 1),
                18,
                true,
                "completa 18 em 01/03 (18a 0m 1d)",
            ),
            Caso(
                "E8",
                LocalDate.of(2008, 2, 29),
                LocalDate.of(2026, 8, 13),
                18,
                true,
                "mesmo nascido de E6/E7, bem depois",
            ),
            Caso(
                "E9",
                LocalDate.of(2008, 2, 29),
                LocalDate.of(2028, 2, 29),
                20,
                true,
                "aniversário em ano bissexto, sem clamping",
            ),
            Caso(
                "E10",
                LocalDate.of(1900, 1, 1),
                LocalDate.of(2026, 8, 13),
                126,
                true,
                "limite inferior de RF-01.3",
            ),
            Caso(
                "E11",
                LocalDate.of(1900, 1, 1),
                LocalDate.of(1918, 1, 1),
                18,
                true,
                "prova o >= também no limite inferior",
            ),
            Caso(
                "E12",
                LocalDate.of(2026, 8, 13),
                LocalDate.of(2026, 8, 13),
                0,
                false,
                "nasceu hoje: válido em RF-01.3, menor aqui",
            ),
            Caso(
                "E13",
                LocalDate.of(2026, 8, 14),
                LocalDate.of(2026, 8, 13),
                0,
                false,
                "futuro de 1 dia: falha fechada (0a 0m -1d), não lança",
            ),
            Caso(
                "E14",
                LocalDate.of(2027, 1, 1),
                LocalDate.of(2026, 8, 13),
                0,
                false,
                "futuro distante: 0a -4m -19d, não lança",
            ),
        )

    @Test
    fun idadeEmAnosCompletosDevolveOValorExatoDaTabelaE1AE14() {
        casos.forEach { caso ->
            assertEquals(
                "${caso.id}: ${caso.motivo}",
                caso.idadeEsperada,
                idadeEmAnosCompletos(caso.nascimento, caso.hoje),
            )
        }
    }

    @Test
    fun ehMaiorDeIdadeDevolveOVeredictoExatoDaTabelaE1AE14() {
        casos.forEach { caso ->
            val verificador = VerificadorDeIdade(relogioEm(caso.hoje))
            val resultado = verificador.ehMaiorDeIdade(caso.nascimento)

            assertEquals("${caso.id}: ${caso.motivo}", caso.maiorEsperado, resultado)
        }
    }

    @Test
    fun quemCompletaDezoitoAnosExatamenteHojeEMaiorDeIdade() {
        // E1, isolado por nome — regra inviolável 5: >=, não >.
        val verificador = VerificadorDeIdade(relogioEm(LocalDate.of(2026, 8, 13)))

        assertTrue(verificador.ehMaiorDeIdade(LocalDate.of(2008, 8, 13)))
    }

    @Test
    fun quemCompletaDezoitoAnosAmanhaAindaEMenorDeIdade() {
        // E2, isolado por nome.
        val verificador = VerificadorDeIdade(relogioEm(LocalDate.of(2026, 8, 13)))

        assertFalse(verificador.ehMaiorDeIdade(LocalDate.of(2008, 8, 14)))
    }

    @Test
    fun nascidoEm29DeFevereiroEMenorDeIdadeEm28DeFevereiroDoAnoNaoBissextoDoDecimoOitavoAniversario() {
        // E6 — decisão de calendário registrada no plano: o "aniversário"
        // de 18 anos de quem nasceu em 29/02 só chega em 01/03, nunca em
        // 28/02, mesmo num ano não bissexto.
        val verificador = VerificadorDeIdade(relogioEm(LocalDate.of(2026, 2, 28)))

        assertEquals(17, idadeEmAnosCompletos(LocalDate.of(2008, 2, 29), LocalDate.of(2026, 2, 28)))
        assertFalse(verificador.ehMaiorDeIdade(LocalDate.of(2008, 2, 29)))
    }

    @Test
    fun nascidoEm29DeFevereiroEMaiorDeIdadeEm1DeMarcoDoAnoNaoBissextoDoDecimoOitavoAniversario() {
        // E7 — descontinuidade: não existe um dia com "18a 0m 0d" para essa
        // pessoa; o período salta de 17a 11m 30d (28/02) para 18a 0m 1d
        // (01/03).
        val verificador = VerificadorDeIdade(relogioEm(LocalDate.of(2026, 3, 1)))

        assertEquals(18, idadeEmAnosCompletos(LocalDate.of(2008, 2, 29), LocalDate.of(2026, 3, 1)))
        assertTrue(verificador.ehMaiorDeIdade(LocalDate.of(2008, 2, 29)))
    }

    @Test
    fun dataDeNascimentoNoFuturoNuncaLancaEDevolveMenorDeIdadePorFalhaFechada() {
        // E13/E14 — Period.getYears() devolve 0 para data futura (só mês/dia
        // ficam negativos); a asserção correta é sobre ehMaiorDeIdade ==
        // false, nunca sobre "idade negativa".
        val hoje = LocalDate.of(2026, 8, 13)
        val verificadorHoje = VerificadorDeIdade(relogioEm(hoje))

        assertEquals(0, idadeEmAnosCompletos(LocalDate.of(2026, 8, 14), hoje))
        assertFalse(verificadorHoje.ehMaiorDeIdade(LocalDate.of(2026, 8, 14)))

        assertEquals(0, idadeEmAnosCompletos(LocalDate.of(2027, 1, 1), hoje))
        assertFalse(verificadorHoje.ehMaiorDeIdade(LocalDate.of(2027, 1, 1)))
    }

    @Test
    fun limiteInferiorDeRf013NaoQuebraOCalculoDeIdade() {
        // E10/E11 — 01/01/1900 não deve estourar nem se comportar de forma
        // diferente de qualquer outra data.
        assertTrue(VerificadorDeIdade(relogioEm(LocalDate.of(2026, 8, 13))).ehMaiorDeIdade(LocalDate.of(1900, 1, 1)))
        assertTrue(VerificadorDeIdade(relogioEm(LocalDate.of(1918, 1, 1))).ehMaiorDeIdade(LocalDate.of(1900, 1, 1)))
    }

    @Test
    fun idadeMinimaParaCadastroEDezoito() {
        assertEquals(18, IDADE_MINIMA_PARA_CADASTRO)
    }

    @Test
    fun relogioInjetadoMudaOVeredictoDaMesmaDataDeNascimentoEmDiasDiferentes() {
        // Espelha `relogioInjetadoMudaOVeredictoDaMesmaDataEmDiasDiferentes`
        // de ValidadorDataNascimentoTest: nascimento 2008-08-14 é 17/false
        // sob o relógio de 2026-08-13 (E2) e 18/true sob o relógio de
        // 2026-08-14 — mesma entrada, veredicto oposto conforme o relógio.
        val nascimento = LocalDate.of(2008, 8, 14)

        val resultadoAntes = VerificadorDeIdade(relogioEm(LocalDate.of(2026, 8, 13))).ehMaiorDeIdade(nascimento)
        val resultadoDepois = VerificadorDeIdade(relogioEm(LocalDate.of(2026, 8, 14))).ehMaiorDeIdade(nascimento)

        assertFalse(resultadoAntes)
        assertTrue(resultadoDepois)
    }
}
