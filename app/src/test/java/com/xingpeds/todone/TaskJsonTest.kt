package com.xingpeds.todoneproto

import com.xingpeds.todone.CompJson
import com.xingpeds.todone.Description
import com.xingpeds.todone.TaskJson
import com.xingpeds.todone.rate.Accelerating
import com.xingpeds.todone.rate.Declining
import com.xingpeds.todone.rate.Maintaining
import com.xingpeds.todone.rate.rateLast7days
import java.util.UUID
import kotlin.math.sqrt
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlinx.datetime.Clock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

@ExperimentalTime
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
        assertEquals("task with no completions has average of zero", 0f, task.avgCompPerWeek)
        val comp1: CompJson = CompJson(1, Clock.System.now() - Duration.days(9))
        task.add(comp1)
        assertEquals(7f / 9f, task.avgCompPerWeek)
    }
    @ExperimentalTime
    @Test
    fun avgCompsPer30days() {
        val task: TaskJson = TaskJson("task1n", Description("desc"), "minutes", 10)
        assertEquals(0f, task.avgCompPer30Days)
        val comp1: CompJson = CompJson(1, Clock.System.now() - Duration.days(9))
        task.add(comp1)
        assertEquals(30f / 9f, task.avgCompPer30Days)
    }
    @Test
    fun compsLast7Days() {
        val task: TaskJson = TaskJson("task1n", Description("desc"), "minutes", 10)
        assertEquals(0, task.numOfCompsLast7Days)
        val comp1: CompJson = CompJson(1, Clock.System.now() - Duration.days(9))
        task.add(comp1)
        assertEquals(0, task.numOfCompsLast7Days)
        task.add(CompJson(1, Clock.System.now() - Duration.days(2)))
        assertEquals(1, task.numOfCompsLast7Days)
    }
    @Test
    fun compsLast30Days() {
        val task: TaskJson = TaskJson("task1n", Description("desc"), "minutes", 10)
        assertEquals(0, task.numOfCompsLast30Days)
        task.add(CompJson(1, Clock.System.now() - Duration.days(2)))
        assertEquals(1, task.numOfCompsLast30Days)
        task.add(CompJson(1, Clock.System.now() - Duration.days(31)))
        assertEquals(1, task.numOfCompsLast30Days)
    }
    @Test
    fun unitsLast7Days() {
        val task: TaskJson = TaskJson("task1n", Description("desc"), "minutes", 10)
        assertEquals(0, task.unitsInLast7Days)
        task.add(CompJson(30))
        assertEquals(30, task.unitsInLast7Days)
        task.add(CompJson(30, Clock.System.now() - Duration.days(9)))
        assertEquals(30, task.unitsInLast7Days)
    }
    @Test
    fun unitsLast30Days() {
        val task: TaskJson = TaskJson("task1n", Description("desc"), "minutes", 10)
        assertEquals(0, task.unitsInLast30Days)
        task.add(CompJson(30))
        task.add(CompJson(50))
        task.add(CompJson(50, Clock.System.now() - Duration.days(31)))
        assertEquals(30 + 50, task.unitsInLast30Days)
    }
    @Test
    fun unitsPerWeek() {
        val task: TaskJson = TaskJson("task1n", Description("desc"), "minutes", 10)
        assertEquals(0, task.unitsPerWeek.size)
        task.add(CompJson(30))
        assertEquals(1, task.unitsPerWeek.size)
        task.add(CompJson(30, Clock.System.now() - Duration.days(7)))
        assertEquals(2, task.unitsPerWeek.size)
    }
    @Test
    fun stdDevWeek() {
        val task: TaskJson = TaskJson("task1n", Description("desc"), "minutes", 10)
        task.add(CompJson(13))
        task.add(CompJson(15, Clock.System.now() - Duration.days(8)))
        task.add(CompJson(19, Clock.System.now() - Duration.days(15)))
        assertEquals(2f * sqrt(7f / 3f), task.stdDev7days)
    }
    @Test
    fun unitsPer30days() {
        val task: TaskJson = TaskJson("task1n", Description("desc"), "minutes", 10)
        assertEquals(0, task.unitsPer30days.size)
        task.add(CompJson(30))
        assertEquals(1, task.unitsPer30days.size)
        task.add(CompJson(30, Clock.System.now() - Duration.days(7)))
        assertEquals(1, task.unitsPer30days.size)
        task.add(CompJson(30, Clock.System.now() - Duration.days(32)))
        assertEquals(2, task.unitsPer30days.size)
    }
    @Test
    fun stdDev30Days() {
        val task: TaskJson = TaskJson("task1n", Description("desc"), "minutes", 10)
        task.add(CompJson(13))
        task.add(CompJson(15, Clock.System.now() - Duration.days(31)))
        task.add(CompJson(19, Clock.System.now() - Duration.days(61)))
        assertEquals(2f * sqrt(7f / 3f), task.stdDev30days)
    }
    @Test
    fun avgUnitPerWeek() {
        val task: TaskJson = TaskJson("task1n", Description("desc"), "minutes", 10)
        task.add(CompJson(13))
        task.add(CompJson(15, Clock.System.now() - Duration.days(8)))
        task.add(CompJson(19, Clock.System.now() - Duration.days(15)))
        assertEquals((13f + 15f + 19f) / 3f, task.avgUnitPerWeek)
    }
    @Test
    fun avgUnits30Days() {
        val task: TaskJson = TaskJson("task1n", Description("desc"), "minutes", 10)
        task.add(CompJson(13))
        task.add(CompJson(15, Clock.System.now() - Duration.days(31)))
        task.add(CompJson(19, Clock.System.now() - Duration.days(61)))
        assertEquals((13f + 15f + 19f) / 3f, task.avgUnitPer30Days)
    }
    @Test
    fun rate7DayOfDecline() {
        // test of decline
        val task: TaskJson = TaskJson("task1n", Description("desc"), "minutes", 10)
        with(task) {
            add(CompJson(100, Clock.System.now() - Duration.days(8)))
            add(CompJson(120, Clock.System.now() - Duration.days(15)))
            add(CompJson(80, Clock.System.now() - Duration.days(22)))
            add(CompJson(50))
        }
        assert(task.rateLast7days() is Declining)
    }
    @Test
    fun rate7DayOfMaintain() {
        val task: TaskJson = TaskJson("task1n", Description("desc"), "minutes", 10)
        with(task) {
            add(CompJson(100, Clock.System.now() - Duration.days(8)))
            add(CompJson(120, Clock.System.now() - Duration.days(15)))
            add(CompJson(80, Clock.System.now() - Duration.days(22)))
            add(CompJson(100))
        }
        assert(task.rateLast7days() is Maintaining)
    }
    @Test
    fun rate7DaysOfAccel() {
        val task: TaskJson = TaskJson("task1n", Description("desc"), "minutes", 10)
        with(task) {
            add(CompJson(100, Clock.System.now() - Duration.days(8)))
            add(CompJson(120, Clock.System.now() - Duration.days(15)))
            add(CompJson(80, Clock.System.now() - Duration.days(22)))
            add(CompJson(130))
        }
        assert(task.rateLast7days() is Accelerating)
    }
}
