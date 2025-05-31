package com.mikulex.tagpile.model

import com.mikulex.tagpile.model.dto.MediaDTO
import javafx.concurrent.Task

interface IMediaModel {
    fun findMedias(query: String?): Task<List<MediaDTO>>
    fun importFile(filePath: String)
    fun findTagsForMedia(pk: Int): List<String>
    fun addTagsToMedia(media: Int, tags: List<String>): Boolean
    fun removeTagFromMedia(mediaPk: Int, tagsToRemove: List<String>)
    fun deleteMedias(selectedMedias: List<Int>)
}