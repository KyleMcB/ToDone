package com.xingpeds.measurethyself

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.xingpeds.measurethyself.ui.theme.MeasurethyselfTheme
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.periodUntil

class MainActivity : ComponentActivity() {
    private val model: DataModel by viewModels<DataModel>()

    @ExperimentalTime
    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val notifyWorkRequest =
            PeriodicWorkRequestBuilder<AbsentNotifyWorker>(8, TimeUnit.HOURS, 1, TimeUnit.HOURS)
                .build()
        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                AbsentNotifyWorker.workName,
                ExistingPeriodicWorkPolicy.REPLACE,
                notifyWorkRequest
            )

        setContent {
            val navController = rememberNavController()
            MeasurethyselfTheme {
                NavHost(navController = navController, startDestination = "tasklist") {
                    composable("tasklist") { TaskListScreen(model, navController) }
                    composable("friendslist") {
                        StatsList(dataModel = model, navController = navController)
                    }
                    /*...*/
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        PersistJsonSource(context = applicationContext).save(model.source)
    }
}

@Composable
fun StatsList(dataModel: DataModel, navController: NavController) {
    Scaffold(topBar = { AppBarNavigation(navController = navController) }) {
        //
    }
}

@ExperimentalMaterialApi
@Composable
fun Task(task: Task, TrailingButton: @Composable () -> Unit, modifier: Modifier = Modifier) {

    ListItem(
        modifier = modifier,
        text = { Text(text = task.name) },
        overlineText = { if (task.lastOrNull() != null) PreviousCompTime(comp = task.last()) },
        trailing = TrailingButton,
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
