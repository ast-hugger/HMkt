package com.github.vassilibykov.hmk

/**
 * Logically, a mapping of type variable names to their replacement types.
 * Used by the Algorithm W to essentially do the job of [TypeUnifier] in a
 * side effect-free world.
 */
class Substitution(private val replacements: Map<String, Monotype>) {
    companion object {
        val empty = Substitution(mapOf())
    }

    fun lookup(name: String) = replacements[name]

    fun without(name: Iterable<String>) = Substitution(replacements.filterKeys { it !in name })

    infix fun union(other: Substitution) = Substitution(mergedMaps(replacements, other.replacements))
}
