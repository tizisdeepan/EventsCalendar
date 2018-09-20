package com.events.calendarsample

import android.content.Context
import android.graphics.Typeface

object FontsManager {
    val OPENSANS_LIGHT = "opensans-light.ttf"
    val OPENSANS_REGULAR = "opensans-regular.ttf"
    val OPENSANS_SEMIBOLD = "opensans-semibold.ttf"
    fun getTypeface(font: String = "", context: Context): Typeface = Typeface.createFromAsset(context.assets,"fonts/$font")
}