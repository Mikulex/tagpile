package com.mikulex.tagpile.sources

import com.mikulex.tagpile.model.dto.FileDTO

interface ImageSource {
    fun initDatabase()
    fun findMedias(query: String?): List<FileDTO>
    fun importFile(filePath: String)
}