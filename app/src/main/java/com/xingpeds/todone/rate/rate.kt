/*
 * Copyright (c) 2021. Kyle McBurnett
 * All rights reserved
 */

package com.xingpeds.todone.rate

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
