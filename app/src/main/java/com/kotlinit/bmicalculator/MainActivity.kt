package com.kotlinit.bmicalculator

import android.app.AlertDialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import kotlin.math.pow

class MainActivity : AppCompatActivity() {
    private lateinit var unitRadioGroup: RadioGroup
    private lateinit var majorHeightInput: TextInputEditText
    private lateinit var minorHeightInput: TextInputEditText
    private lateinit var weightInput: TextInputEditText
    private lateinit var calculateButton: Button
    private lateinit var resetButton: Button
    private lateinit var resultValue: TextView
    private lateinit var resultDescription: TextView
    private lateinit var asianDescentCheckbox: CheckBox
    private lateinit var questionMark: TextView

    private var unitSystem: String = "metric"
    private var isAsianDescent: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        unitRadioGroup = findViewById(R.id.unit_system_radio_group)
        majorHeightInput = findViewById(R.id.major_height_input)
        minorHeightInput = findViewById(R.id.minor_height_input)
        weightInput = findViewById(R.id.weight_input)
        calculateButton = findViewById(R.id.calculate_button)
        resetButton = findViewById(R.id.reset_button)
        resultValue = findViewById(R.id.result_value)
        resultDescription = findViewById(R.id.result_description)
        asianDescentCheckbox = findViewById(R.id.asian_descent_checkbox)
        questionMark = findViewById(R.id.question_mark)

        unitRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.metric_radio_button -> {
                    unitSystem = "metric"
                    majorHeightInput.hint = "m"
                    minorHeightInput.hint = "cm"
                    weightInput.hint = "kg"
                }

                R.id.imperial_radio_button -> {
                    unitSystem = "imperial"
                    majorHeightInput.hint = "ft"
                    minorHeightInput.hint = "in"
                    weightInput.hint = "lbs"
                }
            }
        }

        asianDescentCheckbox.setOnCheckedChangeListener { _, isChecked ->
            isAsianDescent = isChecked
        }

        questionMark.setOnClickListener {
            val message = "This information is required because according to the NCBI, " +
                    "the cutoffs underestimate the obesity risk in the Asian and South Asian populations, " +
                    "so their classification has slight alterations."

            val builder = AlertDialog.Builder(this@MainActivity)
            builder.setMessage(message)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        fun imperialToMetric(majorHeight: Double, minorHeight: Double, weight: Double): Triple<Double, Double, Double> {
            val majorHeightInMeters = majorHeight * 0.3048
            val minorHeightInMeters = minorHeight * 0.0254
            val weightInKg = weight * 0.45359237
            return Triple(majorHeightInMeters, minorHeightInMeters, weightInKg)
        }

        fun calculateBMI(majorHeight: Double, minorHeight: Double, weight: Double, unitSystem: String): Double {
            return if (unitSystem == "imperial") {
                val (majorHeightInMeters, minorHeightInMeters, weightInKg) = imperialToMetric(majorHeight, minorHeight, weight)
                val totalHeightInMeters = majorHeightInMeters + minorHeightInMeters
                weightInKg / totalHeightInMeters.pow(2)
            } else {
                // Assume that majorHeight is in meters and minorHeight is in centimeters in metric system
                val totalHeightInMeters = majorHeight + minorHeight / 100
                weight / totalHeightInMeters.pow(2)
            }
        }

        fun getBmiDescription(bmi: Double, isAsianDescent: Boolean): String {
            return when {
                bmi < 16.5 -> "Severely underweight"
                bmi < 18.5 -> "Underweight"
                bmi < if (isAsianDescent) 23 else 25 -> "Normal weight"
                bmi < if (isAsianDescent) 25 else 30 -> "Overweight"
                bmi < 35 -> "Obesity class I"
                bmi < 40 -> "Obesity class II"
                else -> "Obesity class III"
            }
        }

        fun showKeyboard(view: View) {
            val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }

        fun hideKeyboard(view: View) {
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }

        calculateButton.setOnClickListener {
            hideKeyboard(it)

            val majorHeight = majorHeightInput.text.toString().toDoubleOrNull()
            val minorHeight = minorHeightInput.text.toString().toDoubleOrNull()
            val weight = weightInput.text.toString().toDoubleOrNull()

            when {
                majorHeight == null -> {
                    Toast.makeText(this@MainActivity, "Please enter a valid value for major height", Toast.LENGTH_SHORT).show()
                    majorHeightInput.requestFocus()
                    showKeyboard(majorHeightInput)
                }
                minorHeight == null -> {
                    Toast.makeText(this@MainActivity, "Please enter a valid value for minor height", Toast.LENGTH_SHORT).show()
                    minorHeightInput.requestFocus()
                    showKeyboard(minorHeightInput)
                }
                weight == null -> {
                    Toast.makeText(this@MainActivity, "Please enter a valid value for weight", Toast.LENGTH_SHORT).show()
                    weightInput.requestFocus()
                    showKeyboard(weightInput)
                }
                else -> {
                    val bmi = calculateBMI(majorHeight, minorHeight, weight, unitSystem)
                    val bmiDescription = getBmiDescription(bmi, isAsianDescent)

                    resultValue.text = String.format("%.2f", bmi)
                    resultDescription.text = bmiDescription
                }
            }
        }


        resetButton.setOnClickListener {
            majorHeightInput.text?.clear()
            minorHeightInput.text?.clear()
            weightInput.text?.clear()
            resultValue.text = ""
            resultDescription.text = ""
        }
    }
}
