package com.xingpeds.todone

import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import kotlin.time.ExperimentalTime

const val storageScreenRoute = "storagescreenroute"

@ExperimentalTime
@Composable
fun StorageScreen(
    dataModel: DataModel,
    navController: NavHostController,
    onImport: () -> Unit,
    onExport: () -> Unit
) {
    val scaffoldState = rememberScaffoldState()
    Scaffold(
        scaffoldState = scaffoldState,
        drawerContent = { DrawerContent(navController = navController) },
        topBar = { AppBarNavigation(scaffoldState = scaffoldState) }
    ) {
        Column {
            Text("export internal data as a json file")
            Button(onClick = onExport) { Text("Export") }
            Divider()
            Text("Import and overwrite internal data with an external json file")
            Button(
                onClick = {
                    try {
                        onImport()
                    } catch (e: Throwable) {
                        // warn the user the import failed. Probably snackbar
                    }
                }
            ) { Text("Import") }
        }
    }
}
