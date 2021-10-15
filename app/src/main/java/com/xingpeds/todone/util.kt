/*
 * Copyright (c) 2021. Kyle McBurnett
 * All rights reserved
 */

package com.xingpeds.todone

import android.util.Log

fun <T> T.logcat(name: String): T {
    Log.d("quicklog", "$name: ${this.toString()}")
    return this
}
