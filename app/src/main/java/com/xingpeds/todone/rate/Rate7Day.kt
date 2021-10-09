/*
 * Copyright (c) 2021. Kyle McBurnett
 * All rights reserved
 */

package com.xingpeds.todone.rate

import com.xingpeds.todone.Task
import kotlin.math.absoluteValue

fun rate7DayOf(task: Task): TaskRate {
    // std7day needs at least one week to calculate so put escape hatch
    if (task.daysSinceCreated < 8) return Immature(8 - task.daysSinceCreated.toInt())
    // if the standard deviation is larger than the average the lower bound of maintaince range can
    // go negative making it impossible to be in delcine so I am saying the data is too volatile is
    // the std dev is larger than average
    if (task.stdDev7days > task.avgUnitPerWeek) return Volatile()
    val unitsLast7Days = task.unitsInLast7Days.absoluteValue.toFloat()
    return when (unitsLast7Days) {
        in (task.maintianRange7Day) -> Maintaining()
        in Float.NEGATIVE_INFINITY..task.maintianRange7Day.start -> Declining()
        in (task.maintianRange7Day.endInclusive..Float.POSITIVE_INFINITY) -> Accelerating()
        else -> Maintaining() // I'm covering from -infinity to infinity I don't see why the ide is
    // complaining. this line is dead code
    }
}

val Task.maintianRange7Day: ClosedFloatingPointRange<Float>
    get() {
        val lowerMaintBound = avgUnitPerWeek.absoluteValue - stdDev7days.absoluteValue
        val upperMaintBound = avgUnitPerWeek.absoluteValue + stdDev7days.absoluteValue
        return lowerMaintBound..upperMaintBound
    }

fun Task.rateLast7days() = rate7DayOf(this)
