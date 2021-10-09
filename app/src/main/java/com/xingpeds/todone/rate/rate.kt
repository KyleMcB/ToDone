/*
 * Copyright (c) 2021. Kyle McBurnett
 * All rights reserved
 */

package com.xingpeds.todone.rate

import com.xingpeds.todone.Task
import kotlin.math.absoluteValue

sealed class TaskRate {
    abstract override fun toString(): String
}

class Accelerating : TaskRate() {
    override fun toString() = "Accelerating"
}

class Maintaining : TaskRate() {
    override fun toString() = "Maintaining"
}

class Declining : TaskRate() {
    override fun toString() = "Declining"
}

class Volatile : TaskRate() {
    override fun toString() = "Data is too volatile"
}

class Immature(val days: Int = -1) : TaskRate() {
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
    if (task.stdDev > task.avgUnitPerWindow) return Volatile()
    val unitsInLastWindow = task.unitsInLastWindow.absoluteValue.toFloat()
    return when (unitsInLastWindow) {
        in task.maintianRange -> Maintaining()
        in Float.NEGATIVE_INFINITY..task.lowerMaintianceBound -> Declining()
        in task.upperMaintianceBound..Float.POSITIVE_INFINITY -> Accelerating()
        else -> Maintaining()
    }
}

fun Task.rateLastWindow() = rateOf(this)
