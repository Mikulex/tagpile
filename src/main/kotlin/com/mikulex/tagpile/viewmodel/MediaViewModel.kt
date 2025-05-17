package com.mikulex.tagpile.viewmodel

import com.mikulex.tagpile.model.MediaModel
import com.mikulex.tagpile.model.dto.MediaDTO
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList

class MediaViewModel(private val mediaModel: MediaModel) {
    val mediaFile: ObjectProperty<MediaDTO> = SimpleObjectProperty()
    val tags: ObservableList<String> = FXCollections.observableArrayList()

    fun findTags(pk: Int) {
        val medias = mediaModel.findTagsForMedia(pk)
        tags.setAll(medias)
    }

    fun addTagToMedia(mediaPK: Int, tag: String): Boolean {
        return mediaModel.addTagToMedia(mediaPK, tag)
    }
}