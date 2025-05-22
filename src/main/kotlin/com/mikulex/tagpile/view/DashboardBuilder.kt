package com.mikulex.tagpile.view

import com.mikulex.tagpile.model.dto.MediaDTO
import com.mikulex.tagpile.viewmodel.DashBoardViewModel
import com.mikulex.tagpile.viewmodel.MediaViewModelFactory
import javafx.collections.ListChangeListener
import javafx.concurrent.Task
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.ListView
import javafx.scene.control.ScrollPane
import javafx.scene.control.TextField
import javafx.scene.effect.Blend
import javafx.scene.effect.BlendMode
import javafx.scene.effect.ColorInput
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
import javafx.scene.input.TransferMode
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.text.Text
import javafx.stage.FileChooser
import javafx.stage.Stage
import javafx.util.Builder

class DashboardBuilder(
    private val dashboardViewModel: DashBoardViewModel,
    private val mediaViewModelFactory: MediaViewModelFactory
) : Builder<Region> {

    override fun build(): Region? {
        return BorderPane().apply {
            center = buildImageTiles()
            top = buildHeader()
            left = buildTagBar()
            right = buildMetaDataBar()
        }
    }

    private fun buildMetaDataBar() = VBox().apply {
        val previewImageView = ImageView().apply {
            isPreserveRatio = true
            fitWidth = 300.0
        }

        children += previewImageView

        dashboardViewModel.selectedMedias.addListener(ListChangeListener { change ->
            while (change.next()) {
                if (dashboardViewModel.selectedMedias.size == 1) {
                    dashboardViewModel.selectedMedias.firstOrNull()?.url?.toUri()?.let {
                        previewImageView.image = Image(
                            it.toString(),
                            500.0, 500.0, true, true
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
                dashboardViewModel.searchQuery.set("")
                dashboardViewModel.findMedias()

                setOnMouseClicked { event ->
                    dashboardViewModel.selectedMedias.setAll(emptyList())
                }
            }
        }

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

    }

    private fun buildHeader(): HBox = HBox().apply {
        val fileChooser = FileChooser().apply {
            title = "Open File"
            extensionFilters.setAll(FileChooser.ExtensionFilter("images", "*.jpg", "*.jpeg", "*.png", "*.gif"))
        }
        children += Button("Import Image").apply {
            setOnAction {
                fileChooser.showOpenMultipleDialog(this.scene.window)
                    ?.let {
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
        this.image = null
        this.isPreserveRatio = true
        this.userData = media

        val task = object : Task<Image>() {
            override fun call(): Image {
                return Image(media.url?.toUri().toString(), 150.0, 0.0, true, true)
            }
        }

        task.setOnSucceeded {
            image = task.value
        }
        with(Thread(task)) {
            isDaemon = true
            start()
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
            dashboardViewModel.selectedMedias.setAll(listOf(media))
            event.consume()
            if (event.clickCount == 2) {
                with(Stage()) {
                    val imageViewModel = mediaViewModelFactory.create().apply {
                        mediaFile.set(media)
                        this.findTags(media.pk)
                    }
                    this.scene = Scene(MediaViewerBuilder(imageViewModel).build(), 1920.0, 1080.0)
                    this.show()
                }
            }
        }
    }
}