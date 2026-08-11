---
name: compose-nocturne
description: Paleta, tokens e regras de acessibilidade do tema escuro do Trevo (Compose). Use ao criar ou revisar qualquer Composable de tela, componente ou tema.
---

# Tema Nocturne do Trevo

Fonte: CLAUDE.md seção 6 e o protótipo em `Docs/Trevo - Lotofácil.dc.html`.

## Paleta travada

- Fundo: `#161826`
- Texto: `#e9e9ed`
- Acento: `#9184d9`
- Superfícies secundárias observadas no protótipo: `#232532` (cards), `#3f424d` (borda/contorno neutro), `#423a6a` (chip/badge preenchido)

Essas cores já foram verificadas contra a razão de contraste mínima (4,5:1 texto corrido, 3:1 texto grande/elementos de interface — CLAUDE.md seção 6). **Não adicione cor nova à paleta sem reverificar contraste manualmente** — o Android Lint padrão não calcula contraste renderizado em Compose, então essa checagem não é automática.

## Regras de interface obrigatórias

- Botões primários: contorno de acento sobre transparente, nunca preenchidos (ver protótipo: `border: 1px solid #9184d9; background: transparent`).
- Alvo de toque mínimo 48dp em ambas dimensões.
- Estado de marcação (dezena escolhida, crença ativa) nunca comunicado só por cor — sempre com um segundo sinal (ícone de check, contorno tracejado para "fixa", peso de fonte).
- Toda `String` de interface em `strings.xml`, nunca literal no Composable (CLAUDE.md seção 5).
- Todo elemento interativo com `contentDescription` para TalkBack; dezenas anunciam seu estado de marcação (ex: "dezena 07, marcada").
- Suporte a fonte do sistema até 200% sem corte (RNF-03.3) — evite `Text` com `maxLines` fixo em conteúdo que pode crescer, teste com fonte grande antes de considerar pronto.

## Componentes recorrentes do protótipo

Volante: grade 5×5, dezena marcada com fundo cheio, dezena fixa com contorno tracejado, dezena não marcada neutra. Bola de resultado na conferência: cheia = acerto, tracejada = marcada que não saiu, opaca/neutra = sorteada e não marcada pelo usuário.
