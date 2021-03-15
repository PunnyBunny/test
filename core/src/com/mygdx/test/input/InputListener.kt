package com.mygdx.test.input

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.TimeUtils
import com.mygdx.test.Game
import com.mygdx.test.shape.Vertex
import com.mygdx.test.shape.same
import ktx.app.KtxInputAdapter

class InputListener(private val game: Game, private val camera: OrthographicCamera) : KtxInputAdapter {
    private var lastTouchTime = 0L
    private var dragged = false
    private var lastTouchScreenPos = Vector3()

    private val zoomFactor = 0.1f

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val curTime = TimeUtils.nanoTime()
        val curScreenPos = Vector3(screenX.toFloat(), -screenY.toFloat(), 0f)

        lastTouchScreenPos = curScreenPos
        lastTouchTime = curTime
        dragged = false

        return true
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        val curScreenPos = Vector3(screenX.toFloat(), -screenY.toFloat(), 0f)
        val delta = lastTouchScreenPos.cpy().sub(curScreenPos).scl(camera.zoom)

        camera.translate(delta)
        lastTouchScreenPos = curScreenPos
        dragged = true
        return true
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val curScreenPos = Vector3(screenX.toFloat(), screenY.toFloat(), 0f)
        val curTime = TimeUtils.nanoTime()

        if (!dragged) { // <= 500ms
            touch(curScreenPos)
        }

        lastTouchScreenPos = curScreenPos

        return true
    }

    override fun scrolled(amountX: Float, amountY: Float): Boolean {
        camera.zoom *= (1f + amountY * zoomFactor)

        return true
    }

    private fun touch(screenPos: Vector3) {
        val worldPos3 = camera.unproject(screenPos)
        val worldPos = Vector2(worldPos3.x, worldPos3.y)

        val closestVertex = game.vertices.minByOrNull { it.coor.dst(worldPos.x, worldPos.y) }!!

        data class Node(val curr: Vertex, val prv: Vertex?, val angleSum: Float)

        val visitedVertices = HashSet<String>() // vertex and angle sum
//        val queue = ArrayDeque<Node>()
//        visitedVertices[closestVertex.getKey()] = Node(closestVertex, null, 0f)
//        queue.addLast(Node(closestVertex, null, 0f))

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
        if (cycle in game.filledAreas) game.filledAreas.remove(cycle)
        else game.filledAreas.add(cycle)
        cycle.forEach { println(it.coor) }

    }
}