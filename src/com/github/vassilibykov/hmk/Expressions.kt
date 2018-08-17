package com.github.vassilibykov.hmk

sealed class Expression

class IntLiteral(val value: Int) : Expression() {
    override fun toString() = value.toString()
}

class BoolLiteral(val value: Boolean) : Expression() {
    override fun toString() = value.toString()
}

class Variable(val name: String) : Expression() {
    override fun toString() = name
}

class Application(val function: Expression, val argument: Expression) : Expression() {
    override fun toString() = "$function $argument"
}

class Abstraction(val variable: String, val body: Expression) : Expression() {
    override fun toString() = "\u03BB$variable.$body"
}

class Let(val variable: String, val initializer: Expression, val body: Expression) : Expression() {
    override fun toString() = "let $variable = $initializer in $body"
}
