package com.mygdx.test.input

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.TimeUtils
import ktx.app.KtxInputAdapter

class InputListener(private val camera: OrthographicCamera) : KtxInputAdapter {
    private var lastTouchTime = 0L
    private var lastPos = Vector3()

    private val zoomFactor = 0.1f

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        lastTouchTime = TimeUtils.nanoTime()
        val curPos = camera.unproject(Vector3(screenX.toFloat(), screenY.toFloat(), 0f))
        lastPos = curPos

        return true
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        val curPos = camera.unproject(Vector3(screenX.toFloat(), screenY.toFloat(), 0f))
        camera.translate(lastPos.x - curPos.x, lastPos.y - curPos.y, 0f)
        lastPos = curPos

        return true
    }

    override fun scrolled(amountX: Float, amountY: Float): Boolean {
        camera.zoom *= (1f + amountY * zoomFactor)

        return true
    }
}