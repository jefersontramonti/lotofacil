package com.trevo.app.ritual

import androidx.annotation.StringRes
import com.trevo.app.R
import com.trevo.core.engine.crenca.Amuleto
import com.trevo.core.engine.crenca.OpcaoDeAmuleto

// Reaproveitado por TelaRitual (escolha/revelação/resumo) e por TelaDetalhe
// (RF-11.10 — "cada amuleto reaparece no detalhe, em 'De onde vieram as
// dezenas'", wireframe 1s) — uma única fonte de texto pros dois lugares.
@StringRes
fun nomeDoAmuleto(amuleto: Amuleto): Int =
    when (amuleto) {
        Amuleto.TREVO -> R.string.amuleto_trevo_nome
        Amuleto.FERRADURA -> R.string.amuleto_ferradura_nome
        Amuleto.ANEIS -> R.string.amuleto_aneis_nome
        Amuleto.MOEDAS -> R.string.amuleto_moedas_nome
        Amuleto.BOLA_DE_CRISTAL -> R.string.amuleto_bola_de_cristal_nome
        Amuleto.DADOS -> R.string.amuleto_dados_nome
        Amuleto.ELEFANTE -> R.string.amuleto_elefante_nome
        Amuleto.ESTRELA -> R.string.amuleto_estrela_nome
    }

@StringRes
fun conceitoDoAmuleto(amuleto: Amuleto): Int =
    when (amuleto) {
        Amuleto.TREVO -> R.string.amuleto_trevo_conceito
        Amuleto.FERRADURA -> R.string.amuleto_ferradura_conceito
        Amuleto.ANEIS -> R.string.amuleto_aneis_conceito
        Amuleto.MOEDAS -> R.string.amuleto_moedas_conceito
        Amuleto.BOLA_DE_CRISTAL -> R.string.amuleto_bola_de_cristal_conceito
        Amuleto.DADOS -> R.string.amuleto_dados_conceito
        Amuleto.ELEFANTE -> R.string.amuleto_elefante_conceito
        Amuleto.ESTRELA -> R.string.amuleto_estrela_conceito
    }

@StringRes
fun perguntaDoAmuleto(amuleto: Amuleto): Int =
    when (amuleto) {
        Amuleto.TREVO -> R.string.amuleto_trevo_pergunta
        Amuleto.FERRADURA -> R.string.amuleto_ferradura_pergunta
        Amuleto.ANEIS -> R.string.amuleto_aneis_pergunta
        Amuleto.MOEDAS -> R.string.amuleto_moedas_pergunta
        Amuleto.BOLA_DE_CRISTAL -> R.string.amuleto_bola_de_cristal_pergunta
        Amuleto.DADOS -> R.string.amuleto_dados_pergunta
        Amuleto.ELEFANTE -> R.string.amuleto_elefante_pergunta
        Amuleto.ESTRELA -> R.string.amuleto_estrela_pergunta
    }

@StringRes
fun emojiDoAmuleto(amuleto: Amuleto): Int =
    when (amuleto) {
        Amuleto.TREVO -> R.string.amuleto_trevo_emoji
        Amuleto.FERRADURA -> R.string.amuleto_ferradura_emoji
        Amuleto.ANEIS -> R.string.amuleto_aneis_emoji
        Amuleto.MOEDAS -> R.string.amuleto_moedas_emoji
        Amuleto.BOLA_DE_CRISTAL -> R.string.amuleto_bola_de_cristal_emoji
        Amuleto.DADOS -> R.string.amuleto_dados_emoji
        Amuleto.ELEFANTE -> R.string.amuleto_elefante_emoji
        Amuleto.ESTRELA -> R.string.amuleto_estrela_emoji
    }

