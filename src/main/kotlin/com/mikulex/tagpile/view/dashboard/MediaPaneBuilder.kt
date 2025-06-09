package com.mikulex.tagpile.view.dashboard

import com.mikulex.tagpile.model.dto.MediaDTO
import com.mikulex.tagpile.view.MediaViewerBuilder
import com.mikulex.tagpile.viewmodel.DashBoardViewModel
import com.mikulex.tagpile.viewmodel.MediaViewModelFactory
import javafx.beans.binding.Bindings
import javafx.collections.ListChangeListener
import javafx.concurrent.Task
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.ScrollPane
import javafx.scene.control.SplitPane
import javafx.scene.control.TextInputDialog
import javafx.scene.effect.Blend
import javafx.scene.effect.BlendMode
import javafx.scene.effect.ColorInput
import javafx.scene.effect.ImageInput
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.*
import javafx.scene.layout.Background
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
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
                hgap = 20.0
                vgap = 20.0

                tileAlignment = Pos.CENTER
                padding = Insets(30.0)

                prefTileWidthProperty().bind(Bindings.multiply(250.0, dashboardViewModel.zoomLevel))
                prefTileHeightProperty().bind(Bindings.multiply(250.0, dashboardViewModel.zoomLevel))

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

    private fun createTile(media: MediaDTO) = HBox().also { box ->
        box.alignment = Pos.CENTER
        box.children += ImageView().also { imageView ->
            imageView.image = null
            imageView.userData = media
            imageView.isPickOnBounds = true
            imageView.isPreserveRatio = true

            Thread.startVirtualThread(
                object : Task<Image>() {
                    override fun call(): Image {
                        return Image(media.url?.toUri().toString(), 200.0, 200.0, true, true)
                    }
                }.apply {
                    setOnSucceeded {
                        imageView.image = this.value
                        imageView.fitWidthProperty().bind(box.widthProperty().multiply(0.9))
                        imageView.fitHeightProperty().bind(box.heightProperty().multiply(0.9))
                        imageView.xProperty()
                            .bind(Bindings.subtract(box.widthProperty(), imageView.prefWidth(-1.0)).divide(2))
                        imageView.yProperty()
                            .bind(Bindings.subtract(box.heightProperty(), imageView.prefHeight(-1.0)).divide(2))
                    }
                }
            )

            dashboardViewModel.selectedMedias.addListener(ListChangeListener { change ->
                while (change.next()) {
                    if (change.wasRemoved() && change.removed.contains(media)) {
                        imageView.effect = null
                    }
                    if (change.wasAdded() && change.addedSubList.contains(media)) {
                        imageView.effect = Blend().also { blend ->
                            blend.mode = BlendMode.MULTIPLY
                            blend.bottomInput = ColorInput().also { select ->
                                select.paint = Color.rgb(125, 125, 255)
                                select.widthProperty().bind(box.widthProperty())
                                select.heightProperty().bind(box.heightProperty())
                            }
                        }
                    }
                }
            })

            imageView.addEventHandler(MouseEvent.MOUSE_CLICKED) { event ->
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

            imageView.addEventHandler(MouseEvent.MOUSE_CLICKED) { event ->
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
}

