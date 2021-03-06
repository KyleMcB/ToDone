/*
 * Copyright (c) 2021. Kyle McBurnett
 * All rights reserved
 */

package com.xingpeds.todone.data

import java.util.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable data class Description(var text: String? = null, var picture: String? = null)

@Serializable
data class Completion(
    val units: Int,
    val timeStamp: Instant = Clock.System.now(),
    val desc: Description = Description()
) : Comparable<Completion> {
    override fun compareTo(other: Completion): Int {
        return timeStamp.compareTo(other.timeStamp)
    }
}

const val defaultWindowSize = 7

sealed interface Task : MutableSet<Completion> {
    var name: String
    val desc: Description
    var unit: String
    var defaultAmount: Int

    val id: UUID
    var daysWindow: Int
    fun createCompletion(
        units: Int = defaultAmount,
        description: Description = Description()
    ): Completion
    val avgCompPerWindow: Float

    val avgUnitPerWindow: Float

    val numOfCompsLastWindow: Int

    val unitsInLastWindow: Int

    val stdDev: Float

    val unitsPerWindow: List<Int>

    val daysSinceCreated: Long

    abstract override fun hashCode(): Int
}

val Task.regularity: Float
    get() = if (stdDev == 0f) 0f else (1 - (stdDev / avgUnitPerWindow)) * 100f

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
