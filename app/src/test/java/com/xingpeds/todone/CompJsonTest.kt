/*
 * Copyright (c) 2021. Kyle McBurnett
 * All rights reserved
 */

package com.xingpeds.todoneproto

import com.xingpeds.todone.data.Completion
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test

class CompletionTest {
    @Test
    fun basic() {
        val test = Completion(60)
        val string = Json.encodeToString(test)
        val fromJson = Json.decodeFromString<Completion>(string)
        assert(test == fromJson)
    }
    @ExperimentalTime
    @Test
    fun weekRange() {
        val list =
            listOf<Completion>(
                Completion(30),
                Completion(20, Instant.parse("2020-01-01T00:00:00Z"))
            )
        println(list)
        println(
            list.filter {
                it.timeStamp in (Clock.System.now() - Duration.days(7)..Clock.System.now())
            }
        )
    }
}
