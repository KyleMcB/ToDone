/*
 * Copyright (c) 2021. Kyle McBurnett
 * All rights reserved
 */

package com.xingpeds.todone.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.xingpeds.todone.DataModel
import com.xingpeds.todone.data.Completion
import com.xingpeds.todone.data.Task
import com.xingpeds.todone.instant.toReadableDate
import com.xingpeds.todone.instant.toReadbleTime
import com.xingpeds.todone.logcat
import java.lang.NumberFormatException
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime

const val compdetaileditbaseroute = "/compdetaileditscreenroute/"
const val compIdtag = "{compId}"
const val compdetaileditscreenroute = "$compdetaileditbaseroute$tagTaskId/$compIdtag"

fun addressOfCompEditDetialScreen(task: Task, comp: Completion): String {
    return "$compdetaileditbaseroute${task.id}/${comp.timeStamp}".logcat("address")
}

@Composable
fun CompDetailEditScreen(
    dataModel: DataModel,
    navController: NavHostController,
    comp: Completion,
    units: String,
    onTime: (LocalDateTime) -> Unit,
    onDate: (LocalDateTime) -> Unit,
    onDesc: (String) -> Unit,
    onUnits: (Int) -> Unit
) {
    Screen(comp.desc.text, comp.units, units, comp.timeStamp, onDesc = onDesc, onUnits = onUnits)
}

@Composable
private fun Screen(
    desc: String?,
    units: Int,
    unitType: String,
    timeStamp: Instant,
    onDesc: (String) -> Unit = {},
    onUnits: (Int) -> Unit = {}
) {
    var unitString by remember { mutableStateOf(units.toString()) }
    var localUnits by remember { mutableStateOf(units) }
    var unitError: Boolean by remember { mutableStateOf(false) }
    var localDescription: String by remember { mutableStateOf(desc ?: "") }
    val focusManager = LocalFocusManager.current
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.SpaceAround,
        modifier = Modifier.wrapContentWidth()
    ) {
        Spacer(modifier = Modifier.padding(10.dp))
        TextField(
            value = localDescription,
            onValueChange = { localDescription = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth().onFocusChanged { onDesc(localDescription) },
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )
        Spacer(modifier = Modifier.padding(10.dp))

        TextField(
            value = unitString,
            isError = unitError,
            keyboardOptions =
                KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            onValueChange = {
                unitString = it

                unitError =
                    try {
                        val temp: Int = it.toInt()
                        localUnits = temp
                        false
                    } catch (e: NumberFormatException) {
                        true
                    }
            },
            label = { Text(unitType) },
            modifier = Modifier.fillMaxWidth().onFocusChanged { onUnits(localUnits) }
        )
        Spacer(modifier = Modifier.padding(10.dp))

        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Date: ${timeStamp.toReadableDate()}")
            FloatingActionButton(onClick = { /*TODO launch date picker*/}) {
                Icon(Icons.Default.Event, null)
            }
        }
        Spacer(modifier = Modifier.padding(10.dp))

        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Time: ${timeStamp.toReadbleTime()}")
            FloatingActionButton(onClick = { /*TODO launch time picker*/}) {
                Icon(Icons.Default.Schedule, contentDescription = null)
            }
        }
        Spacer(modifier = Modifier.padding(10.dp))
        FloatingActionButton(
            onClick = { /*TODO delete task*/},
            backgroundColor = MaterialTheme.colors.error
        ) { Icon(Icons.Default.DeleteForever, null) }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewComp() {
    Screen(
        desc = "a task description",
        units = 12,
        unitType = "minutes",
        timeStamp = Clock.System.now()
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewCompnoDesc() {
    Screen(desc = null, units = 1, unitType = "walks", timeStamp = Clock.System.now())
}
