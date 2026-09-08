package com.hydrocare.app

import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import java.time.LocalDate

class MainActivity : AppCompatActivity() {

    private val prefs by lazy { getSharedPreferences("hydrocare", MODE_PRIVATE) }
    private val goal = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        resetIfNewDay()

        val tvIntake = findViewById<TextView>(R.id.tvIntake)
        val tvGoal = findViewById<TextView>(R.id.tvGoalLabel)
        val tvMotivation = findViewById<TextView>(R.id.tvMotivation)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        fun refresh() {
            val intake = prefs.getInt("intake", 0)
            tvIntake.text = "$intake"
            tvGoal.text = "$intake / $goal ml"
            progressBar.progress = minOf(intake, goal)
            tvMotivation.text = when {
                intake >= goal -> "You crushed your goal today!"
                intake >= goal / 2 -> "Halfway there, keep it up!"
                intake > 0 -> "Good start, keep drinking!"
                else -> "Tap a button below to log your first drink!"
            }
        }

        fun add(ml: Int) {
            val current = prefs.getInt("intake", 0)
            prefs.edit().putInt("intake", current + ml).apply()
            refresh()
        }

        findViewById<MaterialButton>(R.id.btn250).setOnClickListener { add(250) }
        findViewById<MaterialButton>(R.id.btn500).setOnClickListener { add(500) }
        findViewById<MaterialButton>(R.id.btn750).setOnClickListener { add(750) }
        findViewById<MaterialButton>(R.id.btnReset).setOnClickListener {
            prefs.edit().putInt("intake", 0).apply()
            refresh()
        }

        refresh()
    }

    private fun resetIfNewDay() {
        val today = LocalDate.now().toString()
        if (prefs.getString("day", "") != today) {
            prefs.edit().putInt("intake", 0).putString("day", today).apply()
        }
    }
}
