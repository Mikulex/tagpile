package com.mikulex.tagpile.view

import com.mikulex.tagpile.model.dto.FileDTO
import com.mikulex.tagpile.viewmodel.ImageViewerViewModel
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.util.Builder

class ImageViewerBuilder(private val model: ImageViewerViewModel) : Builder<Region> {

    override fun build(): Region {
        return VBox().apply {
            children += createImageTile(model.mediaFile.get())
        }
    }

    private fun createImageTile(file: FileDTO) = ImageView().apply {
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