package com.mikulex.tagpile.view

import com.mikulex.tagpile.model.dto.MediaDTO
import com.mikulex.tagpile.viewmodel.MediaViewModel
import javafx.scene.control.ListView
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Region
import javafx.util.Builder

class ImageViewerBuilder(private val model: MediaViewModel) : Builder<Region> {

    override fun build(): Region {
        return BorderPane().apply {
            center = createImageTile(model.mediaFile.get())
            left = ListView(model.tags)
        }
    }

    private fun createImageTile(file: MediaDTO) = ImageView().apply {
        image = Image(file.url?.toUri().toString())
        isPreserveRatio = true
        userData = file

        sceneProperty().addListener { _, _, newScene ->
            if (newScene != null) {
                fitWidthProperty().bind(newScene.widthProperty())
                fitHeightProperty().bind(newScene.heightProperty())
            }
        }

    }
}