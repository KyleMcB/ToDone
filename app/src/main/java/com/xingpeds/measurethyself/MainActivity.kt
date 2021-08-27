package com.xingpeds.measurethyself

import android.annotation.SuppressLint
import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.AndroidViewModel
import com.xingpeds.measurethyself.ui.theme.MeasurethyselfTheme
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.periodUntil

class DataModel(application: Application) : AndroidViewModel(application) {
    val source: SourceJson = PersistJsonSource(application).load()
    val list = mutableStateOf(source, neverEqualPolicy())
    fun createTask(name: String, description: Description, unit: String, defaultAmount: Int) =
        source.createTask(name, description, unit, defaultAmount).let { list.value = list.value }
}

@Composable
fun NewTaskDialog(
    onClose: () -> Unit,
    onCreate: (name: String, description: Description, unit: String, defaultAmount: Int) -> Unit
) {
    SourceJson().createTask("aTask", Description("aDescription"), "miles", 30)
    Dialog(onDismissRequest = onClose) {
        var name: String by remember { mutableStateOf("") }
        var description: String by remember { mutableStateOf("") }
        var unit: String by remember { mutableStateOf("") }
        var amount: Int by remember { mutableStateOf(0) }
        Column(Modifier.background(Color.White).padding(5.dp)) {
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
                onValueChange = { amount = it.toInt() },
                label = { Text("Measurement default") },
            )
            TextButton(
                onClick = { /*TODO*/},
                modifier = Modifier.align(Alignment.End).padding(5.dp)
            ) { Text("Create Task") }
        }
    }
}

class MainActivity : ComponentActivity() {
    private val model: DataModel by viewModels<DataModel>()

    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContent {
            val openDialog = remember { mutableStateOf(false) }
            MeasurethyselfTheme {
                Scaffold(
                    floatingActionButton = {
                        FloatingActionButton(onClick = { openDialog.value = true }) {
                            Icon(imageVector = Icons.Default.Add, null)
                        }
                    }
                ) {
                    Column() {
                        if (openDialog.value) {
                            NewTaskDialog(
                                onClose = { openDialog.value = false },
                                onCreate = model::createTask
                            )
                        }
                        model.list.value.map { mutableStateOf(it, neverEqualPolicy()) }.forEach {
                            stateTask ->
                            Task(mtask = stateTask)
                        }
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        PersistJsonSource(context = applicationContext).save(model.source)
    }
}

@ExperimentalMaterialApi
@Composable
fun Task(mtask: MutableState<Task>) {
    val task = mtask.value
    val description: String = task.lastOrNull()?.desc?.text ?: "no description"
    //    val previous:String = task.lastOrNull()?.timeStamp
    ListItem(
        modifier = Modifier.clickable { /* TODO detail pane here */},
        text = { Text(text = task.name) },
        overlineText = { task.lastOrNull()?.desc?.text?.let { Text(it) } },
        trailing = {
            IconButton(
                onClick = {
                    task.createCompletion()
                    mtask.value = task
                }
            ) { Icon(imageVector = Icons.Default.Done, contentDescription = "create Completion") }
        },
        secondaryText = { if (task.lastOrNull() != null) PreviousComp(comp = task.last()) }
    )
}

@ExperimentalMaterialApi
@SuppressLint("UnrememberedMutableState")
@Preview(showBackground = true)
@Composable
fun TaskPreview() {
    MeasurethyselfTheme {
        val task =
            TaskJson(
                    "exercise",
                    Description("at least 20 minutes of good heart pumping exercise"),
                    "minutes",
                    20
                )
                .apply {
                    add(
                        CompJson(
                            30,
                        )
                    )
                }
        Task(mutableStateOf(task, neverEqualPolicy()))
    }
}

@Composable
fun PreviousComp(comp: Completion) {
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
    Column() {
        Text(build.toString())
        comp.desc.text?.let { Text(it) }
    }
}

@ExperimentalMaterialApi
@Preview(showBackground = true)
@Composable
fun ListTest() {
    ListItem(
        text = { Text(text = "Exercise") },
        secondaryText = { Text("8 days ago") },
        trailing = {
            IconButton(onClick = { /*TODO*/}) {
                Icon(imageVector = Icons.Default.Done, contentDescription = "create Completion")
            }
        },
        overlineText = { Text("overline?") }
    )
}

@Preview
@Composable
fun PreviewLastComp() {
    PreviousComp(
        comp = CompJson(30, Instant.parse("2020-01-01T00:00:00Z"), Description("washed the dishes"))
    )
}
