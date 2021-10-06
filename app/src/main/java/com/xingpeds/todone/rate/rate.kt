/*
 * Copyright (c) 2021. Kyle McBurnett
 * All rights reserved
 */

package com.xingpeds.todone.rate

import com.xingpeds.todone.Task
import kotlin.math.absoluteValue

sealed class TaskRate7Day {
    abstract override fun toString(): String
}

class Accelerating : TaskRate7Day() {
    override fun toString() = "Accelerating"
}

class Maintaining : TaskRate7Day() {
    override fun toString() = "Maintaining"
}

class Declining : TaskRate7Day() {
    override fun toString() = "Declining"
}

class Volatile : TaskRate7Day() {
    override fun toString() = "Data is too volatile"
}

class Immature(val days: Int = -1) : TaskRate7Day() {
    override fun toString(): String {
        if (days > -1) return "need $days more days of data to calculate rate"
        return "need at least 2 weeks of data to calculate rate"
    }
}

fun rate7DayOf(task: Task): TaskRate7Day {
    if (task.daysSinceCreated < 14) return Immature(14 - task.daysSinceCreated.toInt())
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
