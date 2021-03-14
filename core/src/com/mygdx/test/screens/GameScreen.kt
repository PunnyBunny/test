package com.mygdx.test.screens

import com.badlogic.gdx.Gdx.gl
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector3
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

    init {
        camera.position.set(0f, 0f, 0f)
        camera.zoom = 0.01f

        // add reverse entries, aka e(u, v) -> e(p, q) to e(p, q) -> e(u, v)
        externalEdgeMappings.putAll(externalEdgeMappings.map { (k, v) -> Pair(v, k) })
    }

    override fun render(delta: Float) {
        gl.glClearColor(0f, 0f, 0f, 1f);
        gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.zoom *= 1.001f
        camera.translate(delta, delta, 0f)
        camera.update()
        println("${camera.position}")

        val newShapes = HashSet<Shape>()
        val queue = Queue<Shape>()
        shapes.forEach { queue.addLast(it); newShapes.add(it) }
        while (queue.notEmpty()) {
            val shape = queue.first()
            queue.removeFirst()

            for (nextShape in shape.neighbours()) {
                if (camera.frustum.boundsInFrustum(
                                Vector3(nextShape.x, nextShape.y, 0f),
                                Vector3(nextShape.width * 2, nextShape.height * 2, 0f)) // point visible
                        && !newShapes.contains(nextShape)) {
                    newShapes.add(nextShape)
                    queue.addLast(nextShape)
                }
            }
        }

        shapes = newShapes
        shapes.removeIf { shape ->
            !camera.frustum.boundsInFrustum(
                    Vector3(shape.x, shape.y, 0f),
                    Vector3(shape.width * 2, shape.height * 2, 0f),
            )
        }
        shapes.forEach { it.render(camera, shapeRenderer) }
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
    }
}