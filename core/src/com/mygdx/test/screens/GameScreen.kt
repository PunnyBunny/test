package com.mygdx.test.screens

import com.badlogic.gdx.Gdx.gl
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.utils.viewport.Viewport
import ktx.app.KtxScreen
import space.earlygrey.shapedrawer.ShapeDrawer


class GameScreen(
        private val camera: Camera,
        private val viewport: Viewport,
        private val shapeDrawer: ShapeDrawer,
) : KtxScreen {
    override fun render(delta: Float) {
        gl.glClearColor(1f, 1f, 1f, 1f);
        gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
    }
}


private const val SQUARE_LENGTH = 100f
private const val LINE_WIDTH = 10f
private val LIVING_RULE_FOR_ALIVE = listOf(2, 3)
private val LIVING_RULE_FOR_DEAD = listOf(3)
private const val GRID_SIZE = 100

fun Array<BooleanArray>.copy() = Array(GRID_SIZE) {
    get(it).clone()
}
