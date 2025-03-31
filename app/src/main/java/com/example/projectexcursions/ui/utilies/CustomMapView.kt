package com.example.projectexcursions.ui.utilies

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import com.yandex.mapkit.mapview.MapView

class CustomMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
): MapView(context, attrs) {

    var parentScrollView: ViewGroup? = null

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        parentScrollView?.requestDisallowInterceptTouchEvent(true)
        return super.dispatchTouchEvent(ev)
    }
}