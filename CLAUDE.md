# Trevo — CLAUDE.md

Aplicativo Android de palpites para a Lotofácil. Kotlin + Jetpack Compose.

Leia este arquivo antes de qualquer alteração. Ele descreve o que o
projeto é, o que ele não pode ser, e como o código está organizado.

---

## 1. O que o produto é

O Trevo gera volantes da Lotofácil combinando crenças populares que o
apostador escolhe: signo, data de nascimento, números quentes,
atrasados, fase da lua, sonho traduzido pelo jogo do bicho, moldura do
volante, entre outras. O app explica de onde veio cada dezena e confere
o resultado quando o sorteio sai.

### Regras invioláveis

Estas regras vêm de restrição legal e de política da Play Store. Nunca
implemente nada que as contrarie, mesmo que pedido:

1. O app **não recebe aposta**, **não intermedia pagamento** e **não
   paga prêmio**. A aposta é feita pelo usuário na lotérica ou no canal
   oficial da Caixa.
2. Nenhum texto do app pode afirmar, sugerir ou insinuar que qualquer
   método aumenta a chance de acerto.
3. Toda tela que apresenta um palpite ou fechamento também apresenta a
   probabilidade real.
4. Toda cobrança passa pelo Google Play Billing. Nenhuma via de
   pagamento externa.
5. Cadastro exige 18 anos completos.

Se uma tarefa parecer exigir a quebra de alguma dessas regras, pare e
pergunte antes de implementar.

---

## 2. Comandos

```bash
./gradlew assembleDebug          # build de debug
./gradlew test                   # testes unitários (JVM, sem emulador)
./gradlew :core:engine:test      # só o motor — rápido, use durante o desenvolvimento
./gradlew connectedAndroidTest   # testes instrumentados (precisa de emulador)
./gradlew lint                   # Android Lint
./gradlew ktlintCheck            # formatação
./gradlew ktlintFormat           # corrige formatação
```

Antes de considerar uma tarefa concluída: `./gradlew test lint ktlintCheck`
precisa passar.

---

## 3. Estrutura de módulos

```
:core:engine   Kotlin puro. Motor de geração, crenças, cálculo de
               acertos, desdobramentos, validações. SEM dependência de
               Android. É onde ficam os testes mais importantes.
:core:data     Room, DataStore, Retrofit, repositórios. Depende de
               :core:engine.
:core:ui       Tema, cores, tipografia, componentes Compose compartilhados.
:app           Telas Compose, ViewModels, navegação, DI.
```

Regra de dependência: `:app` → `:core:data` → `:core:engine`.
Nunca inverta. `:core:engine` não conhece Android nem Room.

---

## 4. Arquitetura

- **MVVM.** ViewModel expõe um único `StateFlow<UiState>` por tela.
  A tela é função pura do estado.
- **UiState é data class imutável.** Nada de estado mutável espalhado
  em Composables.
- **Eventos de tela** sobem por lambdas passadas ao Composable, nunca
  por referência direta ao ViewModel dentro de componentes filhos.
- **Repositório** é a única porta para dados. ViewModel nunca chama
  Room ou Retrofit diretamente.
- **Hilt** para injeção. Módulos por camada.
- **Offline primeiro.** Toda função exceto a busca de resultado opera
  sem rede. Room é a fonte da verdade; a rede só alimenta o cache.

### Motor de geração

O núcleo do produto está em `:core:engine`. Ele é determinístico sob
semente fixa — isso é requisito, não detalhe:

```kotlin
class PalpiteGenerator(private val random: Random = Random.Default)
```

Sempre injete `Random`. Testes usam `Random(seed)` e comparam saída
exata. Nunca chame `Math.random()` ou `Random.Default` direto dentro
de uma função.

Cada crença implementa a mesma interface e devolve as dezenas que
defende, mais o texto que explica ao usuário de onde elas vieram. Uma
crença sem dado de origem válido devolve lista vazia e o motivo — nunca
lança exceção, nunca usa valor padrão silencioso.

