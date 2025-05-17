package com.mikulex.tagpile.model

import com.mikulex.tagpile.model.dto.MediaDTO

interface IMediaModel {
    fun findMedias(query: String?): List<MediaDTO>
    fun importFile(filePath: String)
    fun findTagsForMedia(pk: Int): List<String>
    fun addTagToMedia(media: Int, tag: String): Boolean
}