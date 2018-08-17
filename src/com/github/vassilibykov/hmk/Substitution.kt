package com.github.vassilibykov.hmk

class Substitution(private val replacements: Map<String, Monotype>) {
    companion object {
        val empty = Substitution(mapOf())
    }

    fun lookup(name: String): Monotype? = replacements[name]

    fun without(name: Iterable<String>) = Substitution(replacements.filterKeys { it !in name })

    infix fun union(other: Substitution) = Substitution(mergedMaps(replacements, other.replacements))
}
