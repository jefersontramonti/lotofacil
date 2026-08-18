package com.trevo.core.data.preferencias

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

internal val Context.preferenciasDataStore by preferencesDataStore(name = "trevo_preferencias")
