package com.xingpeds.todone

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.ui.text.capitalize
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import java.util.*
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

class DataModel(application: Application) : AndroidViewModel(application) {
    private val _sourcej: SourceJson = PersistJsonSource(application).load()
    private val _source: Source =
        _sourcej // force it to use the interface so copies can not be made
    private val _list = mutableStateOf(_source, neverEqualPolicy())
    private val _selectedTab = mutableStateOf(SortMethod.RATE)
    val selectedTab
        get() = _selectedTab.value
    @ExperimentalTime
    val list: List<Task>
        get() = _list.value.sortedWith(TaskSorter(selectedTab))

    fun createTask(name: String, description: String, unit: String, defaultAmount: Int) {
        val descString: String? = if (description.isBlank()) null else description
        if (name.isBlank() || unit.isBlank() || defaultAmount == 0) return
        _source.createTask(name, Description(descString), unit, defaultAmount)
        save()
        reComposeList()
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
            tab.name.lowercase().capitalize(locale = Locale.ENGLISH)
        }

    fun onTabSelect(sortMethod: SortMethod) {
        _selectedTab.value = sortMethod
        reComposeList()
    }
    @ExperimentalTime
    class TaskSorter(val sortMethod: SortMethod) : Comparator<Task> {
        override fun compare(p0: Task?, p1: Task?): Int {
            if (p0 == null) {
                return 1
            }
            if (p1 == null) {
                return -1
            }
            requireNotNull(p1)
            requireNotNull(p0)
            when (sortMethod) {
                SortMethod.RATE ->
                    return ((p0.stdDev7days - p0.unitsInLast7Days) -
                            (p1.stdDev7days - p1.unitsInLast7Days))
                        .toInt()
                SortMethod.UNITS -> return ((p0.unitsInLast7Days) - (p1.unitsInLast7Days))
                SortMethod.TIME -> {
                    val a = p0.lastOrNull()?.timeStamp ?: Instant.DISTANT_PAST
                    val b = p1.lastOrNull()?.timeStamp ?: Instant.DISTANT_PAST
                    return a.compareTo(b)
                }
            }
        }
    }
}
