package com.xingpeds.todone

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

@ExperimentalTime
class DataModel(application: Application) : AndroidViewModel(application) {
    private var _sourcej: SourceJson = PersistJsonSource(application).load()
    private val _source: Source
        get() = _sourcej // force it to use the interface so copies can not be made
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
                SortMethod.UNITS -> {
                    return ((p0.unitsInLast7Days) - (p1.unitsInLast7Days)) +
                        if (p0.unit.lowercase() > p1.unit.lowercase()) Int.MIN_VALUE
                        else Int.MAX_VALUE
                }
                SortMethod.TIME -> {
                    val a = p0.lastOrNull()?.timeStamp ?: Instant.DISTANT_PAST
                    val b = p1.lastOrNull()?.timeStamp ?: Instant.DISTANT_PAST
                    return a.compareTo(b)
                }
            }
        }
    }
}
