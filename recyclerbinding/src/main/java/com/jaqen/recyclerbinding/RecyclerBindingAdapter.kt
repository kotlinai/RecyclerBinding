package com.jaqen.recyclerbinding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.paging.PagingData
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView

class RecyclerBindingAdapter<T : Comparator<T>>(private val itemLayoutId: Int, private val viewModelId: Int)
    : PagingDataAdapter<T, RecyclerBindingAdapter.BindingViewHolder>(ItemComparator()) {

    //private val data = arrayOf<RecyclerItemViewModel>()

    override fun onBindViewHolder(holder: BindingViewHolder, position: Int) {

        holder.viewModel = getItem(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder {

        return BindingViewHolder(parent, itemLayoutId, viewModelId)
    }

    class BindingViewHolder(parent: ViewGroup, itemViewId: Int,
                            private val viewModelId: Int
    ) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(itemViewId, parent)
    ) {
        private val dataBinding = DataBindingUtil.bind<ViewDataBinding>(itemView)

        var viewModel: Any? = null
        set(value) {
            dataBinding?.setVariable(viewModelId, value)
            dataBinding?.executePendingBindings()
        }
    }
}

@BindingAdapter(value = ["itemLayoutId", "viewModelId", "data"])
fun <T : Comparator<T>> setAdapter(view: RecyclerView, itemLayoutId: Int, viewModelId: Int, data: PagingData<T>){
    var adapter : RecyclerBindingAdapter<T>? = null

    if (view.adapter == null || view.adapter !is RecyclerBindingAdapter<*>){
        adapter = RecyclerBindingAdapter(itemLayoutId, viewModelId)
        view.adapter = adapter
    }

    view.findViewTreeLifecycleOwner()?.let {
        adapter?.submitData(it.lifecycle, data)
    }
}