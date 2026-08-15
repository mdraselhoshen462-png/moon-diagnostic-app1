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
    // SCREEN STATE
    // =========================================================

    private enum class Screen {
        LOGIN,
        DASHBOARD,
        ADD_SERIAL,
        TOTAL_SERIAL,
        ADMIN_PANEL
    }

    private var currentScreen = Screen.LOGIN

    // =========================================================
    // AUTO REFRESH
    // =========================================================

    private val refreshHandler = Handler(Looper.getMainLooper())

    // 20 seconds
    private val refreshIntervalMs = 20_000L

    private var lastRefreshText: TextView? = null

    private val refreshRunnable = object : Runnable {

        override fun run() {

            // IMPORTANT:
            // Auto refresh will ONLY work on Dashboard.
            // It will NEVER force another page back to Dashboard.

            if (
                currentScreen == Screen.DASHBOARD &&
                currentUsername.isNotEmpty()
            ) {

                refreshDashboardData()

                refreshHandler.postDelayed(
                    this,
                    refreshIntervalMs
                )
            }
        }
    }

    // =========================================================
    // SERIAL DATA
    // =========================================================

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

    // =========================================================
    // ACTIVITY
    // =========================================================

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        pref = getSharedPreferences(
            PREF_NAME,
            MODE_PRIVATE
        )

        createDefaultAdmin()

        if (
            pref.getBoolean(
                "logged_in",
                false
            )
        ) {

            currentUsername =
                pref.getString(
                    "current_user",
                    ""
                ) ?: ""

            currentRole =
                pref.getString(
                    "current_role",
                    ""
                ) ?: ""

            if (
                currentUsername.isNotEmpty() &&
                currentRole.isNotEmpty()
            ) {

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
                .putString(
                    "user_admin",
                    "admin"
                )
                .putString(
                    "pass_admin",
                    hashPassword("admin123")
                )
                .putString(
                    "role_admin",
                    "Admin"
                )
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

            t.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
            )
        }

        return t
    }

    // =========================================================
    // CONTAINER
    // =========================================================

    private fun verticalContainer(): LinearLayout {

        val l = LinearLayout(this)

        l.orientation =
            LinearLayout.VERTICAL

        l.setPadding(
            16,
            16,
            16,
            24
        )

        return l
    }

    private fun scrollScreen(
        content: View
    ): ScrollView {

        val scroll =
            ScrollView(this)

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

        val drawable =
            GradientDrawable()

        drawable.setColor(color)

        drawable.cornerRadius =
            radius

        if (strokeColor != null) {

            drawable.setStroke(
                2,
                strokeColor
            )
        }

        return drawable
    }

    // =========================================================
    // SPACE
    // =========================================================

    private fun space(
        height: Int
    ): Space {

        val s = Space(this)

        s.layoutParams =
            LinearLayout.LayoutParams(
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

        val b =
            label(
                text,
                16f,
                WHITE,
                true
            )

        b.background =
            background(
                color,
                14f
            )

        b.setPadding(
            12,
            0,
            12,
            0
        )

        b.elevation = 3f

        val params =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                height
            )

        params.setMargins(
            8,
            5,
            8,
            5
        )

        b.layoutParams = params

        b.setOnClickListener {

            onClick()
        }

        return b
    }

    // =========================================================
    // INPUT
    // =========================================================

    private fun input(
        hint: String,
        password: Boolean = false
    ): EditText {

        val e =
            EditText(this)

        e.hint = hint

        e.textSize = 18f

        e.setTextColor(DARK)

        e.setHintTextColor(
            Color.rgb(
                125,
                130,
                135
            )
        )

        e.setPadding(
            16,
            0,
            16,
            0
        )

        e.background =
            background(
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

        val params =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                62
            )

        params.setMargins(
            8,
            7,
            8,
            7
        )

        e.layoutParams = params

        return e
    }

    // =========================================================
    // LOGIN PAGE
    // =========================================================

    private fun showLogin() {

        stopAutoRefresh()

        currentScreen =
            Screen.LOGIN

        currentUsername = ""
        currentRole = ""

        val root =
            verticalContainer()

        root.setPadding(
            16,
            22,
            16,
            28
        )

        root.gravity =
            Gravity.CENTER_HORIZONTAL

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
                "মুন ডায়াগনস্টিক সেন্টার",
                27f,
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

        root.addView(space(18))

        val card =
            LinearLayout(this)

        card.orientation =
            LinearLayout.VERTICAL

        card.setPadding(
            14,
            22,
            14,
            22
        )

        card.background =
            background(
                WHITE,
                20f,
                LIGHT_BORDER
            )

        card.elevation = 6f

        val cardParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

        cardParams.setMargins(
            6,
            0,
            6,
            0
        )

        root.addView(
            card,
            cardParams
        )

        card.addView(
            label(
                "লগইন করুন",
                29f,
                DARK_BLUE,
                true
            )
        )

        card.addView(space(14))

        val username =
            input("ইউজারনেম")

        val password =
            input(
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
                "অ্যাক্সেস শুধুমাত্র অনুমোদিত User / Operator / Admin-এর জন্য",
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
    // LOGIN
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
            pref.getString(
                "user_$username",
                null
            )

        val savedPassword =
            pref.getString(
                "pass_$username",
                null
            )

        val savedRole =
            pref.getString(
                "role_$username",
                null
            )

        if (
            savedUsername != null &&
            savedPassword != null &&
            savedRole != null &&
            savedPassword ==
            hashPassword(password)
        ) {

            currentUsername =
                username

            currentRole =
                savedRole

            pref.edit()
                .putBoolean(
                    "logged_in",
                    true
                )
                .putString(
                    "current_user",
                    username
                )
                .putString(
                    "current_role",
                    savedRole
                )
                .apply()

            toast(
                "সফলভাবে লগইন হয়েছে"
            )

            showDashboard()

        } else {

            toast(
                "Username অথবা Password ভুল"
            )
        }
    }

    // =========================================================
    // DASHBOARD
    // =========================================================

    private fun showDashboard() {

        // First stop any old refresh job.
        stopAutoRefresh()

        // Now we are officially on Dashboard.
        currentScreen =
            Screen.DASHBOARD

        val root =
            verticalContainer()

        root.setPadding(
            12,
            18,
            12,
            28
        )

        // =====================================================
        // HEADER
        // =====================================================

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
                "স্বাগতম, $currentUsername",
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

        // =====================================================
        // LOGOUT
        // =====================================================

        root.addView(
            actionButton(
                "🚪   Logout",
                RED,
                58
            ) {

                logout()
            }
        )

        root.addView(space(10))

        // =====================================================
        // DATE
        // =====================================================

        root.addView(
            label(
                "আজকের তারিখ",
                21f,
                DARK_BLUE,
                true
            )
        )

        val date =
            SimpleDateFormat(
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

        // =====================================================
        // STATISTICS
        // =====================================================

        val dashboardSerials =
            readSerials()

        val waitingCount =
            dashboardSerials.count {
                it.status == "Waiting"
            }

        val completedCount =
            dashboardSerials.count {
                it.status == "Completed"
            }

        val cancelledCount =
            dashboardSerials.count {
                it.status == "Cancelled"
            }

        val stats1 =
            LinearLayout(this)

        stats1.orientation =
            LinearLayout.HORIZONTAL

        stats1.addView(
            statCard(
                "👥",
                "মোট সিরিয়াল",
                "${dashboardSerials.size} জন",
                BLUE
            )
        )

        stats1.addView(
            statCard(
                "⏳",
                "অপেক্ষমাণ",
                "$waitingCount জন",
                ORANGE
            )
        )

        root.addView(stats1)

        val stats2 =
            LinearLayout(this)

        stats2.orientation =
            LinearLayout.HORIZONTAL

        stats2.addView(
            statCard(
                "✓",
                "সম্পন্ন",
                "$completedCount জন",
                GREEN
            )
        )

        stats2.addView(
            statCard(
                "✕",
                "বাতিল",
                "$cancelledCount জন",
                RED
            )
        )

        root.addView(stats2)

        root.addView(space(12))

        // =====================================================
        // QUICK ACTION
        // =====================================================

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

                showTotalSerial()
            }
        )

        root.addView(
            actionButton(
                "＋   অ্যাড সিরিয়াল",
                BLUE
            ) {

                showAddSerial()
            }
        )

        root.addView(
            actionButton(
                "ডাক্তার   অ্যাড ডাক্তার",
                BLUE
            ) {

                toast(
                    "অ্যাড ডাক্তার"
                )
            }
        )

        root.addView(
            actionButton(
                "কেয়ার   অ্যাড কেয়ার অফ",
                BLUE
            ) {

                toast(
                    "অ্যাড কেয়ার অফ"
                )
            }
        )

        root.addView(space(12))

        // =====================================================
        // DOCTOR WISE
        // =====================================================

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

        // =====================================================
        // CARE WISE
        // =====================================================

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

        // =====================================================
        // ADMIN PANEL
        // =====================================================

        if (
            currentRole.equals(
                "Admin",
                true
            )
        ) {

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

        // =====================================================
        // AUTO REFRESH INFORMATION
        // =====================================================

        root.addView(space(18))

        val refreshBox =
            LinearLayout(this)

        refreshBox.orientation =
            LinearLayout.VERTICAL

        refreshBox.gravity =
            Gravity.CENTER

        refreshBox.setPadding(
            12,
            12,
            12,
            12
        )

        refreshBox.background =
            background(
                Color.rgb(
                    232,
                    247,
                    244
                ),
                14f,
                Color.rgb(
                    181,
                    224,
                    216
                )
            )

        refreshBox.elevation = 2f

        refreshBox.addView(
            label(
                "🔄  ডাটা প্রতি ২০ সেকেন্ড পর পর অটো রিফ্রেশ হচ্ছে",
                14f,
                TEAL,
                true
            )
        )

        lastRefreshText =
            label(
                "সর্বশেষ আপডেট: ${currentTime()}",
                13f,
                GRAY
            )

        refreshBox.addView(
            lastRefreshText
        )

        root.addView(
            refreshBox,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(space(18))

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

        // Start refresh ONLY after Dashboard is displayed.
        startAutoRefresh()
    }

    // =========================================================
    // START AUTO REFRESH
    // =========================================================

    private fun startAutoRefresh() {

        stopAutoRefresh()

        if (
            currentScreen !=
            Screen.DASHBOARD
        ) {
            return
        }

        refreshHandler.postDelayed(
            refreshRunnable,
            refreshIntervalMs
        )
    }

    // =========================================================
    // STOP AUTO REFRESH
    // =========================================================

    private fun stopAutoRefresh() {

        refreshHandler.removeCallbacks(
            refreshRunnable
        )
    }

    // =========================================================
    // DASHBOARD REFRESH
    // =========================================================

    private fun refreshDashboardData() {

        // VERY IMPORTANT:
        // Never refresh if user is not currently on Dashboard.

        if (
            currentScreen !=
            Screen.DASHBOARD
        ) {
            return
        }

        currentUsername =
            pref.getString(
                "current_user",
                currentUsername
            ) ?: currentUsername

        currentRole =
            pref.getString(
                "current_role",
                currentRole
            ) ?: currentRole

        /*
         * Rebuild Dashboard ONLY because the user
         * is actually on Dashboard.
         *
         * If the user is on Add Serial, this function
         * will never run.
         */

        showDashboard()
    }

    // =========================================================
    // CURRENT TIME
    // =========================================================

    private fun currentTime(): String {

        return SimpleDateFormat(
            "hh:mm:ss a",
            Locale.getDefault()
        ).format(Date())
    }

    // =========================================================
    // ADD SERIAL
    // =========================================================

    private fun showAddSerial() {

        // IMPORTANT:
        // Stop Dashboard auto refresh immediately.
        stopAutoRefresh()

        // Change screen state BEFORE creating the page.
        currentScreen =
            Screen.ADD_SERIAL

        if (
            currentUsername.isEmpty()
        ) {

            toast(
                "আগে Login করুন"
            )

            showLogin()

            return
        }

        val root =
            verticalContainer()

        root.setPadding(
            14,
            18,
            14,
            28
        )

        root.addView(
            label(
                "➕ নতুন সিরিয়াল",
                28f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            label(
                "রোগীর তথ্য দিয়ে নতুন সিরিয়াল তৈরি করুন",
                14f,
                GRAY
            )
        )

        root.addView(space(14))

        val card =
            LinearLayout(this)

        card.orientation =
            LinearLayout.VERTICAL

        card.setPadding(
            14,
            18,
            14,
            18
        )

        card.background =
            background(
                WHITE,
                20f,
                LIGHT_BORDER
            )

        card.elevation = 5f

        val patient =
            input(
                "রোগীর নাম"
            )

        val careOf =
            input(
                "Care Of / অভিভাবকের নাম"
            )

        val doctor =
            input(
                "ডাক্তারের নাম"
            )

        card.addView(
            label(
                "রোগীর নাম",
                15f,
                DARK_BLUE,
                true
            )
        )

        card.addView(
            patient
        )

        card.addView(
            label(
                "Care Of",
                15f,
                DARK_BLUE,
                true
            )
        )

        card.addView(
            careOf
        )

        card.addView(
            label(
                "ডাক্তার",
                15f,
                DARK_BLUE,
                true
            )
        )

        card.addView(
            doctor
        )

        card.addView(space(8))

        card.addView(
            label(
                "সিরিয়ালটি তৈরি হবে আপনার Login করা নামের অধীনে:",
                13f,
                GRAY
            )
        )

        card.addView(
            label(
                "$currentUsername  •  $currentRole",
                17f,
                TEAL,
                true
            )
        )

        card.addView(space(8))

        card.addView(
            actionButton(
                "✅   সিরিয়াল তৈরি করুন",
                GREEN,
                64
            ) {

                saveSerial(
                    patient.text
                        .toString()
                        .trim(),

                    careOf.text
                        .toString()
                        .trim(),

                    doctor.text
                        .toString()
                        .trim()
                )
            }
        )

        root.addView(
            card
        )

        root.addView(space(14))

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
    // SAVE SERIAL
    // =========================================================

    private fun saveSerial(
        patient: String,
        careOf: String,
        doctor: String
    ) {

        if (patient.isEmpty()) {

            toast(
                "রোগীর নাম লিখুন"
            )

            return
        }

        if (doctor.isEmpty()) {

            toast(
                "ডাক্তারের নাম লিখুন"
            )

            return
        }

        val today =
            SimpleDateFormat(
                "yyyyMMdd",
                Locale.getDefault()
            ).format(Date())

        var next = 1

        for (
            key in pref.all.keys
        ) {

            if (
                key.startsWith(
                    serialPrefix +
                    today +
                    "_"
                )
            ) {

                val n =
                    key.substringAfterLast(
                        "_"
                    ).toIntOrNull()
                        ?: 0

                if (
                    n >= next
                ) {

                    next = n + 1
                }
            }
        }

        val key =
            serialPrefix +
            today +
            "_" +
            next

        val value =
            listOf(
                patient,
                careOf,
                doctor,
                "Waiting",
                currentUsername,
                currentRole,
                currentTime()
            ).joinToString("||")

        pref.edit()
            .putString(
                key,
                value
            )
            .apply()

        toast(
            "সিরিয়াল #$next তৈরি হয়েছে — $currentUsername"
        )

        showTotalSerial()
    }

    // =========================================================
    // READ SERIALS
    // =========================================================

    private fun readSerials():
            List<SerialRecord> {

        val result =
            mutableListOf<SerialRecord>()

        val today =
            SimpleDateFormat(
                "yyyyMMdd",
                Locale.getDefault()
            ).format(Date())

        for (
            key in pref.all.keys.sorted()
        ) {

            if (
                !key.startsWith(
                    serialPrefix +
                    today +
                    "_"
                )
            ) {
                continue
            }

            val number =
                key.substringAfterLast(
                    "_"
                ).toIntOrNull()
                    ?: continue

            val raw =
                pref.getString(
                    key,
                    ""
                ) ?: continue

            val parts =
                raw.split("||")

            if (
                parts.size >= 7
            ) {

                result.add(
                    SerialRecord(
                        number,
                        parts[0],
                        parts[1],
                        parts[2],
                        parts[3],
                        parts[4],
                        parts[5],
                        parts[6]
                    )
                )
            }
        }

        return result.sortedBy {
            it.number
        }
    }

    // =========================================================
    // TOTAL SERIAL
    // =========================================================

    private fun showTotalSerial() {

        // IMPORTANT:
        // Stop Dashboard auto refresh.
        stopAutoRefresh()

        currentScreen =
            Screen.TOTAL_SERIAL

        val root =
            verticalContainer()

        root.setPadding(
            12,
            18,
            12,
            28
        )

        val records =
            readSerials()

        root.addView(
            label(
                "📋 আজকের মোট সিরিয়াল",
                27f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            label(
                "মোট ${records.size} জন",
                17f,
                TEAL,
                true
            )
        )

        root.addView(space(10))

        if (
            records.isEmpty()
        ) {

            root.addView(
                label(
                    "আজ এখনো কোনো সিরিয়াল তৈরি হয়নি",
                    16f,
                    GRAY
                )
            )

        } else {

            records.forEach { r ->

                val card =
                    LinearLayout(this)

                card.orientation =
                    LinearLayout.VERTICAL

                card.setPadding(
                    16,
                    12,
                    16,
                    12
                )

                card.background =
                    background(
                        WHITE,
                        16f,
                        LIGHT_BORDER
                    )

                card.elevation = 2f

                val params =
                    LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )

                params.setMargins(
                    6,
                    5,
                    6,
                    5
                )

                card.layoutParams =
                    params

                card.addView(
                    label(
                        "সিরিয়াল #${r.number}   •   ${r.status}",
                        19f,
                        BLUE,
                        true
                    )
                )

                card.addView(
                    label(
                        "👤 ${r.patient}",
                        17f,
                        DARK,
                        true
                    )
                )

                card.addView(
                    label(
                        "Care Of: ${
                            if (
                                r.careOf.isEmpty()
                            )
                                "—"
                            else
                                r.careOf
                        }",
                        14f,
                        GRAY
                    )
                )

                card.addView(
                    label(
                        "ডাক্তার: ${r.doctor}",
                        15f,
                        DARK
                    )
                )

                // WHO CREATED THE SERIAL
                card.addView(
                    label(
                        "✍ দিয়েছেন: ${r.createdBy} (${r.createdRole})",
                        14f,
                        TEAL,
                        true
                    )
                )

                card.addView(
                    label(
                        "সময়: ${r.createdAt}",
                        12f,
                        GRAY
                    )
                )

                root.addView(
                    card
                )
            }
        }

        root.addView(space(12))

        root.addView(
            actionButton(
                "＋   নতুন সিরিয়াল",
                GREEN
            ) {

                showAddSerial()
            }
        )

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
    // STAT CARD
    // =========================================================

    private fun statCard(
        icon: String,
        title: String,
        value: String,
        color: Int
    ): LinearLayout {

        val card =
            LinearLayout(this)

        card.orientation =
            LinearLayout.VERTICAL

        card.gravity =
            Gravity.CENTER

        card.setPadding(
            8,
            12,
            8,
            12
        )

        card.background =
            background(
                WHITE,
                16f,
                LIGHT_BORDER
            )

        card.elevation = 3f

        val params =
            LinearLayout.LayoutParams(
                0,
                118,
                1f
            )

        params.setMargins(
            4,
            4,
            4,
            4
        )

        card.layoutParams =
            params

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

        // IMPORTANT:
        // Stop Dashboard auto refresh.
        stopAutoRefresh()

        currentScreen =
            Screen.ADMIN_PANEL

        if (
            !currentRole.equals(
                "Admin",
                true
            )
        ) {

            toast(
                "শুধুমাত্র Admin এই পেজ ব্যবহার করতে পারবেন"
            )

            showDashboard()

            return
        }

        val root =
            verticalContainer()

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

        val username =
            input(
                "নতুন Username"
            )

        val password =
            input(
                "নতুন Password",
                true
            )

        root.addView(
            username
        )

        root.addView(
            password
        )

        val roleSpinner =
            Spinner(this)

        val roles =
            arrayOf(
                "Operator",
                "User"
            )

        val adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                roles
            )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        roleSpinner.adapter =
            adapter

        val spinnerParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                62
            )

        spinnerParams.setMargins(
            8,
            6,
            8,
            6
        )

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
                    username.text
                        .toString()
                        .trim(),

                    password.text
                        .toString(),

                    roleSpinner
                        .selectedItem
                        .toString()
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

            toast(
                "Username দিন"
            )

            return
        }

        if (
            password.length < 4
        ) {

            toast(
                "Password কমপক্ষে ৪ অক্ষরের হতে হবে"
            )

            return
        }

        if (
            pref.contains(
                "user_$username"
            )
        ) {

            toast(
                "এই Username আগে থেকেই আছে"
            )

            return
        }

        pref.edit()
            .putString(
                "user_$username",
                username
            )
            .putString(
                "pass_$username",
                hashPassword(password)
            )
            .putString(
                "role_$username",
                role
            )
            .apply()

        toast(
            "$role সফলভাবে তৈরি হয়েছে"
        )

        showAdminPanel()
    }

    // =========================================================
    // USER LIST
    // =========================================================

    private fun showUserList(
        root: LinearLayout
    ) {

        val all =
            pref.all

        var count = 0

        for (
            key in all.keys
        ) {

            if (
                key.startsWith(
                    "user_"
                )
            ) {

                val username =
                    pref.getString(
                        key,
                        ""
                    ) ?: ""

                val role =
                    pref.getString(
                        "role_$username",
                        ""
                    ) ?: ""

                if (
                    username.isNotEmpty() &&
                    !username.equals(
                        "admin",
                        true
                    )
                ) {

                    val card =
                        LinearLayout(this)

                    card.orientation =
                        LinearLayout.HORIZONTAL

                    card.gravity =
                        Gravity.CENTER_VERTICAL

                    card.setPadding(
                        14,
                        8,
                        8,
                        8
                    )

                    card.background =
                        background(
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

                    card.layoutParams =
                        params

                    val info =
                        label(
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

                        deleteUser(
                            username
                        )
                    }

                    card.addView(
                        delete,
                        LinearLayout.LayoutParams(
                            80,
                            42
                        )
                    )

                    root.addView(
                        card
                    )

                    count++
                }
            }
        }

        if (
            count == 0
        ) {

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

    private fun deleteUser(
        username: String
    ) {

        if (
            username.equals(
                "admin",
                true
            )
        ) {

            toast(
                "Admin account মুছা যাবে না"
            )

            return
        }

        pref.edit()
            .remove(
                "user_$username"
            )
            .remove(
                "pass_$username"
            )
            .remove(
                "role_$username"
            )
            .apply()

        toast(
            "$username মুছে ফেলা হয়েছে"
        )

        showAdminPanel()
    }

    // =========================================================
    // LOGOUT
    // =========================================================

    private fun logout() {

        stopAutoRefresh()

        currentScreen =
            Screen.LOGIN

        pref.edit()
            .putBoolean(
                "logged_in",
                false
            )
            .remove(
                "current_user"
            )
            .remove(
                "current_role"
            )
            .apply()

        currentUsername = ""
        currentRole = ""

        toast(
            "Logout সফল হয়েছে"
        )

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
                    .getInstance(
                        "SHA-256"
                    )
                    .digest(
                        password.toByteArray()
                    )

            bytes.joinToString("") {

                "%02x".format(it)
            }

        } catch (
            e: Exception
        ) {

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
    // PAUSE
    // =========================================================

    override fun onPause() {

        super.onPause()

        // Stop timer while app is not active.
        stopAutoRefresh()
    }

    // =========================================================
    // RESUME
    // =========================================================

    override fun onResume() {

        super.onResume()

        /*
         * IMPORTANT:
         *
         * If user is on Dashboard,
         * restart Dashboard refresh.
         *
         * If user is on Add Serial,
         * Total Serial or Admin Panel,
         * DO NOT refresh or navigate anywhere.
         */

        if (
            currentScreen ==
            Screen.DASHBOARD &&
            currentUsername.isNotEmpty()
        ) {

            lastRefreshText?.text =
                "সর্বশেষ আপডেট: ${currentTime()}"

            startAutoRefresh()
        }
    }

    // =========================================================
    // DESTROY
    // =========================================================

    override fun onDestroy() {

        stopAutoRefresh()

        super.onDestroy()
    }

    // =========================================================
    // BACK BUTTON
    // =========================================================

    @Suppress("DEPRECATION")
    override fun onBackPressed() {

        when (currentScreen) {

            Screen.ADD_SERIAL,
            Screen.TOTAL_SERIAL,
            Screen.ADMIN_PANEL -> {

                showDashboard()
            }

            Screen.DASHBOARD -> {

                // Keep the previous behavior:
                // Back on Dashboard does not logout.
                super.onBackPressed()
            }

            Screen.LOGIN -> {

                super.onBackPressed()
            }
        }
    }
}
