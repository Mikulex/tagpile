package com.mikulex.tagpile.view

import com.mikulex.tagpile.model.dto.MediaDTO
import com.mikulex.tagpile.viewmodel.MediaViewModel
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.control.ListView
import javafx.scene.control.TextInputDialog
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

                children += Button("Add Tag").apply {
                    onAction = EventHandler { _ -> openTagDialog() }
                }
            }
        }
    }

    private fun openTagDialog() {
        val mediaPK = model.mediaFile.get().pk
        TextInputDialog().apply {
            headerText = "Add tag"

            resultProperty().addListener { _, _, newValue ->
                newValue?.takeIf { it.isNotBlank() }.let {
                    model.tags.addIf(newValue) { s -> model.addTagToMedia(mediaPK, s) && !model.tags.contains(s) }
                }
            }
        }.show()
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

private fun ObservableList<String>.addIf(newValue: String, func: (String) -> Boolean) {
    if (func(newValue)) {
        this.add(newValue)
    }
}
