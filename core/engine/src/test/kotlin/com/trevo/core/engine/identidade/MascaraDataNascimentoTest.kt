package com.trevo.core.engine.identidade

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * RF-01.9 — "Formatar automaticamente a data de nascimento enquanto o
 * usuário digita apenas números, inserindo as barras dd/mm/aaaa (ex.:
 * "12081986" exibido como "12/08/1986"), sem alterar as regras de
 * validação de RF-01.3."
 *
 * Cobre as duas funções puras de `:core:engine` que implementam a
 * máscara (plano do `trevo-architect` em `.claude/handoff.md`, seção
 * "Plano — RF-01.9", itens 1 e 7):
 *
 * - [formatarDataNascimento]: regra pura da máscara, sem cursor — mantém
 *   só dígitos, descarta do 9º em diante, insere `/` depois do 2º e do
 *   4º dígito só quando existe um dígito seguinte (nunca barra à
 *   direita). Idempotente e determinística.
 * - [aplicarMascaraDataNascimento]: versão com cursor usada pelo campo de
 *   texto — trata digitação, backspace (fim, meio e sobre a barra) e
 *   colagem de texto (formatado, cru, com separador diferente ou não
 *   numérico).
 *
 * Nenhuma das duas envolve `Random`/`Clock`/Android: são funções puras,
 * a saída é sempre exata para uma entrada dada — nunca comportamento
 * aproximado (CLAUDE.md §7).
 *
 * [EdicaoDataNascimento], [formatarDataNascimento] e
 * [aplicarMascaraDataNascimento] ainda não existem em produção: este
 * arquivo deve falhar a compilação (`:core:engine:test`) até que o
 * `trevo-developer` os implemente em
 * `core/engine/src/main/kotlin/com/trevo/core/engine/identidade/MascaraDataNascimento.kt`.
 */
class MascaraDataNascimentoTest {
    // ---------------------------------------------------------------
    // formatarDataNascimento(texto: String): String
    // ---------------------------------------------------------------

    @Test
    fun formatarDataNascimentoSeguindoATabelaDeZeroAOitoDigitos() {
        // Prefixos de "12081986" (o próprio exemplo do requisito), um
        // dígito a mais por linha — tabela completa da seção 1 do plano.
        val casos =
            listOf(
                "" to "",
                "1" to "1",
                "12" to "12",
                "120" to "12/0",
                "1208" to "12/08",
                "12081" to "12/08/1",
                "120819" to "12/08/19",
                "1208198" to "12/08/198",
                "12081986" to "12/08/1986",
            )

        casos.forEach { (entrada, esperado) ->
            assertEquals(
                "${entrada.length} dígito(s)",
                esperado,
                formatarDataNascimento(entrada),
            )
        }
    }

    @Test
    fun formatarDataNascimentoEIdempotente() {
        val entradas =
            listOf(
                "",
                "1",
                "12",
                "12/0",
                "12/08/1986",
                "12/08/198",
                "abc",
                "12-08-1986",
                " 12/08/1986 ",
                "120819867",
            )

        entradas.forEach { entrada ->
            val umaVez = formatarDataNascimento(entrada)
            val duasVezes = formatarDataNascimento(umaVez)
            assertEquals(
                "formatar duas vezes precisa devolver o mesmo resultado de formatar uma vez: \"$entrada\"",
                umaVez,
                duasVezes,
            )
        }
    }

    @Test
    fun formatarDataNascimentoDescartaCaracteresNaoNumericosEmSilencio() {
        val casos =
            listOf(
                "abc" to "",
                "12a08b1986" to "12/08/1986",
                "12-08-1986" to "12/08/1986",
                "12 08 1986" to "12/08/1986",
                " 12/08/1986 " to "12/08/1986",
                "12/08/1986" to "12/08/1986",
            )

        casos.forEach { (entrada, esperado) ->
            assertEquals(
                "entrada \"$entrada\"",
                esperado,
                formatarDataNascimento(entrada),
            )
        }
    }

