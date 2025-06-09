package com.mikulex.tagpile.view.dashboard

import com.mikulex.tagpile.viewmodel.DashBoardViewModel
import javafx.beans.binding.Bindings
import javafx.collections.ListChangeListener
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.Separator
import javafx.scene.control.Slider
import javafx.scene.layout.HBox
import javafx.scene.layout.Region
import javafx.scene.text.Text
import javafx.util.Builder

class InfoBarBuilder(private val dashboardViewModel: DashBoardViewModel) : Builder<Region> {
    override fun build() =
        HBox().apply {
            spacing = 5.0
            children += Text("0 total items").apply {
                dashboardViewModel.results.addListener(ListChangeListener { change ->
                    while (change.next()) {
                        this.text = "${change.list.size} total items"
                    }
                })
            }

            children += Separator().apply {
                orientation = Orientation.VERTICAL
            }

            children += Text("0 items selected").apply {
                dashboardViewModel.selectedMedias.addListener(ListChangeListener { change ->
                    while (change.next()) {
                        this.text = "${change.list.size} items selected"
                    }
                })
            }

            children += Slider().apply {
                dashboardViewModel.zoomLevel.bind(Bindings.divide(valueProperty(), 100.0))
                min = 25.0
                blockIncrement = 5.0
                value = (max-min)/2 + min
            }
        }
}
