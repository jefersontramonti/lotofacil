package com.trevo.app.detalhe

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.trevo.app.R
import com.trevo.core.engine.palpite.combinacoesDe15
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Locale

// A4 a 72dpi (pontos), igual à unidade que PdfDocument espera.
private const val LARGURA_PAGINA = 595
private const val ALTURA_PAGINA = 842
private const val MARGEM = 40f
private const val ALTURA_LINHA = 18f

// RF-08.3 — roda fora da thread principal (RNF-01.3): um fechamento de 20
// dezenas são 15.504 jogos, cada um vira uma linha numa página A4 própria.
suspend fun gerarPdfDoVolante(
    context: Context,
    dezenas: List<Int>,
    numeroDoDia: Int,
    chanceRealUmEm: Long,
): Uri =
    withContext(Dispatchers.Default) {
        val linhas =
            if (dezenas.size > 15) {
                combinacoesDe15(dezenas).map { jogo -> jogo.joinToString(" · ") { "%02d".format(it) } }.toList()
            } else {
                listOf(dezenas.sorted().joinToString(" · ") { "%02d".format(it) })
            }

        val paintTitulo = Paint().apply { textSize = 18f }
        val paintTexto = Paint().apply { textSize = 12f }

        val titulo = context.getString(R.string.pdf_titulo_palpite, numeroDoDia)
        val subtitulo =
            if (dezenas.size > 15) {
                context.getString(R.string.pdf_fechamento_subtitulo, linhas.size, dezenas.size)
            } else {
                null
            }
        val chance = context.getString(R.string.pdf_chance_real, formatarInteiroPdf(chanceRealUmEm))

        val documento = PdfDocument()
        var indiceDaLinha = 0
        var numeroDaPagina = 0
        do {
            numeroDaPagina++
            val info = PdfDocument.PageInfo.Builder(LARGURA_PAGINA, ALTURA_PAGINA, numeroDaPagina).create()
            val pagina = documento.startPage(info)
            val canvas = pagina.canvas
            var y = MARGEM
            if (numeroDaPagina == 1) {
                canvas.drawText(titulo, MARGEM, y, paintTitulo)
                y += ALTURA_LINHA * 2
                if (subtitulo != null) {
                    canvas.drawText(subtitulo, MARGEM, y, paintTexto)
                    y += ALTURA_LINHA * 1.5f
                }
                canvas.drawText(chance, MARGEM, y, paintTexto)
                y += ALTURA_LINHA * 2
            }
            while (indiceDaLinha < linhas.size && y <= ALTURA_PAGINA - MARGEM) {
                canvas.drawText(linhas[indiceDaLinha], MARGEM, y, paintTexto)
                y += ALTURA_LINHA
                indiceDaLinha++
            }
            documento.finishPage(pagina)
        } while (indiceDaLinha < linhas.size)

        val diretorio = File(context.cacheDir, "exportados").apply { mkdirs() }
        val arquivo = File(diretorio, "trevo_palpite_$numeroDoDia.pdf")
        FileOutputStream(arquivo).use { documento.writeTo(it) }
        documento.close()

        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", arquivo)
    }

private fun formatarInteiroPdf(valor: Long): String {
    val formato = NumberFormat.getIntegerInstance(Locale("pt", "BR"))
    return formato.format(valor)
}
