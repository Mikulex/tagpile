package com.mikulex.tagpile.viewmodel

import com.mikulex.tagpile.model.MediaModel
import com.mikulex.tagpile.model.dto.FileDTO
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty

class ImageViewerViewModel(private val mediaModel: MediaModel) {
    val mediaFile: ObjectProperty<FileDTO> = SimpleObjectProperty()
}