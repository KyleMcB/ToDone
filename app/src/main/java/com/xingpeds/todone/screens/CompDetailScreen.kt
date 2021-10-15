/*
 * Copyright (c) 2021. Kyle McBurnett
 * All rights reserved
 */

package com.xingpeds.todone.screens

// import android.inputmethodservice.Keyboard
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.xingpeds.todone.DataModel
import com.xingpeds.todone.DrawerContent
import com.xingpeds.todone.data.Task
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private const val tag = "{taskId}"
const val compdetailpartial = "/compdetailscreenroute/"
const val compdetailscreenroute = "$compdetailpartial$tag"

@ExperimentalMaterialApi
@Composable
fun CompDetailScreen(dataModel: DataModel, navController: NavHostController, task: Task) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History: ${task.name}") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        },
        drawerContent = { DrawerContent(navController = navController) },
    ) {
        LazyColumn() {
            items(task.toList().sortedDescending()) { comp ->
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(10.dp)
                            .shadow(elevation = 3.dp, shape = RoundedCornerShape(20))
                ) {
                    Column {
                        Text(
                            comp.timeStamp.toReadable(),
                            modifier = Modifier.padding(top = 10.dp, start = 10.dp)
                        )
                        val text = comp.desc.text ?: "No Description"

                        Text(text, modifier = Modifier.padding(start = 20.dp, bottom = 10.dp))
                    }
                    Box(modifier = Modifier.padding(15.dp).size(40.dp)) {
                        Surface(
                            onClick = { /* TODO delete completion */},
                            elevation = 10.dp,
                            shape = CircleShape,
                            color = MaterialTheme.colors.error,
                            modifier = Modifier.fillParentMaxSize()
                        ) { Icon(Icons.Default.Clear, null, tint = MaterialTheme.colors.onError) }
                    }
                }
            }
        }
    }
}

private fun Instant.toReadable(): String {
    val compDate = this.toLocalDateTime(TimeZone.currentSystemDefault())
    val nowDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val string = StringBuilder()
    if (compDate.date != nowDate.date) string.append("${compDate.date.toString()} ")
    string.append("${compDate.hour}:")
    if (compDate.minute < 10) string.append("0")
    string.append(compDate.minute.toString())
    return string.toString()
}
