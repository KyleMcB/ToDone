package com.xingpeds.todone

import android.content.Context
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.lang.StringBuilder
import kotlin.time.ExperimentalTime
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@ExperimentalTime
fun _export(file: OutputStream, sourceJson: SourceJson): Unit {
    val stringData = Json.encodeToString(sourceJson)
    file.write(stringData.toByteArray())
}

@ExperimentalTime
fun export(file: OutputStream, source: Source) {
    if (source is SourceJson) {
        _export(file, source)
        return
    }
    val sj: SourceJson = SourceJson().apply { addAll(source) }
    _export(file, sj)
}

@ExperimentalTime
fun import(context: Context, file: InputStream) {
    val persistJsonSource = PersistJsonSource(context)
    val source = _import(file)
    persistJsonSource.save(source)
}

@ExperimentalTime
fun _import(file: InputStream): SourceJson {
    val stringData = readStream(file)
    val source = Json.decodeFromString<SourceJson>(stringData)
    return source
}

private fun readStream(file: InputStream): String {
    val stringBuilder = StringBuilder()
    BufferedReader(InputStreamReader(file)).use { reader ->
        var line: String? = reader.readLine()
        while (line != null) {
            stringBuilder.append(line)
            line = reader.readLine()
        }
    }

    return stringBuilder.toString()
}
