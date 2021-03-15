package com.mygdx.test.shape

import com.badlogic.gdx.math.Vector2
import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.math.round

const val EPSILON = 1e-3
fun same(a: Float, b: Float): Boolean = abs(a - b) <= EPSILON

class Vertex(val coor: Vector2, val type: Int) {
    fun getKey(): String {
        val x = normalisedX.toLong()
        val y = normalisedY.toLong()
        return "$x $y"
    }

    val x: Float get() = coor.x
    val y: Float get() = coor.y
    val normalisedX: Int get() = round(1 / EPSILON * coor.x).toInt()
    val normalisedY: Int get() = round(1 / EPSILON * coor.y).toInt()

    override fun hashCode(): Int {
        var seed = 0
        seed = seed xor (normalisedX shl 6)
        seed = seed xor (normalisedY shl 7)
        return seed
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Vertex
        return normalisedX == other.normalisedX && normalisedY == other.normalisedY && type == other.type
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
