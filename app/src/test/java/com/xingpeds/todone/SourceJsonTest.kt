package com.xingpeds.todoneproto

import com.xingpeds.todone.Description
import com.xingpeds.todone.SourceJson
import com.xingpeds.todone.TaskJson
import kotlin.time.ExperimentalTime
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test

@ExperimentalTime
internal class SourceJsonTest {
    val task1 = TaskJson("task1", Description(), "minutes", 30)
    val task2 = TaskJson("task2", Description("another task"), "meals", 1)
    val sourceNoTasks: SourceJson = SourceJson()
    val sourceOneTask = SourceJson().apply { add(task1) }
    val sourceTwoTasks =
        SourceJson().apply {
            add(task1)
            add(task2)
        }
    @Test
    fun Serialize() {
        val zerotaskstring = Json.encodeToString(sourceNoTasks)
        val onetaskstring = Json.encodeToString(sourceOneTask)
        val twotaskstring = Json.encodeToString(sourceTwoTasks)
        val test0 = Json.decodeFromString<SourceJson>(zerotaskstring)
        val test1 = Json.decodeFromString<SourceJson>(onetaskstring)
        val test2 = Json.decodeFromString<SourceJson>(twotaskstring)
        assert(sourceNoTasks.size == test0.size)
        assert(sourceOneTask.first().id == test1.first().id)
        assert(sourceTwoTasks.size == test2.size)
    }

    @Test
    fun getSize() {
        assert(sourceNoTasks.size == 0)
        assert(sourceTwoTasks.size == 2)
    }

    @Test
    fun isEmpty() {
        assert(sourceNoTasks.isEmpty())
        assert(!sourceTwoTasks.isEmpty())
    }

    @Test
    fun getTaskById() {
        val item = sourceTwoTasks.getTaskById(task2.id)
        assert(item == task2)
    }

    @Test
    fun createTask() {
        val newTask =
            sourceNoTasks.createTask("newTask", Description("created by source object"), "hours", 1)
        assert(sourceNoTasks.first().name == "newTask")
    }

    @Test
    fun canNotAddSameID() {
        val slightlyDiff = task1.apply { name = "a new name" }
        sourceTwoTasks.add(slightlyDiff)
        assert(sourceTwoTasks.size == 2)
    }

    @Test fun addAll() {}

    @Test fun clear() {}

    @Test fun remove() {}
}
