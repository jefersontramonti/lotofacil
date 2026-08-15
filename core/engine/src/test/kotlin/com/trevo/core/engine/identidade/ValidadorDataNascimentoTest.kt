package com.trevo.core.engine.identidade

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * RF-01.3 — "Validar a data no formato dd/mm/aaaa: mês entre 1 e 12, dia
 * existente no mês (considerando ano bissexto), ano entre 1900 e a data
 * atual."
 *
 * Primeiro arquivo-fonte de `:core:engine` (módulo Kotlin puro já
 * declarado em `settings.gradle.kts` e configurado em
 * `core/engine/build.gradle.kts`, mas sem nenhum `.kt` até agora — ver
 * seção 0 do plano de RF-01.3 em `.claude/handoff.md`).
 *
 * [ValidadorDataNascimento], [ResultadoDataNascimento] e
 * [ErroDataNascimento] ainda não existem em produção: este arquivo deve
 * falhar a compilação (`:core:engine:test`) até que o `trevo-developer`
 * os implemente em
 * `core/engine/src/main/kotlin/com/trevo/core/engine/identidade/`.
 *
 * A "data atual" nunca vem de [java.time.LocalDate.now] sem argumento:
 * todo caso aqui injeta um [Clock] fixo, nunca depende do relógio real
 * de execução (CLAUDE.md §7 / RNF-06.2 — o mesmo princípio do
 * `Random(seed)` injetado no motor de geração).
 */
class ValidadorDataNascimentoTest {
    // Instant.parse("2026-08-13T12:00:00Z") em America/Sao_Paulo (UTC-3,
    // sem horário de verão desde 2019) cai em 2026-08-13 — hoje fixo para
    // todo o arquivo.
    private val relogioFixo: Clock =
        Clock.fixed(Instant.parse("2026-08-13T12:00:00Z"), ZoneId.of("America/Sao_Paulo"))

    private val validador = ValidadorDataNascimento(relogioFixo)

    @Test
    fun campoVazioOuSoComEspacosDevolveErroVazio() {
        assertEquals(
            ResultadoDataNascimento.Invalida(ErroDataNascimento.VAZIO),
            validador.validar(""),
        )
        assertEquals(
            ResultadoDataNascimento.Invalida(ErroDataNascimento.VAZIO),
            validador.validar("   "),
        )
    }

    @Test
    fun textoForaDoFormatoDdMmAaaaEstritoDevolveErroDeFormato() {
        val casos =
            listOf(
                "13081986" to "sem barras — o validador é puro; a máscara de RF-01.9 é camada anterior",
                "1/7/1978" to "formato estrito, 1 dígito não serve",
                "14/07/78" to "ano de 2 dígitos",
                "14-07-1978" to "separador errado",
                "aa/bb/cccc" to "não numérico",
            )

        casos.forEach { (entrada, motivo) ->
            assertEquals(
                motivo,
                ResultadoDataNascimento.Invalida(ErroDataNascimento.FORMATO_INVALIDO),
                validador.validar(entrada),
            )
        }
    }

    @Test
    fun dataValidaNoFormatoEstritoDevolveValidaComLocalDateCorrespondente() {
        assertEquals(
            ResultadoDataNascimento.Valida(LocalDate.of(1978, 7, 14)),
            validador.validar("14/07/1978"),
        )
    }

    @Test
    fun dataValidaComEspacosNasBordasEAceitaAposTrim() {
        assertEquals(
            ResultadoDataNascimento.Valida(LocalDate.of(1978, 7, 14)),
            validador.validar(" 14/07/1978 "),
        )
    }

    @Test
    fun mesForaDeUmATrezeDevolveErroDeMesAntesDeCheckarODia() {
        val casos =
            listOf(
                "14/00/1978" to "mês 00",
                "14/13/1978" to "mês 13",
                // "32" também é dia inválido, mas a ordem de avaliação
                // (mês antes do dia) exige MES_INVALIDO aqui, não
                // DIA_INEXISTENTE.
                "32/13/1978" to "ordem: mês antes do dia",
            )

        casos.forEach { (entrada, motivo) ->
            assertEquals(
                motivo,
                ResultadoDataNascimento.Invalida(ErroDataNascimento.MES_INVALIDO),
                validador.validar(entrada),
            )
        }
    }

    @Test
    fun diaInexistenteNoMesDevolveErroDeDia() {
        val casos =
            listOf(
                "00/07/1978" to "dia 00",
                "32/07/1978" to "dia 32",
                "31/04/1978" to "abril tem 30 dias",
                "29/02/2023" to "2023 não é bissexto",
                "29/02/1900" to "divisível por 100 e não por 400 — caso central",
                // "31" também está fora do intervalo permitido de ano
                // (< 1900), mas a ordem de avaliação (dia antes do
                // intervalo) exige DIA_INEXISTENTE aqui.
                "31/04/1899" to "ordem: dia antes do intervalo",
            )

        casos.forEach { (entrada, motivo) ->
            assertEquals(
                motivo,
                ResultadoDataNascimento.Invalida(ErroDataNascimento.DIA_INEXISTENTE),
                validador.validar(entrada),
            )
        }
    }

    @Test
    fun diaNosLimitesDoMesInclusiveFevereiroBissextoDevolveValida() {
        val casos =
            listOf(
                "30/04/1978" to LocalDate.of(1978, 4, 30),
                "29/02/2024" to LocalDate.of(2024, 2, 29),
                "28/02/2023" to LocalDate.of(2023, 2, 28),
                "29/02/2000" to LocalDate.of(2000, 2, 29),
            )

        casos.forEach { (entrada, esperado) ->
            assertEquals(entrada, ResultadoDataNascimento.Valida(esperado), validador.validar(entrada))
        }
    }

