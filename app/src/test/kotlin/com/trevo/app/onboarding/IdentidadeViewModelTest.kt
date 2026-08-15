package com.trevo.app.onboarding

import com.trevo.core.engine.identidade.ErroDataNascimento
import com.trevo.core.engine.identidade.Signo
import com.trevo.core.engine.identidade.ValidadorDataNascimento
import com.trevo.core.engine.identidade.VerificadorDeIdade
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

/**
 * RF-01.2 — "Coletar nome e data de nascimento em campos separados."
 * RF-01.3 — "Validar a data no formato dd/mm/aaaa: mês entre 1 e 12, dia
 * existente no mês (considerando ano bissexto), ano entre 1900 e a data
 * atual."
 * RF-01.4 — "Bloquear o cadastro de menores de 18 anos, com mensagem
 * explícita."
 *
 * Cobre o estado do [IdentidadeViewModel]: valor inicial vazio, mutação
 * independente de cada campo, formatação automática do texto digitado no
 * campo de nascimento (RF-01.9 — barras `dd/mm/aaaa` inseridas enquanto o
 * usuário digita só números, substituindo a garantia de armazenamento
 * verbatim que RF-01.2 fixara para esse campo), o recálculo de
 * `nascimentoValido`/`erroNascimento` a cada `aoAlterarNascimento`,
 * delegado ao [ValidadorDataNascimento] (RF-01.3), e, a partir de RF-01.4,
 * a segunda etapa de julgamento delegada ao [VerificadorDeIdade]: uma data
 * formalmente válida ainda pode reprovar por idade
 * (`ErroDataNascimento.MENOR_DE_IDADE`), sem que `nascimentoValido` deixe
 * de ser `true` (plano `.claude/handoff.md`, decisão D5 — `nascimentoValido`
 * continua significando só "data bem formada"). Ambos os colaboradores
 * usam o mesmo relógio fixo ([RELOGIO_FIXO]) — nenhum teste depende da
 * data real de execução.
 *
 * O campo nome não é afetado por RF-01.9: continua guardado verbatim, sem
 * máscara, sem trim, sem truncamento.
 *
 * [IdentidadeViewModel] e [IdentidadeUiState] ainda não existem em
 * produção: este arquivo deve falhar a compilação (`:app:testDebugUnitTest`
 * / `:app:testReleaseUnitTest`) até que o `trevo-developer` os implemente
 * em `app/src/main/kotlin/com/trevo/app/onboarding/`. `VerificadorDeIdade`
 * também ainda não existe (RF-01.4, `:core:engine`).
 *
 * As mutações do ViewModel são síncronas (sem coroutines) — por isso os
 * testes leem `uiState.value` diretamente, sem `runTest`/coroutines-test
 * (não estão no catálogo de dependências de `:app`, ver CLAUDE.md §9).
 */
class IdentidadeViewModelTest {
    // Instant.parse("2026-08-13T12:00:00Z") em America/Sao_Paulo (UTC-3,
    // sem horário de verão desde 2019) cai em 2026-08-13 — mesmo relógio
    // fixo usado em ValidadorDataNascimentoTest (:core:engine).
    companion object {
        private val RELOGIO_FIXO: Clock =
            Clock.fixed(Instant.parse("2026-08-13T12:00:00Z"), ZoneId.of("America/Sao_Paulo"))
    }

    private fun novoViewModel() =
        IdentidadeViewModel(ValidadorDataNascimento(RELOGIO_FIXO), VerificadorDeIdade(RELOGIO_FIXO))

    @Test
    fun estadoInicialTemNomeENascimentoVazios() {
        val viewModel = novoViewModel()

        assertEquals(IdentidadeUiState(nome = "", nascimento = ""), viewModel.uiState.value)
    }

    @Test
    fun estadoInicialNaoEValidoENaoTemErroDeNascimento() {
        val viewModel = novoViewModel()

        val estado = viewModel.uiState.value
        assertFalse(estado.nascimentoValido)
        assertNull(estado.erroNascimento)
    }

