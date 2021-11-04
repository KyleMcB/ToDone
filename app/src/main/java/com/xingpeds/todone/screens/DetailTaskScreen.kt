/*
 * Copyright (c) 2021. Kyle McBurnett
 * All rights reserved
 */

package com.xingpeds.todone

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.xingpeds.todone.composables.NumberOutlinedTextField
import com.xingpeds.todone.data.Completion
import com.xingpeds.todone.data.Description
import com.xingpeds.todone.data.Task
import com.xingpeds.todone.data.regularity
import kotlin.math.roundToInt
import kotlin.time.ExperimentalTime
import kotlinx.datetime.*

@ExperimentalTime
@ExperimentalMaterialApi
@Composable
fun DetailTaskScreen(dataModel: DataModel, navController: NavController, task: Task) {

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
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxHeight()
        ) {
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
            DetailTaskDaysWindow(
                task.daysWindow,
                onChange = { amount ->
                    task.daysWindow = amount
                    dataModel.save()
                }
            )
            Divider(modifier = Modifier.padding(10.dp))

            Text("completed ${task.numOfCompsLastWindow} times in last ${task.daysWindow} days")
            Text("total of ${task.unitsInLastWindow} ${task.unit} in last ${task.daysWindow} days")
            Text("Regularity ${task.regularity.roundToInt()}%")
            Divider(modifier = Modifier.padding(10.dp))
            // List of completions takes too much room either move editing some where else or more
            // completions somewhere else
            //            Text("Completions")
            //            LazyColumn(
            //                modifier = Modifier.padding(10.dp).weight(1f),
            //                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 5.dp),
            //                verticalArrangement = Arrangement.spacedBy(4.dp)
            //            ) {
            //                items(task.toList().sortedByDescending { it.timeStamp }) { comp ->
            //                    CompletionItem(comp)
            //                }
            //            }
            //            Divider()
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
fun DetailTaskDaysWindow(daysWindow: Int, onChange: (Int) -> Unit) {
    var enabled by remember { mutableStateOf(false) }
    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
        NumberOutlinedTextField(daysWindow, enabled, onChange, "Window length (days)")
        if (enabled) {
            IconButton(onClick = { enabled = !enabled }) { Icon(Icons.Default.Save, "save") }
        } else {
            IconButton(onClick = { enabled = !enabled }) {
                Icon(Icons.Default.Edit, "Edit window length")
            }
        }
    }
}

@Composable
fun DetailTaskScreenDefaultAmount(value: Int, onChange: (Int) -> Unit) {
    var enabled: Boolean by remember { mutableStateOf(false) }
    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
        NumberOutlinedTextField(
            number = value,
            enable = enabled,
            onChange = onChange,
            "Amount for quick complete"
        )
        IconButton(onClick = { enabled = !enabled }) {
            if (enabled) {
                Icon(Icons.Default.Save, null)
            } else {
                Icon(Icons.Default.Edit, null)
            }
        }
    }
}

@Composable
fun DetailTaskScreenUnits(name: String, onChange: (String) -> Unit) {
    var enable: Boolean by remember { mutableStateOf(false) }

    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = name,
            onValueChange = onChange,
            placeholder = { Text("Miles, Minutes, Calories, etc....") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { enable = false }),
            singleLine = true,
            label = { Text("Unit of measurement") },
            enabled = enable
        )
        IconButton(onClick = { enable = !enable }) {
            if (enable) {
                Icon(Icons.Default.Save, null)
            } else {
                Icon(Icons.Default.Edit, null)
            }
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
                        Button(modifier = Modifier.weight(1f), onClick = { showDialog = false }) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
    TextButton(onClick = { showDialog = true }) { Text("Delete Task") }
}

@Composable
fun DetailTaskScreenDescription(desc: String, onChange: (String) -> Unit) {
    var enable: Boolean by remember { mutableStateOf(false) }
    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = desc,
            onValueChange = onChange,
            label = { Text("Description of task") },
            enabled = enable,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { enable = false })
        )
        IconButton(onClick = { enable = !enable }) {
            if (enable) {
                Icon(Icons.Default.Done, null)
            } else Icon(Icons.Default.Edit, null)
        }
    }
}

@Composable
fun DetailTaskScreenTaskName(name: String, onChange: (String) -> Unit) {
    var enable: Boolean by remember { mutableStateOf(false) }

    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = onChange,
            placeholder = { Text("Task name") },
            keyboardActions = KeyboardActions(onDone = { enable = false }),
            singleLine = true,
            label = { Text("Task Name") },
            enabled = enable
        )

        IconButton(onClick = { enable = !enable }) {
            if (enable) Icon(Icons.Default.Save, null) else Icon(Icons.Default.Edit, null)
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
