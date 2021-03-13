package com.mygdx.test.screens

import com.badlogic.gdx.scenes.scene2d.Stage
import ktx.app.KtxScreen
import ktx.scene2d.actors
import ktx.scene2d.label
import ktx.scene2d.table

class MenuScreen(private val stage: Stage) : KtxScreen {
    init {
        stage.actors {
            table {
                setFillParent(true)
                label("Hello world!")
            }
        }
    }

    override fun render(delta: Float) {
        stage.act()
        stage.draw()
    }
}