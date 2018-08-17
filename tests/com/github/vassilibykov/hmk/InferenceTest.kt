package com.github.vassilibykov.hmk

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

abstract class InferenceTest {
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
        val type = typeOf(variable("foo"), Environment(mapOf("foo" to TypeScheme(TInt))))
        assertEquals(TInt, type)
    }

    @Test
    fun `application type should be function return type`() {
        val fType = TFunction(TInt, TBool)
        val type = typeOf(variable("foo") applyTo lit(3), Environment(mapOf("foo" to TypeScheme(fType))))
        assertEquals(TBool, type)
    }

    @Test
    fun `abstraction type should be function`() {
        assertFunAtoA(typeOf(Abstraction("x", variable("x"))))
    }

    @Test
    fun `let type should be body type`() {
        val expr = Let(
                "x", "y" to variable("y"),
                variable("x") applyTo lit(1))
        assertEquals(TInt, typeOf(expr))
    }

    @Test
    fun `let bound identity function`() {
        val expr = Let(
                "id", "x" to variable("x"),
                variable("id"))
        assertFunAtoA(typeOf(expr))
    }

    /**
     * A subclass must override this method to infer the type of the given
     * expression in the given type environment.
     */
    protected abstract fun typeOf(expr: Expression, env: Environment): Monotype

    protected fun typeOf(expr: Expression) = typeOf(expr, Environment(mapOf()))

    protected fun assertType(expr: Expression, type: Monotype) = assertEquals(type, typeOf(expr))
    protected fun assertFunAtoA(type: Monotype) {
        assertTrue { type is TFunction }
        val fromType = (type as TFunction).from
        assertTrue { fromType is TVariable }
        assertEquals(fromType, type.to)
    }
}

fun lit(value: Int) = IntLiteral(value)
fun lit(value: Boolean) = BoolLiteral(value)
fun variable(name: String) = Variable(name)
private infix fun String.to(body: Expression) = Abstraction(this, body)
infix fun Expression.applyTo (arg: Expression) = Application(this, arg)
