/*
 * Copyright (c) 2021. Kyle McBurnett
 * All rights reserved
 */

package com.xingpeds.todone.rate

import com.xingpeds.todone.Task
import kotlin.math.absoluteValue

val Task.maintianRange30Day: ClosedFloatingPointRange<Float>
    get() {
        val lowerMaintBound = avgUnitPer30Days.absoluteValue - stdDev30days.absoluteValue
        val upperMaintBound = avgUnitPer30Days.absoluteValue + stdDev30days.absoluteValue
        return lowerMaintBound..upperMaintBound
    }

fun rate30DayOf(task: Task): TaskRate {
    // escape if less than 30 days
    if (task.daysSinceCreated < 31) return Immature(31 - task.daysSinceCreated.toInt())
    // escape if too volatile
    if (task.stdDev30days > task.avgUnitPer30Days) return Volatile()
    val unitsInLast30Days = task.unitsInLast30Days.absoluteValue.toFloat()
    return when (unitsInLast30Days) {
        in (task.maintianRange30Day) -> Maintaining()
        in Float.NEGATIVE_INFINITY..task.maintianRange30Day.start -> Declining()
        in task.maintianRange30Day.endInclusive..Float.POSITIVE_INFINITY -> Accelerating()
        else -> Maintaining()
    }
}

fun Task.rateLast30Days() = rate30DayOf(this)
