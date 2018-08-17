package com.github.vassilibykov.hmk

class AlgorithmJTest : InferenceTest() {
    override fun typeOf(expr: Expression, env: Environment): Monotype = env.inferTypeJ(expr, TypeUnifier())
}