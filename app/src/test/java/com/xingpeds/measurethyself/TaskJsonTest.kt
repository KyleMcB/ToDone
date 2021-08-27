package com.xingpeds.todoneproto

import com.xingpeds.measurethyself.CompJson
import com.xingpeds.measurethyself.Description
import com.xingpeds.measurethyself.TaskJson
import java.util.UUID
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test

import org.junit.Assert.*

internal class TaskJsonTest {

    val taskNoComps: TaskJson =
        TaskJson("test", Description("task descr"), "minutes", 30, UUID.randomUUID())
    val completion1 = CompJson(30)
    val completion2 = CompJson(45)
    val taskOneComp: TaskJson =
        TaskJson("name1", Description(), "minutes", 30).apply { add(completion1) }
    val taskTwocomp: TaskJson =
        TaskJson("name2", Description(), "minutes", 30).apply {
            add(completion1)
            add(completion2)
        }
    @Test
    fun getSize() {
        assert(taskNoComps.size == 0)
        assert(taskOneComp.size == 1)
    }

    @Test
    fun contains() {
        assert(!taskNoComps.contains(completion1))
        assert(taskOneComp.contains(completion1))
        assert(!taskOneComp.contains(completion2))
    }

    @Test
    fun containsAll() {
        val list = listOf(completion1, completion2)
        assert(!taskNoComps.containsAll(list))
        assert(!taskOneComp.containsAll(list))
        assert(taskTwocomp.containsAll(list))
    }

    @Test
    fun isEmpty() {
        assert(taskNoComps.isEmpty())
        assert(!taskOneComp.isEmpty())
        assert(!taskTwocomp.isEmpty())
    }

    @Test
    fun add() {
        // I'm using add to setup the tests, so if this doesn't work this whole file is moot
    }

    @Test
    fun addAll() {
        taskNoComps.addAll(listOf(completion1, completion2))
        assert(taskNoComps.size == 2)
    }

    @Test
    fun clear() {
        taskTwocomp.clear()
        assert(taskTwocomp.size == 0)
    }

    @Test
    operator fun iterator() {
        var test = false
        taskOneComp.forEach {
            test = true
            // you need an iterator to get here
        }
        assert(test)
    }

    @Test
    fun remove() {
        taskOneComp.remove(completion1)
        assert(taskOneComp.size == 0)
    }

    @Test
    fun removeAll() {
        assert(taskTwocomp.removeAll(listOf(completion1)))
        assert(taskTwocomp.size == 1)
        assert(taskTwocomp.first() == completion2)
    }

    @Test
    fun retainAll() {
        assert(taskTwocomp.retainAll(listOf(completion2)))
        assert(taskTwocomp.size == 1)
        assert(taskTwocomp.first() == completion2)
    }
    @Test
    fun serialize() {
        val string: String = Json.encodeToString(taskTwocomp)
        val copy = Json.decodeFromString<TaskJson>(string)
        assert(taskTwocomp == copy)
    }

    @Test
    fun createQuickCompletion() {
        val comp = taskNoComps.createCompletion()
        assert(taskNoComps.size == 1)
    }
    @Test
    fun createCompletionWithData() {
        val units = 44444
        val description = Description("first!", "link to picture")
        val comp = taskNoComps.createCompletion(units, description = description)
        assert(comp.desc == description && comp.units == units)
    }
}
