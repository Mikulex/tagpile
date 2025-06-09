package com.mikulex.tagpile.view.dashboard

import com.mikulex.tagpile.model.dto.MediaDTO
import com.mikulex.tagpile.view.MediaViewerBuilder
import com.mikulex.tagpile.viewmodel.DashBoardViewModel
import com.mikulex.tagpile.viewmodel.MediaViewModelFactory
import javafx.beans.binding.Bindings
import javafx.collections.ListChangeListener
import javafx.concurrent.Task
import javafx.event.EventHandler
import javafx.geometry.Orientation
import javafx.scene.Scene
import javafx.scene.control.ScrollPane
import javafx.scene.control.SplitPane
import javafx.scene.control.TextInputDialog
import javafx.scene.effect.Blend
import javafx.scene.effect.BlendMode
import javafx.scene.effect.ColorInput
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.*
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import javafx.scene.layout.TilePane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.text.Text
import javafx.stage.Stage
import javafx.util.Builder
import org.slf4j.LoggerFactory

class MediaPaneBuilder(
    private val dashboardViewModel: DashBoardViewModel,
    private val mediaViewModelFactory: MediaViewModelFactory
) : Builder<Region> {
    companion object {
        private val LOG = LoggerFactory.getLogger(MediaPaneBuilder::class.java)
    }

    override fun build() = SplitPane().apply {
        items += createTiles()
        items += MetadataBarBuilder(dashboardViewModel).build()
        dashboardViewModel.imageWidthProperty.bind(
            Bindings.subtract(1, this.dividers[0].positionProperty()).multiply(this.widthProperty()).subtract(20)
        )
        dashboardViewModel.metadataWithProperty.bind(this.widthProperty().multiply(0.30))

        orientation = Orientation.HORIZONTAL
    }

    private fun createTiles() = StackPane().also { stack ->
        LOG.info("Initializing tile pane")

        stack.children += ScrollPane().apply {
            hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
            isFitToWidth = true

            content = TilePane().apply {
                prefColumns = 8
                hgap = 10.0
                vgap = 10.0

                dashboardViewModel.results.addListener(ListChangeListener { change ->
                    while (change.next()) {
                        if (change.wasRemoved()) {
                            change.removed.forEach { r ->
                                children.removeIf { it.userData == r }
                            }
                        }

                        if (change.wasAdded()) {
                            children += change.addedSubList.map { file ->
                                createTile(file)
                            }
                        }
                    }
                })

                setOnMouseClicked { event ->
                    if (!event.isControlDown) {
                        dashboardViewModel.selectedMedias.setAll(emptyList())
                    }
                }

                sceneProperty().addListener { _, _, newScene ->
                    newScene.setOnKeyPressed { event ->
                        if (event.code.equals(KeyCode.ESCAPE)) {
                            dashboardViewModel.selectedMedias.clear()
                        } else if (event.code.equals(KeyCode.A) && event.isControlDown) {
                            dashboardViewModel.selectAll()
                        } else if (event.code.equals(KeyCode.DELETE)) {
                            dashboardViewModel.deleteSelected()
                        } else if (event.code == KeyCode.C && event.isControlDown) {
                            if (dashboardViewModel.selectedMedias.size == 1) {
                                LOG.debug("Copying Media to Clipboard")
                                dashboardViewModel.selectedMedias.first().url?.toUri()?.toString()
                                    ?.let {
                                        Clipboard.getSystemClipboard().setContent(ClipboardContent().apply {
                                            putImage(Image(it))
                                        })
                                    }
                            }
                        } else if (event.code == KeyCode.T) {
                            TextInputDialog().apply {
                                headerText =
                                    "Enter tags to add to ${dashboardViewModel.selectedMedias.size} selected medias"

                                resultProperty().addListener { _, _, newValue ->
                                    newValue?.takeIf { it.isNotBlank() }?.let {
                                        dashboardViewModel.addTagsToSelectedMedias(it)
                                    }
                                }
                                show()

                            }
                        }
                    }
                }

                dashboardViewModel.searchQuery.set("")
                dashboardViewModel.findMedias()
            }
        }

        //Drag and Drop Handling
        val importOverlay = StackPane().apply {
            children += Rectangle().apply {
                fill = Color.LIGHTBLUE.deriveColor(0.0, 1.0, 1.0, 0.8)
                stroke = Color.LIGHTBLUE
            }.also { rect ->
                rect.widthProperty().bind(stack.widthProperty().subtract(100))
                rect.heightProperty().bind(stack.heightProperty().subtract(100))
            }
            children += Text("Drag to import images")
            isVisible = false
        }

        stack.children += importOverlay

        stack.setOnDragEntered { event ->
            importOverlay.isVisible = true
            event.consume()
        }

        stack.setOnDragOver { event ->
            if (event.dragboard.hasFiles()) {
                event.acceptTransferModes(TransferMode.LINK)
            }
            event.consume()
        }

        stack.setOnDragDropped { event ->
            dashboardViewModel.importFiles(event.dragboard.files)
            dashboardViewModel.findMedias()
        }

        stack.setOnDragExited { event ->
            importOverlay.isVisible = false
            event.consume()
        }
        LOG.info("Finished tile pane initialization")
    }

    private fun createTile(media: MediaDTO) = ImageView().apply {
        image = null
        isPreserveRatio = true
        userData = media
        isPickOnBounds = true
        val fitWidth = 200.0
        fitWidthProperty().bind(Bindings.multiply(fitWidth,dashboardViewModel.zoomLevel))
        Thread.startVirtualThread(
            object : Task<Image>() {
                override fun call(): Image {
                    return Image(media.url?.toUri().toString(), 200.0, 0.0, true, true)
                }
            }.apply {
                setOnSucceeded {
                    image = this.value
                }
            }
        )

        dashboardViewModel.selectedMedias.addListener(ListChangeListener { change ->
            while (change.next()) {
                if (change.wasRemoved() && change.removed.contains(media)) {
                    effect = null
                }
                if (change.wasAdded() && change.addedSubList.contains(media)) {
                    effect = Blend().apply {
                        mode = BlendMode.MULTIPLY
                        bottomInput = ColorInput(0.0, 0.0, image.width, image.height, Color.rgb(125, 125, 255))
                    }
                }
            }
        })

        this.addEventHandler(MouseEvent.MOUSE_CLICKED) { event ->
            event.consume()

            if (!event.isShiftDown && !event.isControlDown) {
                dashboardViewModel.setLastSelectedMedia(media)
                dashboardViewModel.selectedMedias.setAll(listOf(media))
            } else if (event.isControlDown && event.isShiftDown) {
                dashboardViewModel.selectTo(media, false)
            } else if (event.isControlDown && !event.isShiftDown) {
                dashboardViewModel.setLastSelectedMedia(media)
                if (dashboardViewModel.selectedMedias.contains(media)) {
                    dashboardViewModel.selectedMedias.remove(media)
                } else {
                    dashboardViewModel.selectedMedias.add(media)
                }
            } else if (!event.isControlDown && event.isShiftDown) {
                dashboardViewModel.selectTo(media, true)
            }
        }

        this.addEventHandler(MouseEvent.MOUSE_CLICKED) { event ->
            if (event.clickCount == 2) {
                with(Stage()) {
                    val imageViewModel =
                        mediaViewModelFactory.create().apply { mediaFile.set(media) }.also { it.findTags() }
                    this.title = "${media.url.toString()} - tagpile"
                    this.scene = Scene(MediaViewerBuilder(imageViewModel).build(), 1920.0, 1080.0)
                    this.show()
                }
            }
        }
    }
}

