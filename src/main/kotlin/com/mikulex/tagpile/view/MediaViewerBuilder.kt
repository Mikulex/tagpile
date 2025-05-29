package com.mikulex.tagpile.view

import com.mikulex.tagpile.model.dto.MediaDTO
import com.mikulex.tagpile.viewmodel.MediaViewModel
import javafx.application.Platform
import javafx.collections.ListChangeListener
import javafx.event.EventHandler
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.util.Builder
import javafx.util.Callback
import org.slf4j.LoggerFactory

class MediaViewerBuilder(private val model: MediaViewModel) : Builder<Region> {
    companion object {
        private val LOG = LoggerFactory.getLogger(MediaViewerBuilder::class.java)
    }

    override fun build(): Region {
        LOG.debug("Initializing Media Viewer for {}", model.mediaFile.get())

        return BorderPane().apply {
            center = createImageTile(model.mediaFile.get())
            left = VBox().apply {
                children += ListView(model.tags).apply {
                    isEditable = false
                    selectionModel.selectionMode = SelectionMode.MULTIPLE
                    cellFactory = Callback { StringListCell(model) }

                    selectionModel.selectedItems.addListener(ListChangeListener { change ->
                        model.tagsToRemove.clear()
                        while (change.next()) {
                            if (change.wasAdded()) {
                                model.tagsToRemove.addAll(change.addedSubList)
                            }
                        }
                    })
                }

                children += TextField().apply {
                    promptText = "Enter Tag"
                    textProperty().bindBidirectional(model.newTag)
                    onAction = EventHandler { _ -> model.addTagToMedia() }
                }

                children += HBox().apply {
                    children += Button("Add Tag").apply {
                        onAction = EventHandler { _ -> model.addTagToMedia() }
                    }

                    children += Button("Remove selected").apply {
                        onAction = EventHandler { _ ->
                            model.removeTags()
                        }
                    }
                }
            }

            setOnKeyPressed { event ->
                if (event.code.equals(KeyCode.ESCAPE)) {
                    scene.window.hide()
                }
            }

            sceneProperty().addListener { _, _, newScene ->
                if (newScene != null) {
                    Platform.runLater {
                        requestFocus()
                    }
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

private class StringListCell(val model: MediaViewModel) : ListCell<String>() {
    override fun updateItem(item: String?, empty: Boolean) {
        super.updateItem(item, empty)
        if (empty || item?.isEmpty() == true) {
            text = null
            graphic = null
        } else {
            text = item
            graphic = Button().also { btn ->
                btn.text = "-"
                btn.setOnAction {
                    model.tagsToRemove.setAll(listOf(item))
                    model.removeTags()
                    model.findTags()
                }
            }
        }
    }
}

