package com.trevo.app

import android.graphics.Color.TRANSPARENT
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.trevo.app.navegacao.TrevoNavHost
import com.trevo.core.ui.TrevoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Tema do Trevo é escuro fixo (Theme.kt), não segue o modo do
        // sistema — por isso o estilo das barras também é fixo em vez de
        // SystemBarStyle.auto (que decidiria ícones claros/escuros pelo
        // night mode do aparelho e contradiria o tema travado).
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(TRANSPARENT),
        )
        setContent {
            TrevoTheme {
                TrevoNavHost()
            }
        }
    }
}
