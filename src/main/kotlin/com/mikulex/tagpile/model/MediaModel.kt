package com.mikulex.tagpile.model

import com.mikulex.tagpile.model.dto.FileDTO
import com.mikulex.tagpile.sources.ImageSource

class MediaModel(private val source: ImageSource) : IMediaModel {

    override fun findMedias(query: String?): List<FileDTO> {
        return source.findMedias(query)
    }

    override fun importFile(filePath: String) {
        return source.importFile(filePath)
    }
}