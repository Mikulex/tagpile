package com.mikulex.tagpile.view

import com.mikulex.tagpile.model.dto.MediaDTO
import com.mikulex.tagpile.viewmodel.MediaViewModelFactory
import com.mikulex.tagpile.viewmodel.SearchStateViewModel
import javafx.collections.ListChangeListener
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import javafx.scene.effect.Blend
import javafx.scene.effect.BlendMode
import javafx.scene.effect.ColorInput
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.stage.FileChooser
import javafx.stage.Stage
import javafx.util.Builder

class DashboardBuilder(
    private val searchStateModel: SearchStateViewModel,
    private val mediaViewModelFactory: MediaViewModelFactory
) : Builder<Region> {

    override fun build(): Region? {
        return BorderPane().apply {
            center = buildCenter()
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

        searchStateModel.selectedMedias.addListener(ListChangeListener { change ->
            while (change.next()) {
                if (searchStateModel.selectedMedias.size == 1) {
                    searchStateModel.selectedMedias.firstOrNull()?.url?.toUri()?.let {
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

    private fun buildCenter() = TilePane().apply {
        prefColumns = 8
        hgap = 10.0
        vgap = 10.0

        searchStateModel.results.addListener(ListChangeListener { change ->
            while (change.next()) {
                if (change.wasRemoved()) {
                    change.removed.forEach { r ->
                        children.removeIf { it.userData == r }
                    }
                }

                if (change.wasAdded()) {
                    children += change.addedSubList.map { file -> createDashboardTile(file) }
                }
            }
        })
        searchStateModel.searchQuery.set("")
        searchStateModel.findMedias()

        addEventHandler(MouseEvent.MOUSE_CLICKED) { event ->
            searchStateModel.selectedMedias.setAll(emptyList())
        }
    }

    private fun buildHeader(): HBox = HBox().apply {
        val fileChooser = FileChooser().apply {
            title = "Open File"
        }
        children += Button("Import Image").apply {
            setOnAction {
                fileChooser.showOpenDialog(this.scene.window)
                    ?.let(searchStateModel::importFile)
            }
        }
        children += TextField().apply {
            this.textProperty().bindBidirectional(searchStateModel.searchQuery)
            this.promptText = "Search"
            this.setOnAction {
                searchStateModel.findMedias()
            }
        }
    }

    private fun buildTagBar(): VBox {
        val listView: ListView<String> = ListView(searchStateModel.resultTags)
        return VBox().apply {
            children.add(listView)
            prefWidth = 100.0
        }
    }

    private fun createDashboardTile(media: MediaDTO) = ImageView().apply {
        this.image = Image(media.url?.toUri().toString(), 150.0, 0.0, true, true)
        this.isPreserveRatio = true
        this.userData = media

        searchStateModel.selectedMedias.addListener(ListChangeListener { change ->
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
            searchStateModel.selectedMedias.setAll(listOf(media))
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