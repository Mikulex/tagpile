package com.mikulex.tagpile.view

import com.mikulex.tagpile.model.dto.MediaDTO
import com.mikulex.tagpile.viewmodel.DashBoardViewModel
import com.mikulex.tagpile.viewmodel.MediaViewModelFactory
import javafx.collections.ListChangeListener
import javafx.concurrent.Task
import javafx.geometry.Orientation
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.effect.Blend
import javafx.scene.effect.BlendMode
import javafx.scene.effect.ColorInput
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseEvent
import javafx.scene.input.TransferMode
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.text.Text
import javafx.stage.FileChooser
import javafx.stage.Stage
import javafx.util.Builder
import org.slf4j.LoggerFactory

class DashboardBuilder(
    private val dashboardViewModel: DashBoardViewModel, private val mediaViewModelFactory: MediaViewModelFactory
) : Builder<Region> {
    companion object {
        private val LOG = LoggerFactory.getLogger(DashboardBuilder::class.java)
    }

    override fun build(): Region? {
        return BorderPane().apply {
            center = buildImageTiles()
            top = buildHeader()
            left = buildTagBar()
            right = buildMetaDataBar()
            bottom = buildInfoBar()
        }
    }

    private fun buildInfoBar() = HBox().apply {
        children += Text("0 total items").apply {
            dashboardViewModel.results.addListener(ListChangeListener { change ->
                while (change.next()) {
                    this.text = "${change.list.size} total items"
                }
            })
        }

        children += Separator().apply {
            orientation = Orientation.VERTICAL
        }

        children += Text("0 items selected").apply {
            dashboardViewModel.selectedMedias.addListener(ListChangeListener { change ->
                while (change.next()) {
                    this.text = "${change.list.size} items selected"
                }
            })
        }
    }

    private fun buildMetaDataBar() = VBox().apply {
        val previewImageView = ImageView().apply {
            isPreserveRatio = true
            fitWidth = 300.0
        }

        children += previewImageView

        children += Text().apply {
            dashboardViewModel.selectedMedias.addListener(ListChangeListener { change ->
                while (change.next()) {
                    this.text = "${change.list.size} items selected"
                }
            })
        }

        dashboardViewModel.selectedMedias.addListener(ListChangeListener { change ->
            while (change.next()) {
                if (dashboardViewModel.selectedMedias.size == 1) {
                    dashboardViewModel.selectedMedias.firstOrNull()?.url?.toUri()?.let {
                        previewImageView.image = Image(
                            it.toString(), 500.0, 500.0, true, true
                        )
                        previewImageView.isVisible = true
                    } ?: let {
                        previewImageView.image = null
                        previewImageView.isVisible = false
                    }
                } else {
                    previewImageView.image = null
                    previewImageView.isVisible = false
                }
            }
        })
    }

    private fun buildImageTiles() = StackPane().also { stack ->
        LOG.info("Initializing tile pane")

        stack.children += ScrollPane().apply {
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
                                createDashboardTile(file)
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

    private fun buildHeader(): HBox = HBox().apply {
        val fileChooser = FileChooser().apply {
            title = "Open File"
            extensionFilters.setAll(FileChooser.ExtensionFilter("images", "*.jpg", "*.jpeg", "*.png", "*.gif"))
        }
        children += Button("Import Image").apply {
            setOnAction {
                fileChooser.showOpenMultipleDialog(this.scene.window)?.let {
                    dashboardViewModel.importFiles(it)
                    dashboardViewModel.findMedias()
                }
            }
        }
        children += TextField().apply {
            this.textProperty().bindBidirectional(dashboardViewModel.searchQuery)
            this.promptText = "Search"
            this.setOnAction {
                dashboardViewModel.findMedias()
            }
        }
    }

    private fun buildTagBar(): VBox {
        val listView: ListView<String> = ListView(dashboardViewModel.resultTags)
        return VBox().apply {
            children.add(listView)
            prefWidth = 100.0
        }
    }

    private fun createDashboardTile(media: MediaDTO) = ImageView().apply {
        image = null
        isPreserveRatio = true
        userData = media
        isPickOnBounds = true

        object : Task<Image>() {
            override fun call(): Image {
                return Image(media.url?.toUri().toString(), 150.0, 0.0, true, true)
            }
        }.apply {
            setOnSucceeded {
                image = this.value
            }
            Thread.startVirtualThread(this)
        }

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