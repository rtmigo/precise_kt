import io.github.rtmigo.precise.preciseSumOf
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
        (abs(this*100*100).toInt()/100.0).toString()+"%"
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

fun Double.round1000() = (this*1000).roundToInt()/1000.0

private fun compute(n: Int) {
    val count = 10.0.pow(n).toInt()

    val r = Random(1)

    val summands = (1..count).map {r.nextDouble(-1E+8, 1E+8).round1000() } //
    val ideal = summands.map { BigDecimal.valueOf(it) }.sumOf { it }.toDouble()
    val errorNaive =  summands.sumOf { it } - ideal
    val errorPrecise =  summands.preciseSumOf { it } - ideal
    println("${count.toGroupedString()} | ${abs(errorNaive).toSig()} | ${abs(errorPrecise).toSig()} | ${(errorPrecise/errorNaive).toPct()}")
}


fun main() {
    (1..7).forEach { compute(it) }
}