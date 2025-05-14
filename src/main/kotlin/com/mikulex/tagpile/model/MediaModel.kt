package com.mikulex.tagpile.model

import com.mikulex.tagpile.model.dto.MediaDTO
import com.mikulex.tagpile.sources.MediaSource

class MediaModel(private val source: MediaSource) : IMediaModel {

    override fun findMedias(query: String?): List<MediaDTO> {
        return source.findMedias(query)
    }

    override fun importFile(filePath: String) {
        return source.importFile(filePath)
    }

    override fun findTagsForMedia(pk: Int): List<String> {
        return source.findTagsForMedia(pk)
    }

}