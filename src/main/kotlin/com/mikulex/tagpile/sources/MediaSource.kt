package com.mikulex.tagpile.sources

import com.mikulex.tagpile.model.dto.MediaDTO

interface MediaSource {
    fun initDatabase()
    fun findMedias(query: String?): List<MediaDTO>
    fun importFile(filePath: String)
    fun findTagsForMedia(pk: Int): List<String>
    fun addTags(mediaPk: Int, tags: List<String>): Boolean
    fun removeTag(mediaPk: Int, tagsToRemove: List<String>)
    fun deleteMedias(selectedMedias: List<Int>)
}