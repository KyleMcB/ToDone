/*
 * Copyright (c) 2021. Kyle McBurnett
 * All rights reserved
 */

package com.xingpeds.todone

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.util.*
import kotlin.time.ExperimentalTime

const val statsListScreenRoute = "statsListScreenRoute"

@ExperimentalTime
@ExperimentalMaterialApi
@Composable
fun StatsList(dataModel: DataModel, navController: NavController) {
    val scaffoldState = rememberScaffoldState()
    Scaffold(
        scaffoldState = scaffoldState,
        drawerContent = { DrawerContent(navController = navController) },
        topBar = { AppBarNavigation(scaffoldState = scaffoldState) }
    ) {
        Column() {
            dataModel.list.map { mutableStateOf(it, neverEqualPolicy()) }.forEach { stateTask ->
                // possibly fragile code, can't call Task(task,{},modifier) directly
                TaskQuickStats(mtask = stateTask, { id -> navController.navigate("/task/$id") })
            }
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun TaskQuickStats(
    mtask: MutableState<Task>,
    goToDetails: (UUID) -> Unit,
    modifier: Modifier = Modifier
) {
    val task = mtask.value

    TaskListItem(
        task,
        TrailingButton = {
            IconButton(onClick = { goToDetails(task.id) }) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Detailed stats"
                )
            }
        },
        modifier =
            modifier
                .clickable { goToDetails(task.id) }
                .shadow(elevation = 3.dp, shape = RoundedCornerShape(20))
                .then(modifier)
    )
}
