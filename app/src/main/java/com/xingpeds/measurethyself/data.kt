package com.xingpeds.measurethyself

import java.util.*
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable data class Description(var text: String? = null, var picture: String? = null)

sealed interface Completion {
    val units: Int
    val timeStamp: Instant
    val desc: Description
}

sealed interface Task : MutableCollection<Completion> {
    var name: String
    val desc: Description
    var unit: String
    var defaultAmount: Int
    val id: UUID
    fun createCompletion(
        units: Int = defaultAmount,
        description: Description = Description()
    ): Completion
    val avgCompPerWeek: Float
    val avgCompPer30Days: Float
    val daysSinceCreated: Long
    val numOfCompsLast7Days: Int
    val numOfCompsLast30Days: Int
    val unitsInLast7Days: Int
    val unitsInLast30Days: Int
    val stdDev7days: Float
    val stdDev30days: Float
    val unitsPerWeek: List<Int>

    operator fun get(i: Int): Completion
}

sealed interface Source : MutableSet<Task> {
    fun getTaskById(id: UUID): Task
    fun createTask(
        name: String,
        description: Description,
        unit: String,
        defaultAmount: Int,
        id: UUID = UUID.randomUUID()
    ): Task
}