    @Test
    fun aoAlterarNomeAtualizaSomenteONomeNoEstado() {
        val viewModel = novoViewModel()

        viewModel.aoAlterarNome("Marlene")

        val estado = viewModel.uiState.value
        assertEquals("Marlene", estado.nome)
        assertEquals("", estado.nascimento)
    }

    @Test
    fun aoAlterarNascimentoAtualizaSomenteONascimentoNoEstado() {
        val viewModel = novoViewModel()

        viewModel.aoAlterarNascimento("14/07/1978")

        val estado = viewModel.uiState.value
        assertEquals("14/07/1978", estado.nascimento)
        assertEquals("", estado.nome)
    }

    @Test
    fun alterarUmCampoNaoSobrescreveOValorJaDigitadoNoOutro() {
        val viewModel = novoViewModel()

        viewModel.aoAlterarNome("Marlene")
        viewModel.aoAlterarNascimento("14/07/1978")
        viewModel.aoAlterarNome("Marlene Souza")

        val estado = viewModel.uiState.value
        assertEquals("Marlene Souza", estado.nome)
        assertEquals("14/07/1978", estado.nascimento)
    }

    @Test
    fun nomeEArmazenadoVerbatimSemTrimEmEspacosNasBordas() {
        // "  Ana  " tem espaços propositais nas duas pontas: trim/normalização
        // é decisão de exibição ou de validação (RF-01.3), não desta captura.
        val viewModel = novoViewModel()

        viewModel.aoAlterarNome("  Ana  ")

        assertEquals("  Ana  ", viewModel.uiState.value.nome)
    }

    @Test
    fun nascimentoParcialAtravessaAMascaraSemMudar() {
        // "14/0" já está com a barra na posição certa (3 dígitos: "14" e
        // "0") — a máscara de RF-01.9 é idempotente sobre esse valor, então
        // o estado não muda: não é ausência de máscara, é ponto fixo dela.
        val viewModel = novoViewModel()

        viewModel.aoAlterarNascimento("14/0")

        assertEquals("14/0", viewModel.uiState.value.nascimento)
    }

    @Test
    fun nascimentoDescartaCaracteresNaoNumericosEmSilencio() {
        // RF-01.9: caracteres não numéricos (letra, espaço, separador
        // diferente de "/") são filtrados em silêncio pela máscara, nunca
        // rejeitados nem preservados. "abc" não tem dígito nenhum, então o
        // resultado é o campo vazio — sem erro, porque campo vazio é
        // silencioso (RF-01.3, ErroDataNascimento.VAZIO não vira mensagem).
        val viewModel = novoViewModel()

        viewModel.aoAlterarNascimento("abc")

        val estado = viewModel.uiState.value
        assertEquals("", estado.nascimento)
        assertNull(estado.erroNascimento)
    }

    @Test
    fun chamarAoAlterarNascimentoVariasVezesRefleteApenasOUltimoValor() {
        val viewModel = novoViewModel()

        viewModel.aoAlterarNascimento("1")
        viewModel.aoAlterarNascimento("14")
        viewModel.aoAlterarNascimento("14/07/1978")

        assertEquals("14/07/1978", viewModel.uiState.value.nascimento)
    }

    @Test
    fun nascimentoValidoMarcaNascimentoValidoEZeraErro() {
        val viewModel = novoViewModel()

        viewModel.aoAlterarNascimento("14/07/1978")

        val estado = viewModel.uiState.value
        assertTrue(estado.nascimentoValido)
        assertNull(estado.erroNascimento)
        assertEquals("14/07/1978", estado.nascimento)
    }

