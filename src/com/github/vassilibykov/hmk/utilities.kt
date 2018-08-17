package com.github.vassilibykov.hmk

fun <K, V> mergedMaps(a: Map<K, V>, b: Map<K, V>): Map<K, V> {
    val merged = a.toMutableMap()
    merged.putAll(b)
    return merged
}

