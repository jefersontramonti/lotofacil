package com.trevo.core.engine.crenca

// RF-02.4: o grupo N puxa a dezena N e a espelhada 26−N. No grupo 13 as
// duas coincidem (26−13 = 13): a dezena aparece uma vez só.
fun dezenasDoGrupoDoBicho(grupo: Int): List<Int> {
    require(grupo in 1..25) { "grupo deve estar entre 1 e 25, era $grupo" }
    return setOf(grupo, 26 - grupo).sorted()
}
