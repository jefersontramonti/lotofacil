package com.trevo.core.engine.crenca

import com.trevo.core.engine.identidade.Signo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class FonteSignoTest {
    private val fonte = FonteSigno()
    private val dadosBase = DadosDeContribuicao(hoje = LocalDate.of(2026, 8, 14))

    @Test
    fun semSignoDevolveListaVaziaEMotivo() {
        val contribuicao = fonte.contribuir(dadosBase.copy(signo = null))

        assertTrue(contribuicao.dezenas.isEmpty())
        assertTrue(contribuicao.explicacao.isNotBlank())
    }

    @Test
    fun comSignoDevolveExatamenteAsDezenasDoSigno() {
        val contribuicao = fonte.contribuir(dadosBase.copy(signo = Signo.CANCER))

        assertEquals(Signo.CANCER.dezenas, contribuicao.dezenas)
    }
}
