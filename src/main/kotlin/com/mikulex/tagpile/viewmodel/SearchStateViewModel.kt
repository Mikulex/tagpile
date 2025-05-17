package com.mikulex.tagpile.viewmodel

import com.mikulex.tagpile.model.MediaModel
import com.mikulex.tagpile.model.dto.MediaDTO
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import java.io.File

class SearchStateViewModel(private val mediaModel: MediaModel) {
    val searchQuery: StringProperty = SimpleStringProperty()
    val results: ObservableList<MediaDTO> = FXCollections.observableArrayList()
    val resultTags: ObservableList<String> = FXCollections.observableArrayList()

    fun importFile(file: File) {
        mediaModel.importFile(file.toURI().toString())
    }

    fun findMedias() {
        val medias = mediaModel.findMedias(searchQuery.get())
        results.setAll(medias)
        val tags = medias.flatMap { it.tags ?: emptyList() }.toSet()
        resultTags.setAll(tags)
    }
}