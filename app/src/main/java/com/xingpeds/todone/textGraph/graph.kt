/*
 * Copyright (c) 2021. Kyle McBurnett
 * All rights reserved
 */

package com.xingpeds.todone.textGraph

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import kotlin.math.absoluteValue

@Composable
fun TextGraph(length: Int, current: Float, avg: Float, stdDev: Float) {
    val _avg = avg.absoluteValue
    val _stdDev = stdDev.absoluteValue
    val _current = current.absoluteValue
    require(length > 1)
    if (stdDev > avg) return
    val line = MutableList<Char>(length) { '_' }
    line[0] = '['
    line[line.lastIndex] = ']'
    val _length = length - 1
    // I need half way of the line to be the avg
    // avg * x = length/2
    // x = (length/2)/avg
    // ratio == x
    val ratio: Float = ((_length / 2) / _avg)
    val lowerBoundIndex: Int = ((_avg - _stdDev).toInt() * ratio).toInt()
    val upperBoundIndex: Int = ((_avg + _stdDev).toInt() * ratio).toInt()
    val currentIndex: Int = (_current * ratio).toInt()
    // set the center to something? not sure
    //    line[_length / 2] = '|'
    line[lowerBoundIndex] = '('
    line[upperBoundIndex] = ')'
    line[currentIndex] = '*'
    Text(
        line.joinToString(
            separator = "",
        )
    )
}

@Composable
fun TextLine(content: @Composable (Int) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        var charWidth by remember { mutableStateOf<Int?>(null) }

        charWidth?.let {
            ProvideTextStyle(
                TextStyle(fontFamily = FontFamily.Monospace),
                { content(constraints.maxWidth / it) }
            )
        }
        // not drawn to screen because of modifier, only used to measure width of character
        Text(
            "H",
            onTextLayout = { charWidth = it.size.width },
            modifier = Modifier.drawWithContent {},
            fontFamily = FontFamily.Monospace
        )
    }
}
