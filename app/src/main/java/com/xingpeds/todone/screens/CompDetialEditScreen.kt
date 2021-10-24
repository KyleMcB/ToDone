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
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.datetime.time.timepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import com.xingpeds.todone.DataModel
import com.xingpeds.todone.data.Completion
import com.xingpeds.todone.data.Task
import com.xingpeds.todone.instant.toReadableDate
import com.xingpeds.todone.instant.toReadbleTime
import com.xingpeds.todone.logcat
import java.lang.NumberFormatException
import kotlinx.datetime.*

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
    onTimeStamp: (LocalDateTime) -> Unit,
    onDesc: (String) -> Unit,
    onUnits: (Int) -> Unit,
    onDelete: () -> Unit
) {
    Screen(
        comp.desc.text,
        comp.units,
        units,
        comp.timeStamp,
        onDesc = onDesc,
        onUnits = onUnits,
        onTimeStamp = onTimeStamp,
        onDelete = onDelete
    )
}

@Composable
private fun Screen(
    desc: String?,
    units: Int,
    unitType: String,
    timeStamp: Instant,
    onDesc: (String) -> Unit = {},
    onUnits: (Int) -> Unit = {},
    onTimeStamp: (LocalDateTime) -> Unit = {},
    onDelete: () -> Unit = {}
) {
    var unitString by remember { mutableStateOf(units.toString()) }
    var localUnits by remember { mutableStateOf(units) }
    var unitError: Boolean by remember { mutableStateOf(false) }
    var localDescription: String by remember { mutableStateOf(desc ?: "") }
    val focusManager = LocalFocusManager.current
    val dateDialog = rememberMaterialDialogState()
    MaterialDialog(
        dialogState = dateDialog,
        buttons = {
            positiveButton("Ok")
            negativeButton("Cancel")
        }
    ) {
        datepicker(
            initialDate =
                timeStamp
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .toJavaLocalDateTime()
                    .toLocalDate()
        ) { date ->
            val newDate = date.toKotlinLocalDate()
            val oldDateTime = timeStamp.toLocalDateTime(TimeZone.currentSystemDefault())
            val combinedDateTime =
                LocalDateTime(
                    year = newDate.year,
                    monthNumber = newDate.monthNumber,
                    dayOfMonth = newDate.dayOfMonth,
                    hour = oldDateTime.hour,
                    minute = oldDateTime.minute,
                    second = oldDateTime.second,
                    nanosecond = oldDateTime.nanosecond
                )
            onTimeStamp(combinedDateTime)
        }
    }

    /* This should be called in an onClick or an Effect */
    //    dateDialog.show()
    val timeDialog = rememberMaterialDialogState()
    MaterialDialog(
        dialogState = timeDialog,
        buttons = {
            positiveButton("Ok")
            negativeButton("Cancel")
        }
    ) {
        timepicker(
            initialTime =
                timeStamp
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .toJavaLocalDateTime()
                    .toLocalTime()
        ) { newTime ->
            val oldDate = timeStamp.toLocalDateTime(TimeZone.currentSystemDefault()).date
            val newDateTime =
                LocalDateTime(
                    year = oldDate.year,
                    monthNumber = oldDate.monthNumber,
                    dayOfMonth = oldDate.dayOfMonth,
                    hour = newTime.hour,
                    minute = newTime.minute,
                    second = newTime.second,
                    nanosecond = newTime.nano
                )
            onTimeStamp(newDateTime)
        }
    }
    var edited by remember { mutableStateOf(false) }
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
            modifier =
                Modifier.fillMaxWidth().onFocusChanged {
                    if (edited) {
                        onDesc(localDescription)
                        edited = false
                    }
                },
            keyboardActions =
                KeyboardActions(
                    onDone = {
                        edited = true
                        focusManager.clearFocus()
                    }
                ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )
        Spacer(modifier = Modifier.padding(10.dp))

        TextField(
            value = unitString,
            isError = unitError,
            keyboardOptions =
                KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions =
                KeyboardActions(
                    onDone = {
                        edited = true
                        focusManager.clearFocus()
                    }
                ),
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
            modifier =
                Modifier.fillMaxWidth().onFocusChanged {
                    if (edited) {
                        onUnits(localUnits)
                        edited = false
                    }
                }
        )
        Spacer(modifier = Modifier.padding(10.dp))

        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Date: ${timeStamp.toReadableDate()}")
            FloatingActionButton(
                onClick = { /*TODO launch date picker*/
                    dateDialog.show()
                }
            ) { Icon(Icons.Default.Event, null) }
        }
        Spacer(modifier = Modifier.padding(10.dp))

        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Time: ${timeStamp.toReadbleTime()}")
            FloatingActionButton(onClick = { timeDialog.show() }) {
                Icon(Icons.Default.Schedule, contentDescription = null)
            }
        }
        Spacer(modifier = Modifier.padding(10.dp))
        var deleteDialog: Boolean by remember { mutableStateOf(false) }
        FloatingActionButton(
            onClick = { deleteDialog = true },
            backgroundColor = MaterialTheme.colors.error
        ) { Icon(Icons.Default.DeleteForever, null) }
        if (deleteDialog) {
            Dialog(onDismissRequest = { deleteDialog = false }) {
                Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colors.error) {
                    Column() {
                        Text("Delete the completion?")
                        Row(horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { deleteDialog = false }) { Text("Cancel") }
                            TextButton(onClick = { onDelete() }) { Text("Delete") }
                        }
                    }
                }
            }
        }
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
