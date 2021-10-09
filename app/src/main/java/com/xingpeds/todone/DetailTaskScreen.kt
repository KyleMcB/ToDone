/*
 * Copyright (c) 2021. Kyle McBurnett
 * All rights reserved
 */

package com.xingpeds.todone

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.xingpeds.todone.rate.maintianRange
import java.util.*
import kotlin.time.ExperimentalTime
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone

@ExperimentalTime
@ExperimentalMaterialApi
@Composable
fun DetailTaskScreen(dataModel: DataModel, navController: NavController, taskId: UUID) {
    val temp = dataModel.list.find { task -> task.id == taskId }
    if (temp == null) {
        Log.d("ugh", "how did I get here?")
        //        navController.popBackStack(mainScreenRoute, true)
        return
    }
    requireNotNull(temp)
    val task: Task = temp
    val scaffoldState = rememberScaffoldState()
    Scaffold(
        scaffoldState = scaffoldState,
        drawerContent = { DrawerContent(navController = navController) },
        topBar = { AppBarNavigation(scaffoldState = scaffoldState) }
    ) {
        val taskName = remember { mutableStateOf(task.name) }
        val taskDescText = remember { mutableStateOf(task.desc.text ?: "") }
        val taskUnits = remember { mutableStateOf(task.unit) }
        val taskDefaultUnits = remember { mutableStateOf(task.defaultAmount) }
        Column {
            DetailTaskScreenTaskName(
                taskName.value,
                onChange = { name ->
                    task.name = name
                    taskName.value = name
                    dataModel.save()
                }
            )

            // TODO add stats
            Divider(modifier = Modifier.padding(10.dp))
            DetailTaskScreenDescription(
                taskDescText.value,
                onChange = { text: String ->
                    taskDescText.value = text
                    task.name = text
                    dataModel.save()
                }
            )
            Divider(modifier = Modifier.padding(10.dp))
            DetailTaskScreenUnits(
                taskUnits.value,
                onChange = { text: String ->
                    taskUnits.value = text
                    task.unit = text
                    dataModel.save()
                }
            )

            Divider(modifier = Modifier.padding(10.dp))
            DetailTaskScreenDefaultAmount(
                taskDefaultUnits.value,
                onChange = { amount: Int ->
                    taskDefaultUnits.value = amount
                    task.defaultAmount = amount
                    dataModel.save()
                }
            )

            Divider(modifier = Modifier.padding(10.dp))
            Text("completed ${task.numOfCompsLastWindow} times in last 7 days")
            Text("total of ${task.unitsInLastWindow} ${task.unit} in last 7 days")
            Text("weekly standard deviation ${task.stdDev}")
            Text("maintanence range ${task.maintianRange}")
            Divider(modifier = Modifier.padding(10.dp))
            Text("Completions")
            LazyColumn(
                modifier = Modifier.padding(10.dp).weight(1f),
                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 5.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(task.toList().sortedByDescending { it.timeStamp }) { comp ->
                    CompletionItem(comp)
                }
            }
            Divider()
            DeleteTaskOption(
                onDelete = {
                    dataModel.deleteTask(task)
                    dataModel.save()
                    navController.navigate(statsListScreenRoute) {
                        popUpTo(statsListScreenRoute) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
fun DetailTaskScreenDefaultAmount(value: Int, onChange: (Int) -> Unit) {
    var editing: Boolean by remember { mutableStateOf(false) }
    var valid: Boolean by remember { mutableStateOf(true) }
    Row() {
        if (editing) {
            TextField(
                value = value.toString(),
                onValueChange = {
                    try {
                        if (it.isBlank()) onChange(0) else onChange(it.toInt())
                        valid = true
                    } catch (e: Throwable) {
                        valid = false
                    }
                },
                placeholder = { Text("Default amount for quick complete") },
                keyboardActions = KeyboardActions(onDone = { editing = false }),
                singleLine = true,
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = !valid
            )
            IconButton(onClick = { editing = false }) {
                Icon(Icons.Default.Done, "finished editing")
            }
        } else {
            Text(
                text = value.toString(),
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { editing = true }) { Icon(Icons.Default.Edit, "edit") }
        }
    }
}

@Composable
fun DetailTaskScreenUnits(name: String, onChange: (String) -> Unit) {
    var editing: Boolean by remember { mutableStateOf(false) }

    Row() {
        if (editing) {
            TextField(
                value = name,
                onValueChange = onChange,
                placeholder = { Text("Miles, Minutes, Calories, etc....") },
                keyboardActions = KeyboardActions(onDone = { editing = false }),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { editing = false }) {
                Icon(Icons.Default.Done, "finished editing")
            }
        } else {
            Text(
                text = name,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { editing = true }) { Icon(Icons.Default.Edit, "edit") }
        }
    }
}

@Composable
fun DeleteTaskOption(onDelete: () -> Unit) {
    var showDialog: Boolean by remember { mutableStateOf(false) }
    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Surface(modifier = Modifier.background(Color.White)) {
                Column() {
                    Text("This can not be undone!!")
                    Row() {
                        TextButton(
                            modifier = Modifier.weight(2f),
                            onClick = {
                                showDialog = false
                                onDelete()
                            }
                        ) { Text("Confirm") }
                        TextButton(
                            modifier = Modifier.weight(1f),
                            onClick = { showDialog = false }
                        ) { Text("Cancel") }
                    }
                }
            }
        }
    }
    TextButton(onClick = { showDialog = true }) { Text("Delete Task") }
}

@Composable
fun DetailTaskScreenDescription(desc: String, onChange: (String) -> Unit) {
    var editing: Boolean by remember { mutableStateOf(false) }
    Row() {
        if (editing) {
            TextField(value = desc, onValueChange = onChange)
            IconButton(onClick = { editing = false }) {
                Icon(Icons.Default.Done, "finished editing")
            }
        } else {

            Text(
                desc,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { editing = true }) { Icon(Icons.Default.Edit, "edit") }
        }
    }
}

@Composable
fun DetailTaskScreenTaskName(name: String, onChange: (String) -> Unit) {
    var editing: Boolean by remember { mutableStateOf(false) }

    Row() {
        if (editing) {
            TextField(
                value = name,
                onValueChange = onChange,
                placeholder = { Text("Task name") },
                keyboardActions = KeyboardActions(onDone = { editing = false }),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { editing = false }) {
                Icon(Icons.Default.Done, "finished editing")
            }
        } else {
            Text(
                text = name,
                fontSize = 30.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { editing = true }) { Icon(Icons.Default.Edit, "edit") }
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun CompletionItem(comp: Completion) {
    // display information about each completion
    Row() {
        CompletionItemDescription(comp.desc, modifier = Modifier.weight(1f))
        CompletionItemTime(comp.timeStamp, modifier = Modifier)
    }
    Divider()
}

@Composable
fun CompletionItemTime(timeStamp: Instant, modifier: Modifier) {
    Column() {
        Text(text = timeStamp.toLocalDateTime(TimeZone.currentSystemDefault()).date.toString())
        val days =
            timeStamp
                .periodUntil(Clock.System.now(), TimeZone.currentSystemDefault())
                .days
                .toString()
        if (days.toInt() == 0) {
            Text("Today")
        } else {
            Text("$days days ago")
        }
    }
}

@Composable
fun CompletionItemDescription(desc: Description, modifier: Modifier) {
    if (desc.text == null) {
        Text(
            "no description",
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Light,
            modifier = modifier
        )
    } else {
        Text(desc.text!!, modifier = modifier)
    }
}
