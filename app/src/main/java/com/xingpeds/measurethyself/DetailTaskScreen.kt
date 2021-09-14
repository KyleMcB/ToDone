package com.xingpeds.measurethyself

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.util.*
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlinx.datetime.Clock

@ExperimentalTime
@ExperimentalMaterialApi
@Composable
fun DetailTaskScreen(dataModel: DataModel, navController: NavController, taskId: UUID) {
    val temp = dataModel.source.find { task -> task.id == taskId }
    requireNotNull(temp)
    val task: Task = temp
    Scaffold(topBar = { AppBarNavigation(navController = navController) }) {
        Column() {
            Row() {
                Text(text = task.name, fontSize = 30.sp)
                IconButton(onClick = { /*TODO*/}) { Icon(Icons.Default.Edit, "edit") }
            }

            Divider(modifier = Modifier.padding(10.dp))
            Row() {
                Text("Edit  ")
                Text("Delete")
            }
            Divider(modifier = Modifier.padding(10.dp))
            Text("a bunch of stats here")
            val lastWeekCount =
                task
                    .filter { comp ->
                        comp.timeStamp in
                            (Clock.System.now() - Duration.days(7)..Clock.System.now())
                    }
                    .count()
            Text("$lastWeekCount done in last week")
            Divider(modifier = Modifier.padding(10.dp))
            Text("Completions")
            LazyColumn(modifier = Modifier.padding(10.dp)) {
                items(task.toList().sortedByDescending { it.timeStamp }) { comp ->
                    CompletionItem(comp)
                }
            }
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun CompletionItem(comp: Completion) {
    ListItem(secondaryText = { Text(comp.timeStamp.toString()) }) {
        comp.desc.text?.let { Text(text = it) }
    }
}
