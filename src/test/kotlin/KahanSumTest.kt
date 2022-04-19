/**
 * SPDX-FileCopyrightText: (c) 2022 Artёm Galkin <github.com/rtmigo>
 * SPDX-License-Identifier: MIT
 **/

import io.github.rtmigo.summation.*
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.doubles.*
import io.kotest.matchers.shouldBe

import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.math.abs
import kotlin.random.Random
import kotlin.system.measureTimeMillis

private typealias SomethingOf<T> = Iterable<T>.(selector: (T) -> Double) -> Double

class KahanSumTest {

    private fun sumError(
        ourSumOf: SomethingOf<Double>,
        items: List<String>): BigDecimal {

        val idealSum = items.map { BigDecimal(it) }.sumOf { it }
        val ourSum = items.map { it.toDouble() }.ourSumOf { it }

        return (idealSum-BigDecimal.valueOf(ourSum)).abs()
    }

    private fun sumsError(
        ourSumOf: SomethingOf<Double>,
        items: List<List<String>>): BigDecimal = items.map { sumError(ourSumOf, it) }.sumOf { it }

    private fun generateRandomSequences(): List<List<String>> {
        return (1..9000).map { (1..100).map { randomDecimalString() } }
    }

    @Test
    fun kahanMoreAccurateThanNaive() {
        val sequences = generateRandomSequences()
        val errKahan = sumsError(Iterable<Double>::kahanSumOf, sequences)
        val errNaive = sumsError(Iterable<Double>::sumOf, sequences)
        errKahan.shouldBeLessThan(errNaive)
    }

    @Test
    fun kleinMoreAccurateThanNaive() {
        val sequences = generateRandomSequences()
        val errKlein = sumsError(Iterable<Double>::accurateSumOf, sequences)
        val errNaive = sumsError(Iterable<Double>::sumOf, sequences)
        errKlein.shouldBeLessThan(errNaive)
    }

    @Test
    fun kleinMoreAccurateThanKahan() {
        val sequences = generateRandomSequences()
        val errKahan = sumsError(Iterable<Double>::kahanSumOf, sequences)
        val errKlein = sumsError(Iterable<Double>::accurateSumOf, sequences)
        errKlein.shouldBeLessThan(errKahan)
    }

    private fun checkAlphaBetterThanBeta(
        alphaSumOf: SomethingOf<Double>,
        betaSumOf: SomethingOf<Double>,
        intSummands: Iterable<Int>,
        factor: Double = 0.1,
        strict: Boolean,
    ) {
        val floatSummands = intSummands.map { it * factor }

        val idealSum = intSummands.sumOf { it } * factor
        val alphaSum = floatSummands.alphaSumOf { it }
        val betaSum = floatSummands.betaSumOf { it }

        val alphaError = abs(alphaSum - idealSum)
        val betaError = abs(betaSum - idealSum)

        alphaError.shouldBeLessThan(0.000001)
        betaError.shouldBeLessThan(0.000001)

        alphaError.shouldBeLessThan(betaError)
    }

    private fun checkSum(altSumOf: SomethingOf<Double>) {
        listOf<Double>().altSumOf { it }.shouldBe(0.0)
        listOf<Double>(1.0, 2.0, 3.0).altSumOf { it }.shouldBe(6.0)
        listOf<Double>(12.34).altSumOf { it }.shouldBe(12.34)
        listOf<Double>(12.34, 0.01).altSumOf { it }.shouldBe(12.35)

        checkAlphaBetterThanBeta(
            altSumOf,
            Iterable<Double>::sumOf,
            (1..100000).map { Random.nextInt(-1000, 1000) },
            strict = true
        )
    }

    @Test
    fun checkKahan() {
        checkSum(Iterable<Double>::kahanSumOf)
    }

    @Test
    fun checkKlein() {
        checkSum(Iterable<Double>::accurateSumOf)
    }

