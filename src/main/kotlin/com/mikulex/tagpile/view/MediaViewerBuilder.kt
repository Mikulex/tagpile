package com.mikulex.tagpile.view

import com.mikulex.tagpile.model.dto.MediaDTO
import com.mikulex.tagpile.viewmodel.MediaViewModel
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.util.Builder

class MediaViewerBuilder(private val model: MediaViewModel) : Builder<Region> {
    override fun build(): Region {
        return BorderPane().apply {
            center = createImageTile(model.mediaFile.get())
            left = VBox().apply {
                children += ListView(model.tags).apply { isEditable = true }
                children += TextField().apply {
                    promptText = "Enter tag"
                    textProperty().bindBidirectional(model.newTag)
                    onAction = EventHandler { _ -> model.addTagToMedia() }
                }

                children += Button("Add Tag").apply {
                    onAction = EventHandler { _ -> model.addTagToMedia() }
                }
            }
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
