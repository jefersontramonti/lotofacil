# Áudios necessários — Trevo

Levantamento de todos os efeitos sonoros que o app precisaria para deixar de
ser 100% silencioso. Verificado no código (2026-08-24): não existe nenhum
áudio hoje — sem `MediaPlayer`, `SoundPool`, pasta `res/raw` ou arquivo
`.mp3`/`.ogg`/`.wav` em nenhum módulo. Esta lista cobre todo ponto do app
onde já existe uma animação, revelação, ritual, confirmação ou transição
que se beneficiaria de som.

Tema obrigatório em todos os itens: **remetem a algo místico** (sino,
cristal, moeda, vento, harpa etérea, tilintar de amuleto) — nunca um "ding"
genérico de app. Nomes de arquivo abaixo são sugestão de convenção
(`sfx_` = efeito curto, `loop_` = ambiente em loop), pode ajustar.

## Como usar os prompts no ElevenLabs

Cada som abaixo tem um **Prompt** pronto pra colar no gerador de efeitos
sonoros (elevenlabs.io → Sound Effects). Algumas notas:

- Prompt em **inglês** de propósito — o modelo de som do ElevenLabs responde
  melhor a descrições em inglês do que em português, mesmo o app sendo pt-BR.
- **Duração**: o gerador tem um campo/slider de duração separado do prompt —
  use o valor sugerido na coluna "Duração" (ele também aparece dentro do
  prompt, mas o campo dedicado é mais confiável que o modelo "entender" o
  número escrito no texto).
- Gere 2-3 variações por prompt (o gerador dá resultados diferentes a cada
  vez) e escolha a que soa menos "genérica" — prompts de efeito mágico tendem
  a sair parecido com harpa/sino de tudo quanto é app, então vale iterar.
- Nenhum prompt pede voz/fala — são só efeitos e ambiência (regra 3 abaixo).
- **Formato ao baixar**: MP3 pros 26 efeitos curtos (menor, sobra qualidade
  pra um tilintar de meio segundo, ajuda no limite de 100MB do APK — RNF-01.5).
  WAV só pro `loop_geracao_ambiente` — encoder de MP3 costuma inserir um
  pequeno silêncio no início do arquivo, e isso gera um clique audível toda
  vez que o loop reinicia; WAV não tem esse problema. Android aceita os dois
  formatos direto em `res/raw`, sem conversão.

## Regras que valem para todo áudio desta lista

1. **Nunca a única fonte de informação.** Tudo que o som comunica (acerto,
   revelação, confirmação) já precisa estar 100% claro só no visual — mesmo
   princípio de RNF-03.5 (marcação nunca só por cor), agora estendido a som,
   para não perder paridade de acessibilidade com quem joga sem áudio.
2. **Nunca sugerir que o som/ritual aumenta a chance de acerto** (regra
   inviolável §1.2 do CLAUDE.md). Efeito "triunfante" demais na revelação do
   amuleto ou no acerto da conferência pode soar como profecia cumprida —
   manter tom de celebração leve, nunca de confirmação mística real.
3. Curtos e discretos (a maioria < 1s) — o app é de uso diário, som cansativo
   vira motivo de desinstalar. Nenhuma voz/locução, só efeitos e ambiência.
4. Recomendo (fora do escopo desta lista, é código) adicionar um toggle de
   "som" nas Notificações do Perfil — hoje não existe nenhuma preferência de
   áudio, e ninguém deveria precisar do modo silencioso do aparelho pra isso.

## Divergência encontrada (avisando, não corrigi)

