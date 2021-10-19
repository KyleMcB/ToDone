/*
 * Copyright (c) 2021. Kyle McBurnett
 * All rights reserved
 */

package com.xingpeds.todone.screens

// import android.inputmethodservice.Keyboard
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import com.xingpeds.todone.DataModel
import com.xingpeds.todone.DrawerContent
import com.xingpeds.todone.composables.NumberOutlinedTextField
import com.xingpeds.todone.data.Completion
import com.xingpeds.todone.data.Description
import com.xingpeds.todone.data.Task
import java.time.format.DateTimeFormatter
import kotlin.time.Duration
import kotlinx.datetime.*

private const val tag = "{taskId}"
const val compdetailpartial = "/compdetailscreenroute/"
const val compdetailscreenroute = "$compdetailpartial$tag"

@ExperimentalMaterialApi
@Composable
fun CompDetailScreen(
    dataModel: DataModel,
    navController: NavHostController,
    task: Task,
    onCompletion: (Completion) -> Unit,
    onDelete: (Completion) -> Unit
) {
    Scaffold(
        floatingActionButton = { CreateNewCompFAB(task, onCompletion) },
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
                CompListItem(comp, task.unit, onDelete)
            }
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun LazyItemScope.CompListItem(comp: Completion, units: String, onDelete: (Completion) -> Unit) {
    Surface(
        modifier =
            Modifier.padding(10.dp)
                .fillMaxWidth()
                .wrapContentHeight()
                .shadow(elevation = 3.dp, shape = RoundedCornerShape(20))
    ) {
        //

        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(start = 5.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.SpaceAround,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                ) {
                    Text(units)

                    Text(comp.units.toString())
                }
                //                Divider(modifier = Modifier.fillMaxHeight().width(1.dp))
                Column(
                    verticalArrangement = Arrangement.SpaceAround,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("Time")
                    Text(comp.timeStamp.toReadbleTime())
                }
                Column(
                    verticalArrangement = Arrangement.SpaceAround,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("Date")
                    Text(comp.timeStamp.toReadableDate())
                }
                Box(modifier = Modifier.padding(15.dp).size(40.dp)) {
                    Surface(
                        onClick = { /*TODO navigate to edit screen */},
                        elevation = 10.dp,
                        shape = CircleShape,
                        color = MaterialTheme.colors.secondary,
                        modifier = Modifier.fillParentMaxSize()
                    ) {
                        Icon(
                            Icons.Outlined.Edit,
                            null,
                            tint = MaterialTheme.colors.onSecondary,
                            modifier = Modifier.padding(5.dp)
                        )
                    }
                }
            }
            comp.desc.text?.let {
                Divider()
                Text(it, modifier = Modifier.padding(start = 10.dp))
            }
        }
    }
}

private fun Instant.toReadableDate(): String {
    val dateTime = this.toLocalDateTime(TimeZone.currentSystemDefault())
    val date = dateTime.date
    return date.toString()
}

private fun Instant.toReadbleTime(): String {
    val dateTime = this.toLocalDateTime(TimeZone.currentSystemDefault())
    val hour = dateTime.hour
    val minute = dateTime.minute
    val minuteString = if (minute < 10) "0$minute" else minute.toString()
    return "$hour:$minuteString"
}

@ExperimentalMaterialApi
@Preview(showBackground = true)
@Composable
fun PreviewCompListItem() {
    LazyColumn() {
        item {
            CompListItem(
                comp =
                    Completion(
                        10,
                        desc =
                            Description(
                                "a thing is a thing with a really long description. so long how will is..."
                            )
                    ),
                units = "Minutes",
                onDelete = {}
            )
        }
    }
}

@ExperimentalMaterialApi
@Preview(showBackground = true)
@Composable
fun PreviewCompListItemNoDesc() {
    LazyColumn() { item { CompListItem(comp = Completion(1), units = "Minutes", onDelete = {}) } }
}

@Composable
fun CreateNewCompFAB(task: Task, onCompletion: (Completion) -> Unit) {
    val dialogState = rememberMaterialDialogState()
    FloatingActionButton(onClick = { dialogState.show() }) { Icon(Icons.Default.Add, null) }
    var description by remember { mutableStateOf<String?>(null) }
    var amount by remember { mutableStateOf<Int?>(null) }
    var timeStamp by remember { mutableStateOf(Clock.System.now()) }
    fun reset() {
        description = null
        amount = null
        timeStamp = Clock.System.now()
    }
    MaterialDialog(
        dialogState = dialogState,
        buttons = {
            positiveButton(
                "Ok",
                onClick = {
                    amount?.let {
                        onCompletion(
                            Completion(
                                it,
                                timeStamp = timeStamp,
                                desc = Description(text = description)
                            )
                        )
                    }
                    reset()
                }
            )
            negativeButton("Cancel", onClick = { reset() })
        }
    ) {
        OutlinedTextField(
            value = description ?: "",
            onValueChange = { description = it },
            label = { Text("Desciption") },
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp).fillMaxWidth()
        )
        NumberOutlinedTextField(
            number = amount,
            enable = true,
            onChange = { amount = it },
            label = "Amount",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp).fillMaxWidth()
        )
        datepicker { date ->
            var temp = date.toKotlinLocalDate().atStartOfDayIn(TimeZone.currentSystemDefault())
            while (task.filter { comp -> comp.timeStamp == temp }.isNotEmpty()) {
                temp += Duration.Companion.seconds(1)
            }
            timeStamp = temp
        }
    }
}

private fun Instant.toReadable(): String {
    val compDate = this.toLocalDateTime(TimeZone.currentSystemDefault())
    val test =
        this.toLocalDateTime(TimeZone.currentSystemDefault())
            .toJavaLocalDateTime()
            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    val nowDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val string = StringBuilder()
    if (compDate.date != nowDate.date) string.append("${compDate.date.toString()} ")
    string.append("${compDate.hour}:")
    if (compDate.minute < 10) string.append("0")
    string.append(compDate.minute.toString())
    //    return test
    return string.toString()
}
