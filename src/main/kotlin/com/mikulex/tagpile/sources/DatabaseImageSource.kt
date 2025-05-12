package com.mikulex.tagpile.sources

import com.mikulex.tagpile.model.dto.FileDTO
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager

class DatabaseImageSource() : ImageSource {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(DatabaseImageSource::class.java)

        private val CREATE_MEDIA_TABLE = """
            CREATE TABLE IF NOT EXISTS media (
            pk INTEGER PRIMARY KEY AUTOINCREMENT,
            path STRING,
            importDate DATE
            )
        """.trimIndent()

        private val CREATE_TAGS_TABLE = """
            CREATE TABLE IF NOT EXISTS tags (
            pk INTEGER PRIMARY KEY AUTOINCREMENT,
            code STRING
            )
        """.trimIndent()

        private val CREATE_TAG_MEDIA_REL_TABLE = """
            CREATE TABLE IF NOT EXISTS tag_media_rel (
            source INTEGER,
            target INTEGER,
            FOREIGN KEY(source) REFERENCES tags(pk),
            FOREIGN KEY(target) REFERENCES media(pk)
            )
        """.trimIndent()
        private val FIND_ALL_MEDIAS = "SELECT media.pk, media.path, media.importDate FROM media"
        private val FIND_MEDIA_WITH_TAGS = """
            SELECT media.pk, media.path, media.importDate FROM media
            JOIN tag_media_rel AS rel ON rel.target = media.pk
            JOIN tags ON rel.source = tags.pk
            WHERE tags.code in (%s)
            """.trimIndent()
    }

    private val connection: Connection = DriverManager.getConnection("jdbc:sqlite:database.db")

    override fun initDatabase() {
        log.debug("initializing database")
        connection.prepareStatement(CREATE_MEDIA_TABLE).execute()
        connection.prepareStatement(CREATE_TAGS_TABLE).execute()
        connection.prepareStatement(CREATE_TAG_MEDIA_REL_TABLE).execute()
    }

    override fun findMedias(query: String?): List<FileDTO> {
        log.debug("finding medias for query $query")
        val statement = if (query == null || query.isEmpty()) {
            connection.prepareStatement(FIND_ALL_MEDIAS)
        } else {
            val tags = query.split(" ")
            val params = List(tags.size) { "?" }.joinToString(",")
            connection.prepareStatement(FIND_MEDIA_WITH_TAGS.format(params)).apply {
                tags.forEachIndexed { idx, tag ->
                    setString(idx + 1, tag)
                }
            }
        }
        val set = statement.executeQuery()
        val list = mutableListOf<FileDTO>()
        while (set.next()) {
            FileDTO().apply {
                url = Path.of(URI.create(set.getString("path")))
                importDate = set.getDate("importDate")
                list.add(this)
            }
        }
        return list
    }

    override fun importFile(filePath: String) {
        val statement = connection.prepareStatement(
            """
            INSERT INTO media (path, importDate) 
            VALUES (?, CURRENT_TIMESTAMP)
        """.trimIndent()
        )
        statement.setString(1, filePath)
        statement.executeUpdate()
    }


}