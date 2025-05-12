package com.mikulex.tagpile.model

import com.mikulex.tagpile.model.dto.FileDTO

interface IMediaModel {
    fun findMedias(query: String?): List<FileDTO>
    fun importFile(filePath: String)
}