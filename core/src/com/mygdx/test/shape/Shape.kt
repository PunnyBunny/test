package com.mygdx.test.shape

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import ktx.graphics.use

data class Vertex(val deltaX: Float, val deltaY: Float)
data class Edge(val vertexA: Vertex, val vertexB: Vertex) {
    override fun hashCode(): Int {
        return vertexA.hashCode() xor vertexB.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Edge

        return (vertexA == other.vertexA && vertexB == other.vertexB) ||
                (vertexA == other.vertexB && vertexB == other.vertexA)
    }
}

data class Region(val edges: List<Edge>, var toggled: Boolean = false)
class Shape(
        val x: Float,
        val y: Float,
        val regions: List<Region>,
        private val externalEdgesMapping: HashMap<Edge, Edge>,
) {
    private val externalEdges: List<Edge>
    private val regionVerticesFlattened: List<FloatArray>
    val width: Float
    val height: Float

    init {
        val edges = regions.map { it.edges }.flatten()
        val edgesCount = edges.groupingBy { it }.eachCount()
        externalEdges = edges.filter { edgesCount[it] == 1 }

        regionVerticesFlattened = regions.map {
            val verticesFlattened =
                    it.edges.map { e -> listOf(e.vertexA.deltaX + x, e.vertexA.deltaY + y) }.flatten() +
                            listOf(it.edges.first().vertexA.deltaX + x, it.edges.first().vertexA.deltaY + y)
            verticesFlattened.toFloatArray()
        }

        width = edges.maxOf { it.vertexA.deltaX } - edges.minOf { it.vertexA.deltaX }
        height = edges.maxOf { it.vertexA.deltaY } - edges.minOf { it.vertexA.deltaY }
    }

    fun neighbours(): List<Shape> {
        return externalEdges.map { externalEdge ->
            val dx = externalEdgesMapping[externalEdge]!!.vertexA.deltaX - externalEdge.vertexA.deltaX
            val dy = externalEdgesMapping[externalEdge]!!.vertexA.deltaY - externalEdge.vertexA.deltaY
            Shape(x + dx, y + dy, regions.map { Region(it.edges, false) }, externalEdgesMapping)
        }
    }

    fun render(camera: OrthographicCamera, shapeRenderer: ShapeRenderer) {
        shapeRenderer.use(ShapeRenderer.ShapeType.Line, camera) { renderer ->
            regionVerticesFlattened.forEach { renderer.polyline(it) }
        }
    }

    override fun hashCode(): Int {
        return Pair(x, y).hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Shape) return false
        return x == other.x && y == other.y
    }
}