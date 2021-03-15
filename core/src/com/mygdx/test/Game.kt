package com.mygdx.test

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Gdx.input
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.mygdx.test.input.InputListener
import com.mygdx.test.screens.GameScreen
import com.mygdx.test.screens.degreesToRadians
import com.mygdx.test.shape.Shape
import com.mygdx.test.shape.Vertex
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.inject.Context
import ktx.inject.register
import kotlin.math.cos
import kotlin.math.sin

class Game : KtxGame<KtxScreen>() {
    private val context = Context()
    var shape = "arrow"

    private val shapes by lazy {
        val json = Gdx.files.internal("vertices.json").readString()
        val decoded = Json.decodeFromString<List<Shape>>(json)
        decoded.associateBy { it.name }
    }
    val vertices by lazy {
        val boxSize = 300f
        val renderBox = Rectangle(-boxSize/2, -boxSize/2, boxSize, boxSize)
        val visitedVertices = HashMap<String, Vertex>()
        val q = ArrayDeque<Vertex>()
        val initialVertex = Vertex(Vector2(0f, 0f), 0) // initial vertex of type 0 located in centre
        q.addLast(initialVertex)
        visitedVertices[initialVertex.getKey()] = initialVertex
        while (q.isNotEmpty()) {
            val v = q.removeFirst()
            for (u in neighbours(v)) {
                if (renderBox.contains(u.coor) &&
                        !visitedVertices.containsKey(u.getKey())) {
                    visitedVertices[u.getKey()] = u
                    q.addLast(u)
                }
            }
        }
        visitedVertices.map { (_, v) -> v }
    }
    val filledAreas = mutableListOf<List<Vertex>>()

    override fun create() {
        context.register {
            bindSingleton(Stage())
            bindSingleton(ShapeRenderer())
            bindSingleton<Camera>(OrthographicCamera().apply { setToOrtho(false, 1000f, 750f) })
            bindSingleton<Viewport>(ExtendViewport(1000f, 750f, inject()))

//            addScreen(MenuScreen(inject()))
            addScreen(GameScreen(
                    this@Game,
                    inject<Camera>() as OrthographicCamera,
                    inject(),
                    inject(),
            ))
            input.inputProcessor = InputListener(
                    this@Game,
                    inject<Camera>() as OrthographicCamera,
            )
        }
        setScreen<GameScreen>()
    }

    override fun dispose() {
        context.dispose()
        super.dispose()
    }

    fun neighbours(v: Vertex) = sequence {
        for (i in shapes[shape]!!.data[v.type].neighbourAngles.indices) {
            val angle = shapes[shape]!!.data[v.type].neighbourAngles[i]
            val distance = shapes[shape]!!.data[v.type].neighbourDistances[i]
            val type = shapes[shape]!!.data[v.type].neighbourTypes[i]
            val x = v.x + distance * sin(degreesToRadians(angle))
            val y = v.y + distance * cos(degreesToRadians(angle))
            yield(Vertex(Vector2(x, y), type))
        }
    }
}
