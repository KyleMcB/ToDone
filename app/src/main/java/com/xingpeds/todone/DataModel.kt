package com.xingpeds.todone

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class DataModel(application: Application) : AndroidViewModel(application) {
    val source: SourceJson = PersistJsonSource(application).load()
    val list = mutableStateOf(source, neverEqualPolicy())
    fun createTask(name: String, description: String, unit: String, defaultAmount: Int) {
        val descString: String? = if (description.isBlank()) null else description
        if (name.isBlank() || unit.isBlank() || defaultAmount == 0) return
        source.createTask(name, Description(descString), unit, defaultAmount)
        save()
        list.value = list.value
    }

    fun save() {
        // PersistJsonSource(context = applicationContext).save(model.source)
        viewModelScope.launch { PersistJsonSource(context = getApplication()).save(source) }
    }
}
