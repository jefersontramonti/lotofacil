package com.trevo.core.engine.crenca

// RF-03.10/03.11: nome, ícone e leitura popular de cada um dos 25 grupos do
// jogo do bicho. Conteúdo tradicional de domínio público, portado fielmente
// do protótipo de referência (Docs/Trevo - Lotofácil.dc.html, array
// BICHOS) — tradição popular, nunca previsão (regra inviolável 2,
// RF-03.13). As dezenas de cada grupo continuam vindo de
// dezenasDoGrupoDoBicho(numero) — esta lista não duplica aquela regra.
data class GrupoDoBicho(
    val numero: Int,
    val nome: String,
    val emoji: String,
    val leituraPopular: String,
)

val GRUPOS_DO_BICHO: List<GrupoDoBicho> =
    listOf(
        GrupoDoBicho(1, "Avestruz", "🦤", "Corrida e fuga. Dizem que anuncia uma decisão que você está adiando."),
        GrupoDoBicho(2, "Águia", "🦅", "Visão de cima. Sinal de que uma oportunidade está sendo enxergada de longe."),
        GrupoDoBicho(3, "Burro", "🐴", "Trabalho teimoso. Lido como aviso de esforço que ainda não deu retorno."),
        GrupoDoBicho(
            4,
            "Borboleta",
            "🦋",
            "Transformação. Dizem que algo antigo está terminando para dar lugar a outro.",
        ),
        GrupoDoBicho(5, "Cachorro", "🐕", "Lealdade. Sonho associado a amizade verdadeira e proteção próxima."),
        GrupoDoBicho(6, "Cabra", "🐐", "Subida difícil. Lido como aviso de caminho íngreme, mas possível."),
        GrupoDoBicho(7, "Carneiro", "🐑", "Rebanho e fartura. Dizem que fala de sustento e de vida em grupo."),
        GrupoDoBicho(8, "Camelo", "🐫", "Travessia longa. Sinal de paciência exigida antes da recompensa."),
        GrupoDoBicho(
            9,
            "Cobra",
            "🐍",
            "O mais comentado nas rodas de jogo. Dizem que avisa de gente falsa por perto.",
        ),
        GrupoDoBicho(10, "Coelho", "🐇", "Multiplicação. Lido como dinheiro que se reproduz depressa."),
        GrupoDoBicho(11, "Cavalo", "🐎", "Força e pressa. Dizem que anuncia notícia que chega rápido."),
        GrupoDoBicho(12, "Elefante", "🐘", "Memória e fortuna. Um dos sonhos mais buscados por quem joga."),
        GrupoDoBicho(13, "Galo", "🐓", "Anúncio. Sinal de que uma novidade vai ser dita em voz alta."),
        GrupoDoBicho(14, "Gato", "🐈", "Independência e mistério. Dizem que pede atenção ao que não está visível."),
        GrupoDoBicho(15, "Jacaré", "🐊", "Espera silenciosa. Lido como perigo parado, que não se move ainda."),
        GrupoDoBicho(16, "Leão", "🦁", "Coragem e comando. Sonho associado a respeito conquistado."),
        GrupoDoBicho(17, "Macaco", "🐒", "Esperteza. Dizem que fala de saída inteligente para um aperto."),
        GrupoDoBicho(18, "Porco", "🐖", "Dinheiro guardado. Um dos sonhos mais ligados a prosperidade."),
        GrupoDoBicho(19, "Pavão", "🦚", "Beleza mostrada. Lido como reconhecimento que chega de fora."),
        GrupoDoBicho(20, "Peru", "🦃", "Festa e mesa cheia. Dizem que anuncia reunião de família."),
        GrupoDoBicho(21, "Touro", "🐂", "Força bruta. Sinal de teimosia que pode ajudar ou atrapalhar."),
        GrupoDoBicho(22, "Tigre", "🐅", "Ataque certeiro. Lido como momento de agir sem hesitar."),
        GrupoDoBicho(23, "Urso", "🐻", "Recolhimento. Dizem que pede descanso antes do próximo passo."),
        GrupoDoBicho(24, "Veado", "🦌", "Leveza e alerta. Sonho associado a fuga rápida de um problema."),
        GrupoDoBicho(25, "Vaca", "🐄", "Abundância mansa. Lido como sustento que não falta."),
    )
