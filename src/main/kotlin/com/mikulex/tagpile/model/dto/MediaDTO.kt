package com.mikulex.tagpile.model.dto

import java.nio.file.Path
import java.util.*

data class MediaDTO(var pk: Int, var url: Path?, var importDate: Date?, var tags: List<String>?) {
    constructor(pk: Int) : this(pk, null, null, null)
}
