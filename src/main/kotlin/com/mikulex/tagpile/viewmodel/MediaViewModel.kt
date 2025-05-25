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
    val tagsToRemove: ObservableList<String> = FXCollections.observableArrayList()

    fun findTags() {
        LOG.debug("Fetching tags for {}", mediaFile.get())

        val mediaTags = mediaModel.findTagsForMedia(mediaFile.get().pk)
        mediaFile.get().tags = mediaTags
        tags.setAll(mediaTags)
    }

    fun addTagToMedia(): Boolean {
        if (newTag.get().isEmpty()) {
            return false
        }

        LOG.debug("Adding tag {} to {}", newTag.get(), mediaFile.get())

        val addSuccess = mediaModel.addTagToMedia(mediaFile.get().pk, newTag.get())
        tags.addIf(newTag.get()) { s -> addSuccess && !tags.contains(s) }
        newTag.set("")
        mediaFile.get().tags = tags

        return addSuccess
    }

    fun removeTags() {
        LOG.debug("Removing tag {} to {}", newTag.get(), mediaFile.get())

        if (tagsToRemove.isEmpty()) {
            return
        }

        mediaModel.removeTagFromMedia(mediaFile.get().pk, tagsToRemove)
        tags.removeAll(tagsToRemove)
        tagsToRemove.clear()
        mediaFile.get().tags = tags
    }
}

private fun ObservableList<String>.addIf(newValue: String, func: (String) -> Boolean) {
    if (func(newValue)) {
        this.add(newValue)
    }
}
