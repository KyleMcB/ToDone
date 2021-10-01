/*
 * Copyright (c) 2021. Kyle McBurnett
 * All rights reserved
 */

package com.xingpeds.todone

import java.util.*
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable data class Description(var text: String? = null, var picture: String? = null)

sealed interface Completion : Comparable<Completion> {
    val units: Int
    val timeStamp: Instant
    override fun compareTo(other: Completion): Int {
        return timeStamp.compareTo(other.timeStamp)
    }

    val desc: Description
}

sealed interface Task : MutableSet<Completion> {
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
    val avgUnitPerWeek: Float
    val avgUnitPer30Days: Float
    val daysSinceCreated: Long
    val numOfCompsLast7Days: Int
    val numOfCompsLast30Days: Int
    val unitsInLast7Days: Int
    val unitsInLast30Days: Int
    val stdDev7days: Float
    val stdDev30days: Float
    val unitsPerWeek: List<Int>
    val unitsPer30days: List<Int>
    abstract override fun hashCode(): Int
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