    @Test
    fun `klein sometimes better than kahan`() {
        // этот пример показывает превосходство алгоритма Клейна:
        // for many sequences of numbers, both algorithms agree, but a simple example due
        // to Peters[11] shows how they can differ. For summing [ 1.0 , + 10 100 , 1.0 , − 10 100 ]
        // [1.0,+10^100,1.0,-10^100] in double precision, Kahan's algorithm yields 0.0, whereas
        // Neumaier's algorithm yields the correct value 2.0.
        val values = listOf(1.0, 1E+100, 1.0, -1E+100)
        values.accurateSumOf { it }.shouldBe(2.0)
        values.kahanSumOf { it }.shouldBe(0.0)

        // в менее драматичном случае такое не происходит
        val x = listOf(1.0, 1E+15, 1.0, -1E+15)
        x.accurateSumOf { it }.shouldBe(2.0)
        x.kahanSumOf { it }.shouldBe(2.0)

        // на самом деле проблема при степени 16
        val y = listOf(1.0, 1E+16, 1.0, -1E+16)
        y.accurateSumOf { it }.shouldBe(2.0)
        y.kahanSumOf { it }.shouldBe(0.0)
    }

    @Test
    fun testKahanMean() {
        listOf<Double>().kahanMeanOf { it }.shouldBe(0.0)
        listOf(1.0, 2.0).kahanMeanOf { it }.shouldBe(1.5)
        listOf(1.0, 2.0, 6.0).kahanMeanOf { it }.shouldBe(3.0)
    }

    @Test
    fun `kahanMean with randoms`() {
        val randoms = (1..100).map { Random.nextDouble(-10.0, 10.0) }
        val naive = randoms.sumOf { it } / randoms.size
        val kahan = randoms.kahanMeanOf { it }
        kahan.shouldBe(naive.plusOrMinus(1E-12))
    }

    @Test
    fun `kahanMean error is smaller than naive`() {
        val doubles = (1..1000).map { 0.1 }

        val naive = doubles.sumOf { it } / doubles.size
        val kahan = doubles.kahanMeanOf { it }

        kahan.shouldBe(0.1.plusOrMinus(1E-12))
        naive.shouldBe(0.1.plusOrMinus(1E-12))

        abs(kahan - 0.1).shouldBeLessThan(abs(naive - 0.1))
    }

    @Test
    fun `MutableKlein same as Klein`() {
        thisIsTheSameAsKlein(::sumByMutableKlein)
    }

    @Test
    fun `ImmutableKlein same as Klein`() {
        thisIsTheSameAsKlein(::sumByImmutableKleinOneByOne)
    }

    @Test
    fun `ImmutableKlein adding iterables same as Klein`() {
        thisIsTheSameAsKlein(::sumByImmutableKleinPlusRandomPortions)
    }

    @Test
    fun `ImmutableKlein adding with minus is same as Klein`() {
        thisIsTheSameAsKlein(::sumByImmutableKleinMinusRandomPortions)
    }


    private fun thisIsTheSameAsKlein(sumFunc: (Iterable<Double>) -> Double) {
        val sequences = generateRandomSequences()

        for (s in sequences) {
            val reference = s.map { it.toDouble() }.accurateSumOf { it }
            val checking = sumFunc(s.map { it.toDouble() })
            checking.shouldBe(reference)
        }
    }

    private fun sumByMutableKlein(s: Iterable<Double>, start: Double = 0.0): Double {
        val mutable = MutableAccurateSum(sum=start)
        mutable.add(s)
        return mutable.toDouble()
    }

    private fun sumByImmutableKleinOneByOne(s: Iterable<Double>, start: Double = 0.0): Double {
        // добавляем Double по одному
        var ks = AccurateSum(sum=start)
        s.forEach { ks+=it }
        return ks.toDouble()
    }

