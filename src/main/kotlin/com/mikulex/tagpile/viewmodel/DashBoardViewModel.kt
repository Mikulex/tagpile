package com.mikulex.tagpile.viewmodel

import com.mikulex.tagpile.model.MediaModel
import com.mikulex.tagpile.model.dto.MediaDTO
import javafx.beans.property.DoubleProperty
import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.math.max
import kotlin.math.min

class DashBoardViewModel(private val mediaModel: MediaModel) {
    private val LOG = LoggerFactory.getLogger(MediaModel::class.java)
    val searchQuery: StringProperty = SimpleStringProperty()
    val results: ObservableList<MediaDTO> = FXCollections.observableArrayList()
    val resultTags: ObservableList<String> = FXCollections.observableArrayList()
    val selectedMedias: ObservableList<MediaDTO> = FXCollections.observableArrayList()
    private val lastSelectedIndex: IntegerProperty = SimpleIntegerProperty(0)
    val imageWidthProperty: DoubleProperty = SimpleDoubleProperty(300.0)
    val metadataWithProperty: DoubleProperty = SimpleDoubleProperty(300.0)

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

    fun setLastSelectedMedia(media: MediaDTO) {
        lastSelectedIndex.set(results.indexOf(media))
    }

    fun selectTo(media: MediaDTO, replace: Boolean = true) {
        val i = lastSelectedIndex.get()
        val j = results.indexOf(media)
        val fromIndex = min(i, j)
        val toIndex = max(i, j)
        val subList = results.subList(fromIndex, toIndex + 1)
        if (replace) {
            selectedMedias.setAll(subList)
        } else {
            selectedMedias.addAll(subList.filter { results.contains(it) })
        }

    }

    fun addTagsToSelectedMedias(query: String) {
        val tagsToAdd = query.split(" ")
        if (tagsToAdd.isEmpty()) {
            return
        }

        for (media: MediaDTO in selectedMedias) {
            LOG.debug("Adding tags {} to {}", tagsToAdd, media)

            val addSuccess = mediaModel.addTagsToMedia(media.pk, tagsToAdd)
            media.tags = tagsToAdd


        }
    }
}