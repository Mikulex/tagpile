package com.mikulex.tagpile.view.dashboard

import com.mikulex.tagpile.viewmodel.DashBoardViewModel
import javafx.collections.ListChangeListener
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.util.Builder

class MetadataBarBuilder(private val dashboardViewModel: DashBoardViewModel) : Builder<Region> {
    override fun build() = VBox().also { outer ->
        val previewImageView = ImageView().apply {
            fitWidthProperty().bind(dashboardViewModel.imageWidthProperty)
            isPreserveRatio = true
        }
        outer.maxWidthProperty().bind(dashboardViewModel.metadataMaxWidthProperty)
        outer.minWidth = 200.0

        outer.children += previewImageView

        outer.children += Text().apply {
            dashboardViewModel.selectedMedias.addListener(ListChangeListener { change ->
                while (change.next()) {
                    this.text = "${change.list.size} items selected"
                }
            })
        }

        dashboardViewModel.selectedMedias.addListener(ListChangeListener { change ->
            while (change.next()) {
                dashboardViewModel.selectedMedias.firstOrNull()?.url?.toUri()?.let {
                    previewImageView.image = Image(it.toString(), true)
                    previewImageView.isVisible = true
                } ?: let {
                    previewImageView.image = null
                    previewImageView.isVisible = false
                }
            }
        })
    }
}
