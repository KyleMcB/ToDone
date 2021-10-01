/*
 * Copyright (c) 2021. Kyle McBurnett
 * All rights reserved
 */

package com.xingpeds.todone

import java.io.ByteArrayOutputStream
import kotlin.time.ExperimentalTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

@ExperimentalTime
class ExportImportKtTest {

    @Test
    fun _export() {
        val s0 = SourceJson()
        val s1: SourceJson = SourceJson().apply { createTask("test", Description(), "unit", 10) }
        val test0 = Json.encodeToString(s0)
        val test1 = Json.encodeToString(s1)
        val stream0 = ByteArrayOutputStream()
        val stream1 = ByteArrayOutputStream()
        com.xingpeds.todone._export(stream0, s0)
        com.xingpeds.todone._export(stream1, s1)
        assertEquals(test0, stream0.toString())
        assertEquals(test1, stream1.toString())
    }

    @Test
    fun import() {
        val s1: SourceJson = SourceJson().apply { createTask("test", Description(), "unit", 10) }
        val stringData = Json.encodeToString<SourceJson>(s1).byteInputStream()
        val s1Import = _import(stringData)
        assertEquals(Json.encodeToString(s1Import), Json.encodeToString(s1))
    }
}
