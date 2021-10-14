/*
 * Copyright (c) 2021. Kyle McBurnett
 * All rights reserved
 */

package com.xingpeds.todone

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.xingpeds.todone.data.*
import com.xingpeds.todone.rate.rateLastWindow
import java.io.InputStream
import java.io.OutputStream
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.launch

@ExperimentalTime
class DataModel(application: Application) : AndroidViewModel(application) {
    private var _sourcej: SourceJson = PersistJsonSource(application).load()
    private val _source: Source
        get() = _sourcej // force it to use the interface so copies can not be made
    private val _list = mutableStateOf(_source, neverEqualPolicy())
    private val _selectedSortTab = mutableStateOf(SortMethod.RATE)
    val selectedTab
        get() = _selectedSortTab.value
    @ExperimentalTime
    val list: List<Task>
        get() =
            when (selectedTab) {
                SortMethod.RATE -> {
                    _list.value.groupBy { it.rateLastWindow() }.flatMap { it.value }
                }
                SortMethod.UNITS -> {

                    _list.value.toList().groupBy { it.unit }.flatMap { entry ->
                        entry.value.sortedBy { it.unitsInLastWindow }
                    }
                }
                SortMethod.TIME -> {
                    _list.value.toList().sortedBy { it.lastOrNull()?.timeStamp }
                }
            }

    //            _list.value.sortedWith(TaskSorter(selectedTab))

    fun createTask(name: String, description: String, unit: String, defaultAmount: Int) {
        val descString: String? = if (description.isBlank()) null else description
        if (name.isBlank() || unit.isBlank() || defaultAmount == 0) return
        _source.createTask(name, Description(descString), unit, defaultAmount)
        save()
        reComposeList()
    }
    fun onImport(stream: InputStream, context: Context) =
        import(context, stream).also {
            _sourcej = PersistJsonSource(context).load()
            _list.value = _source
        }
    fun onExport(stream: OutputStream) {
        export(stream, _source)
    }

    fun reComposeList() {
        _list.value = _list.value
    }
    fun deleteTask(task: Task) {
        _list.value.remove(task)
    }

    fun save() {
        viewModelScope.launch { PersistJsonSource(context = getApplication()).save(_sourcej) }
    }

    // sorting tab section
    enum class SortMethod {
        RATE,
        UNITS,
        TIME
    }
    val tabs =
        SortMethod.values().toList().map { tab ->
            tab.name.lowercase().replaceFirstChar { char -> char.uppercase() }
        }

    fun onTabSelect(sortMethod: SortMethod) {
        _selectedSortTab.value = sortMethod
        reComposeList()
    }
}
