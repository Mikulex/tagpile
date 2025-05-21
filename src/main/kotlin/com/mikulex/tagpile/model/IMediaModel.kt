package com.mikulex.tagpile.model

import com.mikulex.tagpile.model.dto.MediaDTO
import javafx.concurrent.Task

interface IMediaModel {
    fun findMedias(query: String?): Task<List<MediaDTO>>
    fun importFile(filePath: String)
    fun findTagsForMedia(pk: Int): List<String>
    fun addTagToMedia(media: Int, tag: String): Boolean
}