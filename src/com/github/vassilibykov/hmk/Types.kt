package com.github.vassilibykov.hmk

sealed class Monotype {
    open val freeVariables: Set<String> = setOf()

    open fun apply(subst: Substitution): Monotype = this

    open fun mostGeneralUnifier(other: Monotype): Substitution =
            if (other is TVariable) {
                substitutionWith(other.name, this)
            } else {
                throw InferenceError("types $this and $other do not unify")
            }

    fun substitutionWith(name: String, type: Monotype): Substitution = when {
        type is TVariable -> Substitution.empty
        name in type.freeVariables -> throw InferenceError("free variable check fails for $name in $type")
        else -> Substitution(mapOf(name to type))
    }
}

object TInt : Monotype() {
    override fun mostGeneralUnifier(other: Monotype): Substitution =
        if (other == this) Substitution.empty else super.mostGeneralUnifier(other)

    override fun toString() = "Int"
}

object TBool : Monotype() {
    override fun mostGeneralUnifier(other: Monotype): Substitution =
        if (other == this) Substitution.empty else super.mostGeneralUnifier(other)

    override fun toString() = "Bool"
}

data class TVariable(val name: String) : Monotype() {
    companion object {
        private var serial = 0
        fun generate() = TVariable("a" + serial++)
    }

    override val freeVariables = setOf(name)

    override fun apply(subst: Substitution): Monotype = subst.lookup(name) ?: this

    override fun mostGeneralUnifier(other: Monotype): Substitution = substitutionWith(name, other)

    override fun toString(): String = name
}

data class TFunction(val from: Monotype, val to: Monotype) : Monotype() {
    override val freeVariables = from.freeVariables union to.freeVariables

    override fun apply(subst: Substitution): Monotype = TFunction(from.apply(subst), to.apply(subst))

    override fun mostGeneralUnifier(other: Monotype): Substitution =
        if (other is TFunction) {
            val uFrom = from.mostGeneralUnifier(other.from)
            val uTo = to.apply(uFrom).mostGeneralUnifier(other.to.apply(uFrom))
            uFrom union uTo
        } else {
            super.mostGeneralUnifier(other)
        }

    override fun toString(): String =
            if (from is TFunction) "($from) -> $to"
            else "$from -> $to"
}



