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
import javafx.scene.input.ScrollEvent
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.util.Builder
import javafx.util.Callback
import org.slf4j.LoggerFactory
import kotlin.math.max
import kotlin.math.pow


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

    private fun createImageTile(file: MediaDTO) = ScrollPane().also { scrollPane ->
        scrollPane.isPannable = true

        scrollPane.content = ImageView().apply {
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

        scrollPane.addEventFilter(ScrollEvent.SCROLL) { event ->
            event.consume()
            val img = scrollPane.content as ImageView
            img.fitWidthProperty().unbind()
            img.fitHeightProperty().unbind()

            val viewportBounds = scrollPane.viewportBounds
            val contentBounds = img.boundsInLocal

            val scrollableXDistance = contentBounds.width - viewportBounds.width
            val mouseXRatio =
                (event.x + scrollPane.hvalue * scrollableXDistance) / contentBounds.width

            val scrollableYDistance = contentBounds.height - viewportBounds.height
            val mouseYRatio =
                (event.y + scrollPane.vvalue * scrollableYDistance) / contentBounds.height

            val scale = 1.005.pow(event.deltaY)
            img.fitWidth = max(img.fitWidth * scale, img.scene.width)
            img.fitHeight = max(img.fitHeight * scale, img.scene.height)

            val newWidth = img.fitWidth
            val newHeight = img.fitHeight

            scrollPane.hvalue = (mouseXRatio * newWidth - event.x) / (newWidth - viewportBounds.width)
            scrollPane.vvalue = (mouseYRatio * newHeight - event.y) / (newHeight - viewportBounds.height)
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