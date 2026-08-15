package com.trevo.core.engine.identidade

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.MonthDay

/**
 * RF-01.5 — "Calcular o signo a partir da data válida, respeitando o dia de
 * início de cada signo — uma data anterior ao início do signo que começa
 * naquele mês pertence ao signo anterior — e exibir o signo com suas
 * dezenas. Com data inválida, exibir marcador neutro, nunca um signo
 * padrão."
 *
 * CLAUDE.md, seção 4 ("Duas regras de domínio que já quebraram uma vez"):
 * "Signo se calcula pelo dia de início. Uma data anterior ao início do
 * signo que começa naquele mês pertence ao signo anterior. 14/07 é Câncer,
 * não Leão. As dezenas do signo alimentam o motor, então um erro aqui
 * contamina o volante inteiro."
 *
 * Tabela de referência (`.claude/handoff.md`, plano de RF-01.5, seção
 * "2.3 Tabela de início adotada" — verificada percorrendo os 366 dias de
 * um ano bissexto, não estimada):
 *
 * | Signo       | Início | Dezenas regidas       |
 * |-------------|--------|------------------------|
 * | Capricórnio | 22/12  | 4, 8, 13, 17, 22       |
 * | Aquário     | 20/01  | 2, 11, 15, 20, 24      |
 * | Peixes      | 19/02  | 3, 7, 12, 16, 25       |
 * | Áries       | 21/03  | 1, 9, 14, 18, 23       |
 * | Touro       | 21/04  | 6, 10, 15, 19, 24      |
 * | Gêmeos      | 21/05  | 5, 11, 13, 21, 25      |
 * | Câncer      | 21/06  | 2, 7, 12, 20, 22       |
 * | Leão        | 23/07  | 1, 5, 10, 19, 23       |
 * | Virgem      | 23/08  | 3, 8, 14, 17, 24       |
 * | Libra       | 23/09  | 6, 9, 13, 18, 21       |
 * | Escorpião   | 23/10  | 4, 8, 16, 20, 25       |
 * | Sagitário   | 22/11  | 2, 6, 11, 15, 22       |
 *
 * Touro em 21/04 (não 20/04) é decisão explícita do usuário, registrada no
 * plano (adendo "A0"): único ponto em que fontes divergiam.
 *
 * [Signo] (enum) e [signoDe] (`fun signoDe(nascimento: LocalDate): Signo`)
 * ainda não existem em produção: este arquivo deve falhar a compilação
 * (`:core:engine:test`) até que o `trevo-developer` os implemente em
 * `core/engine/src/main/kotlin/com/trevo/core/engine/identidade/Signo.kt`.
 *
 * `signoDe` é total (recebe `LocalDate`, nunca `String`) e não deve conter
 * nenhum caminho de fallback para um "signo padrão" — é exatamente o bug do
 * protótipo (`Docs/Trevo - Lotofácil.dc.html`, `signoDe`, `SIGNOS[3]`) que
 * este requisito proíbe explicitamente.
 */
class SignoTest {
    private data class CasoData(
        val id: String,
        val nascimento: LocalDate,
        val esperado: Signo,
        val motivo: String,
    )

    // Seção 9.3 do plano: dezenas regidas por signo, para asserção direta.
    private val dezenasEsperadasPorSigno =
        mapOf(
            Signo.CAPRICORNIO to listOf(4, 8, 13, 17, 22),
            Signo.AQUARIO to listOf(2, 11, 15, 20, 24),
            Signo.PEIXES to listOf(3, 7, 12, 16, 25),
            Signo.ARIES to listOf(1, 9, 14, 18, 23),
            Signo.TOURO to listOf(6, 10, 15, 19, 24),
            Signo.GEMEOS to listOf(5, 11, 13, 21, 25),
            Signo.CANCER to listOf(2, 7, 12, 20, 22),
            Signo.LEAO to listOf(1, 5, 10, 19, 23),
            Signo.VIRGEM to listOf(3, 8, 14, 17, 24),
            Signo.LIBRA to listOf(6, 9, 13, 18, 21),
            Signo.ESCORPIAO to listOf(4, 8, 16, 20, 25),
            Signo.SAGITARIO to listOf(2, 6, 11, 15, 22),
        )

