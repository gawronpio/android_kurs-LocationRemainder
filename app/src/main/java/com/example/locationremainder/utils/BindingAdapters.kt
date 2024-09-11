package com.example.locationremainder.utils

import android.widget.TextView
import androidx.databinding.BindingAdapter

@BindingAdapter("setDouble")
fun TextView.setDouble(value: Double) {
    text = value.toString()
}