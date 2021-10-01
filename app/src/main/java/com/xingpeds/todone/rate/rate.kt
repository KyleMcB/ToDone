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

fun rate7DayOf(task: Task): TaskRate {
    if (task.stdDev7days > task.avgUnitPerWeek) return Volatile()
    val lowerMaintBound = task.avgUnitPerWeek.absoluteValue - task.stdDev7days.absoluteValue
    val upperMaintBound = task.avgUnitPerWeek.absoluteValue + task.stdDev7days.absoluteValue
    val unitsLast7Days = task.unitsInLast7Days.absoluteValue.toFloat()
    return when (unitsLast7Days) {
        in (lowerMaintBound..upperMaintBound) -> Maintaining()
        in (Float.NEGATIVE_INFINITY..lowerMaintBound) -> Declining()
        in (upperMaintBound..Float.POSITIVE_INFINITY) -> Accelerating()
        else -> Maintaining() // I'm covering from -infinity to infinity I don't see why the ide is
    // complaining. this line is dead code
    }
}

val Task.maintianRange: ClosedFloatingPointRange<Float>
    get() {
        val lowerMaintBound = avgUnitPerWeek.absoluteValue - stdDev7days.absoluteValue
        val upperMaintBound = avgUnitPerWeek.absoluteValue + stdDev7days.absoluteValue
        return lowerMaintBound..upperMaintBound
    }

fun Task.rateLast7days() = rate7DayOf(this)
