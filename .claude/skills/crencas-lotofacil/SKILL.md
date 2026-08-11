---
name: crencas-lotofacil
description: Regras de cada uma das 12 crenças do motor de geração do Trevo — de onde vêm as dezenas de cada uma, como o jogo do bicho traduz sonho, e a regra de peso dominante das dezenas fixas. Use ao implementar, testar ou revisar qualquer código em :core:engine relacionado a crenças.
---

# Crenças do Trevo

Fonte: `Docs/Trevo - Requisitos.dc.html` RF-02.3/RF-02.4 e `Docs/Trevo - Escopo do Projeto.dc.html` seção 5.

## As 12 crenças (RF-02.3)

Signo, data de nascimento, números quentes, atrasados, fase da lua, sonho, moldura, sete pares e oito ímpares, primos e Fibonacci, faixa de soma, repetidas do concurso anterior, numerologia do nome.

## Regra de peso (RF-02.2, escopo seção 5)

Cada crença ativa soma peso às suas dezenas. Dezenas fixas recebem peso dominante e **sempre** entram no volante — não competem no sorteio ponderado, são garantidas. O sorteio ponderado escolhe as demais (até completar 15, ou N no fechamento) entre o restante pelo peso acumulado.

## Jogo do bicho (RF-02.4)

O grupo N puxa a dezena N e a dezena espelhada `26 - N`. Os 25 grupos do jogo do bicho mapeiam 1:1 com as 25 dezenas da Lotofácil — não é um mapeamento arbitrário, é a identidade do grupo mais o espelho.

## Crença sem dado de origem (RF-02.5, CLAUDE.md seção 4)

Uma crença cujo dado de origem esteja ausente ou inválido (ex: numerologia do nome sem nome cadastrado, sonho sem grupo selecionado) **devolve lista vazia e o motivo** — nunca lança exceção, nunca usa valor padrão silencioso. Isso precisa aparecer na explicação que a tela de detalhe mostra ao usuário (RF-04.2).

## Contrato de interface

Cada crença implementa a mesma interface e devolve: as dezenas que defende + o texto que explica ao usuário de onde elas vieram (linguagem comum, não técnica — vai direto para a tela de detalhe).

## Determinismo

O motor inteiro é determinístico sob semente fixa. Toda crença e todo sorteio ponderado recebem `Random` injetado no construtor — nunca `Math.random()` ou `Random.Default` chamado direto dentro de uma função (CLAUDE.md seção 4).
