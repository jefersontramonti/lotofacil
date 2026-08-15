package com.trevo.core.engine.crenca

enum class ModoDeGeracao {
    MISTICO,
    CIENTISTA,
    DESTINO,
}

private val CRENCAS_MISTICAS =
    setOf(Crenca.SIGNO, Crenca.NASCIMENTO, Crenca.LUA, Crenca.SONHO, Crenca.MOLDURA, Crenca.NUMEROLOGIA)
private val CRENCAS_ESTATISTICAS =
    setOf(Crenca.QUENTES, Crenca.ATRASADOS, Crenca.PARES, Crenca.PRIMOS, Crenca.SOMA, Crenca.REPETIDAS)

// O modo filtra quais crenças são efetivamente aplicadas — não apaga a
// seleção do usuário. Trocar de modo nunca perde a escolha de crenças
// (CLAUDE.md §4): quem chama guarda `selecionadas` intacto e só usa o
// resultado filtrado para gerar o palpite.
fun crencasAtivasNoModo(
    modo: ModoDeGeracao,
    selecionadas: Set<Crenca>,
): Set<Crenca> =
    when (modo) {
        ModoDeGeracao.MISTICO -> selecionadas intersect CRENCAS_MISTICAS
        ModoDeGeracao.CIENTISTA -> selecionadas intersect CRENCAS_ESTATISTICAS
        ModoDeGeracao.DESTINO -> selecionadas
    }
