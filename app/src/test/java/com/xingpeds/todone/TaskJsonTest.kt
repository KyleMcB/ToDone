/*
 * Copyright (c) 2021. Kyle McBurnett
 * All rights reserved
 */

package com.xingpeds.todoneproto

import com.xingpeds.todone.data.Completion
import com.xingpeds.todone.data.Description
import com.xingpeds.todone.data.TaskJson
import com.xingpeds.todone.rate.Accelerating
import com.xingpeds.todone.rate.Declining
import com.xingpeds.todone.rate.Maintaining
import com.xingpeds.todone.rate.rateLastWindow
import java.util.*
import kotlin.math.sqrt
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlinx.datetime.Clock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

@ExperimentalTime
internal class TaskJsonTest {

    val taskNoComps: TaskJson =
        TaskJson("test", Description("task descr"), "minutes", 30, UUID.randomUUID())
    val completion1 = Completion(30)
    val completion2 = Completion(45, timeStamp = Clock.System.now() - Duration.days(1))
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
        val size = taskTwocomp.size
        assert(size == 1)
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
        taskNoComps.createCompletion()
        assert(taskNoComps.size == 1)
    }
    @Test
    fun createCompletionWithData() {
        val units = 44444
        val description = Description("first!", "link to picture")
        val comp = taskNoComps.createCompletion(units, description = description)
        assert(comp.desc == description && comp.units == units)
    }

    @ExperimentalTime
    @Test
    fun avgCompsPerWeeks() {
        val task: TaskJson = TaskJson("task1n", Description("desc"), "minutes", 10)
        assertEquals("task with no completions has average of zero", 0f, task.avgCompPerWindow)
        val comp1: Completion = Completion(1, Clock.System.now() - Duration.days(9))
        task.add(comp1)
        assertEquals(7f / 9f, task.avgCompPerWindow)
    }
    @ExperimentalTime
    @Test
    fun avgCompsPer30days() {
        val task: TaskJson = TaskJson("task1n", Description("desc"), "minutes", 10, daysWindow = 30)
        assertEquals(0f, task.avgCompPerWindow)
        val comp1: Completion = Completion(1, Clock.System.now() - Duration.days(9))
        task.add(comp1)
        assertEquals(30f / 9f, task.avgCompPerWindow)
    }
    @Test
    fun compsLast7Days() {
        val task: TaskJson = TaskJson("task1n", Description("desc"), "minutes", 10)
        assertEquals(0, task.numOfCompsLastWindow)
        val comp1: Completion = Completion(1, Clock.System.now() - Duration.days(9))
        task.add(comp1)
        assertEquals(0, task.numOfCompsLastWindow)
        task.add(Completion(1, Clock.System.now() - Duration.days(2)))
        assertEquals(1, task.numOfCompsLastWindow)
    }
    @Test
    fun compsLast30Days() {
        val task: TaskJson = TaskJson("task1n", Description("desc"), "minutes", 10, daysWindow = 30)
        assertEquals(0, task.numOfCompsLastWindow)
        task.add(Completion(1, Clock.System.now() - Duration.days(2)))
        assertEquals(1, task.numOfCompsLastWindow)
        task.add(Completion(1, Clock.System.now() - Duration.days(31)))
        assertEquals(1, task.numOfCompsLastWindow)
    }
    @Test
    fun unitsLast7Days() {
        val task: TaskJson = TaskJson("task1n", Description("desc"), "minutes", 10)
        assertEquals(0, task.unitsInLastWindow)
        task.add(Completion(30))
        assertEquals(30, task.unitsInLastWindow)
        task.add(Completion(30, Clock.System.now() - Duration.days(9)))
        assertEquals(30, task.unitsInLastWindow)
    }
    @Test
    fun unitsLast30Days() {
        val task: TaskJson = TaskJson("task1n", Description("desc"), "minutes", 10, daysWindow = 30)
        assertEquals(0, task.unitsInLastWindow)
        task.add(Completion(30))
        task.add(Completion(50, Clock.System.now() - Duration.days(1)))
        task.add(Completion(50, Clock.System.now() - Duration.days(31)))
        assertEquals(30 + 50, task.unitsInLastWindow)
    }
    @Test
    fun unitsPerWeek() {
        val task: TaskJson = TaskJson("task1n", Description("desc"), "minutes", 10)
        assertEquals(0, task.unitsPerWindow.size)
        task.add(Completion(30))
        assertEquals(1, task.unitsPerWindow.size)
        task.add(Completion(30, Clock.System.now() - Duration.days(task.daysWindow + 1)))
        assertEquals(2, task.unitsPerWindow.size)
    }
    @Test
    fun stdDevWeek() {
        val task: TaskJson = TaskJson("task1n", Description("desc"), "minutes", 10)
        task.add(Completion(13))
        task.add(Completion(15, Clock.System.now() - Duration.days(8)))
        task.add(Completion(19, Clock.System.now() - Duration.days(15)))
        assertEquals(2f * sqrt(7f / 3f), task.stdDev)
    }
    @Test
    fun unitsPer30days() {
        val task: TaskJson = TaskJson("task1n", Description("desc"), "minutes", 10, daysWindow = 30)
        assertEquals(0, task.unitsPerWindow.size)
        task.add(Completion(30))
        assertEquals(1, task.unitsPerWindow.size)
        task.add(Completion(30, Clock.System.now() - Duration.days(7)))
        assertEquals(1, task.unitsPerWindow.size)
        task.add(Completion(30, Clock.System.now() - Duration.days(32)))
        assertEquals(2, task.unitsPerWindow.size)
    }
    @Test
    fun stdDev30Days() {
        val task: TaskJson = TaskJson("task1n", Description("desc"), "minutes", 10, daysWindow = 30)
        task.add(Completion(13))
        task.add(Completion(15, Clock.System.now() - Duration.days(31)))
        task.add(Completion(19, Clock.System.now() - Duration.days(61)))
        assertEquals(2f * sqrt(7f / 3f), task.stdDev)
    }
    @Test
    fun avgUnitPerWeek() {
        val task: TaskJson = TaskJson("task1n", Description("desc"), "minutes", 10)
        task.add(Completion(13))
        task.add(Completion(15, Clock.System.now() - Duration.days(8)))
        task.add(Completion(19, Clock.System.now() - Duration.days(15)))
        assertEquals((13f + 15f + 19f) / 3f, task.avgUnitPerWindow)
    }
    @Test
    fun avgUnits30Days() {
        val task: TaskJson = TaskJson("task1n", Description("desc"), "minutes", 10, daysWindow = 30)
        task.add(Completion(13))
        task.add(Completion(15, Clock.System.now() - Duration.days(31)))
        task.add(Completion(19, Clock.System.now() - Duration.days(61)))
        assertEquals((13f + 15f + 19f) / 3f, task.avgUnitPerWindow)
    }
    @Test
    fun rate7DayOfDecline() {
        // test of decline
        val task: TaskJson = TaskJson("task1n", Description("desc"), "minutes", 10)
        with(task) {
            add(Completion(100, Clock.System.now() - Duration.days(8)))
            add(Completion(120, Clock.System.now() - Duration.days(15)))
            add(Completion(80, Clock.System.now() - Duration.days(22)))
            add(Completion(50))
        }
        assert(task.rateLastWindow() is Declining)
    }
    @Test
    fun rate7DayOfMaintain() {
        val task: TaskJson = TaskJson("task1n", Description("desc"), "minutes", 10)
        with(task) {
            add(Completion(100, Clock.System.now() - Duration.days(8)))
            add(Completion(120, Clock.System.now() - Duration.days(15)))
            add(Completion(80, Clock.System.now() - Duration.days(22)))
            add(Completion(100))
        }
        assert(task.rateLastWindow() is Maintaining)
    }
    @Test
    fun rate7DaysOfAccel() {
        val task: TaskJson = TaskJson("task1n", Description("desc"), "minutes", 10)
        with(task) {
            add(Completion(100, Clock.System.now() - Duration.days(8)))
            add(Completion(120, Clock.System.now() - Duration.days(15)))
            add(Completion(80, Clock.System.now() - Duration.days(22)))
            add(Completion(130))
        }
        assert(task.rateLastWindow() is Accelerating)
    }
}
