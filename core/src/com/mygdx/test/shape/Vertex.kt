package com.mygdx.test.shape

import com.badlogic.gdx.math.Vector2
import kotlinx.serialization.Serializable
import kotlin.math.round

private var idCounter = 0;
private const val EPSILON = 1e-4

class Vertex(val coordinates: Vector2, val type: Int) {
    //    private fun hashCombine(seed: Int, x: Int): Int {
//        return seed xor (x.hashCode() + 0x9e3779b9 + (seed shl 6) + (seed shr 2)).toInt()
//    }
//
//    fun getKey(): Int {
//        var seed = 0
//        seed = hashCombine(seed, floor(1 / EPSILON * coordinate.x).toInt().hashCode())
//        seed = hashCombine(seed, floor(1 / EPSILON * coordinate.y).toInt().hashCode())
//        return seed
//    }
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
