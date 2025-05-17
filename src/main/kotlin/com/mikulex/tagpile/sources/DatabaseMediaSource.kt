package com.mikulex.tagpile.sources

import com.mikulex.tagpile.model.dto.MediaDTO
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager

class DatabaseMediaSource() : MediaSource {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(DatabaseMediaSource::class.java)
        private val MEDIA = "media"
        private val TAGS = "tags"
        private val TAG_MEDIA_REL = "tag_media_rel"

        private val CREATE_MEDIA_TABLE = """
            CREATE TABLE IF NOT EXISTS $MEDIA (
            pk INTEGER PRIMARY KEY AUTOINCREMENT,
            path STRING,
            importDate DATE,
            UNIQUE(path)
            )
        """.trimIndent()

        private val CREATE_TAGS_TABLE = """
            CREATE TABLE IF NOT EXISTS $TAGS (
            pk INTEGER PRIMARY KEY AUTOINCREMENT,
            code STRING,
            UNIQUE(code)
            )
        """.trimIndent()

        private val CREATE_TAG_MEDIA_REL_TABLE = """
            CREATE TABLE IF NOT EXISTS $TAG_MEDIA_REL (
            source INTEGER,
            target INTEGER,
            FOREIGN KEY(source) REFERENCES $TAGS(pk),
            FOREIGN KEY(target) REFERENCES $MEDIA(pk),
            UNIQUE(source, target)
            )
        """.trimIndent()
        private val FIND_ALL_MEDIAS = """
            SELECT $MEDIA.pk, $MEDIA.path, $MEDIA.importDate, group_concat($TAGS.code) as tag_list FROM $MEDIA
            LEFT OUTER JOIN $TAG_MEDIA_REL AS rel ON rel.target = $MEDIA.pk
            LEFT OUTER JOIN $TAGS ON rel.source = $TAGS.pk
            GROUP BY $MEDIA.pk
            """.trimMargin()
        private val FIND_MEDIA_WITH_TAGS = """
            SELECT $MEDIA.pk, $MEDIA.path, $MEDIA.importDate, group_concat($TAGS.code) as tag_list FROM $MEDIA
            LEFT OUTER JOIN $TAG_MEDIA_REL AS rel ON rel.target = $MEDIA.pk
            LEFT OUTER JOIN $TAGS ON rel.source = $TAGS.pk
            GROUP BY $MEDIA.pk
            HAVING $TAGS.code in (%s)
            """.trimIndent()
        private val FIND_TAGS_FOR_MEDIA = """
            SELECT $TAGS.code FROM $MEDIA
            JOIN $TAG_MEDIA_REL AS rel ON rel.target = $MEDIA.pk
            JOIN $TAGS ON rel.source = $TAGS.pk
            WHERE $MEDIA.pk = ?
        """.trimIndent()

        private val ADD_MEDIA_TAG_REL = "INSERT OR IGNORE INTO $TAG_MEDIA_REL(source, target) VALUES (?, ?)"
        private val INSERT_TAG = "INSERT OR IGNORE INTO $TAGS (code) VALUES (?)"
        private val GET_TAG_PK = "SELECT pk FROM $TAGS WHERE code = ?"
        private val INSERT_INTO_MEDIA = "INSERT INTO $MEDIA (path, importDate) VALUES (?, CURRENT_TIMESTAMP)"
    }

    private val connection: Connection = DriverManager.getConnection("jdbc:sqlite:database.db")

    override fun initDatabase() {
        log.debug("initializing database")
        connection.prepareStatement(CREATE_MEDIA_TABLE).execute()
        connection.prepareStatement(CREATE_TAGS_TABLE).execute()
        connection.prepareStatement(CREATE_TAG_MEDIA_REL_TABLE).execute()
    }

    override fun findMedias(query: String?): List<MediaDTO> {
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
        val res = statement.executeQuery()
        val medias = mutableListOf<MediaDTO>()
        while (res.next()) {
            MediaDTO(res.getInt("pk")).apply {
                url = Path.of(URI.create(res.getString("path")))
                importDate = res.getDate("importDate")
                tags = res.getString("tag_list")?.split(",")
                medias.add(this)
            }
        }
        return medias
    }

    override fun importFile(filePath: String) {
        connection.prepareStatement(
            INSERT_INTO_MEDIA
        ).run {
            setString(1, filePath)
            executeUpdate()
        }
    }

    override fun findTagsForMedia(pk: Int): List<String> {
        val statement = connection.prepareStatement(FIND_TAGS_FOR_MEDIA)
        val res = statement.run {
            statement.setInt(1, pk)
            statement.executeQuery()
        }
        return buildList {
            while (res.next()) {
                add(res.getString("code"))
            }
        }
    }

    override fun addTag(mediaPK: Int, tag: String): Boolean {
        try {
            connection.autoCommit = false

            connection.prepareStatement(INSERT_TAG).apply {
                setString(1, tag)
                executeUpdate()
            }

            val tagPK = connection.prepareStatement(
                GET_TAG_PK.trimIndent()
            ).run {
                setString(1, tag)
                executeQuery()
            }.run {
                next()
                getInt("pk")
            }

            connection.prepareStatement(ADD_MEDIA_TAG_REL).run {
                setInt(1, tagPK)
                setInt(2, mediaPK)
                executeUpdate()
            }
            connection.commit()
            connection.autoCommit = true
            return true
        } catch (e: Exception) {
            log.warn("Failed to add tag for media $mediaPK with tag $tag", e)
            connection.rollback()
            return false
        } finally {
            connection.autoCommit = true
        }
    }
}