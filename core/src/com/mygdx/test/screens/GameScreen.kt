package com.mygdx.test.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.Viewport
import com.mygdx.test.shape.Shape
import com.mygdx.test.shape.Vertex
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import ktx.app.KtxScreen
import ktx.graphics.use
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

fun degreesToRadians(degrees: Float): Float = degrees * PI.toFloat() / 180f

class GameScreen(
        private val camera: OrthographicCamera,
        private val viewport: Viewport,
        private val shapeRenderer: ShapeRenderer,
) : KtxScreen {
    private val bufferSize = 100f
    private val shapes: Map<String, Shape>
    private val vertices: List<Vertex>
    var shape = "octagon"

    private fun neighbours(v: Vertex) = sequence {
        for (i in shapes[shape]!!.data[v.type].neighbourAngles.indices) {
            val angle = shapes[shape]!!.data[v.type].neighbourAngles[i]
            val distance = shapes[shape]!!.data[v.type].neighbourDistances[i]
            val type = shapes[shape]!!.data[v.type].neighbourTypes[i]
            val x = v.coordinates.x + distance * sin(degreesToRadians(angle))
            val y = v.coordinates.y + distance * cos(degreesToRadians(angle))
            yield(Vertex(Vector2(x, y), type))
        }
    }

    init {
        camera.zoom = 0.01f
        camera.position.set(0f, 0f, 0f)
        camera.update()

        val json = Gdx.files.internal("vertices.json").readString()
        val decoded = Json.decodeFromString<List<Shape>>(json)
        shapes = decoded.associateBy { it.name }

        val renderBox = Rectangle(-50f, -50f, 100f, 100f)
        val visitedVertices = HashMap<String, Vertex>()
        val q = ArrayDeque<Vertex>()
        val initialVertex = Vertex(Vector2(0f, 0f), 0) // initial vertex of type 0 located in centre
        q.addLast(initialVertex)
        visitedVertices[initialVertex.getKey()] = initialVertex
        while (q.isNotEmpty()) {
            val v = q.first()
            q.removeFirst()
            for (u in neighbours(v)) {
                if (renderBox.contains(u.coordinates) &&
                        !visitedVertices.containsKey(u.getKey())) {
                    visitedVertices[u.getKey()] = u
                    q.addLast(u)
                }

            }
        }
        vertices = visitedVertices.map { (_, v) -> v }
    }

//    fun handleInput() {
//        if (Gdx.input.isTouched) {
//            val x = Gdx.input.deltaX.toFloat() * camera.zoom
//            val y = Gdx.input.deltaY.toFloat() * camera.zoom
//            camera.position.add(-x, y, 0f)
//        }
//        if (Gdx.input.)
//    }

    override fun render(delta: Float) {
        Gdx.gl20.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT)

//        handleInput()

        camera.update()

        shapeRenderer.use(ShapeRenderer.ShapeType.Line, camera) {
            for (v in vertices) {
                for (u in neighbours(v)) {
//                    println("${v.coordinate} ${u.coordinate}")
                    shapeRenderer.line(v.coordinates.x, v.coordinates.y, u.coordinates.x, u.coordinates.y, Color.PINK, Color.PINK)
                }
            }
        }
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
    }
}