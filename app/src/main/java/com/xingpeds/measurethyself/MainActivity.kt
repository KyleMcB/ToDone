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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.xingpeds.measurethyself.ui.theme.MeasurethyselfTheme
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.periodUntil

class DataModel(application: Application) : AndroidViewModel(application) {
    val source: SourceJson = PersistJsonSource(application).load()
    val list = mutableStateOf(source, neverEqualPolicy())
    fun createTask(name: String, description: String, unit: String, defaultAmount: Int) {
        val descString: String? = if (description.isBlank()) null else description
        if (name.isBlank() || unit.isBlank() || defaultAmount == 0) return
        source.createTask(name, Description(descString), unit, defaultAmount)
        save()
        list.value = list.value
    }

    fun save() {
        // PersistJsonSource(context = applicationContext).save(model.source)
        viewModelScope.launch { PersistJsonSource(context = getApplication()).save(source) }
    }
}

@Composable
fun NewTaskDialog(
    onClose: () -> Unit,
    onCreate: (name: String, description: String, unit: String, defaultAmount: Int) -> Unit
) {
    Dialog(onDismissRequest = onClose) {
        var name: String by remember { mutableStateOf("") }
        var description: String by remember { mutableStateOf("") }
        var unit: String by remember { mutableStateOf("") }
        var amount: Int by remember { mutableStateOf(0) }
        Column(
            Modifier.clip(RoundedCornerShape(5))
                .background(Color.White)
                .padding(5.dp)
                .wrapContentSize()
        ) {
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
                onValueChange = { amount = it.toIntOrNull() ?: 0 },
                label = { Text("Measurement default") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            TextButton(
                onClick = {
                    onCreate(name, description, unit, amount)
                    onClose()
                },
                modifier = Modifier.align(Alignment.End).padding(5.dp)
            ) { Text("Create Task") }
        }
    }
}

@Composable
fun ShowCompDialog(mtask: MutableState<Task>, show: MutableState<Boolean>) {
    if (show.value) {
        DetailedTaskCompletionDialog(
            onDismiss = { show.value = false },
            onCreate = { units, description ->
                mtask.value.createCompletion(units, Description(description))
                mtask.value = mtask.value
            }
        )
    }
}

@Composable
fun DetailedTaskCompletionDialog(
    defaultAmount: Int = 0,
    onDismiss: () -> Unit,
    onCreate: (units: Int, description: String?) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        var units: Int by remember { mutableStateOf(defaultAmount) }
        var description: String by remember { mutableStateOf("") }
        Column(modifier = Modifier.wrapContentSize(Alignment.Center).background(Color.White)) {
            OutlinedTextField(
                value = units.toString(),
                onValueChange = { units = it.toIntOrNull() ?: 0 },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text("Measured Amount") }
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                singleLine = true,
            )
            TextButton(
                onClick = {
                    onCreate(units, if (description.isBlank()) null else description)
                    onDismiss()
                }
            ) { Text("Did it!") }
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
                    if (openDialog.value) {
                        NewTaskDialog(
                            onClose = { openDialog.value = false },
                            onCreate = model::createTask
                        )
                    }

                    LazyColumn() {
                        items(model.list.value.map { mutableStateOf(it, neverEqualPolicy()) }) {
                            stateTask ->
                            var detialCompDialog = mutableStateOf(false)
                            Task(
                                mtask = stateTask,
                                {
                                    stateTask.value.createCompletion()
                                    model.save()
                                },
                                modifier = Modifier.clickable { detialCompDialog.value = true }
                            )
                            ShowCompDialog(mtask = stateTask, show = detialCompDialog)
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
fun Task(mtask: MutableState<Task>, onCreateCompletion: () -> Unit, modifier: Modifier = Modifier) {
    val task = mtask.value

    ListItem(
        modifier = modifier,
        text = { Text(text = task.name) },
        overlineText = { if (task.lastOrNull() != null) PreviousCompTime(comp = task.last()) },
        trailing = {
            IconButton(
                onClick = {
                    onCreateCompletion()
                    mtask.value = task
                }
            ) { Icon(imageVector = Icons.Default.Done, contentDescription = "create Completion") }
        },
        secondaryText = { LastCompDescription(task = task) }
    )
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
        Task(mutableStateOf(task, neverEqualPolicy()), {})
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
    Column() { Text(build.toString()) }
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
    PreviousCompTime(
        comp = CompJson(30, Instant.parse("2020-01-01T00:00:00Z"), Description("washed the dishes"))
    )
}
