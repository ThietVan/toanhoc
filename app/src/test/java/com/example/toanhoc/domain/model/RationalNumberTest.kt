package com.example.toanhoc.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class RationalNumberTest {

    @Test
    fun `rational number auto simplifies gcd`() {
        val r = RationalNumber(4, 8)
        assertEquals(1L, r.numerator)
        assertEquals(2L, r.denominator)
        assertEquals("1/2", r.toAcademicString())
    }

    @Test
    fun `rational addition works correctly`() {
        val r1 = RationalNumber(1, 3)
        val r2 = RationalNumber(1, 6)
        val sum = r1 + r2
        assertEquals("1/2", sum.toAcademicString())
    }

    @Test
    fun `rational subtraction works correctly`() {
        val r1 = RationalNumber(3, 4)
        val r2 = RationalNumber(1, 4)
        val diff = r1 - r2
        assertEquals("1/2", diff.toAcademicString())
    }

    @Test
    fun `rational multiplication and division work correctly`() {
        val r1 = RationalNumber(2, 3)
        val r2 = RationalNumber(3, 4)
        val prod = r1 * r2
        val quot = r1 / r2

        assertEquals("1/2", prod.toAcademicString())
        assertEquals("8/9", quot.toAcademicString())
    }

    @Test
    fun `mixed number formatting works correctly`() {
        val r = RationalNumber(7, 3)
        assertEquals("2 1/3", r.toMixedNumberString())
    }
}
