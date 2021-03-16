package com.mygdx.test.input

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.viewport.Viewport
import com.mygdx.test.Game
import com.mygdx.test.shape.Vertex
import com.mygdx.test.shape.same
import ktx.app.KtxInputAdapter

class InputListener(
        private val game: Game,
        private val camera: OrthographicCamera,
        private val viewport: Viewport,
) : KtxInputAdapter {
    private var dragged = false
    private var lastTouchScreenPos = Vector3()

    private val zoomFactor = 0.1f

    private val minVertexX = game.vertices.minOf { it.x }
    private val minVertexY = game.vertices.minOf { it.y }
    private val maxVertexX = game.vertices.maxOf { it.x }
    private val maxVertexY = game.vertices.maxOf { it.y }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val curScreenPos = Vector3(screenX.toFloat(), -screenY.toFloat(), 0f)

        lastTouchScreenPos = curScreenPos
        dragged = false

        return true
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        val curScreenPos = Vector3(screenX.toFloat(), -screenY.toFloat(), 0f)
        val delta = lastTouchScreenPos.cpy().sub(curScreenPos).scl(camera.zoom)

        camera.translate(delta)
        camera.update()
        clampBoard()

        lastTouchScreenPos = curScreenPos
        dragged = true
        return true
    }

    private fun clampBoard() {
        val minVertexProjected = camera.project(Vector3(minVertexX, minVertexY, 0f))
        val maxVertexProjected = camera.project(Vector3(maxVertexX, maxVertexY, 0f))
        println(Vector3(0f.coerceAtLeast(minVertexProjected.x),
                0f.coerceAtLeast(minVertexProjected.y), 0f).scl(camera.zoom))
        println(Vector3(0f.coerceAtLeast(viewport.screenWidth - maxVertexProjected.x),
                0f.coerceAtLeast(viewport.screenHeight - maxVertexProjected.y), 0f).scl(camera.zoom))
        camera.translate(
                Vector3(0f.coerceAtLeast(minVertexProjected.x),
                        0f.coerceAtLeast(minVertexProjected.y), 0f).scl(camera.zoom))
        camera.translate(
                Vector3(0f.coerceAtMost(maxVertexProjected.x - viewport.screenWidth),
                        0f.coerceAtMost(maxVertexProjected.y - viewport.screenHeight), 0f).scl(camera.zoom))
        camera.update()
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val curScreenPos = Vector3(screenX.toFloat(), screenY.toFloat(), 0f)

        if (!dragged) {
            touch(curScreenPos)
        }

        lastTouchScreenPos = curScreenPos

        return true
    }

    override fun scrolled(amountX: Float, amountY: Float): Boolean {
        camera.zoom *= (1f + amountY * zoomFactor)
        camera.zoom = MathUtils.clamp(camera.zoom, 0.005f, 0.1f)

        clampBoard()

        camera.update()

        return true
    }


    private fun touch(screenPos: Vector3) {
        val worldPos3 = camera.unproject(screenPos)
        val worldPos = Vector2(worldPos3.x, worldPos3.y)

        val closestVertex = game.vertices.minByOrNull { it.coor.dst(worldPos.x, worldPos.y) }!!

        val visitedVertices = HashSet<String>()
        val cycle = mutableListOf<Vertex>()

        fun angle(u: Vector2, v: Vector2): Float { // angle uov where o is closestVertex
            val ou = Vector2(u.x - worldPos.x, u.y - worldPos.y)
            val ov = Vector2(v.x - worldPos.x, v.y - worldPos.y)
            val ang = ov.angleDeg()
            ou.rotateDeg(-ang)
            // ov.rotateDeg(-ang)
            // now ov is the x axis
            val ret = ou.angleDeg()
            return if (ret < 180f) ret else ret - 360f
        }

        fun findCycleByIterativeDeepeningSearch(v: Vertex, angleSum: Float, depth: Int): Boolean {
            if (visitedVertices.contains(v.getKey())) {
                return same(angleSum, 360f)
            }
            if (depth <= 0) return false

            visitedVertices.add(v.getKey())
            for (u in game.neighbours(v)) {
                val newAngleSum = angleSum + angle(v.coor, u.coor)
                if (findCycleByIterativeDeepeningSearch(u, newAngleSum, depth - 1)) {
                    cycle.add(v)
                    return true
                }
            }
            visitedVertices.remove(v.getKey())
            return false
        }

        var depth = 3
        while (depth <= 20) {
            visitedVertices.clear()
            if (findCycleByIterativeDeepeningSearch(closestVertex, 0f, depth)) break
            ++depth
        }
        var found = false
        for (i in game.filledAreas.indices) {
            if (game.filledAreas[i].sortedWith(compareBy({ it.normalisedX }, { it.normalisedY })) ==
                    cycle.sortedWith(compareBy({ it.normalisedX }, { it.normalisedY }))) {
                game.filledAreas.removeAt(i)
                found = true
                break
            }
        }
        if (!found)
            game.filledAreas.add(cycle)
    }
}