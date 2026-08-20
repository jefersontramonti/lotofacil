package com.trevo.core.engine.palpite

import com.trevo.core.engine.crenca.ContribuicaoDeCrenca
import com.trevo.core.engine.crenca.Crenca
import com.trevo.core.engine.crenca.DadosDeContribuicao
import com.trevo.core.engine.crenca.FONTES_PADRAO
import com.trevo.core.engine.crenca.FonteDeCrenca
import com.trevo.core.engine.crenca.ModoDeGeracao
import com.trevo.core.engine.crenca.RevelacaoDoAmuleto
import kotlin.random.Random

private const val PESO_BASE = 1.0
private const val PESO_POR_CRENCA = 1.35
private const val MULTIPLICADOR_ALEATORIO_MINIMO = 0.55
private const val MULTIPLICADOR_ALEATORIO_AMPLITUDE = 0.9

class PalpiteGenerator(
    private val random: Random = Random.Default,
) {
    fun gerar(
        crencasAtivas: Set<Crenca>,
        dados: DadosDeContribuicao,
        dezenasFixas: Set<Int> = emptySet(),
        quantidade: Int = 15,
        fontes: List<FonteDeCrenca> = FONTES_PADRAO,
        modo: ModoDeGeracao? = null,
        ritual: List<RevelacaoDoAmuleto> = emptyList(),
    ): Palpite {
        require(quantidade in 1..25) { "quantidade deve estar entre 1 e 25, era $quantidade" }
        require(dezenasFixas.all { it in 1..25 }) { "dezenasFixas devem estar entre 1 e 25" }
        require(dezenasFixas.size <= quantidade) {
            "dezenasFixas (${dezenasFixas.size}) não pode ultrapassar quantidade ($quantidade)"
        }

        val contribuicoes = contribuicoesAtivas(crencasAtivas, dados, fontes)
        val peso = pesosDe(contribuicoes)

        // Dezenas fixas entram sempre (RF-02.2) — reservadas antes do
        // sorteio ponderado, nunca dependentes do peso pra aparecer.
        val candidatas = (1..25).filterNot { it in dezenasFixas }
        val quantasSortear = quantidade - dezenasFixas.size
        val sorteadas =
            candidatas
                .map { dezena -> dezena to peso[dezena] * multiplicadorAleatorio() }
                .sortedByDescending { it.second }
                .take(quantasSortear)
                .map { it.first }

        val dezenas = (dezenasFixas + sorteadas).sorted()

        val contribuicoesNoResultado =
            contribuicoes.mapValues { (_, contribuicao) -> contribuicao.dezenas.filter { it in dezenas } }

        return Palpite(
            dezenas = dezenas,
            dezenasFixas = dezenasFixas.sorted(),
            contribuicoes = contribuicoesNoResultado,
            forca = calcularForca(crencasAtivas, contribuicoesNoResultado),
            modo = modo,
            ritual = ritual,
        )
    }

    // RF-11.7 — mesmo motor de pesos de gerar() (mesmas fontes, mesma
    // fórmula), mas devolve uma única dezena. `dezenasExcluidas` são as já
    // reveladas no ritual — nunca repete uma dezena entre amuletos.
    fun sortearDezenaDoRitual(
        crencasAtivas: Set<Crenca>,
        dados: DadosDeContribuicao,
        dezenasExcluidas: Set<Int>,
        fontes: List<FonteDeCrenca> = FONTES_PADRAO,
    ): Int {
        val peso = pesosDe(contribuicoesAtivas(crencasAtivas, dados, fontes))
        return (1..25)
            .filterNot { it in dezenasExcluidas }
            .maxBy { dezena -> peso[dezena] * multiplicadorAleatorio() }
    }

    private fun contribuicoesAtivas(
        crencasAtivas: Set<Crenca>,
        dados: DadosDeContribuicao,
        fontes: List<FonteDeCrenca>,
    ): Map<Crenca, ContribuicaoDeCrenca> =
        fontes.filter { it.crenca in crencasAtivas }.associate { it.crenca to it.contribuir(dados) }

    private fun pesosDe(contribuicoes: Map<Crenca, ContribuicaoDeCrenca>): DoubleArray {
        val peso = DoubleArray(26) { PESO_BASE }
        contribuicoes.values.forEach { contribuicao ->
            contribuicao.dezenas.forEach { dezena -> if (dezena in 1..25) peso[dezena] += PESO_POR_CRENCA }
        }
        return peso
    }

    // RF-02.8: fechamento é só o mesmo motor pedindo mais dezenas — nenhum
    // cálculo de peso separado, nenhuma lista de combinações de 15 (isso é
    // RF-04.9/04.10, na tela de desdobramento).
    fun gerarFechamento(
        tamanho: TamanhoDeFechamento,
        crencasAtivas: Set<Crenca>,
        dados: DadosDeContribuicao,
        dezenasFixas: Set<Int> = emptySet(),
        fontes: List<FonteDeCrenca> = FONTES_PADRAO,
    ): Palpite =
        gerar(
            crencasAtivas = crencasAtivas,
            dados = dados,
            dezenasFixas = dezenasFixas,
            quantidade = tamanho.quantidade,
            fontes = fontes,
        )

    private fun multiplicadorAleatorio(): Double =
        MULTIPLICADOR_ALEATORIO_MINIMO + random.nextDouble() * MULTIPLICADOR_ALEATORIO_AMPLITUDE

    // RF-02.7: cobertura das crenças ativas — percentual das crenças
    // selecionadas cuja contribuição efetivamente colocou ao menos uma
    // dezena no palpite final.
    private fun calcularForca(
        crencasAtivas: Set<Crenca>,
        contribuicoesNoResultado: Map<Crenca, List<Int>>,
    ): Int {
        if (crencasAtivas.isEmpty()) return 0
        val cobertas = contribuicoesNoResultado.count { (_, dezenasNoResultado) -> dezenasNoResultado.isNotEmpty() }
        return Math.round(cobertas.toDouble() / crencasAtivas.size * 100).toInt()
    }
}
