package com.xingpeds.todone

import java.util.*
import kotlin.math.sqrt
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class CompJson(
    override val units: Int,
    override val timeStamp: Instant = Clock.System.now(),
    override val desc: Description = Description()
) : Completion

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
    @Serializable(with = UUIDSerializer::class) override val id: UUID = UUID.randomUUID()
) : Task {

    // this probably isn't going to serialize properly. need to do private constructor trick
    private val comps: MutableSet<CompJson> = mutableSetOf()
    override fun createCompletion(units: Int, description: Description): Completion {
        val comp = CompJson(units, desc = description)
        comps.add(comp)
        return comp
    }

    override val avgCompPerWeek: Float
        get() {
            if (this.isEmpty()) return 0f
            val numOfDays = this.daysSinceCreated
            val numOfWeeks: Float = numOfDays.toFloat() / 7f

            return this.size / numOfWeeks
        }
    override val avgCompPer30Days: Float
        get() {
            if (this.isEmpty()) return 0f
            val numOf30Days = this.daysSinceCreated / 30f
            return this.size / numOf30Days
        }
    override val avgUnitPerWeek: Float
        get() = unitsPerWeek.sum().toFloat() / unitsPerWeek.size.apply { if (this == 0) return 1f }
    override val avgUnitPer30Days: Float
        get() =
            unitsPer30days.sum().toFloat() / unitsPer30days.size.apply { if (this == 0) return 1f }
    val compsLast7days: List<Completion>
        get() =
            this.filter {
                it.timeStamp in Clock.System.now() - Duration.days(7)..Clock.System.now()
            }
    override val daysSinceCreated: Long
        get() {
            val list = this.sortedBy { it.timeStamp }
            return (Clock.System.now() - list.first().timeStamp).inWholeDays
        }
    val weeksSinceCreated: Long
        get() {
            return daysSinceCreated / 7
        }
    val _30DaysSinceCreate: Long
        get() {
            return daysSinceCreated / 30
        }
    override val numOfCompsLast7Days: Int
        get() = this.compsLast7days.size

    val compsLast30days: List<Completion>
        get() =
            this.filter {
                it.timeStamp in Clock.System.now() - Duration.days(30)..Clock.System.now()
            }
    override val numOfCompsLast30Days: Int
        get() = this.compsLast30days.size
    override val unitsInLast7Days: Int
        get() = this.compsLast7days.sumOf { it.units }
    override val unitsInLast30Days: Int
        get() = this.compsLast30days.sumOf { it.units }
    override val stdDev7days: Float
        get() {
            if (this.isEmpty() || this.size == 1) return 0f
            val weeks = unitsPerWeek
            if (weeks.size < 2) return 0f
            val mean: Float = weeks.sum().toFloat() / weeks.size.toFloat()
            val weekDeviations: List<Float> =
                List<Float>(weeks.size) {
                    val dev = weeks[it] - mean
                    (dev * dev)
                }
            return sqrt(weekDeviations.sum().toFloat() / (weekDeviations.size.toFloat() - 1f))
        }
    override val stdDev30days: Float
        get() {
            if (isEmpty() || size == 1) return 0f
            val months = unitsPer30days
            if (months.size < 2) return 0f
            val mean = months.sum().toFloat() / months.size.toFloat()
            val monthDeviations: List<Float> =
                List<Float>(months.size) {
                    val dev = months[it] - mean
                    (dev * dev)
                }
            return sqrt(monthDeviations.sum() / (monthDeviations.size.toFloat() - 1f))
        }
    override val unitsPer30days: List<Int>
        get() {
            if (this.isEmpty()) return emptyList()
            val _30days = _30DaysSinceCreate
            val months: MutableList<Int> = MutableList<Int>(_30days.toInt() + 1) { 0 }
            forEach {
                val month: Int = (Clock.System.now() - it.timeStamp).inWholeDays.toInt() / 30
                months[month] += it.units
            }
            return months
        }
    override val unitsPerWeek: List<Int>
        get() {
            if (this.isEmpty()) return emptyList()
            val weeks: MutableList<Int> = MutableList<Int>(weeksSinceCreated.toInt() + 1) { 0 }
            forEach {
                val week: Int = (Clock.System.now() - it.timeStamp).inWholeDays.toInt() / 7
                weeks[week] += it.units
            }
            return weeks.toList()
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

    override fun add(element: Completion) =
        comps.add(CompJson(element.units, desc = element.desc, timeStamp = element.timeStamp))
    override fun addAll(elements: Collection<Completion>) =
        comps.addAll(elements.map { CompJson(it.units, desc = it.desc, timeStamp = it.timeStamp) })

    override fun clear() = comps.clear()

    override fun iterator() = comps.iterator()

    override fun remove(element: Completion) = comps.remove(element)

    override fun removeAll(elements: Collection<Completion>) = comps.removeAll(elements)

    override fun retainAll(elements: Collection<Completion>) = comps.retainAll(elements)
}

fun Completion.toCompJson(): CompJson {
    return CompJson(units, timeStamp, desc)
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