    @Test
    fun nascimentoSoComDigitosGanhaBarrasEFicaValido() {
        // Antes de RF-01.9 este teste (então nomeado
        // nascimentoSemBarrasMarcaInvalidoComErroDeFormato) afirmava que
        // "13081986" sem barras marcava FORMATO_INVALIDO — contrato de
        // RF-01.2 (verbatim, sem máscara). RF-01.9 substitui esse contrato:
        // a máscara insere as barras antes de guardar/validar, então o
        // mesmo texto digitado vira uma data válida.
        val viewModel = novoViewModel()

        viewModel.aoAlterarNascimento("13081986")

        val estado = viewModel.uiState.value
        assertEquals("13/08/1986", estado.nascimento)
        assertTrue(estado.nascimentoValido)
        assertNull(estado.erroNascimento)
    }

    @Test
    fun aoAlterarNascimentoComExemploLiteralDoRequisitoResultaEmDataFormatadaValida() {
        // Exemplo literal do texto do requisito RF-01.9: "12081986" exibido
        // como "12/08/1986". Dígito de dia diferente do teste anterior
        // (12, não 13) — cobre o exemplo do requisito além da regressão.
        val viewModel = novoViewModel()

        viewModel.aoAlterarNascimento("12081986")

        val estado = viewModel.uiState.value
        assertEquals("12/08/1986", estado.nascimento)
        assertTrue(estado.nascimentoValido)
        assertNull(estado.erroNascimento)
    }

    @Test
    fun nascimentoComMesInvalidoMarcaInvalidoComErroDeMes() {
        val viewModel = novoViewModel()

        viewModel.aoAlterarNascimento("14/13/1978")

        val estado = viewModel.uiState.value
        assertFalse(estado.nascimentoValido)
        assertEquals(ErroDataNascimento.MES_INVALIDO, estado.erroNascimento)
    }

    @Test
    fun nascimentoComDiaInexistenteMarcaInvalidoComErroDeDia() {
        val viewModel = novoViewModel()

        viewModel.aoAlterarNascimento("31/04/1978")

        val estado = viewModel.uiState.value
        assertFalse(estado.nascimentoValido)
        assertEquals(ErroDataNascimento.DIA_INEXISTENTE, estado.erroNascimento)
    }

    @Test
    fun nascimentoAnteriorA1900MarcaInvalidoComErroDeIntervalo() {
        val viewModel = novoViewModel()

        viewModel.aoAlterarNascimento("01/01/1899")

        val estado = viewModel.uiState.value
        assertFalse(estado.nascimentoValido)
        assertEquals(ErroDataNascimento.FORA_DO_INTERVALO, estado.erroNascimento)
    }

    @Test
    fun voltarParaNascimentoVazioAposValidoLimpaValidacaoSemVirarErro() {
        val viewModel = novoViewModel()

        viewModel.aoAlterarNascimento("14/07/1978")
        viewModel.aoAlterarNascimento("")

        val estado = viewModel.uiState.value
        assertFalse(estado.nascimentoValido)
        assertNull(estado.erroNascimento)
        assertEquals("", estado.nascimento)
    }

    @Test
    fun nascimentoComEspacosNasBordasPerdeOsEspacosNaMascaraESegueValido() {
        // Antes de RF-01.9, " 14/07/1978 " era guardado verbatim, espaços
        // incluídos (RF-01.2). A máscara filtra qualquer caractere que não
        // seja dígito — inclusive espaço —, então o resultado sai sempre
        // sem espaços, e a data continua válida.
        val viewModel = novoViewModel()

        viewModel.aoAlterarNascimento(" 14/07/1978 ")

        val estado = viewModel.uiState.value
        assertEquals("14/07/1978", estado.nascimento)
        assertTrue(estado.nascimentoValido)
        assertNull(estado.erroNascimento)
    }

    @Test
    fun aoAlterarNomeNaoAlteraValidacaoDoNascimento() {
        val viewModel = novoViewModel()

        viewModel.aoAlterarNascimento("14/13/1978")
        val estadoAntes = viewModel.uiState.value

        viewModel.aoAlterarNome("Marlene")
        val estadoDepois = viewModel.uiState.value

        assertEquals(estadoAntes.nascimentoValido, estadoDepois.nascimentoValido)
        assertEquals(estadoAntes.erroNascimento, estadoDepois.erroNascimento)
        assertEquals(ErroDataNascimento.MES_INVALIDO, estadoDepois.erroNascimento)
    }

