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
import com.xingpeds.todone.data.Description
import com.xingpeds.todone.data.Task
import com.xingpeds.todone.screens.*
import com.xingpeds.todone.ui.theme.ToDoneTheme
import java.util.*
import kotlin.time.ExperimentalTime
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone

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
                    composable(compdetaileditscreenroute) { nav ->
                        val taskId = nav.arguments?.getString("taskId")
                        val compId = nav.arguments?.getString("compId")
                        taskId?.let { idString ->
                            val task = model.list.find { it.id == UUID.fromString(idString) }
                            requireNotNull(task)
                            var task_s by remember { mutableStateOf(task, neverEqualPolicy()) }
                            compId?.let { instantString ->
                                val timeStamp = Instant.parse(instantString)
                                val comp = task_s.find { it.timeStamp == timeStamp }
                                if (comp == null) {} else {
                                    CompDetailEditScreen(
                                        dataModel = model,
                                        navController = navController,
                                        comp = comp,
                                        units = task_s.unit,
                                        onTimeStamp = { time: LocalDateTime ->
                                            task_s.remove(comp)
                                            val newComp =
                                                comp.copy(
                                                    timeStamp =
                                                        time.toInstant(
                                                            TimeZone.currentSystemDefault()
                                                        )
                                                )
                                            task_s.add(newComp)
                                            task_s = task_s
                                            navController.navigate(
                                                addressOfCompEditDetialScreen(task, newComp)
                                            ) { popUpTo(compListScreenRoute) }
                                        },
                                        onDesc = { desc: String ->
                                            val old = task_s.remove(comp)
                                            task_s.add(
                                                comp.copy(
                                                    desc = Description(desc, comp.desc.picture)
                                                )
                                            )
                                            task_s = task_s
                                        },
                                        onUnits = { units: Int ->
                                            task_s.remove(comp)
                                            task_s.add(comp.copy(units = units))
                                            task_s = task_s
                                        },
                                        onDelete = {
                                            task_s.remove(comp)
                                            task_s = task_s
                                            model.save()
                                            navController.popBackStack()
                                        }
                                    )
                                }
                            }
                        }
                    }
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
                                    },
                                    onDelete = { comp ->
                                        mtask.remove(comp)
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
                            onImport = { importer.launch(arrayOf("*/*")) },
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
