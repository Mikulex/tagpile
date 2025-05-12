package com.mikulex.tagpile.viewmodel

import com.mikulex.tagpile.model.MediaModel
import com.mikulex.tagpile.model.dto.FileDTO
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import java.io.File

class SearchStateViewModel(private val mediaModel: MediaModel) {
    val query: StringProperty = SimpleStringProperty()
    val results: ObservableList<FileDTO> = FXCollections.observableArrayList()

    fun importFile(file: File) {
        mediaModel.importFile(file.toURI().toString())
    }

    fun findMedias() {
        val medias = mediaModel.findMedias(query.get())
        results.setAll(medias)
    }
}