    @Test
    fun formatarDataNascimentoDescartaDoNonoDigitoEmDiante() {
        assertEquals(
            "9 dígitos: o 9º (\"7\") é descartado, resultado igual ao de 8 dígitos",
            "12/08/1986",
            formatarDataNascimento("120819867"),
        )
        assertEquals(
            "10 dígitos: mantém só os 8 primeiros (\"12345678\")",
            "12/34/5678",
            formatarDataNascimento("1234567890"),
        )
    }

    @Test
    fun formatarDataNascimentoNuncaProduzBarraADireita() {
        val casosSemBarraFinal =
            listOf(
                "1" to "1",
                "12" to "12",
                "123" to "12/3",
                "1234" to "12/34",
                "123456" to "12/34/56",
            )

        casosSemBarraFinal.forEach { (entrada, esperado) ->
            val resultado = formatarDataNascimento(entrada)
            assertEquals("entrada \"$entrada\"", esperado, resultado)
            assertFalse(
                "\"$resultado\" (a partir de \"$entrada\") não pode terminar em \"/\"",
                resultado.endsWith("/"),
            )
        }
    }

    // ---------------------------------------------------------------
    // aplicarMascaraDataNascimento(textoAnterior: String, edicao: EdicaoDataNascimento)
    //   : EdicaoDataNascimento
    // ---------------------------------------------------------------

    @Test
    fun aplicarMascaraCriterioADigitacaoDigitoADigitoMantemCursorNoFimSemBarraADireita() {
        // Critério de aceite A: partindo do campo vazio, digitando
        // 1, 2, 0, 8, 1, 9, 8, 6 um de cada vez, sempre no fim do campo.
        val passos =
            listOf(
                "" to EdicaoDataNascimento("1", 1) to EdicaoDataNascimento("1", 1),
                "1" to EdicaoDataNascimento("12", 2) to EdicaoDataNascimento("12", 2),
                "12" to EdicaoDataNascimento("120", 3) to EdicaoDataNascimento("12/0", 4),
                "12/0" to EdicaoDataNascimento("12/08", 5) to EdicaoDataNascimento("12/08", 5),
                "12/08" to EdicaoDataNascimento("12/081", 6) to EdicaoDataNascimento("12/08/1", 7),
                "12/08/1" to EdicaoDataNascimento("12/08/19", 8) to EdicaoDataNascimento("12/08/19", 8),
                "12/08/19" to EdicaoDataNascimento("12/08/198", 9) to EdicaoDataNascimento("12/08/198", 9),
                "12/08/198" to EdicaoDataNascimento("12/08/1986", 10) to EdicaoDataNascimento("12/08/1986", 10),
            )

        passos.forEach { (entrada, esperado) ->
            val (textoAnterior, edicao) = entrada
            val resultado = aplicarMascaraDataNascimento(textoAnterior, edicao)
            assertEquals("de \"$textoAnterior\" digitando para \"${edicao.texto}\"", esperado, resultado)
            assertFalse(resultado.texto.endsWith("/"))
            assertEquals("cursor sempre no fim durante digitação no fim", resultado.texto.length, resultado.cursor)
        }
    }

    @Test
    fun aplicarMascaraCriterioBExemploLiteralDoRequisitoColadoDeUmaVez() {
        val resultado = aplicarMascaraDataNascimento("", EdicaoDataNascimento("12081986", 8))

        assertEquals(EdicaoDataNascimento("12/08/1986", 10), resultado)
    }

    @Test
    fun aplicarMascaraCriterioCApagarDoFimRemoveUmDigitoPorVezSemNuncaSerIgnorado() {
        // Simula oito backspaces reais e verdadeiros a partir de
        // "12/08/1986": a cada passo o texto anterior é o resultado do
        // passo anterior, e a edição é exatamente o que um backspace no
        // fim de um TextField produziria (remove o último caractere).
        val esperadoPorPasso =
            listOf(
                "12/08/198",
                "12/08/19",
                "12/08/1",
                "12/08",
                "12/0",
                "12",
                "1",
                "",
            )

        var textoAtual = "12/08/1986"
        esperadoPorPasso.forEach { textoEsperado ->
            val textoAposBackspaceNativo = textoAtual.dropLast(1)
            val resultado =
                aplicarMascaraDataNascimento(
                    textoAtual,
                    EdicaoDataNascimento(textoAposBackspaceNativo, textoAposBackspaceNativo.length),
                )

            assertEquals("apagando o fim de \"$textoAtual\"", textoEsperado, resultado.texto)
            assertEquals(resultado.texto.length, resultado.cursor)

            textoAtual = resultado.texto
        }
    }

