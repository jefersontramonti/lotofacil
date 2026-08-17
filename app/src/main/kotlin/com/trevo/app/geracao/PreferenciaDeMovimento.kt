package com.trevo.app.geracao

import android.content.Context
import android.provider.Settings

// CLAUDE.md §6: animações de decoração (giro do ícone, ritmo das frases)
// respeitam a preferência de "remover animações" do sistema — sem API
// dedicada de Compose para isso no Android, a fonte da verdade é a mesma
// que o próprio sistema usa para escalar/zerar animações de UI.
fun movimentoReduzidoAtivado(context: Context): Boolean =
    Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
