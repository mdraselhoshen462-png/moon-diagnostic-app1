package com.moondiagnostic.app

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import java.util.Calendar

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

    // Local serial records. Each record keeps patient, care-of, doctor,
    // status, creator username and creator role.
    private val serialPrefix = "serial_"

    private data class SerialRecord(
        val number: Int,
        val patient: String,
        val careOf: String,
        val doctor: String,
        val status: String,
        val createdBy: String,
        val createdRole: String,
        val createdAt: String
    )

    // Dashboard auto-refresh: 20 seconds
    private val refreshHandler = Handler(Looper.getMainLooper())
    private val refreshIntervalMs = 20_000L
    private var dashboardVisible = false
    private var lastRefreshText: TextView? = null
    private val refreshRunnable = object : Runnable {
        override fun run() {
            if (dashboardVisible && currentUsername.isNotEmpty()) {
                refreshDashboardData()
                refreshHandler.postDelayed(this, refreshIntervalMs)
            }
        }
    }

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
        height: Int = 62,
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
        e.textSize = 18f
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
            62
        )

        params.setMargins(8, 7, 8, 7)

        e.layoutParams = params

        return e
    }

    // =========================================================
    // LOGIN PAGE
    // =========================================================

    private fun showLogin() {

        currentUsername = ""
        currentRole = ""

        dashboardVisible = false
        refreshHandler.removeCallbacks(refreshRunnable)

        val root = verticalContainer()
        root.setPadding(16, 22, 16, 28)
        root.gravity = Gravity.CENTER_HORIZONTAL

        // Logo

        root.addView(space(38))

        root.addView(
            label(
                "MDC",
                56f,
                BLUE,
                true
            )
        )

        root.addView(space(5))

        root.addView(
            label(
                "唳唳� 唳∴唳唳距唳ㄠΩ唰嵿唳苦 唳膏唳ㄠ唳熰唳�",
                27f,
                DARK_BLUE,
                true
            )
        )

        root.addView(space(5))

        root.addView(
            label(
                "唳膏唳苦 唳ㄠ唳班唳｀Ο唳�, 唳膏唳膏唳� 唳溹唳Θ唰囙Π 唳唳班Δ唰嵿Ο唳",
                14f,
                GRAY
            )
        )

        root.addView(space(18))

        // Login Card

        val card = LinearLayout(this)

        card.orientation = LinearLayout.VERTICAL
        card.setPadding(14, 22, 14, 22)

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
                "唳侧唳囙Θ 唳曕Π唰佮Θ",
                29f,
                DARK_BLUE,
                true
            )
        )

        card.addView(space(14))

        val username = input(
            "唳囙唳溹唳班Θ唰囙Ξ"
        )

        val password = input(
            "唳唳膏唳唳距Π唰嵿Α",
            true
        )

        card.addView(username)
        card.addView(password)

        card.addView(space(10))

        card.addView(
            actionButton(
                "馃攼   唳侧唳囙Θ",
                BLUE,
                62
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
                "唳呧唳唳曕唳膏唳� 唳多唳о唳唳む唳� 唳呧Θ唰佮Ξ唰嬥Ζ唳苦Δ User / Operator / Admin-唳忇Π 唳溹Θ唰嵿Ο",
                13f,
                GRAY
            )
        )

        root.addView(space(16))

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
                "唳嗋Κ唳ㄠ唳� 唳唳多唳Ω唰嵿Δ 唳膏唳唳膏唳ム唳Ω唰囙Μ唳� 唳曕唳ㄠ唳︵唳�",
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
            toast("Username 唳侧唳栢唳�")
            return
        }

        if (password.isEmpty()) {
            toast("Password 唳侧唳栢唳�")
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

            toast("唳膏Λ唳侧Ν唳距Μ唰� 唳侧唳囙Θ 唳灌Ο唳监唳涏")

            showDashboard()

        } else {

            toast("Username 唳呧Ε唳 Password 唳唳�")
        }
    }

    // =========================================================
    // DASHBOARD
    // =========================================================

    private fun showDashboard() {

        dashboardVisible = true
        refreshHandler.removeCallbacks(refreshRunnable)

        val root = verticalContainer()
        root.setPadding(12, 18, 12, 28)

        // Header

        root.addView(
            label(
                "MDC",
                50f,
                BLUE,
                true
            )
        )

        root.addView(
            label(
                "唳膏唳唳椸Δ唳�, $currentUsername",
                23f,
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
                "馃毆   Logout",
                RED,
                58
            ) {

                logout()
            }
        )

        root.addView(space(10))

        // Date

        root.addView(
            label(
                "唳嗋唳曕唳� 唳む唳班唳�",
                21f,
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
                18f,
                DARK
            )
        )

        root.addView(space(12))

        // Statistics

        val dashboardSerials = readSerials()
        val waitingCount = dashboardSerials.count { it.status == "Waiting" }
        val completedCount = dashboardSerials.count { it.status == "Completed" }
        val cancelledCount = dashboardSerials.count { it.status == "Cancelled" }
        val stats1 = LinearLayout(this)
        stats1.orientation = LinearLayout.HORIZONTAL

        stats1.addView(
            statCard(
                "馃懃",
                "唳唳� 唳膏唳班唳唳距Σ",
                "${dashboardSerials.size} 唳溹Θ",
                BLUE
            )
        )

        stats1.addView(
            statCard(
                "鈴�",
                "唳呧Κ唰囙唰嵿Ψ唳唳�",
                "${waitingCount} 唳溹Θ",
                ORANGE
            )
        )

        root.addView(stats1)

        val stats2 = LinearLayout(this)
        stats2.orientation = LinearLayout.HORIZONTAL

        stats2.addView(
            statCard(
                "鉁�",
                "唳膏Ξ唰嵿Κ唳ㄠ唳�",
                "${completedCount} 唳溹Θ",
                GREEN
            )
        )

        stats2.addView(
            statCard(
                "鉁�",
                "唳唳む唳�",
                "${cancelledCount} 唳溹Θ",
                RED
            )
        )

        root.addView(stats2)

        root.addView(space(12))

        // Quick Action title

        root.addView(
            label(
                "唳︵唳班唳� 唳呧唳唳曕Χ唳�",
                24f,
                DARK_BLUE,
                true
            )
        )

        root.addView(space(5))

        root.addView(
            actionButton(
                "馃搵   唳熰唳熰唳� 唳膏唳班唳唳距Σ",
                BLUE
            ) {
                showTotalSerial()
            }
        )

        root.addView(
            actionButton(
                "锛�   唳呧唳唳� 唳膏唳班唳唳距Σ",
                BLUE
            ) {
                showAddSerial()
            }
        )

        root.addView(
            actionButton(
                "唳∴唳曕唳む唳�   唳呧唳唳� 唳∴唳曕唳む唳�",
                BLUE
            ) {
                toast("唳呧唳唳� 唳∴唳曕唳む唳�")
            }
        )

        root.addView(
            actionButton(
                "唳曕唳唳距Π   唳呧唳唳� 唳曕唳唳距Π 唳呧Λ",
                BLUE
            ) {
                toast("唳呧唳唳� 唳曕唳唳距Π 唳呧Λ")
            }
        )

        root.addView(space(12))

        // Doctor Wise

        root.addView(
            label(
                "唳∴唳曕唳む唳� 唳撪Ο唳监唳囙 唳膏唳班唳唳距Σ",
                23f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            label(
                "唳∴唳曕唳む唳� 唳ㄠ唳班唳唳氞Θ 唳曕Π唰� 唳む唳� 唳膏唳班唳唳距Σ唳椸唳侧 唳︵唳栢 唳唳",
                14f,
                GRAY
            )
        )

        root.addView(space(12))

        // Care Wise

        root.addView(
            label(
                "唳曕唳唳距Π 唳撪Ο唳监唳囙 唳膏唳班唳唳距Σ",
                23f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            label(
                "唳曕唳唳距Π 唳呧Λ 唳ㄠ唳班唳唳氞Θ 唳曕Π唰� 唳膏唳多唳侧唳粪唳� 唳膏唳班唳唳距Σ唳椸唳侧 唳︵唳栢 唳唳",
                14f,
                GRAY
            )
        )

        // Admin Control Panel

        if (currentRole.equals("Admin", true)) {

            root.addView(space(18))

            root.addView(
                label(
                    "馃憫 Admin Control Panel",
                    22f,
                    PURPLE,
                    true
                )
            )

            root.addView(
                label(
                    "User 唳忇Μ唳� Operator 唳Π唳苦唳距Σ唳ㄠ 唳曕Π唰佮Θ",
                    14f,
                    GRAY
                )
            )

            root.addView(space(5))

            root.addView(
                actionButton(
                    "鈿�   Admin Control Panel",
                    PURPLE
                ) {

                    showAdminPanel()
                }
            )
        }

        root.addView(space(18))

        val refreshBox = LinearLayout(this)
        refreshBox.orientation = LinearLayout.VERTICAL
        refreshBox.gravity = Gravity.CENTER
        refreshBox.setPadding(12, 12, 12, 12)
        refreshBox.background = background(Color.rgb(232, 247, 244), 14f, Color.rgb(181, 224, 216))
        refreshBox.elevation = 2f

        refreshBox.addView(label("馃攧  唳∴唳熰 唳唳班Δ唳� 唰ㄠЕ 唳膏唳曕唳ㄠ唳� 唳Π 唳Π 唳呧唰� 唳班唳侧唳� 唳灌唰嵿唰�", 14f, TEAL, true))
        lastRefreshText = label("唳膏Π唰嵿Μ唳多唳� 唳嗋Κ唳∴唳�: ${currentTime()}", 13f, GRAY)
        refreshBox.addView(lastRefreshText)
        root.addView(refreshBox, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        root.addView(space(18))

        root.addView(
            label(
                "唳唳� 唳∴唳唳距唳ㄠΩ唰嵿唳苦 唳膏唳ㄠ唳熰唳�",
                15f,
                GRAY,
                true
            )
        )

        root.addView(
            label(
                "唳嗋Κ唳ㄠ唳� 唳唳多唳Ω唰嵿Δ 唳膏唳唳膏唳ム唳Ω唰囙Μ唳� 唳曕唳ㄠ唳︵唳�",
                13f,
                GRAY
            )
        )

        setContentView(
            scrollScreen(root)
        )

        lastRefreshText?.text = "唳膏Π唰嵿Μ唳多唳� 唳嗋Κ唳∴唳�: ${currentTime()}"
        refreshHandler.postDelayed(refreshRunnable, refreshIntervalMs)
    }

    private fun currentTime(): String =
        SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date())

    private fun refreshDashboardData() {
        // Re-read locally stored account/session data and refresh the visible status.
        // When Firebase is connected later, this is the single hook to replace with a Firestore snapshot read.
        currentUsername = pref.getString("current_user", currentUsername) ?: currentUsername
        currentRole = pref.getString("current_role", currentRole) ?: currentRole
        lastRefreshText?.text = "唳膏Π唰嵿Μ唳多唳� 唳嗋Κ唳∴唳�: ${currentTime()}"
        // Rebuild the dashboard so serial counts reflect newly saved records.
        if (dashboardVisible) showDashboard()
    }

    // =========================================================
    // ADD SERIAL
    // =========================================================

    private fun showAddSerial() {

        if (currentUsername.isEmpty()) {
            toast("唳嗋唰� Login 唳曕Π唰佮Θ")
            return
        }

        val root = verticalContainer()
        root.setPadding(14, 18, 14, 28)

        root.addView(label("鉃� 唳ㄠΔ唰佮Θ 唳膏唳班唳唳距Σ", 28f, DARK_BLUE, true))
        root.addView(label("唳班唳椸唳� 唳むΕ唰嵿Ο 唳︵唳唰� 唳ㄠΔ唰佮Θ 唳膏唳班唳唳距Σ 唳む唳班 唳曕Π唰佮Θ", 14f, GRAY))
        root.addView(space(14))

        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setPadding(14, 18, 14, 18)
        card.background = background(WHITE, 20f, LIGHT_BORDER)
        card.elevation = 5f

        val patient = input("唳班唳椸唳� 唳ㄠ唳�")
        val careOf = input("Care Of / 唳呧Ν唳苦Ν唳距Μ唳曕唳� 唳ㄠ唳�")
        val doctor = input("唳∴唳曕唳む唳班唳� 唳ㄠ唳�")

        card.addView(label("唳班唳椸唳� 唳ㄠ唳�", 15f, DARK_BLUE, true))
        card.addView(patient)
        card.addView(label("Care Of", 15f, DARK_BLUE, true))
        card.addView(careOf)
        card.addView(label("唳∴唳曕唳む唳�", 15f, DARK_BLUE, true))
        card.addView(doctor)

        card.addView(space(8))
        card.addView(label("唳膏唳班唳唳距Σ唳熰 唳む唳班 唳灌Μ唰� 唳嗋Κ唳ㄠ唳� Login 唳曕Π唳� 唳ㄠ唳唳� 唳呧Η唰€唳ㄠ:", 13f, GRAY))
        card.addView(label("$currentUsername  鈥�  $currentRole", 17f, TEAL, true))
        card.addView(space(8))

        card.addView(actionButton("鉁�   唳膏唳班唳唳距Σ 唳む唳班 唳曕Π唰佮Θ", GREEN, 64) {
            saveSerial(
                patient.text.toString().trim(),
                careOf.text.toString().trim(),
                doctor.text.toString().trim()
            )
        })

        root.addView(card)
        root.addView(space(14))
        root.addView(actionButton("鈫�   Dashboard-唳� 唳唳班 唳唳�", BLUE) { showDashboard() })

        setContentView(scrollScreen(root))
    }

    private fun saveSerial(patient: String, careOf: String, doctor: String) {
        if (patient.isEmpty()) { toast("唳班唳椸唳� 唳ㄠ唳� 唳侧唳栢唳�"); return }
        if (doctor.isEmpty()) { toast("唳∴唳曕唳む唳班唳� 唳ㄠ唳� 唳侧唳栢唳�"); return }

        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        var next = 1
        for (key in pref.all.keys) {
            if (key.startsWith(serialPrefix + today + "_")) {
                val n = key.substringAfterLast("_").toIntOrNull() ?: 0
                if (n >= next) next = n + 1
            }
        }

        val key = serialPrefix + today + "_" + next
        val value = listOf(
            patient,
            careOf,
            doctor,
            "Waiting",
            currentUsername,
            currentRole,
            currentTime()
        ).joinToString("||")

        pref.edit().putString(key, value).apply()
        toast("唳膏唳班唳唳距Σ #$next 唳む唳班 唳灌Ο唳监唳涏 鈥� $currentUsername")
        showTotalSerial()
    }

    private fun readSerials(): List<SerialRecord> {
        val result = mutableListOf<SerialRecord>()
        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

        for (key in pref.all.keys.sorted()) {
            if (!key.startsWith(serialPrefix + today + "_")) continue
            val number = key.substringAfterLast("_").toIntOrNull() ?: continue
            val raw = pref.getString(key, "") ?: continue
            val parts = raw.split("||")
            if (parts.size >= 7) {
                result.add(SerialRecord(number, parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6]))
            }
        }
        return result.sortedBy { it.number }
    }

    // =========================================================
    // TOTAL SERIAL
    // =========================================================

    private fun showTotalSerial() {
        val root = verticalContainer()
        root.setPadding(12, 18, 12, 28)
        val records = readSerials()

        root.addView(label("馃搵 唳嗋唳曕唳� 唳唳� 唳膏唳班唳唳距Σ", 27f, DARK_BLUE, true))
        root.addView(label("唳唳� ${records.size} 唳溹Θ", 17f, TEAL, true))
        root.addView(space(10))

        if (records.isEmpty()) {
            root.addView(label("唳嗋 唳忇唳ㄠ 唳曕唳ㄠ 唳膏唳班唳唳距Σ 唳む唳班 唳灌Ο唳监Θ唳�", 16f, GRAY))
        } else {
            records.forEach { r ->
                val card = LinearLayout(this)
                card.orientation = LinearLayout.VERTICAL
                card.setPadding(16, 12, 16, 12)
                card.background = background(WHITE, 16f, LIGHT_BORDER)
                card.elevation = 2f
                val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                params.setMargins(6, 5, 6, 5)
                card.layoutParams = params

                card.addView(label("唳膏唳班唳唳距Σ #${r.number}   鈥�   ${r.status}", 19f, BLUE, true))
                card.addView(label("馃懁 ${r.patient}", 17f, DARK, true))
                card.addView(label("Care Of: ${if (r.careOf.isEmpty()) "鈥�" else r.careOf}", 14f, GRAY))
                card.addView(label("唳∴唳曕唳む唳�: ${r.doctor}", 15f, DARK))
                card.addView(label("鉁� 唳︵唳唰囙唰囙Θ: ${r.createdBy} (${r.createdRole})", 14f, TEAL, true))
                card.addView(label("唳膏Ξ唰�: ${r.createdAt}", 12f, GRAY))
                root.addView(card)
            }
        }

        root.addView(space(12))
        root.addView(actionButton("锛�   唳ㄠΔ唰佮Θ 唳膏唳班唳唳距Σ", GREEN) { showAddSerial() })
        root.addView(actionButton("鈫�   Dashboard-唳� 唳唳班 唳唳�", BLUE) { showDashboard() })
        setContentView(scrollScreen(root))
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
            118,
            1f
        )

        params.setMargins(4, 4, 4, 4)

        card.layoutParams = params

        card.addView(
            label(
                icon,
                30f,
                color,
                true
            )
        )

        card.addView(
            label(
                title,
                17f,
                DARK,
                true
            )
        )

        card.addView(
            label(
                value,
                18f,
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

            toast("唳多唳о唳唳む唳� Admin 唳忇 唳唳� 唳唳Μ唳灌唳� 唳曕Π唳む 唳唳班Μ唰囙Θ")
            return
        }

        val root = verticalContainer()

        root.addView(
            label(
                "馃憫 Admin Control Panel",
                25f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            label(
                "User 唳忇Μ唳� Operator 唳Π唳苦唳距Σ唳ㄠ 唳曕Π唰佮Θ",
                14f,
                GRAY
            )
        )

        root.addView(space(12))

        val username = input(
            "唳ㄠΔ唰佮Θ Username"
        )

        val password = input(
            "唳ㄠΔ唰佮Θ Password",
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
            62
        )

        spinnerParams.setMargins(8, 6, 8, 6)

        root.addView(
            roleSpinner,
            spinnerParams
        )

        root.addView(space(5))

        root.addView(
            actionButton(
                "锛�   唳ㄠΔ唰佮Θ User / Operator 唳む唳班 唳曕Π唰佮Θ",
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
                "唳Π唰嵿Δ唳唳� User / Operator",
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
                "鈫�   Dashboard-唳� 唳唳班 唳唳�",
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
            toast("Username 唳︵唳�")
            return
        }

        if (password.length < 4) {
            toast("Password 唳曕Ξ唳唰嵿Ψ唰� 唰� 唳呧唰嵿Ψ唳班唳� 唳灌Δ唰� 唳灌Μ唰�")
            return
        }

        if (
            pref.contains("user_$username")
        ) {

            toast("唳忇 Username 唳嗋唰� 唳ム唳曕唳� 唳嗋唰�")
            return
        }

        pref.edit()
            .putString("user_$username", username)
            .putString("pass_$username", hashPassword(password))
            .putString("role_$username", role)
            .apply()

        toast("$role 唳膏Λ唳侧Ν唳距Μ唰� 唳む唳班 唳灌Ο唳监唳涏")

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
                            "唳唳涏唳�",
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
                    "唳忇唳ㄠ 唳曕唳ㄠ User / Operator 唳む唳班 唳曕Π唳� 唳灌Ο唳监Θ唳�",
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
            toast("Admin account 唳唳涏 唳唳 唳ㄠ")
            return
        }

        pref.edit()
            .remove("user_$username")
            .remove("pass_$username")
            .remove("role_$username")
            .apply()

        toast("$username 唳唳涏 唳唳侧 唳灌Ο唳监唳涏")

        showAdminPanel()
    }

    // =========================================================
    // LOGOUT
    // =========================================================

    private fun logout() {

        dashboardVisible = false
        refreshHandler.removeCallbacks(refreshRunnable)

        pref.edit()
            .putBoolean("logged_in", false)
            .remove("current_user")
            .remove("current_role")
            .apply()

        currentUsername = ""
        currentRole = ""

        toast("Logout 唳膏Λ唳� 唳灌Ο唳监唳涏")

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

    override fun onPause() {
        super.onPause()
        refreshHandler.removeCallbacks(refreshRunnable)
    }

    override fun onResume() {
        super.onResume()
        if (dashboardVisible && currentUsername.isNotEmpty()) {
            refreshDashboardData()
            refreshHandler.removeCallbacks(refreshRunnable)
            refreshHandler.postDelayed(refreshRunnable, refreshIntervalMs)
        }
    }

    override fun onDestroy() {
        refreshHandler.removeCallbacks(refreshRunnable)
        super.onDestroy()
    }

    override fun onBackPressed() {

        if (currentRole.isNotEmpty()) {

            showDashboard()

        } else {

            super.onBackPressed()
        }
    }
}
