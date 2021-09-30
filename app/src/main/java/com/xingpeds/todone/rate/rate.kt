package com.xingpeds.todone.rate

import com.xingpeds.todone.Task
import kotlin.math.absoluteValue

sealed class TaskRate

class Accelerating : TaskRate()

class Maintaining : TaskRate()

class Declining : TaskRate()

fun rate7DayOf(task: Task): TaskRate {
    val lowerMaintBound = task.avgUnitPerWeek.absoluteValue - task.stdDev7days.absoluteValue
    val upperMaintBound = task.avgUnitPerWeek.absoluteValue + task.stdDev7days.absoluteValue
    val unitsLast7Days = task.unitsInLast7Days.absoluteValue.toFloat()
    return when (unitsLast7Days) {
        in (lowerMaintBound..upperMaintBound) -> Maintaining()
        in (Float.NEGATIVE_INFINITY..lowerMaintBound) -> Declining()
        in (upperMaintBound..Float.POSITIVE_INFINITY) -> Accelerating()
        else -> Maintaining()
    }
}

fun Task.rateLast7days() = rate7DayOf(this)
