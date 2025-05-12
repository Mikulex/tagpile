package com.mikulex.tagpile.model.dto

import java.nio.file.Path
import java.util.*

class FileDTO(var url: Path?, var importDate: Date?) {
    constructor() : this(null, null)
}
