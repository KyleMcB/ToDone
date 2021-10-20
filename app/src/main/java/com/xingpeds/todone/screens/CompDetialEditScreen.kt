/*
 * Copyright (c) 2021. Kyle McBurnett
 * All rights reserved
 */

package com.xingpeds.todone.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.xingpeds.todone.DataModel
import com.xingpeds.todone.data.Completion
import com.xingpeds.todone.instant.toReadableDate
import com.xingpeds.todone.instant.toReadbleTime
import java.lang.NumberFormatException
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime

const val compdetaileditpartial = "/compdetaileditscreenroute/"
const val compIdtag = "{compId}"
const val compdetaileditscreenroute = "$compdetaileditpartial$tagTaskId/$compIdtag"

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
) {}

@Composable
private fun Screen(desc: String?, units: Int, unitType: String, timeStamp: Instant) {
    var unitString by remember { mutableStateOf(units.toString()) }
    var error: Boolean by remember { mutableStateOf(false) }
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.SpaceAround,
        modifier = Modifier.wrapContentWidth()
    ) {
        Spacer(modifier = Modifier.padding(10.dp))
        TextField(value = desc ?: "", onValueChange = {}, label = { Text("Description") })
        Spacer(modifier = Modifier.padding(10.dp))

        TextField(
            value = unitString,
            onValueChange = {
                unitString = it
                try {
                    val temp: Int = it.toInt()
                    error = false
                    // TODO call onchange...
                } catch (e: NumberFormatException) {
                    error = true
                }
            },
            label = { Text(unitType) }
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