---

## 5. Convenções de código

- Kotlin oficial code style. ktlint decide o resto.
- Nomes de domínio em **português** (`Palpite`, `Crenca`, `Concurso`,
  `Dezena`, `Fechamento`). Nomes técnicos em inglês
  (`Repository`, `ViewModel`, `UseCase`). Não misture dentro do mesmo
  identificador.
- Composables em PascalCase, arquivo por tela.
- Sem comentário que descreve o que a linha faz. Comentário só para
  explicar por que uma decisão não óbvia foi tomada.
- `String` de interface sempre em `strings.xml`, nunca literal no
  Composable.
- Valores monetários em `BigDecimal` ou centavos em `Long`. Nunca
  `Double`.

---

## 6. Interface

- Tema escuro é o padrão. Fundo `#161826`, texto `#e9e9ed`, acento
  `#9184d9`.
- Botões primários são contorno de acento sobre transparente, nunca
  preenchidos.
- Alvo de toque mínimo de 48 dp.
- Contraste mínimo de 4,5:1 para texto corrido, 3:1 para texto grande
  e elementos de interface.
- Estado de marcação nunca comunicado apenas por cor.
- Suporte a fonte do sistema até 200% sem corte.
- Todo elemento interativo com `contentDescription` para o TalkBack.
  Dezenas anunciam seu estado de marcação.

---

## 7. Testes

Prioridade de cobertura:

1. `:core:engine` — motor, crenças, cálculo de acertos, validação de
   data, desdobramentos. Meta de 80%.
2. Repositórios — cache, fila de conferência, comportamento offline.
3. ViewModels — transições de estado.
4. Instrumentados — cadastro, geração, edição, conferência.

Todo teste do motor usa semente fixa e afirma saída exata. Teste que
depende de aleatoriedade real está errado.

---

## 8. Dados externos

**API de resultados da Caixa.** Única dependência de rede. Trate como
instável: recuo exponencial, no máximo cinco tentativas por concurso,
resultado sempre cacheado em Room. Falha de rede nunca pode impedir a
geração de palpites nem perder um palpite salvo.

**Nunca** invente dados de sorteio. Se a API não respondeu, a tela
mostra o estado de erro ou de offline correspondente.

---

## 9. Como trabalhar neste repositório

- Uma tarefa por branch, uma branch por requisito (`rf-04-05-editar-volante`).
- Diff pequeno e revisável. Se a mudança tocar mais de dez arquivos,
  divida.
- Cada requisito implementado referencia seu ID (`RF-04.5`) na mensagem
  de commit.
- Não altere a estrutura de módulos, o esquema do Room ou as
  dependências do Gradle sem perguntar.
- Não adicione biblioteca nova sem justificar. O app precisa ficar
  abaixo de 25 MB.
- Migração de Room sempre versionada. Nunca destrua dados do usuário
  entre versões.

---

## 10. Fora de escopo

Registrado para evitar ambiguidade. Não implemente:

- Receber valores de aposta, intermediar pagamento ou custodiar
  dinheiro de bolão.
- Registrar aposta junto à Caixa em nome do usuário.
- Pagar prêmio ou operar premiação própria.
- Qualquer promessa de aumento de probabilidade.
- Outras loterias além da Lotofácil na primeira versão.

---

## 11. Documentos de referência

- `docs/escopo.pdf` — escopo do projeto, fases e arquitetura.
- `docs/requisitos.pdf` — 74 requisitos funcionais e 39 não funcionais,
  com ID, prioridade MoSCoW e fase. É a fonte da verdade sobre
  comportamento esperado.
- `docs/wireframes.pdf` — as 16 telas do protótipo.

Quando um requisito e este arquivo divergirem, o requisito vence —
e avise, para que este arquivo seja corrigido.
