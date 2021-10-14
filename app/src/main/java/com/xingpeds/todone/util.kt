/*
 * Copyright (c) 2021. Kyle McBurnett
 * All rights reserved
 */

package com.xingpeds.todone

import android.util.Log

fun Any.logcat(name: String) = Log.d("quicklog", "$name: ${this.toString()}")
