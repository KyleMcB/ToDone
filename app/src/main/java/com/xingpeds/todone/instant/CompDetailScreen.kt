/*
 * Copyright (c) 2021. Kyle McBurnett
 * All rights reserved
 */

package com.xingpeds.todone.instant

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun Instant.toReadableDate(): String {
    val dateTime = this.toLocalDateTime(TimeZone.currentSystemDefault())
    val date = dateTime.date
    return date.toString()
}

fun Instant.toReadbleTime(): String {
    val dateTime = this.toLocalDateTime(TimeZone.currentSystemDefault())
    val hour = dateTime.hour
    val minute = dateTime.minute
    val minuteString = if (minute < 10) "0$minute" else minute.toString()
    return "$hour:$minuteString"
}
