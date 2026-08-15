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

Três modos decidem o que alimenta o volante:

- **Místico** — só as crenças (signo, nascimento, lua, sonho, moldura,
  numerologia).
- **Cientista** — só as estatísticas (quentes, atrasados, pares, primos,
  soma, repetidas).
- **Destino** — as duas mais o **ritual dos amuletos**: trevo de quatro
  folhas, ferradura e anéis de ouro em sequência, cada um revelando uma
  dezena depois da escolha do apostador. No modo Destino o botão de
  gerar dá lugar ao ritual.

O ritual é o diferencial do produto. A escolha é do usuário, a dezena é
sorteada pelo mesmo motor — e a tela diz isso.

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

O modo filtra quais crenças são efetivamente aplicadas — não as apaga da
seleção do usuário. Trocar de modo nunca perde a escolha de crenças.

### Ritual dos amuletos

Cada amuleto sorteia sua dezena pelo mesmo motor de pesos, **excluindo as
já reveladas no ritual**. As dezenas reveladas entram forçadas no volante
final e aparecem na explicação de origem como fonte própria, ao lado das
crenças.

Ao montar o palpite, **limpe o ritual**. Se as dezenas reveladas
sobreviverem ao palpite, todo volante seguinte nasce com as mesmas quatro
dezenas sem nova escolha — foi um bug real do protótipo.

### Duas regras de domínio que já quebraram uma vez

Os dois erros abaixo foram encontrados no protótipo depois de passarem
desapercebidos. Ambos precisam de teste próprio:

1. **Dezenas do grupo do bicho são deduplicadas.** O grupo N puxa N e
   26−N. No grupo 13 as duas coincidem (26−13 = 13): a dezena aparece
   uma vez só, no card e no peso do motor.
2. **Signo se calcula pelo dia de início.** Uma data anterior ao início
   do signo que começa naquele mês pertence ao **signo anterior**.
   14/07 é Câncer, não Leão. As dezenas do signo alimentam o motor, então
   um erro aqui contamina o volante inteiro.

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
- Cada um dos 25 grupos do jogo do bicho tem card próprio: nome, número
  do grupo, leitura popular do sonho, as dezenas que ele puxa e a
  confirmação como sonho do dia. A leitura é apresentada como tradição
  popular, nunca como previsão.
- As animações do ritual e do card (giro, halo, entrada) são decoração:
  nenhuma informação pode existir só nelas, e todas respeitam a
  preferência de movimento reduzido do sistema.
- `enableEdgeToEdge()` sempre com `statusBarStyle`/`navigationBarStyle`
  explícitos (`SystemBarStyle.dark(...)`), nunca o padrão. O padrão
  (`auto`) segue o modo claro/escuro do sistema, mas o tema do Trevo é
  escuro fixo — sem isso os ícones da status bar ficam pretos sobre o
  fundo escuro quando o aparelho está em modo claro (achado real em
  RF-01.1: contraste 1,19:1 contra o mínimo de 3:1 desta seção).

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

Teste instrumentado de Compose (`createComposeRule()`) não hospeda
insets reais de sistema (status bar, navigation bar) nem o modo
claro/escuro do aparelho. Mudança de layout que interage com borda de
tela, barras de sistema, ou que precisa bater com um wireframe, exige
verificação visual em device/emulador real (screenshot, `uiautomator
dump`) além do teste verde — em RF-01.1, dois bugs de layout (CTA sob a
navigation bar; ícones de status bar pretos em modo claro) passaram
pelos 6 testes instrumentados e só foram achados medindo em device.

---

## 8. Dados externos

**API de resultados da Caixa.** Única dependência de rede. Trate como
instável: recuo exponencial, no máximo cinco tentativas por concurso,
resultado sempre cacheado em Room. Falha de rede nunca pode impedir a
geração de palpites nem perder um palpite salvo.

**Nunca** invente dados de sorteio. Se a API não respondeu, a tela
mostra o estado de erro ou de offline correspondente.

---


## 11. Documentos de referência

Todos em `../../../Lotofacil/Docs` (D maiúsculo), formato `.dc.html`:

- `Docs/Trevo - Escopo do Projeto.dc.html` — escopo do projeto, fases e
  arquitetura.
- `Docs/Trevo - Requisitos.dc.html` — 94 requisitos funcionais (RF) e 44
  não funcionais (RNF), com ID, prioridade MoSCoW e fase — inclui o
  RF-11 dos três modos de geração e do ritual dos amuletos. É a fonte
  da verdade sobre comportamento esperado.
- `Docs/Trevo - Wireframes.dc.html` — as 20 telas do protótipo.
- `Docs/Trevo - Lotofácil.dc.html` — protótipo interativo (mockup
  navegável) que serve de referência de comportamento e regras de
  negócio (motor de pesos, crenças, modos, ritual dos amuletos,
  validações).

Quando um requisito e este arquivo divergirem, o requisito vence —
e avise, para que este arquivo seja corrigido.
