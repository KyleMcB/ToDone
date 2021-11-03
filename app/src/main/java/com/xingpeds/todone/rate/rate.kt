/*
 * Copyright (c) 2021. Kyle McBurnett
 * All rights reserved
 */

package com.xingpeds.todone.rate

import com.xingpeds.todone.data.Task
import com.xingpeds.todone.data.regularity
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

sealed abstract class TaskRate(regularity: Float) {
    abstract override fun toString(): String
}

class Accelerating(val regularity: Float) : TaskRate(regularity) {
    override fun toString() = "Accelerating reg: ${regularity.roundToInt()}%"
}

class Maintaining(val regularity: Float) : TaskRate(regularity) {
    override fun toString() = "Maintaining reg: ${regularity.roundToInt()}%"
}

class Declining(val regularity: Float) : TaskRate(regularity) {
    override fun toString() = "Declining reg: ${regularity.roundToInt()}%"
}

class Volatile(val regularity: Float) : TaskRate(regularity) {
    override fun toString() =
        "Try doing this task more consistently reg: ${regularity.roundToInt()}%"
}

class Immature(val days: Int = -1) : TaskRate(0f) {
    override fun toString(): String {
        if (days > -1) return "need $days more days of data to calculate rate"
        return "Not enough data to calculate rate"
    }
}

val Task.lowerMaintianceBound
    get() = avgUnitPerWindow.absoluteValue.toFloat() - stdDev.absoluteValue.toFloat()
val Task.upperMaintianceBound
    get() = avgUnitPerWindow.absoluteValue.toFloat() + stdDev.absoluteValue.toFloat()
val Task.maintianRange: ClosedFloatingPointRange<Float>
    get() = lowerMaintianceBound..upperMaintianceBound

fun rateOf(task: Task): TaskRate {
    if (task.daysSinceCreated < task.daysWindow + 1)
        return Immature(1 + task.daysWindow - task.daysSinceCreated.toInt())
    if (task.stdDev > task.avgUnitPerWindow) return Volatile(task.regularity)
    val unitsInLastWindow = task.unitsInLastWindow.absoluteValue.toFloat()
    return when (unitsInLastWindow) {
        in task.maintianRange -> Maintaining(task.regularity)
        in Float.NEGATIVE_INFINITY..task.lowerMaintianceBound -> Declining(task.regularity)
        in task.upperMaintianceBound..Float.POSITIVE_INFINITY -> Accelerating(task.regularity)
        else -> Maintaining(task.regularity)
    }
}

fun Task.rateLastWindow() = rateOf(this)
