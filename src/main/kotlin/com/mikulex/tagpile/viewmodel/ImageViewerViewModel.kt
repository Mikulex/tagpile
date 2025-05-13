package com.mikulex.tagpile.viewmodel

import com.mikulex.tagpile.model.dto.FileDTO
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty

class ImageViewerViewModel() {
    val mediaFile: ObjectProperty<FileDTO> = SimpleObjectProperty()
}