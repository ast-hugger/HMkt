package com.github.vassilibykov.hmk

/**
 * Essentially an implementation of a disjoint set, but geared specifically
 * towards unifying types as required by the Algorithm J. Most importantly,
 * unification is not optimized to minimize the length of child-parent
 * chains as in a generic disjoint set. Instead, when unifying a type
 * variable and a term the term must become the representative.
 */
class TypeUnifier {

    private val nodes = mutableMapOf<Monotype, Node>()

    fun find(type: Monotype): Monotype {
        val node = nodes[type]
        return if (node != null) {
            node.topmostParent().value
        } else {
            nodes[type] = Node(type)
            type
        }
    }

    fun unify(a: Monotype, b: Monotype) {
        val aRep = find(a)
        val bRep = find(b)
        if (aRep == bRep) return
        // The only parametric type in our system is TFunction
        if (aRep is TFunction && bRep is TFunction) {
            unify(aRep.from, bRep.from)
            unify(aRep.to, bRep.to)
        } else if (aRep is TVariable) {
            unifyVar(aRep, bRep)
        } else if (bRep is TVariable) {
            unifyVar(bRep, aRep)
        } else {
            throw InferenceError("types $a and $b do not match")
        }
    }

    private fun unifyVar(typeVar: TVariable, term: Monotype) {
        nodes[typeVar]!!.parent = nodes[term]!!
    }

    private class Node(val value: Monotype) {
        var parent: Node? = null
        fun topmostParent(): Node = parent?.topmostParent() ?: this
    }
}