package com.moondiagnostic.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private val PREF_NAME = "MDC_LOGIN"
    private val KEY_LOGGED_IN = "logged_in"
    private val KEY_USERNAME = "username"
    private val KEY_ROLE = "role"

    private fun text(
        value: String,
        size: Float,
        color: Int = Color.DKGRAY,
        bold: Boolean = false
    ): TextView {

        val t = TextView(this)

        t.text = value
        t.textSize = size
        t.setTextColor(color)
        t.gravity = Gravity.CENTER
        t.setPadding(12, 12, 12, 12)

        if (bold) {
            t.setTypeface(null, Typeface.BOLD)
        }

        return t
    }

    private fun button(
        icon: String,
        title: String
    ): TextView {

        val b = text(
            "$icon\n$title",
            15f,
            Color.DKGRAY,
            true
        )

        b.setBackgroundColor(Color.WHITE)

        val params = LinearLayout.LayoutParams(
            0,
            150,
            1f
        )

        params.setMargins(8, 8, 8, 8)

        b.layoutParams = params

        b.setOnClickListener {

            Toast.makeText(
                this,
                "$title নির্বাচন করা হয়েছে",
                Toast.LENGTH_SHORT
            ).show()
        }

        return b
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences(
            PREF_NAME,
            MODE_PRIVATE
        )

        val loggedIn = prefs.getBoolean(
            KEY_LOGGED_IN,
            false
        )

        if (loggedIn) {

            showDashboard(
                prefs.getString(KEY_USERNAME, "User") ?: "User",
                prefs.getString(KEY_ROLE, "User") ?: "User"
            )

        } else {

            showLogin()
        }
    }

    // =========================
    // LOGIN SCREEN
    // =========================

    private fun showLogin() {

        val root = LinearLayout(this)

        root.orientation = LinearLayout.VERTICAL
        root.gravity = Gravity.CENTER
        root.setBackgroundColor(
            Color.rgb(238, 247, 255)
        )

        root.setPadding(30, 30, 30, 30)

        root.addView(
            text(
                "MDC",
                48f,
                Color.rgb(20, 70, 130),
                true
            )
        )

        root.addView(
            text(
                "মুন ডায়াগনস্টিক সেন্টার",
                24f,
                Color.rgb(10, 80, 150),
                true
            )
        )

        root.addView(
            text(
                "লগইন করুন",
                22f,
                Color.rgb(20, 70, 120),
                true
            )
        )

        val username = EditText(this)

        username.hint = "Username"
        username.textSize = 17f
        username.setSingleLine(true)

        root.addView(
            username,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                60
            ).apply {
                setMargins(0, 20, 0, 10)
            }
        )

        val password = EditText(this)

        password.hint = "Password"
        password.textSize = 17f
        password.setSingleLine(true)

        password.inputType =
            android.text.InputType.TYPE_CLASS_TEXT or
            android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD

        root.addView(
            password,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                60
            ).apply {
                setMargins(0, 10, 0, 10)
            }
        )

        root.addView(
            text(
                "Role নির্বাচন করুন",
                16f,
                Color.DKGRAY,
                true
            )
        )

        val roleSpinner = Spinner(this)

        val roles = arrayOf(
            "Admin",
            "Operator",
            "User"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            roles
        )

        roleSpinner.adapter = adapter

        root.addView(
            roleSpinner,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                60
            ).apply {
                setMargins(0, 5, 0, 20)
            }
        )

        val loginButton = Button(this)

        loginButton.text = "LOGIN"
        loginButton.textSize = 17f

        root.addView(
            loginButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                60
            ).apply {
                setMargins(0, 10, 0, 20)
            }
        )

        root.addView(
            text(
                "Moon Diagnostic Center\nসঠিক নির্ণয়, সুস্থ জীবনের প্রত্যয়",
                14f,
                Color.GRAY
            )
        )

        loginButton.setOnClickListener {

            val user = username.text
                .toString()
                .trim()

            val pass = password.text
                .toString()
                .trim()

            val selectedRole =
                roleSpinner.selectedItem.toString()

            var valid = false

            if (
                selectedRole == "Admin" &&
                user == "admin" &&
                pass == "admin123"
            ) {
                valid = true
            }

            if (
                selectedRole == "Operator" &&
                user == "operator" &&
                pass == "operator123"
            ) {
                valid = true
            }

            if (
                selectedRole == "User" &&
                user == "user" &&
                pass == "user123"
            ) {
                valid = true
            }

            if (valid) {

                getSharedPreferences(
                    PREF_NAME,
                    MODE_PRIVATE
                )
                    .edit()
                    .putBoolean(KEY_LOGGED_IN, true)
                    .putString(KEY_USERNAME, user)
                    .putString(KEY_ROLE, selectedRole)
                    .apply()

                Toast.makeText(
                    this,
                    "Login সফল হয়েছে",
                    Toast.LENGTH_SHORT
                ).show()

                showDashboard(
                    user,
                    selectedRole
                )

            } else {

                Toast.makeText(
                    this,
                    "Username, Password অথবা Role ভুল হয়েছে",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        setContentView(root)
    }

    // =========================
    // DASHBOARD
    // =========================

    private fun showDashboard(
        username: String,
        role: String
    ) {

        val scrollView = ScrollView(this)

        val root = LinearLayout(this)

        root.orientation = LinearLayout.VERTICAL

        root.setBackgroundColor(
            Color.rgb(238, 247, 255)
        )

        root.setPadding(18, 20, 18, 20)

        scrollView.addView(root)

        // Header

        val header = LinearLayout(this)

        header.orientation = LinearLayout.VERTICAL
        header.gravity = Gravity.CENTER

        header.addView(
            text(
                "MDC",
                42f,
                Color.rgb(20, 70, 130),
                true
            )
        )

        header.addView(
            text(
                "মুন ডায়াগনস্টিক সেন্টার",
                25f,
                Color.rgb(10, 80, 150),
                true
            )
        )

        header.addView(
            text(
                "সঠিক নির্ণয়, সুস্থ জীবনের প্রত্যয়",
                15f,
                Color.rgb(30, 80, 130)
            )
        )

        root.addView(
            header,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                180
            )
        )

        // User information

        val userInfo = LinearLayout(this)

        userInfo.orientation = LinearLayout.HORIZONTAL
        userInfo.gravity = Gravity.CENTER_VERTICAL

        val welcome = text(
            "স্বাগতম, $username\nRole: $role",
            17f,
            Color.DKGRAY,
            true
        )

        userInfo.addView(
            welcome,
            LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        val logoutButton = Button(this)

        logoutButton.text = "Logout"

        userInfo.addView(
            logoutButton,
            LinearLayout.LayoutParams(
                120,
                55
            )
        )

        root.addView(
            userInfo,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                90
            )
        )

        // Logout action

        logoutButton.setOnClickListener {

            getSharedPreferences(
                PREF_NAME,
                MODE_PRIVATE
            )
                .edit()
                .clear()
                .apply()

            Toast.makeText(
                this,
                "Logout সফল হয়েছে",
                Toast.LENGTH_SHORT
            ).show()

            showLogin()
        }

        // Statistics

        val stats = LinearLayout(this)

        stats.orientation = LinearLayout.HORIZONTAL

        stats.addView(
            text(
                "📅\nআজকের তারিখ\nআজ",
                14f,
                Color.DKGRAY,
                true
            ),
            LinearLayout.LayoutParams(
                0,
                150,
                1f
            )
        )

        stats.addView(
            text(
                "👥\nমোট সিরিয়াল\n54 জন",
                14f,
                Color.DKGRAY,
                true
            ),
            LinearLayout.LayoutParams(
                0,
                150,
                1f
            )
        )

        root.addView(stats)

        val stats2 = LinearLayout(this)

        stats2.orientation = LinearLayout.HORIZONTAL

        stats2.addView(
            text(
                "⏳\nঅপেক্ষমাণ\n28 জন",
                14f,
                Color.DKGRAY,
                true
            ),
            LinearLayout.LayoutParams(
                0,
                150,
                1f
            )
        )

        stats2.addView(
            text(
                "✓\nসম্পন্ন সিরিয়াল\n26 জন",
                14f,
                Color.DKGRAY,
                true
            ),
            LinearLayout.LayoutParams(
                0,
                150,
                1f
            )
        )

        root.addView(stats2)

        root.addView(
            text(
                "দ্রুত অ্যাকশন",
                20f,
                Color.rgb(20, 70, 120),
                true
            )
        )

        // Buttons

        val actions = LinearLayout(this)

        actions.orientation = LinearLayout.HORIZONTAL

        actions.addView(
            button(
                "📋",
                "টোটাল সিরিয়াল"
            )
        )

        actions.addView(
            button(
                "➕",
                "অ্যাড সিরিয়াল"
            )
        )

        actions.addView(
            button(
                "👨‍⚕️",
                "অ্যাড ডাক্তার"
            )
        )

        actions.addView(
            button(
                "👤",
                "অ্যাড কেয়ার অফ"
            )
        )

        root.addView(actions)

        // Doctor wise

        root.addView(
            text(
                "ডাক্তার ওয়াইজ সিরিয়াল",
                19f,
                Color.rgb(20, 70, 120),
                true
            )
        )

        root.addView(
            text(
                "ডাক্তার নির্বাচন করে তার সিরিয়ালগুলো দেখা যাবে",
                14f
            )
        )

        // Care wise

        root.addView(
            text(
                "কেয়ার ওয়াইজ সিরিয়াল",
                19f,
                Color.rgb(20, 70, 120),
                true
            )
        )

        root.addView(
            text(
                "কেয়ার অফ নির্বাচন করে সংশ্লিষ্ট সিরিয়ালগুলো দেখা যাবে",
                14f
            )
        )

        root.addView(
            text(
                "\nমুন ডায়াগনস্টিক সেন্টার\nআপনার বিশ্বস্ত স্বাস্থ্যসেবা কেন্দ্র",
                14f,
                Color.GRAY
            )
        )

        setContentView(scrollView)
    }
}
