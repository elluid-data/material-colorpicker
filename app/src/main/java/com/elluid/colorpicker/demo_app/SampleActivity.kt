package com.elluid.colorpicker.demo_app

import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.elluid.colorpicker.ColorPickerView



class SampleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)

        val colorPickerView = findViewById<ColorPickerView>(R.id.colorpicker)
        val button = findViewById<Button>(R.id.color_button)
        colorPickerView.addOnColorSelectedListener { selectedColor ->
            button.setBackgroundColor(selectedColor)
        }




    }
}