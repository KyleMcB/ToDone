/*
 * Copyright (c) 2021. Kyle McBurnett
 * All rights reserved
 */

package com.xingpeds.todone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.xingpeds.todone.ui.theme.ToDoneTheme
import java.util.*
import kotlin.time.ExperimentalTime
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.periodUntil

@ExperimentalTime
class MainActivity : ComponentActivity() {
    private val model: DataModel by viewModels()
    private val exporter =
        registerForActivityResult(ActivityResultContracts.CreateDocument()) { uri ->
            if (uri != null) contentResolver.openOutputStream(uri)?.use { model.onExport(it) }
        }
    private val importer =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null)
                contentResolver.openInputStream(uri)?.use {
                    model.onImport(it, this)

                    model.reComposeList()
                }
        }

    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            ToDoneTheme {
                NavHost(navController = navController, startDestination = mainScreenRoute) {
                    composable(mainScreenRoute) { TaskListScreen(model, navController) }
                    composable(statsListScreenRoute) {
                        StatsList(dataModel = model, navController = navController)
                    }
                    composable("/task/{taskId}") {
                        DetailTaskScreen(
                            dataModel = model,
                            navController = navController,
                            it.arguments?.getString("taskId").toUUID()
                        )
                    }

                    composable(storageScreenRoute) {
                        StorageScreen(
                            dataModel = model,
                            navController = navController,
                            onImport = { importer.launch(arrayOf("application/json")) },
                            onExport = { exporter.launch("data.json") }
                        )
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        model.save()
    }
}

private fun String?.toUUID(): UUID {
    return if (this != null) {
        UUID.fromString(this)
    } else UUID.randomUUID()
}

@ExperimentalMaterialApi
@Composable
fun TaskListItem(
    task: Task,
    TrailingButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    secondary: @Composable () -> Unit = { LastCompDescription(task = task) },
) {

    ListItemLayout(
        modifier = modifier,
        text = { Text(text = task.name) },
        overlineText = { if (task.lastOrNull() != null) PreviousCompTime(comp = task.last()) },
        trailing = { TrailingButton() },
        secondaryText = secondary
    )
}

@Composable
fun ListItemLayout(
    modifier: Modifier,
    text: @Composable () -> Unit,
    overlineText: @Composable () -> Unit,
    trailing: @Composable () -> Unit,
    secondaryText: @Composable () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                //                .clip(RoundedCornerShape(20))
                //                .shadow(elevation = 3.dp, shape = RoundedCornerShape(2))
                .padding(10.dp)
                .fillMaxWidth()
                .then(modifier)
    ) {
        Column(modifier = Modifier.padding(15.dp)) {
            ProvideTextStyle(value = MaterialTheme.typography.overline, overlineText)
            ProvideTextStyle(value = MaterialTheme.typography.subtitle1, text)
            ProvideTextStyle(value = MaterialTheme.typography.body2, secondaryText)
        }
        // why isn't this at the end?
        trailing()
    }
}

@Composable
fun LastCompDescription(task: Task) {
    val comps = task.filterNotNull()
    if (comps.isNotEmpty() && comps.last().desc.text != null) {
        Text("Last ->" + comps.last().desc.text!!)
    } else if (task.desc.text != null) {
        Text(task.desc.text!!)
    }
}

@Composable
fun PreviousCompTime(comp: Completion) {
    val timeDistance =
        comp.timeStamp.periodUntil(Clock.System.now(), TimeZone.currentSystemDefault())
    val build: StringBuilder = StringBuilder()
    with(build) {
        if (timeDistance.years > 0) append("${timeDistance.years} years ")
        if (timeDistance.months > 0) append("${timeDistance.months} months ")
        if (timeDistance.days > 0) append("${timeDistance.days} days ")
        if (timeDistance.hours > 0) append("${timeDistance.hours} hours ")
        if (this.any()) append("ago") else append("completed recently")
    }
    Column { Text(build.toString()) }
}
