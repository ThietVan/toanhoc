package com.example.toanhoc.domain.model

import kotlin.math.abs

/**
 * Immutable Rational Number class supporting GCD simplification, arithmetic, and academic string formatting.
 */
class RationalNumber(num: Long, den: Long = 1L) : Comparable<RationalNumber> {

    val numerator: Long
    val denominator: Long

    init {
        require(den != 0L) { "Denominator cannot be zero" }

        val sign = if ((num < 0 && den > 0) || (num > 0 && den < 0)) -1L else 1L
        val absNum = abs(num)
        val absDen = abs(den)
        val g = gcd(absNum, absDen)

        this.numerator = sign * (absNum / g)
        this.denominator = absDen / g
    }

    operator fun plus(other: RationalNumber): RationalNumber {
        val newNum = this.numerator * other.denominator + other.numerator * this.denominator
        val newDen = this.denominator * other.denominator
        return RationalNumber(newNum, newDen)
    }

    operator fun minus(other: RationalNumber): RationalNumber {
        val newNum = this.numerator * other.denominator - other.numerator * this.denominator
        val newDen = this.denominator * other.denominator
        return RationalNumber(newNum, newDen)
    }

    operator fun times(other: RationalNumber): RationalNumber {
        val newNum = this.numerator * other.numerator
        val newDen = this.denominator * other.denominator
        return RationalNumber(newNum, newDen)
    }

    operator fun div(other: RationalNumber): RationalNumber {
        require(other.numerator != 0L) { "Cannot divide by zero rational number" }
        val newNum = this.numerator * other.denominator
        val newDen = this.denominator * other.numerator
        return RationalNumber(newNum, newDen)
    }

    fun toMixedNumberString(): String {
        if (denominator == 1L) return "$numerator"
        val whole = numerator / denominator
        val rem = abs(numerator % denominator)

        return when {
            whole == 0L -> "$numerator/$denominator"
            rem == 0L -> "$whole"
            else -> "$whole $rem/$denominator"
        }
    }

    fun toDouble(): Double = numerator.toDouble() / denominator.toDouble()

    fun toAcademicString(): String {
        return if (denominator == 1L) "$numerator" else "$numerator/$denominator"
    }

    override fun compareTo(other: RationalNumber): Int {
        val diff = this.numerator * other.denominator - other.numerator * this.denominator
        return diff.compareTo(0L)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RationalNumber) return false
        return numerator == other.numerator && denominator == other.denominator
    }

    override fun hashCode(): Int {
        var result = numerator.hashCode()
        result = 31 * result + denominator.hashCode()
        return result
    }

    override fun toString(): String = toAcademicString()

    companion object {
        fun gcd(a: Long, b: Long): Long {
            var x = abs(a)
            var y = abs(b)
            while (y != 0L) {
                val temp = y
                y = x % y
                x = temp
            }
            return if (x == 0L) 1L else x
        }
    }
}