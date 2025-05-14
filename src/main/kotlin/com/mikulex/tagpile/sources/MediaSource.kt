package com.mikulex.tagpile.sources

import com.mikulex.tagpile.model.dto.MediaDTO

interface MediaSource {
    fun initDatabase()
    fun findMedias(query: String?): List<MediaDTO>
    fun importFile(filePath: String)
    fun findTagsForMedia(pk: Int): List<String>
}