    // Seção 2.3 do plano: dia de início de cada signo, independente de ano
    // (java.time.MonthDay).
    private val inicioEsperadoPorSigno =
        mapOf(
            Signo.CAPRICORNIO to MonthDay.of(12, 22),
            Signo.AQUARIO to MonthDay.of(1, 20),
            Signo.PEIXES to MonthDay.of(2, 19),
            Signo.ARIES to MonthDay.of(3, 21),
            Signo.TOURO to MonthDay.of(4, 21),
            Signo.GEMEOS to MonthDay.of(5, 21),
            Signo.CANCER to MonthDay.of(6, 21),
            Signo.LEAO to MonthDay.of(7, 23),
            Signo.VIRGEM to MonthDay.of(8, 23),
            Signo.LIBRA to MonthDay.of(9, 23),
            Signo.ESCORPIAO to MonthDay.of(10, 23),
            Signo.SAGITARIO to MonthDay.of(11, 22),
        )

    // --- 9.1 · Casos obrigatórios do requisito e do CLAUDE.md ---

    private val casos91 =
        listOf(
            CasoData(
                "9.1-1",
                LocalDate.of(1978, 7, 14),
                Signo.CANCER,
                "exemplo literal do CLAUDE.md §4; Leão só começa em 23/07",
            ),
            CasoData("9.1-2", LocalDate.of(2000, 7, 22), Signo.CANCER, "véspera do início de Leão"),
            CasoData("9.1-3", LocalDate.of(2000, 7, 23), Signo.LEAO, "dia de início pertence ao signo novo"),
            CasoData("9.1-4", LocalDate.of(2000, 12, 21), Signo.SAGITARIO, "véspera de Capricórnio"),
            CasoData("9.1-5", LocalDate.of(2000, 12, 22), Signo.CAPRICORNIO, "início de Capricórnio"),
            CasoData("9.1-6", LocalDate.of(2000, 12, 31), Signo.CAPRICORNIO, "virada de ano, lado de dezembro"),
            CasoData("9.1-7", LocalDate.of(2001, 1, 1), Signo.CAPRICORNIO, "virada de ano, lado de janeiro"),
            CasoData("9.1-8", LocalDate.of(2001, 1, 19), Signo.CAPRICORNIO, "último dia de Capricórnio"),
            CasoData("9.1-9", LocalDate.of(2001, 1, 20), Signo.AQUARIO, "início de Aquário"),
            CasoData("9.1-10", LocalDate.of(2024, 2, 29), Signo.PEIXES, "29 de fevereiro (ano bissexto)"),
            CasoData("9.1-11", LocalDate.of(2023, 2, 28), Signo.PEIXES, "fevereiro em ano comum"),
            CasoData(
                "9.1-12",
                LocalDate.of(2000, 4, 20),
                Signo.ARIES,
                "célula em decisão: com Touro em 20/04 viraria Touro",
            ),
            CasoData("9.1-13", LocalDate.of(2000, 4, 21), Signo.TOURO, "início de Touro conforme decisão adotada"),
        )

    @Test
    fun signoDeDevolveOSignoExatoDaTabela91ParaCadaCasoObrigatorio() {
        casos91.forEach { caso ->
            assertEquals("${caso.id}: ${caso.motivo}", caso.esperado, signoDe(caso.nascimento))
        }
    }

    @Test
    fun quatorzeDeJulhoECancerNaoLeao() {
        // Exemplo literal do CLAUDE.md §4, isolado por nome — é o caso mais
        // citado do requisito e não pode depender só de uma linha dentro de
        // uma tabela maior.
        assertEquals(Signo.CANCER, signoDe(LocalDate.of(1978, 7, 14)))
    }

    @Test
    fun quatorzeDeJulhoECancerIndependenteDoAno() {
        // O cálculo depende só de dia e mês (MonthDay), nunca do ano.
        listOf(1900, 1978, 2024, 2026).forEach { ano ->
            assertEquals(
                "ano $ano",
                Signo.CANCER,
                signoDe(LocalDate.of(ano, 7, 14)),
            )
        }
    }

