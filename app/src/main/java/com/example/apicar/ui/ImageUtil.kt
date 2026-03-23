package com.example.apicar.ui

import android.widget.ImageView
import com.example.apicar.R
import com.squareup.picasso.Picasso

fun ImageView.loadUrl(url: String?) {
    if (url.isNullOrEmpty()) return
    Picasso.get()
        .load(url)
        .placeholder(R.drawable.ic_launcher_background)
        .error(R.drawable.ic_launcher_background)
        .transform(CircleTransform())
        .into(this)
}