    @Test
    fun aplicarMascaraCriterioDApagarNoMeioRemascaraOTextoInteiro() {
        // "12/08/1986" com o cursor logo depois do 8 de "08" (índice 5):
        // um backspace nativo remove o caractere anterior ao cursor (o
        // '8' no índice 4), produzindo "12/0/1986" com cursor em 4.
        val resultado =
            aplicarMascaraDataNascimento(
                "12/08/1986",
                EdicaoDataNascimento("12/0/1986", 4),
            )

        // Dígitos restantes: 1201986 -> remascarados como "12/01/986".
        assertEquals("12/01/986", resultado.texto)
        assertEquals("cursor entre o \"0\" e o \"1\" de \"12/01/986\"", 4, resultado.cursor)
    }

    @Test
    fun aplicarMascaraCriterioEApagarABarraRemoveODigitoAnteriorSemVirarTeclaMorta() {
        // "12/08" com o cursor logo depois da barra (índice 3): um
        // backspace nativo remove a barra (índice 2), produzindo "1208"
        // com cursor em 2 — mesma quantidade de dígitos que antes, o que
        // sinaliza "apagou uma barra" e aciona a remoção extra do dígito
        // anterior ao cursor.
        val resultado =
            aplicarMascaraDataNascimento(
                "12/08",
                EdicaoDataNascimento("1208", 2),
            )

        assertEquals("10/8", resultado.texto)
        // Dígitos restantes após remover o "2" (dígito anterior à barra):
        // "108" -> "10/8"; cursor fica logo após o "0", antes da barra.
        assertEquals(1, resultado.cursor)
        assertFalse(
            "backspace sobre a barra não pode ser tecla morta (texto tinha que mudar)",
            resultado.texto == "12/08",
        )
    }

    @Test
    fun aplicarMascaraCriterioFColarDigitosCrusSubstituindoSelecaoFormataComoDigitacao() {
        // Colar substitui um texto selecionado ("12") pelo valor colado
        // inteiro ("12081986") — o resultado não pode depender do que
        // havia antes.
        val resultado = aplicarMascaraDataNascimento("12", EdicaoDataNascimento("12081986", 8))

        assertEquals(EdicaoDataNascimento("12/08/1986", 10), resultado)
    }

    @Test
    fun aplicarMascaraCriterioGColarJaFormatadoNaoDuplicaBarraIdempotente() {
        val resultado = aplicarMascaraDataNascimento("", EdicaoDataNascimento("12/08/1986", 10))

        assertEquals(EdicaoDataNascimento("12/08/1986", 10), resultado)
    }

    @Test
    fun aplicarMascaraCriterioHColarTextoNaoNumericoResultaCampoVazioSemErro() {
        val resultado = aplicarMascaraDataNascimento("", EdicaoDataNascimento("abc", 3))

        assertEquals(EdicaoDataNascimento("", 0), resultado)
    }

    @Test
    fun aplicarMascaraCriterioIColarComSeparadorDiferenteOuEspacosNormalizaParaBarraPadrao() {
        val colagensEquivalentes =
            listOf(
                "12-08-1986",
                "12 08 1986",
                "12/08/1986 ",
            )

        colagensEquivalentes.forEach { textoColado ->
            val resultado =
                aplicarMascaraDataNascimento(
                    "",
                    EdicaoDataNascimento(textoColado, textoColado.length),
                )

            assertEquals("colando \"$textoColado\"", EdicaoDataNascimento("12/08/1986", 10), resultado)
        }
    }

