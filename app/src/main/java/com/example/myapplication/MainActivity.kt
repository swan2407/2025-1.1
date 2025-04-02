package com.example.myapplication

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 시스템 바 패딩 적용
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // LinearLayout으로 선언된 seatContainer 찾기
        val seatLayout = findViewById<LinearLayout>(R.id.seatContainer)

        // 좌석 1~180 생성 및 배치
        for (i in 1..180) {
            val seatButton = Button(this).apply {
                layoutParams = LinearLayout.LayoutParams(100, 120).apply {
                    setMargins(16, 16, 16, 16)
                }
                text = i.toString()
                textSize = 12f
                gravity = Gravity.CENTER
                setTextColor(Color.WHITE)

                background = when (i) {
                    in 1..25, in 99..106, in 148..157 -> ContextCompat.getDrawable(
                        this@MainActivity, R.drawable.seat_available_window)
                    else -> ContextCompat.getDrawable(
                        this@MainActivity, R.drawable.seat_available)
                }
            }
            seatLayout.addView(seatButton)
        }
    }
}
