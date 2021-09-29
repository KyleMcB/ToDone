package com.xingpeds.todone.rate

import com.xingpeds.todone.Task

sealed class TaskRate

class Accelerating : TaskRate()

class Maintaining : TaskRate()

class Declining : TaskRate()

fun rate7DayOf(task: Task): TaskRate {
    TODO("need to make sure comps stay in order")
}