    @Test
    fun aplicarMascaraCriterioJNonoDigitoNoFimDeDataCompletaEIgnoradoSilenciosamente() {
        // Com "12/08/1986" completo e cursor no fim, digitar mais um
        // dígito ("7") não pode mudar nada — nem o texto, nem o cursor.
        val resultado =
            aplicarMascaraDataNascimento(
                "12/08/1986",
                EdicaoDataNascimento("12/08/19867", 11),
            )

        assertEquals(EdicaoDataNascimento("12/08/1986", 10), resultado)
    }

    @Test
    fun aplicarMascaraCriterioJNonoDigitoInseridoNoMeioDescartaOUltimoDigitoDoResultado() {
        // Cursor depois de "12" (índice 2) em "12/08/1986", digitando
        // "5": insere "125/08/1986". Os dígitos resultantes (125081986,
        // 9 dígitos) mantêm só os 8 primeiros (12508198) -> "12/50/8198".
        val resultado =
            aplicarMascaraDataNascimento(
                "12/08/1986",
                EdicaoDataNascimento("125/08/1986", 3),
            )

        assertEquals("12/50/8198", resultado.texto)
        assertEquals(4, resultado.cursor)
    }

    @Test
    fun aplicarMascaraSemEdicaoRealDevolveOMesmoTextoComCursorConsistente() {
        // Mover o cursor sem digitar nada (ex.: seta do teclado) não pode
        // alterar o texto nem quebrar o cálculo do cursor.
        val resultado =
            aplicarMascaraDataNascimento(
                "12/08/1986",
                EdicaoDataNascimento("12/08/1986", 10),
            )

        assertEquals(EdicaoDataNascimento("12/08/1986", 10), resultado)
    }

    // ---------------------------------------------------------------
    // Achado A1 do trevo-reviewer (.claude/handoff.md, seção "Achados"):
    // `aplicarMascaraDataNascimento` pode lançar IllegalArgumentException
    // em vez de devolver um resultado seguro.
    // ---------------------------------------------------------------

    @Test
    fun aplicarMascaraApagandoUnicoCaractereNaoNumericoDoTextoAnteriorNaoLancaEDevolveEdicaoVazia() {
        // Caso alcançável descrito no achado A1: `textoAnterior` tem
        // exatamente 1 caractere, e esse caractere não é dígito ("a").
        // A edição o remove por completo (`edicao.texto == ""`). Os
        // dígitos de antes e de depois são ambos vazios
        // (`"a".filter(Char::isDigit) == "" == "".filter(Char::isDigit)`),
        // então `apagouUmaBarra` fica `true` mesmo sem nenhuma barra
        // real ter sido apagada — a função tenta então "remover o dígito
        // imediatamente antes do cursor" num texto já vazio:
        // `(edicao.cursor - 1).coerceIn(0, edicao.texto.lastIndex)` com
        // `edicao.texto.lastIndex == -1` chama `coerceIn(0, -1)`, mínimo
        // maior que máximo, o que lança `IllegalArgumentException` em
        // tempo de execução.
        //
        // Não é um caminho alcançável pelo fluxo normal de digitação de
        // hoje — todo produtor de `nascimento` na tela passa antes por
        // `formatarDataNascimento`, que nunca deixa um único caractere
        // não numérico sozinho no campo — mas `aplicarMascaraDataNascimento`
        // é API pública de `:core:engine`, sem nenhuma pré-condição
        // documentada sobre `textoAnterior`, e RF-07.1 (editar
        // nascimento a partir de um valor já existente, potencialmente
        // fora do controle desta tela) é um chamador futuro concreto.
        // Uma função pura de `:core:engine` não pode lançar exceção não
        // documentada para uma entrada dentro do seu próprio tipo
        // (`String`); o resultado esperado é o mais conservador possível:
        // texto e cursor vazios, exatamente como se o campo tivesse sido
        // esvaziado com sucesso.
        val resultado = aplicarMascaraDataNascimento("a", EdicaoDataNascimento("", 0))

        assertEquals(EdicaoDataNascimento("", 0), resultado)
    }
}
