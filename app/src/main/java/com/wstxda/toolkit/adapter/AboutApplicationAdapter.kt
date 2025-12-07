package com.wstxda.toolkit.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.shape.ShapeAppearanceModel
import com.wstxda.toolkit.data.AboutItem
import com.wstxda.toolkit.databinding.ItemAboutLinkBinding
import com.google.android.material.R

class AboutApplicationAdapter(
    private val onClick: (AboutItem) -> Unit
) : ListAdapter<AboutItem, AboutApplicationAdapter.LinkViewHolder>(LinkDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = LinkViewHolder(
        ItemAboutLinkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: LinkViewHolder, position: Int) {
        holder.bind(getItem(position), position, itemCount)
    }

    inner class LinkViewHolder(private val binding: ItemAboutLinkBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(link: AboutItem, position: Int, totalItems: Int) = with(binding) {
            link.title?.let { titleItem.setText(it) }
            titleItem.isVisible = link.title != null

            link.icon?.let { iconItem.setImageResource(it) }
            iconItem.isVisible = link.icon != null

            link.summary?.let { summaryItem.setText(it) }
            summaryItem.isVisible = link.summary != null

            val isClickable = link.url != null
            cardItem.isClickable = isClickable
            cardItem.isFocusable = isClickable
            cardItem.setOnClickListener(
                if (isClickable) {
                { onClick(link) }
            } else null)

            applyCardStyle(position, totalItems)
        }

        private fun applyCardStyle(position: Int, totalItems: Int) {
            val context = binding.root.context

            val shapeStyleResId = when {
                totalItems == 1 -> R.style.ShapeAppearance_Material3_ListItem_Single
                position == 0 -> R.style.ShapeAppearance_Material3_ListItem_First
                position == totalItems - 1 -> R.style.ShapeAppearance_Material3_ListItem_Last
                else -> R.style.ShapeAppearance_Material3_ListItem_Middle
            }

            binding.cardItem.shapeAppearanceModel = ShapeAppearanceModel.builder(
                context, shapeStyleResId, 0
            ).build()

            val density = context.resources.displayMetrics.density
            val isFirst = position == 0
            val isLast = position == totalItems - 1

            val marginTop = if (isFirst) 16 else 0
            val marginBottom = if (isLast) 16 else 2

            binding.cardItem.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = (marginTop * density).toInt()
                bottomMargin = (marginBottom * density).toInt()
            }
        }
    }

    object LinkDiffCallback : DiffUtil.ItemCallback<AboutItem>() {
        override fun areItemsTheSame(oldItem: AboutItem, newItem: AboutItem) =
            oldItem.title == newItem.title

        override fun areContentsTheSame(oldItem: AboutItem, newItem: AboutItem) = oldItem == newItem
    }
}