package com.mikulex.tagpile.viewmodel

import com.mikulex.tagpile.model.MediaModel

class MediaViewModelFactory(private val mediaModel: MediaModel) {
    fun create(): MediaViewModel {
        return MediaViewModel(mediaModel)
    }
}
