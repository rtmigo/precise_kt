/*
 * SPDX-FileCopyrightText: (c) 2022 Artsiom iG <github.com/rtmigo>
 * SPDX-License-Identifier: MIT
 */

@file:JvmName("Precise")
@file:JvmMultifileClass
package io.github.rtmigo.precise

@ExperimentalApi
fun Iterable<Double>.preciseSum() = this.preciseSumOf { it }  // TODO add unit-test

@ExperimentalApi
fun Collection<Double>.preciseMean() = this.preciseMeanOf { it }  // TODO add unit-test

@ExperimentalApi
fun Collection<Double>.preciseStdevMean() = this.preciseStdevMeanOf { it }  // TODO add unit-test