package com.mikulex.tagpile.view.dashboard

import com.mikulex.tagpile.viewmodel.DashBoardViewModel
import javafx.collections.ListChangeListener
import javafx.geometry.Orientation
import javafx.scene.control.Separator
import javafx.scene.layout.HBox
import javafx.scene.layout.Region
import javafx.scene.text.Text
import javafx.util.Builder

class InfoBarBuilder(private val dashboardViewModel: DashBoardViewModel) : Builder<Region> {
    override fun build() =
        HBox().apply {
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
        }
}
