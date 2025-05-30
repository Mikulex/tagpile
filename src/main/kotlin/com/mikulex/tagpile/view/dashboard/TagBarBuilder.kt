package com.mikulex.tagpile.view.dashboard

import com.mikulex.tagpile.viewmodel.DashBoardViewModel
import javafx.scene.control.ListView
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.util.Builder
import org.slf4j.LoggerFactory

class TagBarBuilder(private val dashboardViewModel: DashBoardViewModel) : Builder<Region> {
    companion object {
        private val LOG = LoggerFactory.getLogger(TagBarBuilder::class.java)
    }

    override fun build(): Region {
        LOG.debug("Initialize TagBar")
        val listView: ListView<String> = ListView(dashboardViewModel.resultTags)
        return VBox().apply {
            children.add(listView)
            prefWidth = 100.0
        }
    }
}