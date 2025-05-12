package com.mikulex.tagpile.view

import com.mikulex.tagpile.model.dto.FileDTO
import com.mikulex.tagpile.viewmodel.SearchStateViewModel
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.scene.control.Button
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.stage.FileChooser
import javafx.util.Builder

class DashboardBuilder(searchStateModel: SearchStateViewModel) : Builder<Region> {
    private val model = searchStateModel

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
        model.results.addListener(ListChangeListener { change ->
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
                    ?.let(model::importFile)
            }
        }
        children += TextField().apply {
            this.textProperty().bindBidirectional(model.query)
            this.promptText = "Search"
            this.setOnAction {
                model.query.bindBidirectional(this.textProperty())
                model.findMedias()
            }
        }
    }

    private fun buildSideBar(): VBox {
        val tags: ObservableList<String> = FXCollections.observableArrayList()
        model.query.addListener { observable -> tags.setAll(model.query.get().split(" ")) }
        val listView: ListView<String> = ListView(tags)
        return VBox().apply {
            children.add(listView)
            prefWidth = 100.0
        }
    }

    private fun createImageTile(file: FileDTO) = ImageView().apply {
        image = Image(file.url?.toUri().toString(), 150.0, 150.0, true, true)
        isPreserveRatio = true
        userData = file
    }
}