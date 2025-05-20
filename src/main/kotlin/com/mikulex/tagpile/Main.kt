package com.mikulex.tagpile

import com.mikulex.tagpile.model.MediaModel
import com.mikulex.tagpile.sources.DatabaseMediaSource
import com.mikulex.tagpile.view.DashboardBuilder
import com.mikulex.tagpile.viewmodel.DashBoardViewModel
import com.mikulex.tagpile.viewmodel.MediaViewModelFactory
import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import org.slf4j.LoggerFactory

class Main : Application() {
    companion object {
        private const val WINDOW_WIDTH = 1920.0
        private const val WINDOW_HEIGHT = 1080.0
        private val LOG = LoggerFactory.getLogger(Main::class.java)
    }

    var mediaModel: MediaModel? = null

    override fun start(stage: Stage) {
        with(stage) {
            title = "tagpile"
            scene = createMainScene()
            show()
        }
    }

    override fun init() {
        LOG.debug("Starting init thread")
        val source = DatabaseMediaSource()
        source.initDatabase()
        mediaModel = MediaModel(source)
    }

    private fun createMainScene(): Scene {
        val viewModel = DashBoardViewModel(mediaModel!!)
        val dashboardBuilder = DashboardBuilder(viewModel, MediaViewModelFactory(mediaModel!!))
        val rootNode = dashboardBuilder.build()
        return Scene(rootNode, WINDOW_WIDTH, WINDOW_HEIGHT)
    }
}

fun main() {
    Application.launch(Main::class.java)
}