/*
 * Copyright (c) 2021. Kyle McBurnett
 * All rights reserved
 */

package com.xingpeds.todone

import androidx.compose.foundation.clickable
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.navigation.NavController
import com.xingpeds.todone.composables.TaskLazyList
import com.xingpeds.todone.composables.TaskListItem
import com.xingpeds.todone.screens.compdetailpartial

const val compListScreenRoute = "compListScreenRoute"

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CompListMainScreen(dataModel: DataModel, navController: NavController) {

    val scaffoldState = rememberScaffoldState()
    Scaffold(
        scaffoldState = scaffoldState,
        drawerContent = { DrawerContent(navController = navController) },
        topBar = { AppBarNavigation(scaffoldState = scaffoldState, title = "History") }
    ) {
        TaskLazyList(dataModel = dataModel) { dataModel, mtask ->
            val task = mtask.value
            TaskListItem(
                task = task,
                modifier =
                    Modifier.clickable { // TODO go individual history list of comps
                    },
                TrailingButton = {
                    IconButton(
                        onClick = {
                            navController.navigate(
                                "$compdetailpartial${task.id.toString()}".logcat("route string")
                            )
                        }
                    ) { Icon(Icons.Default.KeyboardArrowRight, null) }
                },
                secondary = {
                    task.desc.text?.let { Text(it) }
                        ?: Text("No Task Description", fontStyle = FontStyle.Italic)
                }
            )
        }
    }
}
