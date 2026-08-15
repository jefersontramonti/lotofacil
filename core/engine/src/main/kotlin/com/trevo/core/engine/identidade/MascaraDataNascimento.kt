package com.trevo.core.engine.identidade

private const val MAXIMO_DE_DIGITOS = 8

data class EdicaoDataNascimento(
    val texto: String,
    val cursor: Int,
)

fun formatarDataNascimento(texto: String): String {
    val digitos = texto.filter(Char::isDigit).take(MAXIMO_DE_DIGITOS)
    return buildString {
        digitos.forEachIndexed { indice, digito ->
            if (indice == 2 || indice == 4) append('/')
            append(digito)
        }
    }
}

fun aplicarMascaraDataNascimento(
    textoAnterior: String,
    edicao: EdicaoDataNascimento,
): EdicaoDataNascimento {
    val digitosAnterior = textoAnterior.filter(Char::isDigit)
    val digitosEdicao = edicao.texto.filter(Char::isDigit)
    val apagouUmaBarra =
        edicao.texto.length == textoAnterior.length - 1 && digitosAnterior == digitosEdicao

    val texto: String
    val cursor: Int
    if (apagouUmaBarra && edicao.texto.isEmpty()) {
        texto = edicao.texto
        cursor = edicao.cursor
    } else if (apagouUmaBarra) {
        val indiceDoDigitoAnterior = (edicao.cursor - 1).coerceIn(0, edicao.texto.lastIndex)
        texto = edicao.texto.removeRange(indiceDoDigitoAnterior, indiceDoDigitoAnterior + 1)
        cursor = indiceDoDigitoAnterior
    } else {
        texto = edicao.texto
        cursor = edicao.cursor
    }

    val digitosAntesDoCursor = texto.take(cursor).count(Char::isDigit).coerceAtMost(MAXIMO_DE_DIGITOS)
    val textoFormatado = formatarDataNascimento(texto)

    return EdicaoDataNascimento(textoFormatado, cursorAposNesimoDigito(textoFormatado, digitosAntesDoCursor))
}

private fun cursorAposNesimoDigito(
    texto: String,
    n: Int,
): Int {
    if (n == 0) return 0

    var digitosVistos = 0
    texto.forEachIndexed { indice, caractere ->
        if (caractere.isDigit()) {
            digitosVistos++
            if (digitosVistos == n) return indice + 1
        }
    }
    return texto.length
}
