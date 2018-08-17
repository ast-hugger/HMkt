package com.github.vassilibykov.hmk

class Environment(private val bindings: Map<String, TypeScheme>) {

    val freeVariables: Set<String> by lazy {
        val union = mutableSetOf<String>()
        for (value in bindings.values) {
            union.addAll(value.freeVariables)
        }
        union
    }

    fun without(name: String): Environment {
        val copy = bindings.toMutableMap()
        copy.remove(name)
        return Environment(copy)
    }

    fun with(name: String, scheme: TypeScheme): Environment {
        val copy = bindings.toMutableMap()
        copy[name] = scheme
        return Environment(copy)
    }

    fun apply(subst: Substitution) = Environment(bindings.mapValues { it.value.apply(subst) })

    infix fun union(other: Environment) = Environment(mergedMaps(bindings, other.bindings))

    fun generalize(type: Monotype) = TypeScheme(type.freeVariables subtract freeVariables, type)

    fun inferTypeW(expr: Expression): Pair<Substitution, Monotype> = when (expr) {
        is IntLiteral -> Substitution.empty to TInt
        is BoolLiteral -> Substitution.empty to TBool
        is Variable -> bindings[expr.name]
                ?.let { Substitution.empty to it.instantiate() }
                ?: throw InferenceError("Unbound variable ${expr.name}")
        is Application -> {
            val a = TVariable.generate()
            val (s1, t1) = inferTypeW(expr.function)
            val (s2, t2) = apply(s1).inferTypeW(expr.argument)
            val mgu = t1.apply(s2).mostGeneralUnifier(TFunction(t2, a))
            (mgu union s2 union s1) to a.apply(mgu)
        }
        is Abstraction -> {
            val a = TVariable.generate()
            val env = without(expr.variable) union Environment(mapOf(expr.variable to TypeScheme(a)))
            val (bodySubst, bodyType) = env.inferTypeW(expr.body)
            bodySubst to TFunction(a.apply(bodySubst), bodyType)
        }
        is Let -> {
            val (initSubst, initType) = inferTypeW(expr.initializer)
            val genScheme = this.apply(initSubst).generalize(initType)
            val env = this.without(expr.variable).with(expr.variable, genScheme)
            val (bodySubst, bodyType) = env.apply(initSubst).inferTypeW(expr.body)
            (bodySubst union initSubst) to bodyType
        }
    }

    fun inferTypeJ(expr: Expression, unifier: TypeUnifier): Monotype = when (expr) {
        is IntLiteral -> TInt
        is BoolLiteral -> TBool
        is Variable -> bindings[expr.name]?.instantiate() ?: throw InferenceError("Unbound variable ${expr.name}")
        is Application -> {
            val fType = inferTypeJ(expr.function, unifier)
            val aType = inferTypeJ(expr.argument, unifier)
            val a = TVariable.generate()
            unifier.unify(fType, TFunction(aType, a))
            unifier.find(a)
        }
        is Abstraction -> {
            val a = TVariable.generate()
            val rType = this.with(expr.variable, TypeScheme(a)).inferTypeJ(expr.body, unifier)
            // TODO do we need to look up the representative of these?
            TFunction(a, rType)
        }
        is Let -> {
            val initType = inferTypeJ(expr.initializer, unifier)
            val genScheme = this.generalize(initType)
            val env = this.with(expr.variable, genScheme)
            env.inferTypeJ(expr.body, unifier)
        }
    }
}

class InferenceError(message: String) : RuntimeException(message)