O CLAUDE.md (§1) descreve o ritual como só **3 amuletos** — trevo de quatro
folhas, ferradura, anéis de ouro. O código implementado (`ORDEM_DO_RITUAL`,
`core/engine`) tem **8**: trevo, ferradura, anéis, moedas, bola de cristal,
dados, elefante, estrela. Pela própria regra do CLAUDE.md ("quando um
requisito e este arquivo divergirem, o requisito vence"), a lista abaixo
segue os 8 do código — vale atualizar o CLAUDE.md depois.

---

## 1. Ritual dos Amuletos — o coração místico do app (`TelaRitual.kt`)

O ritual é o diferencial do produto (CLAUDE.md §1), então é onde o áudio
rende mais. Cada amuleto tem um som de identidade próprio ao entrar na tela
de escolha; a revelação (giro + halo, `CirculoDeRevelacao`) é o momento mais
importante do app pra ter som.

### `sfx_amuleto_trevo.mp3`
**Onde/quando:** entra na tela de escolha do 🍀 Trevo da Sorte
**Prompt:** Enchanted four-leaf clover awakening: rustling leaves shimmering with fairy dust sparkle, blooming into a warm glowing lucky chime, swirling arcane energy, magical and radiant, no voice
**Duração:** 1.5s

### `sfx_amuleto_ferradura.mp3`
**Onde/quando:** entra na tela do 🐎 Ferradura da Fortuna
**Prompt:** Enchanted horseshoe charm activating: metallic clink wrapped in shimmering magical sparkle, a faint mystical hoofbeat echo fading into arcane glow, warm folkloric enchantment, no voice
**Duração:** 1.5s

### `sfx_amuleto_aneis.mp3`
**Onde/quando:** entra na tela dos 💍 Anéis de Ouro
**Prompt:** Golden rings enchantment: delicate jewelry clinking blooming into a shimmering arcane sparkle cascade, elegant magical resonance, glowing and radiant, no voice
**Duração:** 1.5s

### `sfx_amuleto_moedas.mp3`
**Onde/quando:** entra na tela das 🪙 Moedas da Prosperidade
**Prompt:** Enchanted coins of prosperity: golden coins falling and clinking, each impact triggering a tiny magical sparkle, swelling into a warm shimmering arcane glow, no voice
**Duração:** 1.5s

### `sfx_amuleto_bola_cristal.mp3`
**Onde/quando:** entra na tela da 🔮 Bola de Cristal
**Prompt:** Crystal ball divination: deep ethereal glass resonance swirling with mystical energy, shimmering otherworldly singing-bowl tone, long magical reverb trailing into arcane whispers of light, no voice
**Duração:** 2s

### `sfx_amuleto_dados.mp3`
**Onde/quando:** entra na tela dos 🎲 Dados do Destino
**Prompt:** Enchanted dice of destiny: dice tumbling wrapped in a light magical shimmer, each bounce sparkling with arcane energy, playful mystical fortune-telling feel, no voice
**Duração:** 1.5s

### `sfx_amuleto_elefante.mp3`
**Onde/quando:** entra na tela do 🐘 Elefante da Fortuna
**Prompt:** Elephant fortune talisman awakening: soft deep enchanted gong swelling with mystical shimmer, a gentle magical elephant call echoing through arcane mist, warm and majestic, no voice
**Duração:** 2s

### `sfx_amuleto_estrela.mp3`
**Onde/quando:** entra na tela da ⭐ Estrela da Sorte
**Prompt:** Lucky star igniting: bright celestial sparkle cascading into a crystalline magical bell, twinkling stardust shimmer trailing into the night sky, radiant and enchanted, no voice
**Duração:** 1.5s

### `sfx_ritual_escolha.mp3`
**Onde/quando:** toque em qualquer `CartaoDeOpcao` (escolha confirmada, antes de saber a dezena)
**Prompt:** Magical choice confirmed: single soft arcane bell chime wrapped in a subtle sparkle, clean enchanted click, no voice
**Duração:** 0.5s

### `sfx_ritual_revelacao.mp3`
**Onde/quando:** entrada no estado `Revelando`, sincronizado com o giro/halo (o som "mágico" principal do app)
**Prompt:** Mystical amulet revelation: swirling magical whoosh building into a radiant shimmering chime bloom, arcane energy blossoming into sparkling light, enchanted and awe-inspiring but gentle — not overpowering or triumphant, no voice
**Duração:** 2s

### `sfx_ritual_refazer.mp3`
**Onde/quando:** botão "↻" no resumo do ritual
**Prompt:** Time-reversing enchantment: soft magical wind spiraling backward, shimmering arcane energy unwinding, mystical undo spell, no voice
**Duração:** 1s

### `sfx_ritual_bloqueado.mp3`
**Onde/quando:** toque num tamanho de fechamento bloqueado sem Pro (16/18/20)
**Prompt:** Sealed enchantment: very short muted magical thud with a faint dim shimmer, soft arcane barrier sound, non-punitive, subtle, no voice
**Duração:** 0.4s

### `sfx_ritual_montar.mp3`
**Onde/quando:** botão final "✦ Montar o palpite" no resumo
**Prompt:** Final spell sealing: warm arcane chime cluster swirling together into a radiant shimmering resolve, magical seal closing with a soft glowing sparkle, subtly triumphant enchantment, no voice
**Duração:** 2s

## 2. TelaGerando — motor de geração (RF-02.9)

Frases que ciclam durante a geração: "Cruzando os atrasados…", "Consultando
a fase da lua…", "Traduzindo o sonho…", "Montando o seu volante…" — com o
ícone do trevo girando (2000ms/volta).

### `loop_geracao_ambiente.wav`
**Onde/quando:** enquanto `TelaGerando` está visível (loop)
**Prompt:** Seamless mystical ambient loop: distant enchanted temple bells, soft shimmering arcane drone, swirling magical mist atmosphere, ethereal and calm, no melody, no voice, loopable without a click
**Duração:** 8-10s (loop)

### `sfx_geracao_frase.mp3`
**Onde/quando:** a cada troca das 4 frases
**Prompt:** Magical page-turn whisper: soft enchanted paper rustle wrapped in a tiny arcane sparkle, mystical divination transition, subtle, no voice
**Duração:** 0.4s

### `sfx_geracao_concluida.mp3`
**Onde/quando:** ao sair de `TelaGerando` com o palpite pronto
**Prompt:** Spell completion chime: warm glowing bell tone blooming with a soft magical sparkle, enchanted and light, no voice
**Duração:** 1s

## 3. Home — sorte do dia, lua, signo, sonho (`TelaHome.kt`)

### `sfx_sonho_confirmar.mp3`
**Onde/quando:** confirmar o grupo do jogo do bicho como sonho do dia
**Prompt:** Dream vision confirmed: soft ethereal bell shimmering with dreamy arcane sparkle, mystical and gentle, non-literal, no voice
**Duração:** 1s

### `sfx_modo_selecionar.mp3`
**Onde/quando:** trocar entre Místico/Cientista/Destino
**Prompt:** Mystical mode selection: soft UI click wrapped in a light arcane shimmer and reverb, magical and neutral, no voice
**Duração:** 0.4s

### `sfx_pull_refresh_puxar.mp3`
**Onde/quando:** início do gesto de puxar pra atualizar (Home e Conferência)
**Prompt:** Magical energy gathering: soft rising enchanted wind whoosh with a faint arcane shimmer, mystical gesture sound, no voice
**Duração:** 0.6s

### `sfx_pull_refresh_concluido.mp3`
**Onde/quando:** fim do refresh (Home e Conferência)
**Prompt:** Enchantment refreshed: short soft magical bell ding with a light sparkle, clean and radiant, no voice
**Duração:** 0.5s

## 4. Conferência de resultado (`TelaConferencia.kt`)

### `sfx_conferencia_resultado_pronto.mp3`
**Onde/quando:** resultado carrega com sucesso
**Prompt:** Mystical result revealed: subtle magical fanfare, warm shimmering arcane chime cluster blooming softly, gently uplifting enchantment, understated, not overly triumphant, no voice
**Duração:** 2s

### `sfx_conferencia_acerto.mp3`
**Onde/quando:** cada dezena marcada como acerto na grade
**Prompt:** Lucky number chime: short bright crystalline magical sparkle tick, radiant and pleasant, no voice
**Duração:** 0.4s

### `sfx_conferencia_grade_confirmar.mp3`
**Onde/quando:** confirmar as 15 dezenas no diálogo de resultado manual
**Prompt:** Grid confirmed enchantment: soft magical bell with a subtle arcane shimmer, clean and neutral, no voice
**Duração:** 0.6s

Sem acertos ou falha de rede: **sem áudio negativo** de propósito — jogo
responsável (CLAUDE.md §10) não deveria soar como punição.

## 5. Onboarding e ações gerais

### `sfx_abertura_boas_vindas.mp3`
**Onde/quando:** primeira tela do app (`TelaAbertura`)
**Prompt:** Mystical welcome chime: warm inviting bell blooming with soft magical sparkle, gentle enchanted greeting, radiant, no voice
**Duração:** 1.5s

### `sfx_crenca_marcar.mp3`
**Onde/quando:** marcar/desmarcar uma crença (`TelaCrencas`, toggle)
**Prompt:** Belief awakened: very short soft tick wrapped in a tiny magical sparkle, subtle arcane toggle, no voice
**Duração:** 0.3s

### `sfx_acao_confirmar.mp3`
**Onde/quando:** copiar texto do palpite, compartilhar — genérico de "feito"
**Prompt:** Enchanted confirmation: clean soft bell tone with a light magical shimmer, neutral and pleasant, no voice
**Duração:** 0.5s

### `sfx_acao_excluir.mp3`
**Onde/quando:** confirmar exclusão de palpite ou exclusão de dados (LGPD)
**Prompt:** Enchantment dissolving: soft magical whoosh fading with a gentle shimmer of dispersing sparkle, calm and non-scary, mystical dismissal, no voice
**Duração:** 1s

---

## Resumo — 27 arquivos ao todo

13 no ritual dos amuletos (8 identidades + escolha + revelação + refazer +
bloqueado + montar) · 3 na geração do palpite · 4 na Home · 3 na
conferência · 4 gerais (onboarding, crenças, confirmar, excluir).
