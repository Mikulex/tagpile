package com.mikulex.tagpile.view.dashboard

import com.mikulex.tagpile.viewmodel.DashBoardViewModel
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.Region
import javafx.stage.FileChooser
import javafx.util.Builder
import org.slf4j.LoggerFactory

class HeaderBuilder(private val dashboardViewModel: DashBoardViewModel) : Builder<Region> {
    companion object {
        private val LOG = LoggerFactory.getLogger(HeaderBuilder::class.java)
    }

    override fun build() = HBox().apply {
        LOG.debug("Initialize Header")
        val fileChooser = FileChooser().apply {
            title = "Open File"
            extensionFilters.setAll(FileChooser.ExtensionFilter("images", "*.jpg", "*.jpeg", "*.png", "*.gif"))
        }

        children += Button("Import Image").apply {
            setOnAction {
                fileChooser.showOpenMultipleDialog(this.scene.window)?.let {
                    dashboardViewModel.importFiles(it)
                    dashboardViewModel.findMedias()
                }
            }
        }

        children += TextField().apply {
            this.textProperty().bindBidirectional(dashboardViewModel.searchQuery)
            this.promptText = "Search"
            this.setOnAction { dashboardViewModel.findMedias() }
        }

        children += Button("Search").apply {
            this.setOnAction { dashboardViewModel.findMedias() }
        }
    }
}
