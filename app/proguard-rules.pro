# Room, Hilt, Retrofit, Compose, Play Billing e Play Services Ads já trazem
# suas próprias regras de consumidor (consumer-rules.pro) empacotadas nos
# respectivos AARs — não duplicar aqui.

# kotlinx.serialization precisa que as classes @Serializable do próprio app
# (com.trevo.core.data.resultado.ResultadoDto/RateioDto) e seus serializers
# gerados sobrevivam ao shrink; sem isso, Json.decodeFromString quebra em
# runtime só no release (regra oficial do projeto kotlinx.serialization).
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keep,includedescriptorclasses class com.trevo.**$$serializer { *; }
-keepclassmembers class com.trevo.** {
    *** Companion;
}
-keepclasseswithmembers class com.trevo.** {
    kotlinx.serialization.KSerializer serializer(...);
}
