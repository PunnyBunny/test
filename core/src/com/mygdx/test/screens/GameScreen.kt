package com.mygdx.test.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.viewport.Viewport
import ktx.app.KtxScreen
import ktx.graphics.use


class GameScreen(
        private val camera: Camera,
        private val viewport: Viewport,
        private val shapeRenderer: ShapeRenderer,
) : KtxScreen {
    private var deltaSum = 0f

    private var grid = Array(GRID_SIZE) { BooleanArray(GRID_SIZE) }
    private val tmpGrid = grid.copy()

    private var isDoneInput = false

    init {
        (camera as OrthographicCamera).apply {
            zoom = 3f
            position.x = 2000f
            position.y = 1500f
        }
    }

    private fun input() {
        if (Gdx.input.justTouched()) {
            val x = Gdx.input.x
            val y = Gdx.input.y
            val v = camera.unproject(Vector3(x.toFloat(), y.toFloat(), 0f))
            val i = v.x / SQUARE_LENGTH
            val j = v.y / SQUARE_LENGTH
            grid[i.toInt()][j.toInt()] = !grid[i.toInt()][j.toInt()]   // flip the bit
        }
        if (Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
            isDoneInput = true
        }
    }

    private fun run(delta: Float) {
        deltaSum += delta
        (camera as OrthographicCamera).zoom += 0.1f * delta

        if (deltaSum > 1f) {
            for (i in 0 until GRID_SIZE) for (j in 0 until GRID_SIZE) {
                var cnt = 0

                for (di in -1..1) for (dj in -1..1) {
                    val ni = i + di
                    val nj = j + dj
                    if ((di == 0 && dj == 0) || ni < 0 || nj < 0 || ni >= GRID_SIZE || nj >= GRID_SIZE)
                        continue
                    cnt += if (grid[ni][nj]) 1 else 0
                }
                tmpGrid[i][j] = cnt in (if (grid[i][j]) LIVING_RULE_FOR_ALIVE else LIVING_RULE_FOR_DEAD)
            }
            deltaSum -= 1f
            grid = tmpGrid.copy()
        }

    }

    override fun render(delta: Float) {
        if (isDoneInput) run(delta)
        else input()

        Gdx.gl.glClearColor(0f, 0f, 0f, 0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        camera.update()

        shapeRenderer.use(ShapeRenderer.ShapeType.Filled, camera) { renderer ->
            for (i in 0 until GRID_SIZE) for (j in 0 until GRID_SIZE) {
                val x = i * SQUARE_LENGTH
                val y = j * SQUARE_LENGTH

                if (grid[i][j]) {
                    renderer.color = Color.RED
                    renderer.rect(x, y, SQUARE_LENGTH, SQUARE_LENGTH)
                }

                renderer.color = Color.WHITE
                renderer.rectLine(x, y, x + SQUARE_LENGTH, y, LINE_WIDTH)
                renderer.rectLine(x, y, x, y + SQUARE_LENGTH, LINE_WIDTH)
            }
        }
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
