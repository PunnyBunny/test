package com.mygdx.test

import com.badlogic.gdx.Gdx.input
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.mygdx.test.input.InputListener
import com.mygdx.test.screens.GameScreen
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.inject.Context
import ktx.inject.register

class Game : KtxGame<KtxScreen>() {
    private val context = Context()

    override fun create() {
        context.register {
            bindSingleton(Stage())
            bindSingleton(ShapeRenderer())
            bindSingleton<Camera>(OrthographicCamera().apply { setToOrtho(false, 1000f, 750f) })
            bindSingleton<Viewport>(ExtendViewport(1000f, 700f, inject()))

//            addScreen(MenuScreen(inject()))
            addScreen(GameScreen(
                    inject<Camera>() as OrthographicCamera,
                    inject(),
                    inject(),
            ))
            input.inputProcessor = InputListener(inject<Camera>() as OrthographicCamera)
        }
        setScreen<GameScreen>()
    }

    override fun dispose() {
        context.dispose()
        super.dispose()
    }
}