// RF-11.5 — o rótulo nunca indica qual dezena a opção esconde, só a posição/
// identidade dentro do amuleto (ex.: "do meio", nunca um número).
@StringRes
fun rotuloDaOpcao(opcao: OpcaoDeAmuleto): Int =
    when (opcao) {
        OpcaoDeAmuleto.TREVO_SORTE -> R.string.opcao_trevo_sorte
        OpcaoDeAmuleto.TREVO_PROSPERIDADE -> R.string.opcao_trevo_prosperidade
        OpcaoDeAmuleto.TREVO_AMOR -> R.string.opcao_trevo_amor
        OpcaoDeAmuleto.TREVO_FE -> R.string.opcao_trevo_fe
        OpcaoDeAmuleto.FERRADURA_ESQUERDA -> R.string.opcao_ferradura_esquerda
        OpcaoDeAmuleto.FERRADURA_MEIO -> R.string.opcao_ferradura_meio
        OpcaoDeAmuleto.FERRADURA_DIREITA -> R.string.opcao_ferradura_direita
        OpcaoDeAmuleto.ANEIS_PRIMEIRO -> R.string.opcao_aneis_primeiro
        OpcaoDeAmuleto.ANEIS_SEGUNDO -> R.string.opcao_aneis_segundo
        OpcaoDeAmuleto.ANEIS_TERCEIRO -> R.string.opcao_aneis_terceiro
        OpcaoDeAmuleto.MOEDAS_CARA -> R.string.opcao_moedas_cara
        OpcaoDeAmuleto.MOEDAS_COROA -> R.string.opcao_moedas_coroa
        OpcaoDeAmuleto.BOLA_DE_CRISTAL_LUZ -> R.string.opcao_bola_de_cristal_luz
        OpcaoDeAmuleto.BOLA_DE_CRISTAL_NEVOA -> R.string.opcao_bola_de_cristal_nevoa
        OpcaoDeAmuleto.BOLA_DE_CRISTAL_FAISCA -> R.string.opcao_bola_de_cristal_faisca
        OpcaoDeAmuleto.DADOS_VERMELHO -> R.string.opcao_dados_vermelho
        OpcaoDeAmuleto.DADOS_DOURADO -> R.string.opcao_dados_dourado
        OpcaoDeAmuleto.DADOS_PRETO -> R.string.opcao_dados_preto
        OpcaoDeAmuleto.ELEFANTE_ERGUIDA -> R.string.opcao_elefante_erguida
        OpcaoDeAmuleto.ELEFANTE_AO_CENTRO -> R.string.opcao_elefante_ao_centro
        OpcaoDeAmuleto.ELEFANTE_CURVADA -> R.string.opcao_elefante_curvada
        OpcaoDeAmuleto.ESTRELA_NORTE -> R.string.opcao_estrela_norte
        OpcaoDeAmuleto.ESTRELA_ORIENTE -> R.string.opcao_estrela_oriente
        OpcaoDeAmuleto.ESTRELA_GUIA -> R.string.opcao_estrela_guia
    }

@StringRes
fun fraseDaEscolha(opcao: OpcaoDeAmuleto): Int =
    when (opcao) {
        OpcaoDeAmuleto.TREVO_SORTE -> R.string.frase_escolha_trevo_sorte
        OpcaoDeAmuleto.TREVO_PROSPERIDADE -> R.string.frase_escolha_trevo_prosperidade
        OpcaoDeAmuleto.TREVO_AMOR -> R.string.frase_escolha_trevo_amor
        OpcaoDeAmuleto.TREVO_FE -> R.string.frase_escolha_trevo_fe
        OpcaoDeAmuleto.FERRADURA_ESQUERDA -> R.string.frase_escolha_ferradura_esquerda
        OpcaoDeAmuleto.FERRADURA_MEIO -> R.string.frase_escolha_ferradura_meio
        OpcaoDeAmuleto.FERRADURA_DIREITA -> R.string.frase_escolha_ferradura_direita
        OpcaoDeAmuleto.ANEIS_PRIMEIRO -> R.string.frase_escolha_aneis_primeiro
        OpcaoDeAmuleto.ANEIS_SEGUNDO -> R.string.frase_escolha_aneis_segundo
        OpcaoDeAmuleto.ANEIS_TERCEIRO -> R.string.frase_escolha_aneis_terceiro
        OpcaoDeAmuleto.MOEDAS_CARA -> R.string.frase_escolha_moedas_cara
        OpcaoDeAmuleto.MOEDAS_COROA -> R.string.frase_escolha_moedas_coroa
        OpcaoDeAmuleto.BOLA_DE_CRISTAL_LUZ -> R.string.frase_escolha_bola_de_cristal_luz
        OpcaoDeAmuleto.BOLA_DE_CRISTAL_NEVOA -> R.string.frase_escolha_bola_de_cristal_nevoa
        OpcaoDeAmuleto.BOLA_DE_CRISTAL_FAISCA -> R.string.frase_escolha_bola_de_cristal_faisca
        OpcaoDeAmuleto.DADOS_VERMELHO -> R.string.frase_escolha_dados_vermelho
        OpcaoDeAmuleto.DADOS_DOURADO -> R.string.frase_escolha_dados_dourado
        OpcaoDeAmuleto.DADOS_PRETO -> R.string.frase_escolha_dados_preto
        OpcaoDeAmuleto.ELEFANTE_ERGUIDA -> R.string.frase_escolha_elefante_erguida
        OpcaoDeAmuleto.ELEFANTE_AO_CENTRO -> R.string.frase_escolha_elefante_ao_centro
        OpcaoDeAmuleto.ELEFANTE_CURVADA -> R.string.frase_escolha_elefante_curvada
        OpcaoDeAmuleto.ESTRELA_NORTE -> R.string.frase_escolha_estrela_norte
        OpcaoDeAmuleto.ESTRELA_ORIENTE -> R.string.frase_escolha_estrela_oriente
        OpcaoDeAmuleto.ESTRELA_GUIA -> R.string.frase_escolha_estrela_guia
    }
