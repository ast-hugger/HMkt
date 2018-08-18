package com.github.vassilibykov.hmk

/**
 * A mapping of (program, not type) variable names to their types. Classically,
 * the range of the mapping are types in the general sense: both monotypes and
 * type schemes. To simplify the typing, in this implementation the target is
 * always a type scheme, with monotypes represented as type schemes with no
 * quantified variables.
 */
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

    fun with(name: String, type: Monotype) = with(name, TypeScheme(type))

    fun apply(subst: Substitution) = Environment(bindings.mapValues { it.value.apply(subst) })

    infix fun union(other: Environment) = Environment(mergedMaps(bindings, other.bindings))

    fun generalize(type: Monotype) = TypeScheme(type.freeVariables subtract freeVariables, type)

    fun inferTypeW(expr: Expression): Pair<Monotype, Substitution> = when (expr) {
        is IntLiteral -> TInt and Substitution.empty
        is BoolLiteral -> TBool and Substitution.empty
        is Variable -> bindings[expr.name]
                ?.let { it.instantiate() and Substitution.empty }
                ?: throw InferenceError("Unbound variable ${expr.name}")
        is Application -> {
            val a = TVariable.generate()
            val (funType, funSubst) = inferTypeW(expr.function)
            val (argType, argSubst) = apply(funSubst).inferTypeW(expr.argument)
            val mgu = funType.apply(argSubst).mostGeneralUnifier(TFunction(argType, a))
            a.apply(mgu) and (mgu union argSubst union funSubst)
        }
        is Abstraction -> {
            val a = TVariable.generate()
            val env = this.without(expr.variable) union Environment(mapOf(expr.variable to TypeScheme(a)))
            val (bodyType, bodySubst) = env.inferTypeW(expr.body)
            TFunction(a.apply(bodySubst), bodyType) and bodySubst
        }
        is Let -> {
            val (initType, initSubst) = inferTypeW(expr.initializer)
            val genScheme = this.apply(initSubst).generalize(initType)
            val env = this.with(expr.variable, genScheme)
            val (bodyType, bodySubst) = env.apply(initSubst).inferTypeW(expr.body)
            bodyType and (bodySubst union initSubst)
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
            val rType = this.with(expr.variable, a).inferTypeJ(expr.body, unifier)
            TFunction(unifier.find(a), unifier.find(rType)) // I think we need to look up the rep like this for both values
        }
        is Let -> {
            val initType = inferTypeJ(expr.initializer, unifier)
            val genScheme = this.generalize(initType)
            val env = this.with(expr.variable, genScheme)
            env.inferTypeJ(expr.body, unifier)
        }
    }
}

/**
 * Same as [Pair.to], but more readable in this context.
 */
private infix fun<T, U> T.and(that: U): Pair<T, U> = Pair(this, that)

class InferenceError(message: String) : RuntimeException(message)
