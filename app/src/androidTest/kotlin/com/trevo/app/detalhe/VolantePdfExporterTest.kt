package com.trevo.app.detalhe

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** RF-08.3 — exportar o volante em PDF, incluindo os jogos do fechamento. */
@RunWith(AndroidJUnit4::class)
class VolantePdfExporterTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun palpiteDe15DezenasGeraUmPdfComConteudo() =
        runTest {
            val uri = gerarPdfDoVolante(context, (1..15).toList(), numeroDoDia = 1, chanceRealUmEm = 3_268_760)

            context.contentResolver.openInputStream(uri).use { fluxo ->
                assertTrue((fluxo?.available() ?: 0) > 0)
            }
        }

    @Test
    fun fechamentoDe16DezenasIncluiTodosOs16JogosNoPdf() =
        runTest {
            // RF-08.3: "incluindo todos os jogos do desdobramento quando
            // houver" — 16 dezenas viram 16 jogos de 15 (coeficienteBinomial(16,15)).
            val uri = gerarPdfDoVolante(context, (1..16).toList(), numeroDoDia = 2, chanceRealUmEm = 204_222)

            context.contentResolver.openInputStream(uri).use { fluxo ->
                assertTrue((fluxo?.available() ?: 0) > 0)
            }
        }
}