    // RF-01.4 — casos V1 a V13 da tabela em ".claude/handoff.md", seção
    // "6. Tabela para o test-engineer", sob RELOGIO_FIXO (hoje = 2026-08-13).

    @Test
    fun v1NascidoExatamenteDezoitoAnosHojeEValidoESemErro() {
        val viewModel = novoViewModel()

        viewModel.aoAlterarNascimento("13/08/2008")

        val estado = viewModel.uiState.value
        assertEquals("13/08/2008", estado.nascimento)
        assertTrue(estado.nascimentoValido)
        assertNull(estado.erroNascimento)
    }

    @Test
    fun v2NascidoQueCompletaDezoitoAnosAmanhaEValidoComErroDeMenorDeIdade() {
        // Trava o par de D5: nascimentoValido continua true (data bem
        // formada) mesmo quando erroNascimento acusa MENOR_DE_IDADE.
        val viewModel = novoViewModel()

        viewModel.aoAlterarNascimento("14/08/2008")

        val estado = viewModel.uiState.value
        assertEquals("14/08/2008", estado.nascimento)
        assertTrue(estado.nascimentoValido)
        assertEquals(ErroDataNascimento.MENOR_DE_IDADE, estado.erroNascimento)
    }

    @Test
    fun v3NascidoEm2020EValidoComErroDeMenorDeIdade() {
        val viewModel = novoViewModel()

        viewModel.aoAlterarNascimento("14/07/2020")

        val estado = viewModel.uiState.value
        assertEquals("14/07/2020", estado.nascimento)
        assertTrue(estado.nascimentoValido)
        assertEquals(ErroDataNascimento.MENOR_DE_IDADE, estado.erroNascimento)
    }

    @Test
    fun v4DigitosCrusQueAMascaraFormataParaMenorDeIdadeAcusaErroDeIdade() {
        // Cobre máscara (RF-01.9) e idade (RF-01.4) no mesmo caminho:
        // "13082020" vira "13/08/2020" pela máscara, e essa data é menor de
        // idade sob o relógio fixo.
        val viewModel = novoViewModel()

        viewModel.aoAlterarNascimento("13082020")

        val estado = viewModel.uiState.value
        assertEquals("13/08/2020", estado.nascimento)
        assertTrue(estado.nascimentoValido)
        assertEquals(ErroDataNascimento.MENOR_DE_IDADE, estado.erroNascimento)
    }

    @Test
    fun v5NascidoEm1978EValidoESemErro() {
        val viewModel = novoViewModel()

        viewModel.aoAlterarNascimento("14/07/1978")

        val estado = viewModel.uiState.value
        assertEquals("14/07/1978", estado.nascimento)
        assertTrue(estado.nascimentoValido)
        assertNull(estado.erroNascimento)
    }

    @Test
    fun v6LimiteInferiorDeRf013EValidoESemErroDeIdade() {
        val viewModel = novoViewModel()

        viewModel.aoAlterarNascimento("01/01/1900")

        val estado = viewModel.uiState.value
        assertEquals("01/01/1900", estado.nascimento)
        assertTrue(estado.nascimentoValido)
        assertNull(estado.erroNascimento)
    }

    @Test
    fun v7NascidoHojeEValidoComErroDeMenorDeIdadeNaoDeIntervalo() {
        // Caso não óbvio do plano: "hoje" é o limite superior inclusivo de
        // RF-01.3 (data válida, FORA_DO_INTERVALO não se aplica), então a
        // reprovação tem de vir da idade (0 anos completos).
        val viewModel = novoViewModel()

        viewModel.aoAlterarNascimento("13/08/2026")

        val estado = viewModel.uiState.value
        assertEquals("13/08/2026", estado.nascimento)
        assertTrue(estado.nascimentoValido)
        assertEquals(ErroDataNascimento.MENOR_DE_IDADE, estado.erroNascimento)
    }

