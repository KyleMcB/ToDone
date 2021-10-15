/*
 * Copyright (c) 2021. Kyle McBurnett
 * All rights reserved
 */

package com.xingpeds.todone.screens

// import android.inputmethodservice.Keyboard
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.xingpeds.todone.AppBarNavigation
import com.xingpeds.todone.DataModel
import com.xingpeds.todone.DrawerContent
import com.xingpeds.todone.data.Task

const val tag = "{taskId}"
const val compdetailpartial = "/compdetailscreenroute/"
const val compdetailscreenroute = "$compdetailpartial$tag"

@Composable
fun CompDetailScreen(dataModel: DataModel, navController: NavHostController, task: Task) {
    val scaffoldState = rememberScaffoldState()
    Scaffold(
        topBar = { AppBarNavigation(scaffoldState = scaffoldState, title = task.name) },
        drawerContent = { DrawerContent(navController = navController) },
    ) {
        LazyColumn() {
            items(task.toList()) { comp ->
                Row(modifier = Modifier.fillMaxWidth()) { Text(comp.timeStamp.toString()) }
            }
        }
    }
}