    @Test
    fun anoAbaixoDe1900OuDataFuturaDevolveErroDeIntervalo() {
        val casos =
            listOf(
                "31/12/1899" to "abaixo de 1900",
                "14/08/2026" to "amanhã",
                "01/01/2027" to "ano futuro",
                "31/12/9999" to "futuro distante",
            )

        casos.forEach { (entrada, motivo) ->
            assertEquals(
                motivo,
                ResultadoDataNascimento.Invalida(ErroDataNascimento.FORA_DO_INTERVALO),
                validador.validar(entrada),
            )
        }
    }

    @Test
    fun limitesInclusivosDeAnoEDeHojeDevolvemValida() {
        assertEquals(
            "01/01/1900 é o limite inferior inclusivo",
            ResultadoDataNascimento.Valida(LocalDate.of(1900, 1, 1)),
            validador.validar("01/01/1900"),
        )
        assertEquals(
            "hoje (13/08/2026, conforme o relógio fixo) é válido — limite superior inclusivo",
            ResultadoDataNascimento.Valida(LocalDate.of(2026, 8, 13)),
            validador.validar("13/08/2026"),
        )
    }

    @Test
    fun dataDeMenorDeIdadeEValidaAquiPoisRF014EhRequisitoSeparado() {
        // "01/01/2020" faz a pessoa ter menos de 18 anos em 2026, mas
        // RF-01.3 valida só formato/mês/dia/intervalo. O bloqueio por
        // idade é RF-01.4, ainda não implementado — não pode vazar para
        // este validador.
        assertEquals(
            ResultadoDataNascimento.Valida(LocalDate.of(2020, 1, 1)),
            validador.validar("01/01/2020"),
        )
    }

    @Test
    fun ehAnoBissextoSeguemARegraGregorianaCompleta() {
        assertEquals("1900 é divisível por 100 e não por 400", false, ehAnoBissexto(1900))
        assertEquals("2000 é divisível por 400", true, ehAnoBissexto(2000))
        assertEquals("2023 não é divisível por 4", false, ehAnoBissexto(2023))
        assertEquals("2024 é divisível por 4 e não por 100", true, ehAnoBissexto(2024))
        assertEquals("2100 é divisível por 100 e não por 400", false, ehAnoBissexto(2100))
    }

    @Test
    fun validarNuncaDevolveErroDeMenorDeIdadeParaNenhumaEntrada() {
        // RF-01.4 (caso E15, invariante): a checagem de 18 anos completos é
        // um segundo julgamento, feito por VerificadorDeIdade fora deste
        // validador (ver ".claude/handoff.md", seção "Plano de
        // implementação — RF-01.4", decisão D3). ValidadorDataNascimento só
        // julga formato/mês/dia/intervalo — MENOR_DE_IDADE nunca pode sair
        // daqui, mesmo para datas de menores de idade, sob pena de quebrar
        // dataDeMenorDeIdadeEValidaAquiPoisRF014EhRequisitoSeparado.
        val entradas =
            listOf(
                "",
                "   ",
                "13081986",
                "1/7/1978",
                "14/07/78",
                "14-07-1978",
                "aa/bb/cccc",
                "14/07/1978",
                " 14/07/1978 ",
                "14/00/1978",
                "14/13/1978",
                "32/13/1978",
                "00/07/1978",
                "32/07/1978",
                "31/04/1978",
                "29/02/2023",
                "29/02/1900",
                "31/04/1899",
                "30/04/1978",
                "29/02/2024",
                "28/02/2023",
                "29/02/2000",
                "31/12/1899",
                "14/08/2026",
                "01/01/2027",
                "31/12/9999",
                "01/01/1900",
                "13/08/2026",
                "01/01/2020",
                "14/07/2020",
            )

        entradas.forEach { entrada ->
            val resultado = validador.validar(entrada)
            val ehErroDeMenorDeIdade =
                resultado is ResultadoDataNascimento.Invalida &&
                    resultado.erro == ErroDataNascimento.MENOR_DE_IDADE

            assertEquals(
                "validar(\"$entrada\") não pode devolver MENOR_DE_IDADE — foi: $resultado",
                false,
                ehErroDeMenorDeIdade,
            )
        }
    }

    @Test
    fun relogioInjetadoMudaOVeredictoDaMesmaDataEmDiasDiferentes() {
        // Prova de que "hoje" vem do Clock injetado, não de uma data
        // fixa no código: a mesma entrada ("01/06/2026") é FORA_DO_INTERVALO
        // sob um relógio anterior a ela e Valida sob um relógio posterior.
        val relogioAntes =
            Clock.fixed(Instant.parse("2026-01-01T12:00:00Z"), ZoneId.of("America/Sao_Paulo"))
        val relogioDepois =
            Clock.fixed(Instant.parse("2026-12-01T12:00:00Z"), ZoneId.of("America/Sao_Paulo"))

        val resultadoAntes = ValidadorDataNascimento(relogioAntes).validar("01/06/2026")
        val resultadoDepois = ValidadorDataNascimento(relogioDepois).validar("01/06/2026")

        assertEquals(
            ResultadoDataNascimento.Invalida(ErroDataNascimento.FORA_DO_INTERVALO),
            resultadoAntes,
        )
        assertEquals(
            ResultadoDataNascimento.Valida(LocalDate.of(2026, 6, 1)),
            resultadoDepois,
        )
    }
}
