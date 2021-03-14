package com.mygdx.test.shape

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import ktx.graphics.use

data class Vertex(val deltaX: Float, val deltaY: Float)

private fun min(a: Vertex, b: Vertex): Vertex {
    if (a.deltaX < b.deltaX || (a.deltaX == b.deltaX && a.deltaY < b.deltaY)) return a
    return b
}

private fun max(a: Vertex, b: Vertex): Vertex {
    if (a.deltaX > b.deltaX || (a.deltaX == b.deltaX && a.deltaY > b.deltaY)) return a
    return b
}

data class Edge(val vertexA: Vertex, val vertexB: Vertex) {
    val a = min(vertexA, vertexB)
    val b = max(vertexA, vertexB)

    override fun equals(other: Any?): Boolean {
        if (other !is Edge) return false
        return a == other.a && b == other.b
    }

    override fun hashCode(): Int {
        var result = a.hashCode()
        result = 31 * result + b.hashCode()
        return result
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
                    it.edges.map { e -> listOf(e.a.deltaX + x, e.a.deltaY + y) }.flatten() +
                            listOf(it.edges.first().a.deltaX + x, it.edges.first().a.deltaY + y)
            verticesFlattened.toFloatArray()
        }

        width = edges.maxOf { it.a.deltaX } - edges.minOf { it.a.deltaX }
        height = edges.maxOf { it.a.deltaY } - edges.minOf { it.a.deltaY }
    }

    fun neighbours(): List<Shape> {
        return externalEdges.map { externalEdge ->
            val dx = externalEdge.a.deltaX - externalEdgesMapping[externalEdge]!!.a.deltaX
            val dy = externalEdge.a.deltaY - externalEdgesMapping[externalEdge]!!.a.deltaY
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