/*
 * Copyright (c) 2021. Kyle McBurnett
 * All rights reserved
 */

package com.xingpeds.todone.data

import java.util.*
import kotlin.math.sqrt
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlinx.datetime.Clock
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

// @Serializable
// data class CompJson(
//    override val units: Int,
//    override val timeStamp: Instant = Clock.System.now(),
//    override val desc: Description = Description()
// ) : Completion

class UUIDSerializer : KSerializer<UUID> {
    override fun deserialize(decoder: Decoder): UUID {
        val string: String = decoder.decodeString()
        return UUID.fromString(string)
    }

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: UUID) {
        val string: String = value.toString()
        encoder.encodeString(string)
    }
}

@ExperimentalTime
@Serializable
data class TaskJson(
    override var name: String,
    override val desc: Description,
    override var unit: String,
    override var defaultAmount: Int,
    @Serializable(with = UUIDSerializer::class) override val id: UUID = UUID.randomUUID(),
    override var daysWindow: Int = 7
) : Task {

    // this probably isn't going to serialize properly. need to do private constructor trick
    private val comps: MutableList<Completion> = mutableListOf()
    //    private val comps: MutableList<CompJson> = mutableListOf()
    init {
        comps.sort()
    }
    override fun createCompletion(units: Int, description: Description): Completion {
        val comp = Completion(units, desc = description)
        comps.add(comp)
        return comp
    }

    override val avgCompPerWindow: Float
        get() {
            if (this.isEmpty()) return 0f
            val numOfWindows = this.daysSinceCreated.toFloat() / this.daysWindow
            return this.size / numOfWindows
        }

    override val avgUnitPerWindow: Float
        get() =
            unitsPerWindow.sum().toFloat() / unitsPerWindow.size.apply { if (this == 0) return 1f }

    override val numOfCompsLastWindow: Int
        get() = this.compsLastWindow.size

    override val daysSinceCreated: Long
        get() {
            if (isEmpty()) return 0
            val list = this.sortedBy { it.timeStamp }
            return (Clock.System.now() - list.first().timeStamp).inWholeDays
        }

    val compsLastWindow: List<Completion>
        get() =
            this.filter {
                it.timeStamp in Clock.System.now() - Duration.days(daysWindow)..Clock.System.now()
            }

    override val unitsInLastWindow: Int
        get() = this.compsLastWindow.sumOf { it.units }

    override val stdDev: Float
        get() {
            // if we only have 1 or 0 data points or if they are all in the same windows -> escape
            // hatch
            if (this.size < 2 || unitsPerWindow.size < 2) return 0f
            val windows = unitsPerWindow
            val mean: Float = windows.sum().toFloat() / windows.size.toFloat()
            val windowDeviations: List<Float> =
                List(windows.size) {
                    val deviation = windows[it] - mean
                    (deviation * deviation)
                }
            return sqrt(windowDeviations.sum() / (windowDeviations.size - 1f))
        }

    override val unitsPerWindow: List<Int>
        get() {
            if (this.isEmpty()) return emptyList()
            val windows: MutableList<Int> =
                MutableList((daysSinceCreated.toInt() / daysWindow) + 1) { 0 }
            forEach {
                val window: Int =
                    (Clock.System.now() - it.timeStamp).inWholeDays.toInt() / daysWindow
                windows[window] += it.units
            }
            return windows
        }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override val size
        get() = comps.size

    override fun contains(element: Completion): Boolean {
        return comps.contains(element)
    }

    override fun containsAll(elements: Collection<Completion>): Boolean {
        return comps.containsAll(elements)
    }

    override fun isEmpty(): Boolean {
        return comps.isEmpty()
    }

    override fun add(element: Completion): Boolean {
        if (contains(element = element)) return false
        val result =
            comps.add(Completion(element.units, desc = element.desc, timeStamp = element.timeStamp))
        comps.sort() // TODO super inefficient. I need to upgrade to a DB
        return result
    }
    @Deprecated("not compaitable with sorted list")
    override fun addAll(elements: Collection<Completion>): Boolean {

        return false
    }

    override fun clear() = comps.clear()

    override fun iterator() = comps.iterator()

    override fun remove(element: Completion) = comps.remove(element)

    override fun removeAll(elements: Collection<Completion>) = comps.removeAll(elements)

    override fun retainAll(elements: Collection<Completion>) = comps.retainAll(elements)
}

@ExperimentalTime
fun Task.toTaskJson(): TaskJson {
    val other = this
    return TaskJson(name, desc, unit, defaultAmount, id).apply { addAll(other) }
}

@ExperimentalTime
@Serializable
class SourceJson : Source {
    private val tasks: MutableSet<TaskJson> = mutableSetOf<TaskJson>()
    override val size
        get() = tasks.size

    override fun contains(element: Task) = tasks.contains(element)

    override fun containsAll(elements: Collection<Task>) = tasks.containsAll(elements)

    override fun isEmpty() = tasks.isEmpty()

    override fun getTaskById(id: UUID) = tasks.first { taskJson -> taskJson.id == id }

    override fun createTask(
        name: String,
        description: Description,
        unit: String,
        defaultAmount: Int,
        id: UUID
    ): Task {
        val task = TaskJson(name, description, unit, defaultAmount, id)
        tasks.add(task)
        return task
    }

    override fun add(element: Task): Boolean {

        return if (contains(element)) false // not tested :(
        else if (element is TaskJson) {
            tasks.add(element)
            true
        } else {
            tasks.add(
                TaskJson(
                        element.name,
                        element.desc,
                        element.unit,
                        element.defaultAmount,
                        element.id
                    )
                    .apply { addAll(element) } // not tested :(
            )
            true
        }
    }

    override fun addAll(elements: Collection<Task>): Boolean {
        elements.forEach(this::add)
        return true
    }

    override fun clear() = tasks.clear()

    override fun iterator() = tasks.iterator()

    override fun remove(element: Task) = tasks.remove(element)

    override fun removeAll(elements: Collection<Task>) = tasks.removeAll(elements)

    override fun retainAll(elements: Collection<Task>) = tasks.retainAll(elements)
}