    @Test
    fun v8DiaInexistenteNaoChegaAChecagemDeIdade() {
        val viewModel = novoViewModel()

        viewModel.aoAlterarNascimento("31/04/1978")

        val estado = viewModel.uiState.value
        assertFalse(estado.nascimentoValido)
        assertEquals(ErroDataNascimento.DIA_INEXISTENTE, estado.erroNascimento)
    }

    @Test
    fun v9MesInvalidoNaoChegaAChecagemDeIdade() {
        val viewModel = novoViewModel()

        viewModel.aoAlterarNascimento("14/13/1978")

        val estado = viewModel.uiState.value
        assertFalse(estado.nascimentoValido)
        assertEquals(ErroDataNascimento.MES_INVALIDO, estado.erroNascimento)
    }

    @Test
    fun v10ForaDoIntervaloNaoChegaAChecagemDeIdade() {
        val viewModel = novoViewModel()

        viewModel.aoAlterarNascimento("01/01/1899")

        val estado = viewModel.uiState.value
        assertFalse(estado.nascimentoValido)
        assertEquals(ErroDataNascimento.FORA_DO_INTERVALO, estado.erroNascimento)
    }

    @Test
    fun v11FormatoInvalidoNaoChegaAChecagemDeIdade() {
        val viewModel = novoViewModel()

        viewModel.aoAlterarNascimento("12/08/198")

        val estado = viewModel.uiState.value
        assertFalse(estado.nascimentoValido)
        assertEquals(ErroDataNascimento.FORMATO_INVALIDO, estado.erroNascimento)
    }

    @Test
    fun v12CampoVazioContinuaSilenciosoSemErroDeIdade() {
        val viewModel = novoViewModel()

        viewModel.aoAlterarNascimento("")

        val estado = viewModel.uiState.value
        assertEquals("", estado.nascimento)
        assertFalse(estado.nascimentoValido)
        assertNull(estado.erroNascimento)
    }

    @Test
    fun v13CorrigirDataDeMenorDeIdadeParaMaiorDeIdadeLimpaOErro() {
        val viewModel = novoViewModel()

        viewModel.aoAlterarNascimento("14/07/2020")
        viewModel.aoAlterarNascimento("14/07/1978")

        val estado = viewModel.uiState.value
        assertEquals("14/07/1978", estado.nascimento)
        assertTrue(estado.nascimentoValido)
        assertNull(estado.erroNascimento)
    }

    // RF-01.5 — tabela 9.4 do plano em ".claude/handoff.md" (relógio fixo
    // 2026-08-13, o mesmo já usado nos testes acima). O signo vem sempre do
    // `LocalDate` que `ValidadorDataNascimento` já validou (D4 do plano),
    // nunca de um reparse do texto do campo, e não é afetado pelo julgamento
    // de idade de RF-01.4.

    @Test
    fun estadoInicialNaoTemSignoCalculado() {
        // Critério de aceite 13 do plano: signo == null antes de qualquer
        // digitação.
        val viewModel = novoViewModel()

        assertNull(viewModel.uiState.value.signo)
    }

    @Test
    fun dataDeMenorDeIdadeCalculaOSignoAoMesmoTempoQueAcusaOErroDeIdade() {
        // Critério de aceite 12, e o caso mais importante da tabela 9.4:
        // "14/07/2020" prova, num único caso, que idade (RF-01.4) e signo
        // (RF-01.5) são julgamentos independentes sobre o mesmo LocalDate.
        // nascimentoValido == true, erroNascimento == MENOR_DE_IDADE e
        // signo == CANCER têm que valer simultaneamente.
        val viewModel = novoViewModel()

        viewModel.aoAlterarNascimento("14/07/2020")

        val estado = viewModel.uiState.value
        assertTrue(estado.nascimentoValido)
        assertEquals(ErroDataNascimento.MENOR_DE_IDADE, estado.erroNascimento)
        assertEquals(Signo.CANCER, estado.signo)
    }

