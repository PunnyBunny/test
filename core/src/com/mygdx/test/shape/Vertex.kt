package com.mygdx.test.shape

import com.badlogic.gdx.math.Vector2
import kotlinx.serialization.Serializable
import kotlin.math.round

private var idCounter = 0
private const val EPSILON = 1e-3

class Vertex(val coordinates: Vector2, val type: Int) {
    fun getKey(): String {
        val x = round(1 / EPSILON * coordinates.x).toLong()
        val y = round(1 / EPSILON * coordinates.y).toLong()
        return "$x $y"
    }
}

@Serializable
data class VertexData(
        val type: Int,
        val neighbourTypes: List<Int>,
        val neighbourAngles: List<Float>,
        val neighbourDistances: List<Float>,
)

@Serializable
data class Shape(val name: String, val data: List<VertexData>)
