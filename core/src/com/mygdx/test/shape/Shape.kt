package com.mygdx.test.shape

import com.badlogic.gdx.graphics.Color
import space.earlygrey.shapedrawer.ShapeDrawer

data class Vertex(val deltaX: Float, val deltaY: Float)
data class Edge(val vertexA: Vertex, val vertexB: Vertex)
class Shape(private val x: Float, private val y: Float, private val edges: List<Edge>) {
    fun render(shapeDrawer: ShapeDrawer) {
        for (edge in edges) {
            shapeDrawer.line(
                    x + edge.vertexA.deltaX,
                    y + edge.vertexA.deltaY,
                    x + edge.vertexB.deltaX,
                    y + edge.vertexB.deltaY,
                    3f,
                    true,
                    Color(1f, 1f, 1f, 1f),
                    Color(1f, 1f, 1f, 1f),
            )
        }
    }
}