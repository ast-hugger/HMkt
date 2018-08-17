package com.github.vassilibykov.hmk

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class InferenceTest {
    @Test
    fun `int literal type should be TInt`() {
        assertType(lit(3), TInt)
    }

    @Test
    fun `bool literal type should be TBool`() {
        assertType(lit(true), TBool)
    }

    @Test
    fun `var type should be its bound type`() {
        val env = Environment(mapOf("foo" to TypeScheme(TInt)))
        val (_, type) = env.inferTypeW(variable("foo"))
        assertEquals(TInt, type)
    }

    @Test
    fun `application type should be function return type`() {
        val fType = TFunction(TInt, TBool)
        val env = Environment(mapOf("foo" to TypeScheme(fType)))
        val (_, type) = env.inferTypeW(variable("foo") applyTo lit(3))
        assertEquals(TBool, type)
    }

    @Test
    fun `abstraction type should be function`() {
        val type = typeOf(Abstraction("x", variable("x")))
        assertTrue { type is TFunction }
        val fromType = (type as TFunction).from
        assertTrue { fromType is TVariable }
        assertEquals(fromType, type.to)
    }

    @Test
    fun `let type should be body type`() {
        val expr = Let(
                "x", "y" to variable("y"),
                variable("x") applyTo lit(1))
        assertEquals(TInt, typeOf(expr))
    }

    private fun assertType(expr: Expression, type: Monotype) = assertEquals(type, typeOf(expr))
    private fun typeOf(expr: Expression) = Environment(mapOf()).inferTypeW(expr).second
}

fun lit(value: Int) = IntLiteral(value)
fun lit(value: Boolean) = BoolLiteral(value)
fun variable(name: String) = Variable(name)
private infix fun String.to(body: Expression) = Abstraction(this, body)
infix fun Expression.applyTo (arg: Expression) = Application(this, arg)
