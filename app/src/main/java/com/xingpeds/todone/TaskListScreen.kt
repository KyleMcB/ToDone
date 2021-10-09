/*
 * Copyright (c) 2021. Kyle McBurnett
 * All rights reserved
 */

package com.xingpeds.todone

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.xingpeds.todone.rate.*
import kotlin.time.ExperimentalTime

const val mainScreenRoute = "mainScreenRoute"

@Composable
fun NewTaskDialog(
    onClose: () -> Unit,
    onCreate: (name: String, description: String, unit: String, defaultAmount: Int) -> Unit
) {
    Dialog(onDismissRequest = onClose) {
        var name: String by remember { mutableStateOf("") }
        var description: String by remember { mutableStateOf("") }
        var unit: String by remember { mutableStateOf("") }
        var amount: Int by remember { mutableStateOf(0) }
        Column(
            Modifier.clip(RoundedCornerShape(5))
                .background(MaterialTheme.colors.background)
                .padding(5.dp)
                .wrapContentSize()
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("name here") },
                label = { Text("Name") }
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("Description") },
                label = { Text("Description") }
            )
            OutlinedTextField(
                value = unit,
                onValueChange = { unit = it },
                label = { Text("Measurement Unit") },
                placeholder = { Text("Minutes, Miles, Meals, Days, etc") }
            )
            val amountText: String = if (amount == 0) "" else amount.toString()
            OutlinedTextField(
                value = amountText,
                onValueChange = { amount = it.toIntOrNull() ?: 0 },
                label = { Text("Measurement default") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            TextButton(
                onClick = {
                    onCreate(name, description, unit, amount)
                    onClose()
                },
                modifier = Modifier.align(Alignment.End).padding(5.dp)
            ) { Text("Create Task") }
        }
    }
}

@Composable
fun ShowCompDialog(mtask: MutableState<Task>, show: MutableState<Boolean>) {
    if (show.value) {
        DetailedTaskCompletionDialog(
            onDismiss = { show.value = false },
            onCreate = { units, description ->
                mtask.value.createCompletion(units, Description(description))
                mtask.value = mtask.value
            }
        )
    }
}

@Composable
fun DetailedTaskCompletionDialog(
    defaultAmount: Int = 0,
    onDismiss: () -> Unit,
    onCreate: (units: Int, description: String?) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        var units: Int by remember { mutableStateOf(defaultAmount) }
        var description: String by remember { mutableStateOf("") }
        Column(
            modifier =
                Modifier.wrapContentSize(Alignment.Center)
                    .background(MaterialTheme.colors.background)
        ) {
            OutlinedTextField(
                value = units.toString(),
                onValueChange = { units = it.toIntOrNull() ?: 0 },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text("Measured Amount") }
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                singleLine = true,
            )
            TextButton(
                onClick = {
                    onCreate(units, if (description.isBlank()) null else description)
                    onDismiss()
                }
            ) { Text("Did it!") }
        }
    }
}

@ExperimentalTime
@ExperimentalMaterialApi
@Composable
fun TaskListScreen(dataModel: DataModel, navController: NavController) {
    val openDialog = remember { mutableStateOf(false) }
    val scaffoldState = rememberScaffoldState()
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = { AppBarNavigation(scaffoldState = scaffoldState) },
        drawerContent = { DrawerContent(navController = navController) },
        floatingActionButton = {
            FloatingActionButton(onClick = { openDialog.value = true }) {
                Icon(imageVector = Icons.Default.Add, null)
            }
        }
    ) {
        if (openDialog.value) {
            NewTaskDialog(onClose = { openDialog.value = false }, onCreate = dataModel::createTask)
        }
        Column() {
            TabRow(selectedTabIndex = dataModel.selectedTab.ordinal) {
                dataModel.tabs.forEachIndexed { index, tabName ->
                    Tab(
                        selected = index == dataModel.selectedTab.ordinal,
                        onClick = {
                            dataModel.onTabSelect(DataModel.SortMethod.valueOf(tabName.uppercase()))
                        },
                        text = { Text(tabName) }
                    )
                }
            }
            LazyColumn(modifier = Modifier.wrapContentSize()) {
                items(dataModel.list.map { mutableStateOf(it, neverEqualPolicy()) }) { stateTask ->
                    val detialCompDialog = mutableStateOf(false)
                    TaskQuickComplete(
                        mtask = stateTask,
                        {
                            stateTask.value.createCompletion()
                            dataModel.save()
                            dataModel.reComposeList()
                        },
                        modifier = Modifier.clickable { detialCompDialog.value = true },
                        dataModel.selectedTab
                    )
                    ShowCompDialog(mtask = stateTask, show = detialCompDialog)
                }
            }
        }
    }
}

@ExperimentalTime
@ExperimentalMaterialApi
@Composable
fun TaskQuickComplete(
    mtask: MutableState<Task>,
    onCreateCompletion: () -> Unit,
    modifier: Modifier = Modifier,
    sortMethod: DataModel.SortMethod
) {
    val task = mtask.value

    val seconday =
        when (sortMethod) {
            DataModel.SortMethod.RATE -> task.rateLast7days().toString()
            DataModel.SortMethod.TIME -> task.lastOrNull()?.desc?.text ?: "no description"
            DataModel.SortMethod.UNITS ->
                "${task.unitsInLast7Days}: ${task.unit} recorded in the last 7 days"
        }
    TaskListItem(
        task,
        {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                Surface(
                    color = MaterialTheme.colors.secondary,
                    shape = CircleShape,
                    modifier =
                        Modifier.size(50.dp).clip(CircleShape).clickable { onCreateCompletion() },
                    elevation = 10.dp
                ) {
                    Box(contentAlignment = Alignment.Center) { Text(task.defaultAmount.toString()) }
                }
            }
        },
        modifier = modifier.shadow(elevation = 3.dp, shape = RoundedCornerShape(20)),
        secondary = { Text(seconday) }
    )
}
