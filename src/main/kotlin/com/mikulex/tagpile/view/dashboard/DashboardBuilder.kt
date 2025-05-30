package com.mikulex.tagpile.view.dashboard

import com.mikulex.tagpile.viewmodel.DashBoardViewModel
import com.mikulex.tagpile.viewmodel.MediaViewModelFactory
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Region
import javafx.util.Builder
import org.slf4j.LoggerFactory

class DashboardBuilder(
    private val dashboardViewModel: DashBoardViewModel, private val mediaViewModelFactory: MediaViewModelFactory
) : Builder<Region> {
    companion object {
        private val LOG = LoggerFactory.getLogger(DashboardBuilder::class.java)
    }

    override fun build(): Region? {
        LOG.debug("Initialize dashboard")
        return BorderPane().apply {
            center = MediaPaneBuilder(dashboardViewModel, mediaViewModelFactory).build()
            top = HeaderBuilder(dashboardViewModel).build()
            left = TagBarBuilder(dashboardViewModel).build()
            right = MetadataBarBuilder(dashboardViewModel).build()
            bottom = InfoBarBuilder(dashboardViewModel).build()
        }
    }
}