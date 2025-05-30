package com.mikulex.tagpile.view.dashboard

import com.mikulex.tagpile.viewmodel.DashBoardViewModel
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.input.KeyCode
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.util.Builder
import javafx.util.Callback
import org.slf4j.LoggerFactory

class TagBarBuilder(private val dashboardViewModel: DashBoardViewModel) : Builder<Region> {
    companion object {
        private val LOG = LoggerFactory.getLogger(TagBarBuilder::class.java)
    }

    override fun build(): Region {
        LOG.debug("Initialize TagBar")
        val listView: ListView<String> = ListView(dashboardViewModel.resultTags).apply {
            cellFactory = Callback { _ -> StringListCell(dashboardViewModel) }

            onMouseClicked = EventHandler { event ->
                if (event.clickCount == 2 && !selectionModel.selectedItem.isNullOrBlank()) {
                    addToInputHandler(dashboardViewModel)
                }
            }

            onKeyPressed = EventHandler { event ->
                if (event.code == KeyCode.ENTER) {
                    addToInputHandler(dashboardViewModel)
                }
            }
        }

        return VBox().apply {
            children.add(listView)
            prefWidth = 100.0
        }
    }

}

private class StringListCell(val model: DashBoardViewModel) : ListCell<String>() {
    override fun updateItem(item: String?, empty: Boolean) {
        super.updateItem(item, empty)
        if (empty || item?.isEmpty() == true) {
            text = null
            graphic = null
        } else {
            text = item
            graphic = Button().also { btn ->
                btn.text = "+"
                btn.setOnAction {
                    with(model.searchQuery.get()) {
                        model.searchQuery.set("$this ${item}".trim())
                    }
                }
            }
        }
    }
}

private fun ListView<String>.addToInputHandler(dashboardViewModel: DashBoardViewModel) {
    with(dashboardViewModel.searchQuery.get()) {
        dashboardViewModel.searchQuery.set("$this ${selectionModel.selectedItem}".trim())
        selectionModel.clearSelection()
    }
}