    @Test
    fun viradaDeAnoEmCapricornioTransitaCorretamenteNasSeisDatasCriticas() {
        assertEquals(Signo.SAGITARIO, signoDe(LocalDate.of(2000, 12, 21)))
        assertEquals(Signo.CAPRICORNIO, signoDe(LocalDate.of(2000, 12, 22)))
        assertEquals(Signo.CAPRICORNIO, signoDe(LocalDate.of(2000, 12, 31)))
        assertEquals(Signo.CAPRICORNIO, signoDe(LocalDate.of(2001, 1, 1)))
        assertEquals(Signo.CAPRICORNIO, signoDe(LocalDate.of(2001, 1, 19)))
        assertEquals(Signo.AQUARIO, signoDe(LocalDate.of(2001, 1, 20)))
    }

    @Test
    fun vinteNoveDeFevereiroDeAnoBissextoNaoLancaEDevolvePeixes() {
        assertEquals(Signo.PEIXES, signoDe(LocalDate.of(2024, 2, 29)))
    }

    // --- 9.2 · As 12 transições — dia de início e véspera (24 asserções) ---

    private data class Transicao(
        val signo: Signo,
        val vespera: LocalDate,
        val signoAnterior: Signo,
        val diaDeInicio: LocalDate,
    )

    private val transicoes92 =
        listOf(
            Transicao(Signo.CAPRICORNIO, LocalDate.of(2000, 12, 21), Signo.SAGITARIO, LocalDate.of(2000, 12, 22)),
            Transicao(Signo.AQUARIO, LocalDate.of(2000, 1, 19), Signo.CAPRICORNIO, LocalDate.of(2000, 1, 20)),
            Transicao(Signo.PEIXES, LocalDate.of(2000, 2, 18), Signo.AQUARIO, LocalDate.of(2000, 2, 19)),
            Transicao(Signo.ARIES, LocalDate.of(2000, 3, 20), Signo.PEIXES, LocalDate.of(2000, 3, 21)),
            Transicao(Signo.TOURO, LocalDate.of(2000, 4, 20), Signo.ARIES, LocalDate.of(2000, 4, 21)),
            Transicao(Signo.GEMEOS, LocalDate.of(2000, 5, 20), Signo.TOURO, LocalDate.of(2000, 5, 21)),
            Transicao(Signo.CANCER, LocalDate.of(2000, 6, 20), Signo.GEMEOS, LocalDate.of(2000, 6, 21)),
            Transicao(Signo.LEAO, LocalDate.of(2000, 7, 22), Signo.CANCER, LocalDate.of(2000, 7, 23)),
            Transicao(Signo.VIRGEM, LocalDate.of(2000, 8, 22), Signo.LEAO, LocalDate.of(2000, 8, 23)),
            Transicao(Signo.LIBRA, LocalDate.of(2000, 9, 22), Signo.VIRGEM, LocalDate.of(2000, 9, 23)),
            Transicao(Signo.ESCORPIAO, LocalDate.of(2000, 10, 22), Signo.LIBRA, LocalDate.of(2000, 10, 23)),
            Transicao(Signo.SAGITARIO, LocalDate.of(2000, 11, 21), Signo.ESCORPIAO, LocalDate.of(2000, 11, 22)),
        )

    @Test
    fun asDozeTransicoesTemVesperaNoSignoAnteriorEDiaDeInicioNoSignoNovo() {
        // 12 signos x 2 asserções = 24 asserções, exatamente a tabela 9.2 do
        // plano.
        transicoes92.forEach { transicao ->
            assertEquals(
                "véspera de ${transicao.signo}: ${transicao.vespera} deveria ser ${transicao.signoAnterior}",
                transicao.signoAnterior,
                signoDe(transicao.vespera),
            )
            assertEquals(
                "início de ${transicao.signo}: ${transicao.diaDeInicio} deveria já ser ${transicao.signo}",
                transicao.signo,
                signoDe(transicao.diaDeInicio),
            )
        }
    }

    // --- 9.3 · Dezenas por signo + invariantes ---

    @Test
    fun cadaSignoTemExatamenteAsCincoDezenasDaTabela93() {
        dezenasEsperadasPorSigno.forEach { (signo, dezenas) ->
            assertEquals("dezenas de $signo", dezenas, signo.dezenas)
        }
    }

    @Test
    fun signoTemExatamenteDozeConstantes() {
        assertEquals(12, Signo.entries.size)
    }

    @Test
    fun cadaSignoTemCincoDezenasDistintasEntreUmEVinteECinco() {
        Signo.entries.forEach { signo ->
            assertEquals("$signo deveria ter 5 dezenas", 5, signo.dezenas.size)
            assertEquals("$signo não pode ter dezena repetida", signo.dezenas.size, signo.dezenas.toSet().size)
            signo.dezenas.forEach { dezena ->
                assertTrue("$signo tem dezena fora do intervalo 1..25: $dezena", dezena in 1..25)
            }
        }
    }

