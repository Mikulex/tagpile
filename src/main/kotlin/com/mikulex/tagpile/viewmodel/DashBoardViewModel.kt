package com.mikulex.tagpile.viewmodel

import com.mikulex.tagpile.model.MediaModel
import com.mikulex.tagpile.model.dto.MediaDTO
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.slf4j.LoggerFactory
import java.io.File

class DashBoardViewModel(private val mediaModel: MediaModel) {
    private val LOG = LoggerFactory.getLogger(MediaModel::class.java)
    val searchQuery: StringProperty = SimpleStringProperty()
    val results: ObservableList<MediaDTO> = FXCollections.observableArrayList()
    val resultTags: ObservableList<String> = FXCollections.observableArrayList()
    val selectedMedias: ObservableList<MediaDTO> = FXCollections.observableArrayList()

    fun importFiles(file: List<File>) {
        file.map { it.toURI().toString() }.forEach { it -> mediaModel.importFile(it) }
    }

    fun findMedias() {
        LOG.info("finding medias")
        val task = mediaModel.findMedias(searchQuery.get())

        task.setOnSucceeded { event ->
            val medias: List<MediaDTO> = task.value
            LOG.info("updating media state")
            results.setAll(medias)
            val tags = medias.flatMap { it.tags ?: emptyList() }.toSet()
            LOG.info("updating tag state")
            resultTags.setAll(tags)
            LOG.info("finished fetching medias")
            selectedMedias.clear()
        }

        task.setOnFailed { event ->
            LOG.warn("Failed to fetch medias", task.exception)
        }

        with(Thread(task)) {
            isDaemon = true
            start()
        }
    }

    fun selectAll() {
        selectedMedias.setAll(results)
    }

    fun deleteSelected() {
        mediaModel.deleteMedias(selectedMedias.map { it.pk })
        findMedias()
    }
}