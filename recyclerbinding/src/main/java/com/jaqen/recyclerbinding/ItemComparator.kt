package com.jaqen.recyclerbinding

import androidx.recyclerview.widget.DiffUtil

class ItemComparator<T: Comparator<T>>: DiffUtil.ItemCallback<T>() {

    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem.isSame(newItem)
    }

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem.isContentSame(newItem)
    }
}