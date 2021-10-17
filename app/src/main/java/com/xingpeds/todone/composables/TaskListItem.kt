/*
 * Copyright (c) 2021. Kyle McBurnett
 * All rights reserved
 */

package com.xingpeds.todone.composables

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.xingpeds.todone.LastCompDescription
import com.xingpeds.todone.PreviousCompTime
import com.xingpeds.todone.data.Task

@ExperimentalMaterialApi
@Composable
fun TaskListItem(
    task: Task,
    TrailingButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    secondary: @Composable () -> Unit = { LastCompDescription(task = task) },
) {
    ListItemLayout(
        modifier = Modifier.shadow(elevation = 3.dp, shape = RoundedCornerShape(20)).then(modifier),
        text = { Text(text = task.name) },
        overlineText = { if (task.lastOrNull() != null) PreviousCompTime(comp = task.last()) },
        trailing = { TrailingButton() },
        secondaryText = secondary
    )
}

@Composable
fun ListItemLayout(
    modifier: Modifier,
    text: @Composable () -> Unit,
    overlineText: @Composable () -> Unit,
    trailing: @Composable () -> Unit,
    secondaryText: @Composable () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(10.dp).fillMaxWidth().then(modifier)
    ) {
        Column(modifier = Modifier.padding(15.dp)) {
            ProvideTextStyle(value = MaterialTheme.typography.overline, overlineText)
            ProvideTextStyle(value = MaterialTheme.typography.subtitle1, text)
            ProvideTextStyle(value = MaterialTheme.typography.body2, secondaryText)
        }
        // why isn't this at the end?
        trailing()
    }
}
