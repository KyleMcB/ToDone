/*
 * Copyright (c) 2021. Kyle McBurnett
 * All rights reserved
 */

package com.xingpeds.todone

import android.content.Context
import java.io.File
import kotlin.time.ExperimentalTime
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

const val FILENAME = "data.json"

@ExperimentalTime
class PersistJsonSource(val context: Context, val filename: String = FILENAME) {
    fun save(source: SourceJson) {
        val fileContents = Json.encodeToString(source)
        val file = File(context.filesDir, filename)
        file.createNewFile()
        file.writeText(fileContents)
    }
    fun load(): SourceJson {
        val file = File(context.filesDir, filename)
        file.createNewFile()
        val fileContents = file.readText()

        if (fileContents.isEmpty()) {
            return SourceJson()
        } else {
            return Json.decodeFromString<SourceJson>(fileContents)
        }
    }
}
