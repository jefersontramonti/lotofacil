package com.trevo.app.detalhe

import com.trevo.core.engine.crenca.Crenca
import com.trevo.core.engine.crenca.ModoDeGeracao
import com.trevo.core.engine.crenca.RevelacaoDoAmuleto
import java.math.BigDecimal

data class OrigemDeDezenasUiState(
    val crenca: Crenca,
    val dezenas: List<Int>,
)

data class DetalheUiState(
    val carregando: Boolean = true,
    val palpiteExiste: Boolean = true,
    val numeroDoDia: Int = 0,
    val dezenas: List<Int> = emptyList(),
    val dezenasFixas: List<Int> = emptyList(),
    val forca: Int = 0,
    val origens: List<OrigemDeDezenasUiState> = emptyList(),
    // RF-11.10 — o ritual dos amuletos, como fonte própria, ao lado das
    // crenças em `origens`. Vazio pra palpites fora do modo Destino.
    val origensDoRitual: List<RevelacaoDoAmuleto> = emptyList(),
    val modo: ModoDeGeracao? = null,
    val soma: Int = 0,
    val pares: Int = 0,
    val impares: Int = 0,
    val moldura: Int = 0,
    val miolo: Int = 0,
    val custo: BigDecimal = BigDecimal.ZERO,
    // RF-04.4 — probabilidade real de 15 acertos, como "1 em N".
    val chanceRealUmEm: Long = 0,
    val quantidadeDeDezenas: Int = 15,
    val podeVerDesdobramentos: Boolean = false,
    val palpiteParaConfirmarExclusao: Boolean = false,
    val modoEdicao: Boolean = false,
    val dezenasEmEdicao: Set<Int> = emptySet(),
    val guardarComoFixasAoSalvar: Boolean = false,
    // RF-08.1/08.2 — nulo quando o concurso do palpite ainda não foi
    // sorteado/conferido: CLAUDE.md §8 proíbe inventar número de concurso,
    // então a mensagem de compartilhamento omite esse trecho nesse caso.
    val numeroDoConcurso: Int? = null,
    val compartilhando: Boolean = false,
    val copiado: Boolean = false,
) {
    val faltamOuSobram: Int get() = quantidadeDeDezenas - dezenasEmEdicao.size
}