    @Test
    fun asSessentaPosicoesDeDezenasCobremAsVinteECincoDezenasSemFaltarNenhuma() {
        val todasAsDezenas = Signo.entries.flatMap { it.dezenas }
        assertEquals(60, todasAsDezenas.size)
        assertEquals((1..25).toSet(), todasAsDezenas.toSet())
    }

    @Test
    fun cadaSignoExpoeOInicioDeclaradoNaTabela23() {
        inicioEsperadoPorSigno.forEach { (signo, inicio) ->
            assertEquals("início de $signo", inicio, signo.inicio)
        }
    }

    // --- Varredura completa: pega qualquer off-by-one que as 24 fronteiras
    // isoladas de 9.2 deixem passar. ---

    @Test
    fun osTrezentosESessentaESeisDiasDeUmAnoBissextoFormamBlocosContiguosComecandoNoInicioDeclarado() {
        val ano = 2024
        val primeiroDia = LocalDate.of(ano, 1, 1)
        val dias = generateSequence(primeiroDia) { it.plusDays(1) }.takeWhile { it.year == ano }.toList()
        assertEquals("2024 é bissexto", 366, dias.size)

        val signos = dias.map { signoDe(it) }

        // Nenhum caminho de fallback: os 12 signos aparecem, nenhum a mais.
        assertEquals(Signo.entries.toSet(), signos.toSet())

        // 01/01 é continuação do bloco de Capricórnio que começou em 22/12
        // do ano anterior — não é uma fronteira nova.
        assertEquals(Signo.CAPRICORNIO, signos.first())

        // Cada troca de signo ao longo da sequência ocorre exatamente no dia
        // de início declarado do novo signo (tabela 2.3 do plano). É o
        // teste que pega qualquer off-by-one que as 24 fronteiras isoladas
        // de 9.2 deixem passar, porque varre TODOS os 366 dias, não só as
        // bordas escolhidas a dedo.
        for (i in 1 until dias.size) {
            if (signos[i] != signos[i - 1]) {
                assertEquals(
                    "troca em ${dias[i]}: deveria ser exatamente o início de ${signos[i]}",
                    signos[i].inicio,
                    MonthDay.from(dias[i]),
                )
            }
        }

        // Bloco contíguo: cada signo ocupa um único trecho contínuo de
        // índices, exceto Capricórnio — que, por atravessar a virada do
        // ano, aparece em duas pontas dentro de um único ano civil
        // (01/01–19/01, continuação do ano anterior, e 22/12–31/12, início
        // do próximo bloco).
        val blocosPorSigno = mutableMapOf<Signo, MutableList<IntRange>>()
        var inicioBloco = 0
        for (i in 1..dias.size) {
            if (i == dias.size || signos[i] != signos[i - 1]) {
                val signoDoBloco = signos[inicioBloco]
                blocosPorSigno.getOrPut(signoDoBloco) { mutableListOf() }.add(inicioBloco until i)
                inicioBloco = i
            }
        }
        Signo.entries.forEach { signo ->
            val blocos = blocosPorSigno[signo].orEmpty()
            val esperado = if (signo == Signo.CAPRICORNIO) 2 else 1
            assertEquals(
                "$signo deveria formar ${if (esperado == 1) "um único bloco contíguo" else "2 trechos (virada do ano)"}",
                esperado,
                blocos.size,
            )
        }

        // Soma de todos os blocos cobre os 366 dias sem sobreposição nem
        // buraco.
        assertEquals(366, blocosPorSigno.values.flatten().sumOf { it.count() })
    }

    @Test
    fun signoDeNuncaLancaExcecaoParaQualquerDataValida() {
        // signoDe é total (critério de aceite 2): nenhuma data real deveria
        // fazer a função lançar.
        val datasDeBorda =
            listOf(
                LocalDate.of(1900, 1, 1),
                LocalDate.of(2000, 2, 29),
                LocalDate.of(2024, 2, 29),
                LocalDate.of(2026, 8, 13),
                LocalDate.of(2100, 12, 31),
            )
        datasDeBorda.forEach { data ->
            signoDe(data)
        }
    }
}
