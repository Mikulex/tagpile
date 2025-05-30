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
    override fun build() = VBox().apply {
        val previewImageView = ImageView().apply {
            isPreserveRatio = true
            fitWidth = 299.0
        }

        children += previewImageView

        children += Text().apply {
            dashboardViewModel.selectedMedias.addListener(ListChangeListener { change ->
                while (change.next()) {
                    this.text = "${change.list.size} items selected"
                }
            })
        }

        dashboardViewModel.selectedMedias.addListener(ListChangeListener { change ->
            while (change.next()) {
                dashboardViewModel.selectedMedias.firstOrNull()?.url?.toUri()?.let {
                    previewImageView.image = Image(
                        it.toString(), 499.0, 500.0, true, true
                    )
                    previewImageView.isVisible = true
                } ?: let {
                    previewImageView.image = null
                    previewImageView.isVisible = false
                }
            }
        })
    }
}