    @Test
    fun dataValidaDeMaiorDeIdadeCalculaOSignoSemErro() {
        val viewModel = novoViewModel()

        viewModel.aoAlterarNascimento("14/07/1978")

        val estado = viewModel.uiState.value
        assertTrue(estado.nascimentoValido)
        assertNull(estado.erroNascimento)
        assertEquals(Signo.CANCER, estado.signo)
    }

    @Test
    fun dataValidaNaViradaDeAnoCalculaCapricornioTantoNoUltimoDiaDoAnoQuantoNoPrimeiro() {
        val viewModel = novoViewModel()

        viewModel.aoAlterarNascimento("22/12/1990")
        assertEquals(Signo.CAPRICORNIO, viewModel.uiState.value.signo)

        viewModel.aoAlterarNascimento("01/01/1990")
        assertEquals(Signo.CAPRICORNIO, viewModel.uiState.value.signo)
    }

    @Test
    fun dataValidaNoDiaDeInicioDeLeaoCalculaLeaoNaoCancer() {
        // "23/07/1990" é o dia de início de Leão — não pode cair no signo
        // anterior (mesma regra do exemplo obrigatório 14/07 -> Câncer).
        val viewModel = novoViewModel()

        viewModel.aoAlterarNascimento("23/07/1990")

        assertEquals(Signo.LEAO, viewModel.uiState.value.signo)
    }

    @Test
    fun camposVazioComFormatoInvalidoComMesInvalidoComDiaInexistenteEComForaDoIntervaloNaoTemSigno() {
        // Critério de aceite 11: para TODOS os 5 erros de
        // ValidadorDataNascimento (incluindo VAZIO, silencioso na tela),
        // signo == null — nunca um signo padrão.
        val viewModel = novoViewModel()

        viewModel.aoAlterarNascimento("")
        assertNull(viewModel.uiState.value.signo)

        viewModel.aoAlterarNascimento("12/08/198")
        assertEquals(ErroDataNascimento.FORMATO_INVALIDO, viewModel.uiState.value.erroNascimento)
        assertNull(viewModel.uiState.value.signo)

        viewModel.aoAlterarNascimento("14/13/1978")
        assertEquals(ErroDataNascimento.MES_INVALIDO, viewModel.uiState.value.erroNascimento)
        assertNull(viewModel.uiState.value.signo)

        viewModel.aoAlterarNascimento("31/04/1978")
        assertEquals(ErroDataNascimento.DIA_INEXISTENTE, viewModel.uiState.value.erroNascimento)
        assertNull(viewModel.uiState.value.signo)

        viewModel.aoAlterarNascimento("01/01/1899")
        assertEquals(ErroDataNascimento.FORA_DO_INTERVALO, viewModel.uiState.value.erroNascimento)
        assertNull(viewModel.uiState.value.signo)
    }

    @Test
    fun apagarUmDigitoDeUmaDataValidaDerrubaOSignoParaNuloNoMesmoEvento() {
        // Critério de aceite 14: nenhum signo velho sobrevive a uma edição
        // que torna a data inválida de novo.
        val viewModel = novoViewModel()

        viewModel.aoAlterarNascimento("14/07/1978")
        assertEquals(Signo.CANCER, viewModel.uiState.value.signo)

        viewModel.aoAlterarNascimento("14/07/197")

        val estado = viewModel.uiState.value
        assertFalse(estado.nascimentoValido)
        assertNull(estado.signo)
    }

    @Test
    fun aoAlterarNomeNaoAlteraOSignoJaCalculado() {
        // Critério de aceite 15.
        val viewModel = novoViewModel()

        viewModel.aoAlterarNascimento("14/07/1978")
        val signoAntes = viewModel.uiState.value.signo

        viewModel.aoAlterarNome("Marlene")

        assertEquals(signoAntes, viewModel.uiState.value.signo)
        assertEquals(Signo.CANCER, viewModel.uiState.value.signo)
    }
}
