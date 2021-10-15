/*
 * Copyright (c) 2021. Kyle McBurnett
 * All rights reserved
 */

package com.xingpeds.todone.composables

import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.ui.Modifier
import com.xingpeds.todone.DataModel
import com.xingpeds.todone.data.Task

@ExperimentalMaterialApi
@Composable
fun TaskLazyList(
    dataModel: DataModel,
    content: @Composable (DataModel, MutableState<Task>) -> Unit
) {
    LazyColumn(modifier = Modifier.wrapContentSize()) {
        items(dataModel.list.map { mutableStateOf(it, neverEqualPolicy()) }) { stateTask ->
            content(dataModel, stateTask)
        }
    }
}
