package com.mikulex.tagpile.view

import com.mikulex.tagpile.model.dto.MediaDTO
import com.mikulex.tagpile.viewmodel.MediaViewModelFactory
import com.mikulex.tagpile.viewmodel.SearchStateViewModel
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
import javafx.scene.layout.*
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
            left = buildSideBar()
            right = VBox()
        }
    }

    private fun buildCenter() = TilePane().apply {
        prefColumns = 4
        hgap = 10.0
        vgap = 10.0
        searchStateModel.results.addListener(ListChangeListener { change ->
            while (change.next()) {
                if (change.wasAdded()) {
                    children += change.addedSubList.map { file -> createImageTile(file) }
                }
                if (change.wasRemoved()) {
                    change.removed.forEach { r ->
                        children.removeIf { it.userData == r }
                    }
                }
            }
        })
    }

    private fun buildHeader(): HBox = HBox().apply {
        val fileChooser = FileChooser().apply {
            title = "Open File"
        }
        children += Button("Open").apply {
            setOnAction {
                fileChooser.showOpenDialog(this.scene.window)
                    ?.let(searchStateModel::importFile)
            }
        }
        children += TextField().apply {
            this.textProperty().bindBidirectional(searchStateModel.query)
            this.promptText = "Search"
            this.setOnAction {
                searchStateModel.query.bindBidirectional(this.textProperty())
                searchStateModel.findMedias()
            }
        }
    }

    private fun buildSideBar(): VBox {
        val tags: ObservableList<String> = FXCollections.observableArrayList()
        searchStateModel.query.addListener { observable -> tags.setAll(searchStateModel.query.get().split(" ")) }
        val listView: ListView<String> = ListView(tags)
        return VBox().apply {
            children.add(listView)
            prefWidth = 100.0
        }
    }

    private fun createImageTile(file: MediaDTO) = ImageView().apply {
        this.image = Image(file.url?.toUri().toString(), 150.0, 150.0, true, true)
        this.isPreserveRatio = true
        this.userData = file
        this.addEventHandler(MouseEvent.MOUSE_CLICKED) { event ->
            if (event.clickCount == 2) {
                with(Stage()) {
                    val imageViewModel = mediaViewModelFactory.create().apply {
                        mediaFile.set(file)
                        this.findTags(file.pk)
                    }
                    this.scene = Scene(ImageViewerBuilder(imageViewModel).build(), 1920.0, 1080.0)
                    this.show()
                }
            }
        }
    }
}