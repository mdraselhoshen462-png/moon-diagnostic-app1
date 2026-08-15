package com.moondiagnostic.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*

import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {

    // =========================================================
    // COLORS
    // =========================================================

    private val BG = Color.rgb(242, 248, 253)
    private val BLUE = Color.rgb(28, 91, 145)
    private val DARK_BLUE = Color.rgb(20, 67, 110)
    private val TEAL = Color.rgb(18, 137, 128)
    private val RED = Color.rgb(198, 58, 58)
    private val GREEN = Color.rgb(39, 135, 91)
    private val ORANGE = Color.rgb(224, 143, 39)
    private val PURPLE = Color.rgb(103, 78, 161)
    private val WHITE = Color.WHITE
    private val DARK = Color.rgb(45, 50, 55)
    private val GRAY = Color.rgb(105, 110, 115)
    private val LIGHT_BORDER = Color.rgb(205, 220, 232)

    // =========================================================
    // STORAGE
    // =========================================================

    private val PREF_NAME = "MDC_APP_DATA"
    private lateinit var pref: android.content.SharedPreferences

    private var currentUsername = ""
    private var currentRole = ""

    // =========================================================
    // ACTIVITY
    // =========================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE)

        createDefaultAdmin()

        if (pref.getBoolean("logged_in", false)) {
            currentUsername = pref.getString("current_user", "") ?: ""
            currentRole = pref.getString("current_role", "") ?: ""

            if (currentUsername.isNotEmpty() && currentRole.isNotEmpty()) {
                showDashboard()
            } else {
                showLogin()
            }
        } else {
            showLogin()
        }
    }

    // =========================================================
    // DEFAULT ADMIN
    // =========================================================

    private fun createDefaultAdmin() {

        if (!pref.contains("user_admin")) {

            pref.edit()
                .putString("user_admin", "admin")
                .putString("pass_admin", hashPassword("admin123"))
                .putString("role_admin", "Admin")
                .apply()
        }
    }

    // =========================================================
    // BASIC TEXT
    // =========================================================

    private fun label(
        text: String,
        size: Float,
        color: Int = DARK,
        bold: Boolean = false
    ): TextView {

        val t = TextView(this)

        t.text = text
        t.textSize = size
        t.setTextColor(color)
        t.gravity = Gravity.CENTER
        t.includeFontPadding = true

        if (bold) {
            t.setTypeface(Typeface.DEFAULT, Typeface.BOLD)
        }

        return t
    }

    // =========================================================
    // CONTAINER
    // =========================================================

    private fun verticalContainer(): LinearLayout {

        val l = LinearLayout(this)

        l.orientation = LinearLayout.VERTICAL
        l.setPadding(16, 16, 16, 24)

        return l
    }

    private fun scrollScreen(content: View): ScrollView {

        val scroll = ScrollView(this)

        scroll.setBackgroundColor(BG)
        scroll.isFillViewport = true

        scroll.addView(content)

        return scroll
    }

    // =========================================================
    // ROUNDED BACKGROUND
    // =========================================================

    private fun background(
        color: Int,
        radius: Float = 18f,
        strokeColor: Int? = null
    ): GradientDrawable {

        val drawable = GradientDrawable()

        drawable.setColor(color)
        drawable.cornerRadius = radius

        if (strokeColor != null) {
            drawable.setStroke(2, strokeColor)
        }

        return drawable
    }

    // =========================================================
    // SPACING
    // =========================================================

    private fun space(height: Int): Space {

        val s = Space(this)

        s.layoutParams = LinearLayout.LayoutParams(
            1,
            height
        )

        return s
    }

    // =========================================================
    // BUTTON
    // =========================================================

    private fun actionButton(
        text: String,
        color: Int = BLUE,
        height: Int = 56,
        onClick: () -> Unit
    ): TextView {

        val b = label(
            text,
            16f,
            WHITE,
            true
        )

        b.background = background(
            color,
            14f
        )

        b.setPadding(12, 0, 12, 0)

        b.elevation = 3f

        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            height
        )

        params.setMargins(8, 5, 8, 5)

        b.layoutParams = params

        b.setOnClickListener {
            onClick()
        }

        return b
    }

    // =========================================================
    // EDIT TEXT
    // =========================================================

    private fun input(
        hint: String,
        password: Boolean = false
    ): EditText {

        val e = EditText(this)

        e.hint = hint
        e.textSize = 16f
        e.setTextColor(DARK)
        e.setHintTextColor(Color.rgb(125, 130, 135))

        e.setPadding(16, 0, 16, 0)

        e.background = background(
            WHITE,
            14f,
            TEAL
        )

        if (password) {
            e.inputType =
                InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_VARIATION_PASSWORD
        } else {
            e.inputType =
                InputType.TYPE_CLASS_TEXT
        }

        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            54
        )

        params.setMargins(8, 6, 8, 6)

        e.layoutParams = params

        return e
    }

    // =========================================================
    // LOGIN PAGE
    // =========================================================

    private fun showLogin() {

        currentUsername = ""
        currentRole = ""

        val root = verticalContainer()

        root.gravity = Gravity.CENTER_HORIZONTAL

        // Logo

        root.addView(space(70))

        root.addView(
            label(
                "MDC",
                52f,
                BLUE,
                true
            )
        )

        root.addView(space(5))

        root.addView(
            label(
                "মুন ডায়াগনস্টিক সেন্টার",
                25f,
                DARK_BLUE,
                true
            )
        )

        root.addView(space(5))

        root.addView(
            label(
                "সঠিক নির্ণয়, সুস্থ জীবনের প্রত্যয়",
                14f,
                GRAY
            )
        )

        root.addView(space(28))

        // Login Card

        val card = LinearLayout(this)

        card.orientation = LinearLayout.VERTICAL
        card.setPadding(14, 18, 14, 18)

        card.background = background(
            WHITE,
            20f,
            LIGHT_BORDER
        )

        card.elevation = 6f

        val cardParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        cardParams.setMargins(6, 0, 6, 0)

        root.addView(card, cardParams)

        card.addView(
            label(
                "লগইন করুন",
                25f,
                DARK_BLUE,
                true
            )
        )

        card.addView(space(12))

        val username = input(
            "ইউজারনেম"
        )

        val password = input(
            "পাসওয়ার্ড",
            true
        )

        card.addView(username)
        card.addView(password)

        card.addView(space(10))

        card.addView(
            actionButton(
                "🔐   লগইন",
                BLUE,
                54
            ) {

                loginUser(
                    username.text.toString().trim(),
                    password.text.toString()
                )
            }
        )

        root.addView(space(18))

        root.addView(
            label(
                "অ্যাক্সেস শুধুমাত্র অনুমোদিত User / Operator / Admin-এর জন্য",
                13f,
                GRAY
            )
        )

        root.addView(space(25))

        root.addView(
            label(
                "Moon Diagnostic Center",
                15f,
                GRAY,
                true
            )
        )

        root.addView(
            label(
                "আপনার বিশ্বস্ত স্বাস্থ্যসেবা কেন্দ্র",
                13f,
                GRAY
            )
        )

        setContentView(
            scrollScreen(root)
        )
    }

    // =========================================================
    // LOGIN FUNCTION
    // =========================================================

    private fun loginUser(
        username: String,
        password: String
    ) {

        if (username.isEmpty()) {
            toast("Username লিখুন")
            return
        }

        if (password.isEmpty()) {
            toast("Password লিখুন")
            return
        }

        val savedUsername =
            pref.getString("user_$username", null)

        val savedPassword =
            pref.getString("pass_$username", null)

        val savedRole =
            pref.getString("role_$username", null)

        if (
            savedUsername != null &&
            savedPassword != null &&
            savedRole != null &&
            savedPassword == hashPassword(password)
        ) {

            currentUsername = username
            currentRole = savedRole

            pref.edit()
                .putBoolean("logged_in", true)
                .putString("current_user", username)
                .putString("current_role", savedRole)
                .apply()

            toast("সফলভাবে লগইন হয়েছে")

            showDashboard()

        } else {

            toast("Username অথবা Password ভুল")
        }
    }

    // =========================================================
    // DASHBOARD
    // =========================================================

    private fun showDashboard() {

        val root = verticalContainer()

        // Header

        root.addView(
            label(
                "MDC",
                46f,
                BLUE,
                true
            )
        )

        root.addView(
            label(
                "স্বাগতম, $currentUsername",
                21f,
                DARK,
                true
            )
        )

        root.addView(
            label(
                "Role: $currentRole",
                15f,
                TEAL,
                true
            )
        )

        root.addView(space(8))

        // Logout

        root.addView(
            actionButton(
                "🚪   Logout",
                RED,
                48
            ) {

                logout()
            }
        )

        root.addView(space(10))

        // Date

        root.addView(
            label(
                "আজকের তারিখ",
                18f,
                DARK_BLUE,
                true
            )
        )

        val date = SimpleDateFormat(
            "dd-MM-yyyy",
            Locale.getDefault()
        ).format(Date())

        root.addView(
            label(
                date,
                16f,
                DARK
            )
        )

        root.addView(space(12))

        // Statistics

        val stats1 = LinearLayout(this)
        stats1.orientation = LinearLayout.HORIZONTAL

        stats1.addView(
            statCard(
                "👥",
                "মোট সিরিয়াল",
                "54 জন",
                BLUE
            )
        )

        stats1.addView(
            statCard(
                "⏳",
                "অপেক্ষমাণ",
                "28 জন",
                ORANGE
            )
        )

        root.addView(stats1)

        val stats2 = LinearLayout(this)
        stats2.orientation = LinearLayout.HORIZONTAL

        stats2.addView(
            statCard(
                "✓",
                "সম্পন্ন",
                "26 জন",
                GREEN
            )
        )

        stats2.addView(
            statCard(
                "✕",
                "বাতিল",
                "0 জন",
                RED
            )
        )

        root.addView(stats2)

        root.addView(space(12))

        // Quick Action title

        root.addView(
            label(
                "দ্রুত অ্যাকশন",
                24f,
                DARK_BLUE,
                true
            )
        )

        root.addView(space(5))

        root.addView(
            actionButton(
                "📋   টোটাল সিরিয়াল",
                BLUE
            ) {
                toast("টোটাল সিরিয়াল")
            }
        )

        root.addView(
            actionButton(
                "＋   অ্যাড সিরিয়াল",
                BLUE
            ) {
                toast("অ্যাড সিরিয়াল")
            }
        )

        root.addView(
            actionButton(
                "ডাক্তার   অ্যাড ডাক্তার",
                BLUE
            ) {
                toast("অ্যাড ডাক্তার")
            }
        )

        root.addView(
            actionButton(
                "কেয়ার   অ্যাড কেয়ার অফ",
                BLUE
            ) {
                toast("অ্যাড কেয়ার অফ")
            }
        )

        root.addView(space(12))

        // Doctor Wise

        root.addView(
            label(
                "ডাক্তার ওয়াইজ সিরিয়াল",
                23f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            label(
                "ডাক্তার নির্বাচন করে তার সিরিয়ালগুলো দেখা যাবে",
                14f,
                GRAY
            )
        )

        root.addView(space(12))

        // Care Wise

        root.addView(
            label(
                "কেয়ার ওয়াইজ সিরিয়াল",
                23f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            label(
                "কেয়ার অফ নির্বাচন করে সংশ্লিষ্ট সিরিয়ালগুলো দেখা যাবে",
                14f,
                GRAY
            )
        )

        // Admin Control Panel

        if (currentRole.equals("Admin", true)) {

            root.addView(space(18))

            root.addView(
                label(
                    "👑 Admin Control Panel",
                    22f,
                    PURPLE,
                    true
                )
            )

            root.addView(
                label(
                    "User এবং Operator পরিচালনা করুন",
                    14f,
                    GRAY
                )
            )

            root.addView(space(5))

            root.addView(
                actionButton(
                    "⚙   Admin Control Panel",
                    PURPLE
                ) {

                    showAdminPanel()
                }
            )
        }

        root.addView(space(20))

        root.addView(
            label(
                "মুন ডায়াগনস্টিক সেন্টার",
                15f,
                GRAY,
                true
            )
        )

        root.addView(
            label(
                "আপনার বিশ্বস্ত স্বাস্থ্যসেবা কেন্দ্র",
                13f,
                GRAY
            )
        )

        setContentView(
            scrollScreen(root)
        )
    }

    // =========================================================
    // STAT CARD
    // =========================================================

    private fun statCard(
        icon: String,
        title: String,
        value: String,
        color: Int
    ): LinearLayout {

        val card = LinearLayout(this)

        card.orientation = LinearLayout.VERTICAL
        card.gravity = Gravity.CENTER
        card.setPadding(8, 12, 8, 12)

        card.background = background(
            WHITE,
            16f,
            LIGHT_BORDER
        )

        card.elevation = 3f

        val params = LinearLayout.LayoutParams(
            0,
            92,
            1f
        )

        params.setMargins(4, 4, 4, 4)

        card.layoutParams = params

        card.addView(
            label(
                icon,
                25f,
                color,
                true
            )
        )

        card.addView(
            label(
                title,
                14f,
                DARK,
                true
            )
        )

        card.addView(
            label(
                value,
                14f,
                color,
                true
            )
        )

        return card
    }

    // =========================================================
    // ADMIN PANEL
    // =========================================================

    private fun showAdminPanel() {

        if (!currentRole.equals("Admin", true)) {

            toast("শুধুমাত্র Admin এই পেজ ব্যবহার করতে পারবেন")
            return
        }

        val root = verticalContainer()

        root.addView(
            label(
                "👑 Admin Control Panel",
                25f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            label(
                "User এবং Operator পরিচালনা করুন",
                14f,
                GRAY
            )
        )

        root.addView(space(12))

        val username = input(
            "নতুন Username"
        )

        val password = input(
            "নতুন Password",
            true
        )

        root.addView(username)
        root.addView(password)

        val roleSpinner = Spinner(this)

        val roles = arrayOf(
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

        roleSpinner.adapter = adapter

        val spinnerParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            54
        )

        spinnerParams.setMargins(8, 6, 8, 6)

        root.addView(
            roleSpinner,
            spinnerParams
        )

        root.addView(space(5))

        root.addView(
            actionButton(
                "＋   নতুন User / Operator তৈরি করুন",
                TEAL
            ) {

                createUser(
                    username.text.toString().trim(),
                    password.text.toString(),
                    roleSpinner.selectedItem.toString()
                )
            }
        )

        root.addView(space(15))

        root.addView(
            label(
                "বর্তমান User / Operator",
                23f,
                DARK_BLUE,
                true
            )
        )

        root.addView(space(5))

        showUserList(root)

        root.addView(space(15))

        root.addView(
            actionButton(
                "←   Dashboard-এ ফিরে যান",
                BLUE
            ) {

                showDashboard()
            }
        )

        setContentView(
            scrollScreen(root)
        )
    }

    // =========================================================
    // CREATE USER
    // =========================================================

    private fun createUser(
        username: String,
        password: String,
        role: String
    ) {

        if (username.isEmpty()) {
            toast("Username দিন")
            return
        }

        if (password.length < 4) {
            toast("Password কমপক্ষে ৪ অক্ষরের হতে হবে")
            return
        }

        if (
            pref.contains("user_$username")
        ) {

            toast("এই Username আগে থেকেই আছে")
            return
        }

        pref.edit()
            .putString("user_$username", username)
            .putString("pass_$username", hashPassword(password))
            .putString("role_$username", role)
            .apply()

        toast("$role সফলভাবে তৈরি হয়েছে")

        showAdminPanel()
    }

    // =========================================================
    // USER LIST
    // =========================================================

    private fun showUserList(
        root: LinearLayout
    ) {

        val all = pref.all

        var count = 0

        for (key in all.keys) {

            if (key.startsWith("user_")) {

                val username =
                    pref.getString(key, "") ?: ""

                val role =
                    pref.getString(
                        "role_$username",
                        ""
                    ) ?: ""

                if (
                    username.isNotEmpty() &&
                    !username.equals("admin", true)
                ) {

                    val card = LinearLayout(this)

                    card.orientation =
                        LinearLayout.HORIZONTAL

                    card.gravity = Gravity.CENTER_VERTICAL

                    card.setPadding(
                        14,
                        8,
                        8,
                        8
                    )

                    card.background = background(
                        WHITE,
                        14f,
                        LIGHT_BORDER
                    )

                    val params =
                        LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            58
                        )

                    params.setMargins(
                        8,
                        4,
                        8,
                        4
                    )

                    card.layoutParams = params

                    val info = label(
                        "$username\nRole: $role",
                        14f,
                        DARK,
                        true
                    )

                    val infoParams =
                        LinearLayout.LayoutParams(
                            0,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            1f
                        )

                    card.addView(
                        info,
                        infoParams
                    )

                    val delete =
                        label(
                            "মুছুন",
                            13f,
                            WHITE,
                            true
                        )

                    delete.background =
                        background(
                            RED,
                            10f
                        )

                    delete.setPadding(
                        12,
                        0,
                        12,
                        0
                    )

                    delete.setOnClickListener {

                        deleteUser(username)
                    }

                    card.addView(
                        delete,
                        LinearLayout.LayoutParams(
                            80,
                            42
                        )
                    )

                    root.addView(card)

                    count++
                }
            }
        }

        if (count == 0) {

            root.addView(
                label(
                    "এখনও কোনো User / Operator তৈরি করা হয়নি",
                    14f,
                    GRAY
                )
            )
        }
    }

    // =========================================================
    // DELETE USER
    // =========================================================

    private fun deleteUser(username: String) {

        if (username.equals("admin", true)) {
            toast("Admin account মুছা যাবে না")
            return
        }

        pref.edit()
            .remove("user_$username")
            .remove("pass_$username")
            .remove("role_$username")
            .apply()

        toast("$username মুছে ফেলা হয়েছে")

        showAdminPanel()
    }

    // =========================================================
    // LOGOUT
    // =========================================================

    private fun logout() {

        pref.edit()
            .putBoolean("logged_in", false)
            .remove("current_user")
            .remove("current_role")
            .apply()

        currentUsername = ""
        currentRole = ""

        toast("Logout সফল হয়েছে")

        showLogin()
    }

    // =========================================================
    // PASSWORD HASH
    // =========================================================

    private fun hashPassword(
        password: String
    ): String {

        return try {

            val bytes =
                MessageDigest
                    .getInstance("SHA-256")
                    .digest(password.toByteArray())

            bytes.joinToString("") {
                "%02x".format(it)
            }

        } catch (e: Exception) {

            password
        }
    }

    // =========================================================
    // TOAST
    // =========================================================

    private fun toast(
        message: String
    ) {

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    // =========================================================
    // BACK BUTTON
    // =========================================================

    override fun onBackPressed() {

        if (currentRole.isNotEmpty()) {

            showDashboard()

        } else {

            super.onBackPressed()
        }
    }
}
