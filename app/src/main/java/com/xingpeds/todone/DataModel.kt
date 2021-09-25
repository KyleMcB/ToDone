package com.xingpeds.todone

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class DataModel(application: Application) : AndroidViewModel(application) {
    private val _sourcej: SourceJson = PersistJsonSource(application).load()
    private val _source: Source =
        _sourcej // force it to use the interface so copies can not be made
    private val _list = mutableStateOf(_source, neverEqualPolicy())
    var list
        get() = _list.value
        set(value) {
            _list.value = value
        }
    fun createTask(name: String, description: String, unit: String, defaultAmount: Int) {
        val descString: String? = if (description.isBlank()) null else description
        if (name.isBlank() || unit.isBlank() || defaultAmount == 0) return
        _source.createTask(name, Description(descString), unit, defaultAmount)
        save()
        reComposeList()
    }
    fun reComposeList() {
        list = list
    }

    fun save() {
        viewModelScope.launch { PersistJsonSource(context = getApplication()).save(_sourcej) }
    }
}