    private fun sumByImmutableKleinPlusRandomPortions(s: Iterable<Double>, start: Double = 0.0): Double {

        // добавляем иногда Double, а иногда Iterable<Double>
        // (иногда даже пустые последовательности)

        var ks = AccurateSum(sum=start)

        val iterator = s.iterator()

        while (iterator.hasNext()) {
            val accumulator = iterator.take(Random.nextInt(0, 10))
            if (accumulator.size==1)
                ks+=accumulator[0]
            else
                ks+=accumulator
        }

        return ks.toDouble()
    }

    private fun <T> Iterator<T>.take(n: Int): List<T> {
        val result = mutableListOf<T>()
        while (result.size<n && this.hasNext()) {
            result.add(this.next())
        }
        return result
    }

    private fun sumByImmutableKleinMinusRandomPortions(s: Iterable<Double>, start: Double = 0.0): Double {

        // добавляем иногда Double, а иногда Iterable<Double>
        // (иногда даже пустые последовательности)

        val negatedItems = s.map { -it }.toList()

        var ks = AccurateSum(sum=start)

        val iterator = negatedItems.iterator()

        while (iterator.hasNext()) {
            val accumulator = iterator.take(Random.nextInt(0, 10))
            if (accumulator.size==1)
                ks-=accumulator[0]
            else
                ks-=accumulator
        }

        return ks.toDouble()
    }

    @Test
    fun `MutableKlein start matter`() {
        sumByMutableKlein(listOf(1.0, 2.5, 7.7), start = 0.0).shouldBe(11.2)
        sumByMutableKlein(listOf(1.0, 2.5, 7.7), start = 1.1).shouldBe(12.3)
    }

    @Test
    fun `MutableKlein faster than creating BigDecimals`() {

        for (attempt in 1..2) {
            val n = 1000
            val seq = (1..1000).map { Random.nextDouble() }

            val t1 = measureTimeMillis {
                for (i in 1..n)
                    sumByMutableKlein(seq)
            }

            val t2 = measureTimeMillis {
                for (i in 1..n) {
                    var sum = BigDecimal.ZERO
                    seq.forEach { sum += BigDecimal.valueOf(it) }
                }
            }

            t1.shouldBeLessThan(t2)
            //println("Klein: $t1 | BigDecimals: $t2")
        }
    }

    @Test
    fun `ImmutableKlein faster than creating BigDecimals`() {

        for (attempt in 1..2) {
            val n = 1000
            val seq = (1..1000).map { Random.nextDouble() }

            val t1 = measureTimeMillis {
                for (i in 1..n)
                    sumByImmutableKleinOneByOne(seq)
            }

            val t2 = measureTimeMillis {
                for (i in 1..n) {
                    var sum = BigDecimal.ZERO
                    seq.forEach { sum += BigDecimal.valueOf(it) }
                }
            }

            t1.shouldBeLessThan(t2)

            //println("Klein: $t1 | BigDecimals: $t2")
        }
    }

    @Test
    fun mutableSumReset() {
        val data = (1..100).map { Random.nextDouble() }

        val running = MutableAccurateSum()
        running.value.shouldBe(0.0)

        running.add(data)
        val sumA = running.value
        running.reset(0.0)
        running.value.shouldBe(0.0)

        running.add(data)
        val sumB = running.value

        sumA.shouldBe(sumB)

        running.reset(1.23)
        running.add(data)
        val sumC = running.value
        sumC.shouldBe((sumB+1.23).plusOrMinus(1E-14))
    }

    @Test
    fun experiment1() {
        val numbers = List(100000) { 0.001 }

        numbers.accurateSumOf { it }.shouldBe(100.0)
        numbers.sumOf { it }.shouldBe(100.00000000011343)
    }

    @Test
    fun experiment2() {
        val numbers = listOf(0.1, 0.2, 0.1, 0.2, 0.1, 0.2, 0.1)
        numbers.sumOf { it }.shouldBe(1.0000000000000002)
        numbers.accurateSumOf { it }.shouldBe(1.0)
    }

}