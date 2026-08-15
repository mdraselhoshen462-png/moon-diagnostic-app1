package com.moondiagnostic.app

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*

class LoginActivity : Activity() {

    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var role: Spinner
    private lateinit var prefs: SharedPreferences

    private val bgColor = Color.rgb(238, 247, 255)
    private val blue = Color.rgb(20, 75, 130)
    private val dark = Color.rgb(45, 45, 45)
    private val green = Color.rgb(0, 137, 123)

    private fun makeText(
        value: String,
        size: Float,
        color: Int = dark,
        bold: Boolean = false
    ): TextView {

        val view = TextView(this)

        view.text = value
        view.textSize = size
        view.setTextColor(color)
        view.gravity = Gravity.CENTER
        view.setPadding(8, 8, 8, 8)

        if (bold) {
            view.setTypeface(null, Typeface.BOLD)
        }

        return view
    }

    private fun makeInput(
        hint: String,
        passwordField: Boolean = false
    ): EditText {

        val input = EditText(this)

        input.hint = hint
        input.textSize = 16f

        // গুরুত্বপূর্ণ: লেখার রং কালো
        input.setTextColor(Color.rgb(25, 25, 25))

        // গুরুত্বপূর্ণ: Hint-এর রংও দৃশ্যমান
        input.setHintTextColor(Color.rgb(100, 100, 100))

        input.setSingleLine(true)

        input.setPadding(20, 0, 20, 0)

        input.background = GradientDrawable().apply {
            setColor(Color.WHITE)
            setStroke(2, green)
            cornerRadius = 12f
        }

        if (passwordField) {
            input.inputType =
                InputType.TYPE_CLASS_TEXT or
                        InputType.TYPE_TEXT_VARIATION_PASSWORD
        } else {
            input.inputType = InputType.TYPE_CLASS_TEXT
        }

        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            60
        )

        params.setMargins(25, 5, 25, 12)

        input.layoutParams = params

        return input
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences(
            "moon_diagnostic_login",
            MODE_PRIVATE
        )

        // আগে Login করা থাকলে সরাসরি MainActivity
        if (prefs.getBoolean("logged_in", false)) {

            openMain()

            return
        }

        val root = LinearLayout(this)

        root.orientation = LinearLayout.VERTICAL
        root.gravity = Gravity.CENTER_HORIZONTAL

        root.setBackgroundColor(bgColor)

        root.setPadding(25, 20, 25, 20)

        // =====================================
        // MDC
        // =====================================

        root.addView(
            makeText(
                "MDC",
                48f,
                blue,
                true
            ),
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                90
            )
        )

        root.addView(
            makeText(
                "মুন ডায়াগনস্টিক সেন্টার",
                25f,
                blue,
                true
            )
        )

        root.addView(
            makeText(
                "লগইন করুন",
                22f,
                blue,
                true
            )
        )

        val space = Space(this)

        root.addView(
            space,
            LinearLayout.LayoutParams(
                1,
                25
            )
        )

        // =====================================
        // USERNAME
        // =====================================

        root.addView(
            makeText(
                "ইউজারনেম",
                16f,
                dark,
                true
            )
        )

        username = makeInput(
            "ইউজারনেম লিখুন"
        )

        root.addView(username)

        // =====================================
        // PASSWORD
        // =====================================

        root.addView(
            makeText(
                "পাসওয়ার্ড",
                16f,
                dark,
                true
            )
        )

        password = makeInput(
            "পাসওয়ার্ড লিখুন",
            true
        )

        root.addView(password)

        // =====================================
        // ROLE
        // =====================================

        root.addView(
            makeText(
                "Role নির্বাচন করুন",
                16f,
                dark,
                true
            )
        )

        role = Spinner(this)

        val roles = arrayOf(
            "Admin",
            "Operator",
            "User"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            roles
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        role.adapter = adapter

        role.setBackgroundColor(Color.WHITE)

        val roleParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            60
        )

        roleParams.setMargins(
            25,
            5,
            25,
            15
        )

        role.layoutParams = roleParams

        root.addView(role)

        // =====================================
        // LOGIN BUTTON
        // =====================================

        val loginButton = Button(this)

        loginButton.text = "লগইন"
        loginButton.textSize = 18f

        loginButton.setTextColor(Color.WHITE)

        loginButton.setTypeface(
            null,
            Typeface.BOLD
        )

        loginButton.setBackgroundColor(green)

        val loginParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            60
        )

        loginParams.setMargins(
            25,
            5,
            25,
            15
        )

        loginButton.layoutParams = loginParams

        root.addView(loginButton)

        // =====================================
        // LOGIN
        // =====================================

        loginButton.setOnClickListener {

            val user =
                username.text.toString().trim()

            val pass =
                password.text.toString().trim()

            val selectedRole =
                role.selectedItem.toString()

            if (user.isEmpty()) {

                username.error = "ইউজারনেম লিখুন"
                username.requestFocus()

                return@setOnClickListener
            }

            if (pass.isEmpty()) {

                password.error = "পাসওয়ার্ড লিখুন"
                password.requestFocus()

                return@setOnClickListener
            }

            // ---------------------------------
            // ADMIN
            // ---------------------------------

            if (
                user == "admin" &&
                pass == "1234" &&
                selectedRole == "Admin"
            ) {

                saveLogin(
                    user,
                    selectedRole
                )

                Toast.makeText(
                    this,
                    "Admin Login সফল হয়েছে",
                    Toast.LENGTH_SHORT
                ).show()

                openMain()

                return@setOnClickListener
            }

            // ---------------------------------
            // ADMIN CREATED USERS
            // ---------------------------------

            val savedUser =
                prefs.getString("user_$user", null)

            val savedPass =
                prefs.getString("pass_$user", null)

            val savedRole =
                prefs.getString("role_$user", null)

            if (
                savedUser != null &&
                savedPass == pass &&
                savedRole == selectedRole
            ) {

                saveLogin(
                    user,
                    selectedRole
                )

                Toast.makeText(
                    this,
                    "$selectedRole Login সফল হয়েছে",
                    Toast.LENGTH_SHORT
                ).show()

                openMain()

            } else {

                Toast.makeText(
                    this,
                    "ইউজারনেম, পাসওয়ার্ড অথবা Role সঠিক নয়",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        // =====================================
        // FOOTER
        // =====================================

        root.addView(
            makeText(
                "Moon Diagnostic Center",
                15f,
                Color.GRAY
            )
        )

        root.addView(
            makeText(
                "সঠিক নির্ণয়, সুস্থ জীবনের প্রত্যয়",
                14f,
                Color.GRAY
            )
        )

        setContentView(root)
    }

    private fun saveLogin(
        user: String,
        selectedRole: String
    ) {

        prefs.edit()
            .putBoolean("logged_in", true)
            .putString("username", user)
            .putString("role", selectedRole)
            .apply()
    }

    private fun openMain() {

        val intent = Intent(
            this,
            MainActivity::class.java
        )

        startActivity(intent)

        finish()
    }
}
