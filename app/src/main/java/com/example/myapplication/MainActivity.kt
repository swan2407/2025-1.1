package com.example.myapplication

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.setPadding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val seatLayout = findViewById<LinearLayout>(R.id.seatContainer)
        seatLayout.orientation = LinearLayout.VERTICAL

        var rowLayout = createNewRow()

        for (i in 1..180) {
            if ((i - 1) % 20 == 0) {
                rowLayout = createNewRow()
                seatLayout.addView(rowLayout)
            }

            val seatButton = Button(this).apply {
                layoutParams = LinearLayout.LayoutParams(100, 120).apply {
                    setMargins(8, 8, 8, 8)
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
                tag = "availanle"
                val buttonCopy = this

                setOnClickListener {
                    if (buttonCopy.tag == "unavailable") {
                        AlertDialog.Builder(this@MainActivity)
                            .setTitle("Notice")
                            .setMessage("배정하실 수 없는 좌석입니다.")
                            .setPositiveButton("OK") { d, _ -> d.dismiss() }
                            .show()
                        return@setOnClickListener
                    }

                    val title = TextView(this@MainActivity).apply {
                        text = "Notice"
                        textSize = 18f
                        setTextColor(Color.BLACK)
                        gravity = Gravity.CENTER
                        setPadding(0, 30, 0, 20)
                    }

                    val message = TextView(this@MainActivity).apply {
                        text = "2층 커뮤니티라운지 / ${i}번 좌석\n아래의 좌석으로 배정하시겠습니까?"
                        textSize = 16f
                        setTextColor(Color.BLACK)
                        gravity = Gravity.CENTER
                    }

                    val dialogLayout = LinearLayout(this@MainActivity).apply {
                        orientation = LinearLayout.VERTICAL
                        gravity = Gravity.CENTER
                        setPadding(30, 40, 30, 20)
                        addView(message)
                    }

                    val dialog = AlertDialog.Builder(this@MainActivity)
                        .setCustomTitle(title)
                        .setView(dialogLayout)
                        .setPositiveButton("Assign") { dialog, _ ->
                            val hasEntryRecord = false // 실제 로직에 맞게 설정

                            if (!hasEntryRecord) {
                                AlertDialog.Builder(this@MainActivity)
                                    .setTitle("배정좌석확인")
                                    .setMessage("출입기록이 없습니다. 게이트 재진입 후 예약 바랍니다.")
                                    .setPositiveButton("OK") { innerDialog, _ ->
                                        innerDialog.dismiss()
                                    }
                                    .show()
                            } else {
                                buttonCopy.background = ContextCompat.getDrawable(
                                    this@MainActivity, R.drawable.seat_disavailable
                                )
                                buttonCopy.tag = "unavailable"
                            }

                            dialog.dismiss()
                        }
                        .setNegativeButton("Cancel") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .create()

                    dialog.setOnShowListener {
                        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        positiveButton.setBackgroundColor(Color.parseColor("#0F66AE"))
                        positiveButton.setTextColor(Color.WHITE)
                        positiveButton.setPadding(0, 0, 0, 0)
                        val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        negativeButton.setBackgroundColor(Color.parseColor("#F2F0F0"))
                        negativeButton.setTextColor(Color.BLACK)
                        negativeButton.setPadding(0, 0, 0, 0)
                        val parent = positiveButton.parent as? LinearLayout
                        parent?.gravity = Gravity.CENTER_HORIZONTAL
                    }

                    dialog.show()
                }
            }

            rowLayout.addView(seatButton)
        }
    }

    private fun createNewRow(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_HORIZONTAL
        }
    }
}
