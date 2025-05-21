package com.mikulex.tagpile.model

import com.mikulex.tagpile.model.dto.MediaDTO
import com.mikulex.tagpile.sources.MediaSource
import javafx.concurrent.Task

class MediaModel(private val source: MediaSource) : IMediaModel {
    override fun findMedias(query: String?): Task<List<MediaDTO>> {
        return object : Task<List<MediaDTO>>() {
            override fun call(): List<MediaDTO>? {
                return source.findMedias(query)
            }
        }
    }

    override fun importFile(filePath: String) {
        return source.importFile(filePath)
    }

    override fun findTagsForMedia(pk: Int): List<String> {
        return source.findTagsForMedia(pk)
    }

    override fun addTagToMedia(pk: Int, code: String): Boolean {
        return source.addTag(pk, code)
    }

}