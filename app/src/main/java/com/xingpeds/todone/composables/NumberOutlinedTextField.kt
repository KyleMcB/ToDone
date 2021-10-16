/*
 * Copyright (c) 2021. Kyle McBurnett
 * All rights reserved
 */

package com.xingpeds.todone.composables

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun NumberOutlinedTextField(
    number: Int?,
    enable: Boolean,
    onChange: (Int) -> Unit,
    label: String? = null,
    modifier: Modifier = Modifier
) {
    var value by remember { mutableStateOf(number?.toString()) }
    var valid by remember { mutableStateOf(true) }
    OutlinedTextField(
        value = value ?: "",
        onValueChange = {
            value = it
            try {
                val temp: Int = it.toInt()
                onChange(temp)
                valid = true
            } catch (e: NumberFormatException) {
                valid = false
            }
        },
        isError = !valid,
        label = { if (label != null) Text(label) },
        singleLine = true,
        maxLines = 1,
        keyboardOptions =
            KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
        enabled = enable,
        modifier = modifier
    )
}
