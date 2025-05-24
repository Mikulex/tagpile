package com.mikulex.tagpile.viewmodel

import com.mikulex.tagpile.model.MediaModel
import com.mikulex.tagpile.model.dto.MediaDTO
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.slf4j.LoggerFactory

class MediaViewModel(private val mediaModel: MediaModel) {
    companion object {
        private val LOG = LoggerFactory.getLogger(MediaViewModel::class.java)
    }

    val mediaFile: ObjectProperty<MediaDTO> = SimpleObjectProperty()
    val tags: ObservableList<String> = FXCollections.observableArrayList()
    val newTag: StringProperty = SimpleStringProperty()

    fun findTags(pk: Int) {
        LOG.debug("Fetching tags for $pk")
        val medias = mediaModel.findTagsForMedia(pk)
        tags.setAll(medias)
    }

    fun addTagToMedia(): Boolean {
        if (newTag.get().isEmpty()) {
            return false
        }

        val addSuccess = mediaModel.addTagToMedia(mediaFile.get().pk, newTag.get())
        tags.addIf(newTag.get()) { s -> addSuccess && !tags.contains(s) }
        newTag.set("")

        return addSuccess
    }
}

private fun ObservableList<String>.addIf(newValue: String, func: (String) -> Boolean) {
    if (func(newValue)) {
        this.add(newValue)
    }
}
