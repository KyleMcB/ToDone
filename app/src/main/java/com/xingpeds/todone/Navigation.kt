/*
 * Copyright (c) 2021. Kyle McBurnett
 * All rights reserved
 */

package com.xingpeds.todone

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@Composable
fun AppBarNavigation(scaffoldState: ScaffoldState, title: String = "ToDone") {
    val scope = rememberCoroutineScope()
    TopAppBar(
        title = { Text(title) },
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
    ListItem(
        icon = { Icon(Icons.Default.History, "History") },
        modifier =
            Modifier.clickable {
                navController.navigate(compListScreenRoute) { popUpTo(mainScreenRoute) }
            }
    ) { Text("History") }
    ListItem(
        icon = { Icon(Icons.Default.AccountBox, "storage settings") },
        modifier =
            Modifier.clickable {
                navController.navigate(storageScreenRoute) { popUpTo(mainScreenRoute) }
            }
    ) { Text("Data location") }
}
