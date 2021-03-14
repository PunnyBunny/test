package com.mygdx.test.screens

import com.badlogic.gdx.Gdx.gl
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Queue
import com.badlogic.gdx.utils.viewport.Viewport
import com.mygdx.test.shape.Edge
import com.mygdx.test.shape.Region
import com.mygdx.test.shape.Shape
import com.mygdx.test.shape.Vertex
import ktx.app.KtxScreen

class GameScreen(
        private val camera: OrthographicCamera,
        private val viewport: Viewport,
        private val shapeRenderer: ShapeRenderer,
) : KtxScreen {
    private val region1 = Region(listOf(
            Edge(Vertex(0f, 0f), Vertex(0f, 1f)),
            Edge(Vertex(0f, 1f), Vertex(1f, 1f)),
            Edge(Vertex(1f, 1f), Vertex(1f, 0f)),
            Edge(Vertex(1f, 0f), Vertex(0f, 0f)),
    ))
    private val region2 = Region(listOf(
            Edge(Vertex(1f, 0f), Vertex(1f, 1f)),
            Edge(Vertex(1f, 1f), Vertex(2f, 1f)),
            Edge(Vertex(2f, 1f), Vertex(2f, 0f)),
            Edge(Vertex(2f, 0f), Vertex(1f, 0f)),
    ))
    private val externalEdgeMappings = hashMapOf(
            Pair(
                    Edge(Vertex(0f, 1f), Vertex(1f, 1f)),
                    Edge(Vertex(0f, 0f), Vertex(1f, 0f)),
            ),
            Pair(
                    Edge(Vertex(1f, 1f), Vertex(2f, 1f)),
                    Edge(Vertex(1f, 0f), Vertex(2f, 0f)),
            ),
            Pair(
                    Edge(Vertex(0f, 1f), Vertex(0f, 0f)),
                    Edge(Vertex(2f, 1f), Vertex(2f, 0f)),
            ),
    )
    private var shapes = hashSetOf(Shape(0f, 0f, listOf(region1, region2), externalEdgeMappings))
    private val screenBuffer = 200f

    init {
        camera.position.set(0f, 0f, 0f)
        camera.zoom = 0.01f

        // add reverse entries, aka e(u, v) -> e(p, q) to e(p, q) -> e(u, v)
        externalEdgeMappings.putAll(externalEdgeMappings.map { (k, v) -> Pair(v, k) })
        externalEdgeMappings.putAll(externalEdgeMappings.map { (k, v) ->
            Pair(Edge(k.b, k.a), Edge(v.b, v.a))
        })
    }

    override fun render(delta: Float) {
        camera.update()
        gl.glClearColor(0f, 0f, 0f, 1f)
        gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        camera.update()

        val newShapes = HashSet<Shape>()
        val queue = Queue<Shape>()
        shapes.forEach { queue.addLast(it); newShapes.add(it) }

//        for ((k, v) in externalEdgeMappings) {
//            println("e((${k.a.deltaX}, ${k.a.deltaY}),(${k.b.deltaX}, ${k.b.deltaY}))->" +
//                    "e((${v.a.deltaX}, ${v.a.deltaY}),(${v.b.deltaX}, ${v.b.deltaY}))")
//        }
        while (queue.notEmpty()) {
            val shape = queue.first()
            queue.removeFirst()

            for (nextShape in shape.neighbours()) {
                println("${nextShape.x} ${nextShape.y}")
                if (camera.frustum.boundsInFrustum(
                                nextShape.x, nextShape.y, 0f,
                                nextShape.width, nextShape.height, 0f
                        ) && !newShapes.contains(nextShape)) {
                    newShapes.add(nextShape)
                    queue.addLast(nextShape)
                }
            }
        }

        shapes = newShapes
        shapes.forEach { it.render(camera, shapeRenderer) }
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
    }
}