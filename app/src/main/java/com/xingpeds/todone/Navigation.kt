package com.xingpeds.todone

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@Composable
fun AppBarNavigation(scaffoldState: ScaffoldState) {
    val scope = rememberCoroutineScope()
    TopAppBar(
        title = { Text("S.M.A.R.T GOALS") },
        navigationIcon = {
            IconButton(onClick = { scope.launch { scaffoldState.drawerState.open() } }) {
                Icon(Icons.Filled.Menu, contentDescription = null)
            }
        }
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ColumnScope.DrawerContent(navController: NavController) {
    ListItem(
        icon = { Icon(Icons.Default.Home, null) },
        modifier =
            Modifier.clickable {
                navController.navigate(mainScreenRoute) {
                    popUpTo(mainScreenRoute) { inclusive = true }
                }
            }
    ) { Text("Tasks") }
    ListItem(
        icon = { Icon(Icons.Default.List, null) },
        modifier =
            Modifier.clickable {
                navController.navigate(statsListScreenRoute) { popUpTo(mainScreenRoute) }
            }
    ) { Text("Stats") }
}
