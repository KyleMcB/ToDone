package com.xingpeds.todoneproto

import com.xingpeds.measurethyself.CompJson
import com.xingpeds.measurethyself.Description
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertNotEquals
import org.junit.Test

class CompJsonTest {
    @Test
    fun basic() {
        val test = CompJson(60)
        val string = Json.encodeToString(test)
        val fromJson = Json.decodeFromString<CompJson>(string)
        assert(test == fromJson)
    }
    @ExperimentalTime
    @Test
    fun weekRange() {
        val list =
            listOf<CompJson>(CompJson(30), CompJson(20, Instant.parse("2020-01-01T00:00:00Z")))
        println(list)
        println(
            list.filter {
                it.timeStamp in (Clock.System.now() - Duration.days(7)..Clock.System.now())
            }
        )
    }

}
