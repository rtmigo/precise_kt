import io.github.rtmigo.precise.*

fun Iterable<Double>.preciseSum() = this.preciseSumOf { it }  // TODO add unit-test
fun Collection<Double>.preciseMean() = this.preciseMeanOf { it }  // TODO add unit-test
fun Collection<Double>.preciseStdevMean() = this.preciseStdevMeanOf { it }  // TODO add unit-test