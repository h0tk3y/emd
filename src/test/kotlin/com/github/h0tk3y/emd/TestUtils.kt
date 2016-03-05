package com.github.h0tk3y.emd

import java.util.*

val random = Random()

fun Random.intsSequence() = generateSequence { nextInt() }
fun Random.intsSequence(bound: Int) = generateSequence { nextInt(bound) }

