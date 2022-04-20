import io.github.rtmigo.precise.*
import java.math.*
import java.text.*
import kotlin.math.*
import kotlin.random.Random

private val plainStringFormat = DecimalFormat(
    "0.0",
    DecimalFormatSymbols.getInstance()) // Locale.ENGLISH
    .also {
        it.maximumFractionDigits = 18

    }

private fun Double.toPlainString(): String = plainStringFormat.format(this)

private fun Double.toSig() = BigDecimal.valueOf(this).round(MathContext(1)).toPlainString()

private fun Double.toPct() =
    if (this.isFinite())
        (abs(this * 100 * 100).toInt() / 100.0).toString() + "%"
    //BigDecimal.valueOf(abs(this)).apply { this.setScale(2, RoundingMode.HALF_UP) }.round( MathContext(2)) .toPlainString()
    else
        "NaN"


private fun Int.toGroupedString(): String =

    DecimalFormat(
        "0.0",
        DecimalFormatSymbols.getInstance()) // Locale.ENGLISH
        .also {
            it.maximumFractionDigits = 0
            it.groupingSize = 3
            it.isGroupingUsed = true

        }.format(this)

fun Double.round4() = BigDecimal.valueOf(this)
    .apply { this.setScale(4, RoundingMode.HALF_UP) }
    .toDouble()

class ComputedError(seed: Int, count: Int) {
    val r = Random(seed)

    // we will sum up to 1,000,000 terms, and we don't want the sum to be too large
    // So we avoid picking too large value for the terms

    val terms = (1..count).map { r.nextDouble(-1E+6, 1E+6).round4() } //
    val ideal = terms.map { BigDecimal.valueOf(it) }.sumOf { it }.toDouble()
    val errorNaive = terms.cascadeSumOf { it } - ideal
    val errorPrecise = terms.preciseSumOf { it } - ideal
//    val errorNaive = terms.sumOf { it } - ideal
//    val errorPrecise = terms.cascadeSumOf { it } - ideal

}

private fun compute(n: Int) {
    val count = 10.0.pow(n).toInt()

    val errors = (1..10).map { ComputedError(seed = it, count = count) }

    val errorNaive = errors.map { it.errorNaive }.average()
    val errorPrecise = errors.map { it.errorPrecise }.average()

    println("${count.toGroupedString()} | ${abs(errorNaive).toSig()} | ${abs(errorPrecise).toSig()} | ${(errorPrecise / errorNaive).toPct()}")
}


fun main() {
    (1..6).forEach { compute(it) }
}