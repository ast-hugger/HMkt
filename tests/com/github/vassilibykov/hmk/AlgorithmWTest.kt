package com.github.vassilibykov.hmk

/**
 * A test of the Algorithm W implementation in [Environment.inferTypeW].
 */
class AlgorithmWTest : InferenceTest() {
    override fun typeOf(expr: Expression, env: Environment) = env.inferTypeW(expr).second
}