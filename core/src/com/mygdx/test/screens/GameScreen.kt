package com.mygdx.test.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.TimeUtils
import com.badlogic.gdx.utils.viewport.Viewport
import com.mygdx.test.Game
import ktx.app.KtxScreen
import ktx.graphics.use
import java.util.*
import kotlin.math.PI

fun degreesToRadians(degrees: Float): Float = degrees * PI.toFloat() / 180f

class GameScreen(
        private val camera: OrthographicCamera,
        private val viewport: Viewport,
        private val shapeRenderer: ShapeRenderer,
        private val game: Game
) : KtxScreen {
    private fun randomColor(): Color {
        val rand = Random(TimeUtils.nanoTime())
        val r = rand.nextFloat() / 2 + 0.5f
        val g = rand.nextFloat() / 2 + 0.5f
        val b = rand.nextFloat() / 2 + 0.5f
        return Color(r, g, b, 1f)
    }
    private val colors = game.vertices.map { randomColor() }
    init {
        camera.zoom = 0.01f
        camera.position.set(0f, 0f, 0f)
        camera.update()
    }

    override fun render(delta: Float) {
        Gdx.gl20.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT)
        camera.update()

        shapeRenderer.use(ShapeRenderer.ShapeType.Line, camera) {
            for (v in game.vertices) {
                for (u in game.neighbours(v)) {
                    shapeRenderer.line(
                            v.coordinates.x, v.coordinates.y,
                            u.coordinates.x, u.coordinates.y,
                            colors[v.type], colors[u.type])
                }
            }
        }
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
    }
}