package com.example.locationremainder.utils

import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.BindingAdapter

@BindingAdapter("setDouble")
fun TextView.setDouble(value: Double) {
    text = value.toString()
}

@BindingAdapter("visible")
fun ProgressBar.visible(show: Boolean) {
    visibility = if(show) {
        View.VISIBLE
    } else {
        View.GONE
    }
}
