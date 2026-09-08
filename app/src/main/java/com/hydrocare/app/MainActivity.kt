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

        fun updateUI() {
            val intake = prefs.getInt("intake", 0)
            tvIntake.text = intake.toString()
            tvGoal.text = "$intake / $goal ml daily goal"
            progressBar.progress = minOf(intake, goal)
            tvMotivation.text = when {
                intake >= goal -> "Amazing! You've hit your goal today!"
                intake >= goal / 2 -> "Keep going, you're doing great!"
                intake > 0 -> "Good start, keep drinking!"
                else -> "Start your hydration journey!"
            }
        }

        fun addWater(ml: Int) {
            val current = prefs.getInt("intake", 0)
            prefs.edit().putInt("intake", current + ml).apply()
            updateUI()
        }

        findViewById<MaterialButton>(R.id.btn250).setOnClickListener { addWater(250) }
        findViewById<MaterialButton>(R.id.btn500).setOnClickListener { addWater(500) }
        findViewById<MaterialButton>(R.id.btn750).setOnClickListener { addWater(750) }
        findViewById<MaterialButton>(R.id.btnReset).setOnClickListener {
            prefs.edit().putInt("intake", 0).apply()
            updateUI()
        }

        updateUI()
    }

    private fun resetIfNewDay() {
        val today = LocalDate.now().toString()
        val lastDay = prefs.getString("last_day", "")
        if (lastDay != today) {
            prefs.edit().putInt("intake", 0).putString("last_day", today).apply()
        }
    }
}
