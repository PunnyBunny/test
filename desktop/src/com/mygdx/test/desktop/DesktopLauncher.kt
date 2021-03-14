package com.mygdx.test.desktop

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.mygdx.test.Game

object DesktopLauncher {
    @JvmStatic
    fun main(arg: Array<String>) {
        val config = Lwjgl3ApplicationConfiguration().apply {
            setTitle("test")
            setWindowedMode(1000, 750)
            setForegroundFPS(60)
        }
        Lwjgl3Application(Game(), config)
    }
}