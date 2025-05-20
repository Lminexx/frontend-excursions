package com.example.projectexcursions.utilies

import android.content.Context
import android.view.ViewGroup
import jp.wasabeef.blurry.Blurry

class Blur() {

    fun blur(context: Context, radius: Int, sampling: Int, viewGroup: ViewGroup) {
        Blurry.with(context)
            .radius(radius)
            .sampling(sampling)
            .async()
            .onto(viewGroup)
    }

    fun unblur(viewGroup: ViewGroup) {
        Blurry.delete(viewGroup)
    }
}