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
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.xingpeds.todone.data.Completion
import com.xingpeds.todone.data.Task
import com.xingpeds.todone.screens.CompDetailScreen
import com.xingpeds.todone.screens.compdetailscreenroute
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

    override fun onResume() {
        super.onResume()
        model.reComposeList()
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
                    composable("/task/{taskId}") { navBackStack ->
                        navBackStack.arguments?.getString("taskId")?.toUUID()?.let { id ->
                            model.list.find { task -> task.id == id }?.let { task ->
                                DetailTaskScreen(
                                    dataModel = model,
                                    navController = navController,
                                    task
                                )
                            }
                        }
                    }
                    composable(compdetailscreenroute) { navBackStackEntry ->
                        navBackStackEntry.arguments?.getString("taskId")?.let {
                            val id = it.toUUID()
                            val task = model.list.find { task -> task.id == id }
                            task?.let {
                                var mtask by remember { mutableStateOf(it, neverEqualPolicy()) }
                                CompDetailScreen(
                                    dataModel = model,
                                    navController = navController,
                                    task = mtask,
                                    onCompletion = { comp ->
                                        mtask.add(comp)
                                        mtask = mtask
                                        model.save()
                                    }
                                )
                            }
                        }
                    }
                    composable(compListScreenRoute) {
                        CompListMainScreen(dataModel = model, navController = navController)
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

private fun String.toUUID(): UUID {
    return UUID.fromString(this)
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
