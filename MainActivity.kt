package com.example.mycalculator

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

class MainActivity : Activity() {
    private lateinit var display: TextView
    private lateinit var detail: TextView
    private var entry = "0"
    private var stored: BigDecimal? = null
    private var operator: String? = null
    private var fresh = false
    private var error = false
    private val mc = MathContext(14, RoundingMode.HALF_UP)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(13, 18, 29)
        window.navigationBarColor = Color.rgb(13, 18, 29)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(24), dp(18), dp(24))
            setBackgroundColor(Color.rgb(13, 18, 29))
        }
        detail = TextView(this).apply {
            gravity = Gravity.END or Gravity.BOTTOM
            textSize = 18f
            setTextColor(Color.rgb(145, 158, 179))
            setPadding(dp(8), 0, dp(8), dp(8))
        }
        display = TextView(this).apply {
            text = "0"
            gravity = Gravity.END or Gravity.BOTTOM
            textSize = 54f
            maxLines = 1
            if (android.os.Build.VERSION.SDK_INT >= 26) setAutoSizeTextTypeUniformWithConfiguration(22, 54, 2, 1)
            typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
            setTextColor(Color.WHITE)
            setPadding(dp(8), 0, dp(8), dp(20))
        }
        root.addView(detail, LinearLayout.LayoutParams(-1, 0, 0.6f))
        root.addView(display, LinearLayout.LayoutParams(-1, 0, 1.25f))
        val keys = listOf(
            listOf("AC", "⌫", "%", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "−"),
            listOf("1", "2", "3", "+"),
            listOf("±", "0", ".", "=")
        )
        val board = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        keys.forEach { rowKeys ->
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            rowKeys.forEach { key ->
                val primary = key == "="
                val special = key in setOf("AC", "⌫", "%", "÷", "×", "−", "+")
                val color = when {
                    primary -> Color.rgb(90, 114, 255)
                    special -> Color.rgb(38, 54, 77)
                    else -> Color.rgb(26, 36, 52)
                }
                val button = TextView(this).apply {
                    text = key
                    gravity = Gravity.CENTER
                    textSize = if (key == "AC" || key == "⌫") 25f else 29f
                    setTextColor(if (special) Color.rgb(145, 183, 255) else Color.WHITE)
                    typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                    background = GradientDrawable().apply {
                        setColor(color)
                        cornerRadius = dp(19).toFloat()
                    }
                    setOnClickListener { onKey(key) }
                }
                val lp = LinearLayout.LayoutParams(0, -1, 1f).apply { setMargins(dp(4), dp(4), dp(4), dp(4)) }
                row.addView(button, lp)
            }
            board.addView(row, LinearLayout.LayoutParams(-1, 0, 1f))
        }
        root.addView(board, LinearLayout.LayoutParams(-1, 0, 3.7f))
        setContentView(root)
        if (savedInstanceState != null) {
            entry = savedInstanceState.getString("entry", "0")
            stored = savedInstanceState.getString("stored")?.toBigDecimalOrNull()
            operator = savedInstanceState.getString("operator")
            fresh = savedInstanceState.getBoolean("fresh")
            error = savedInstanceState.getBoolean("error")
            display.text = entry
            detail.text = if (operator != null && stored != null) "${formatted(stored!!)} $operator" else ""
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("entry", entry)
        outState.putString("stored", stored?.toPlainString())
        outState.putString("operator", operator)
        outState.putBoolean("fresh", fresh)
        outState.putBoolean("error", error)
        super.onSaveInstanceState(outState)
    }

    private fun onKey(key: String) {
        if (error) {
            if (key == "AC") { /* handled below */ }
            else if (key.length == 1 && key[0] in '0'..'9') { entry = "0"; error = false; fresh = true }
            else return
        }
        when (key) {
            "AC" -> { entry = "0"; stored = null; operator = null; fresh = false; error = false }
            "⌫" -> { if (fresh) { entry = "0"; fresh = false } else entry = if (entry.length <= 1 || (entry.length == 2 && entry.startsWith("-"))) "0" else entry.dropLast(1) }
            "." -> { if (fresh) { entry = "0"; fresh = false }; if (!entry.contains('.')) entry += "." }
            "±" -> { if (entry != "0") entry = if (entry.startsWith("-")) entry.drop(1) else "-$entry" }
            "%" -> { entry = formatted(number().divide(BigDecimal(100), mc)); fresh = false }
            "+", "−", "×", "÷" -> setOperator(key)
            "=" -> calculate()
            else -> if (key.length == 1 && key[0] in '0'..'9') {
                entry = if (fresh || entry == "0") key else if (entry.length < 18) entry + key else entry
                fresh = false
            }
        }
        display.text = entry
        detail.text = if (operator != null && stored != null) "${formatted(stored!!)} $operator" else ""
    }

    private fun number(): BigDecimal = entry.toBigDecimalOrNull() ?: BigDecimal.ZERO

    private fun setOperator(next: String) {
        if (stored != null && operator != null && !fresh) {
            val result = operate(stored!!, number(), operator!!)
            if (result == null) { fail(); return }
            stored = result
            entry = formatted(result)
        } else stored = number()
        operator = next
        fresh = true
    }

    private fun calculate() {
        val left = stored ?: return
        val op = operator ?: return
        val right = number()
        val result = operate(left, right, op)
        if (result == null) { fail(); return }
        entry = formatted(result)
        stored = null
        operator = null
        fresh = true
    }

    private fun operate(a: BigDecimal, b: BigDecimal, op: String): BigDecimal? = when (op) {
        "+" -> a.add(b, mc)
        "−" -> a.subtract(b, mc)
        "×" -> a.multiply(b, mc)
        "÷" -> if (b.compareTo(BigDecimal.ZERO) == 0) null else a.divide(b, mc)
        else -> b
    }

    private fun formatted(v: BigDecimal): String = v.stripTrailingZeros().toPlainString().let {
        if (it.length <= 18) it else v.stripTrailingZeros().round(MathContext(9)).toString()
    }
    private fun fail() { entry = "Error"; error = true; stored = null; operator = null; fresh = true }
    private fun dp(value: Int) = (value * resources.displayMetrics.density + 0.5f).toInt()
}
