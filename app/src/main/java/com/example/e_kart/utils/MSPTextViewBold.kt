package com.example.e_kart.utils

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class MSPTextViewBold(context : Context, attrs : AttributeSet) : AppCompatTextView(context, attrs) {
    init {
        // Call the function to apply the font to the components.
        applyFont()
    }

    private fun applyFont(){

        // This is used to get the file from asset folder and set it to the title textView
        val typeface : Typeface = Typeface.createFromAsset(context.assets,"Montserrat-Bold.ttf")
        setTypeface(typeface)
    }
}