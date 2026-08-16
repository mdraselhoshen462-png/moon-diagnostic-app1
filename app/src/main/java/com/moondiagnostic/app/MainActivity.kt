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
import android.content.SharedPreferences

import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {

    // =========================================================
    // COLORS
    // =========================================================

    private val BG = Color.rgb(242, 248, 253)
    private val BLUE = Color.rgb(35, 103, 157)
    private val DARK_BLUE = Color.rgb(20, 67, 110)
    private val TEAL = Color.rgb(25, 143, 134)
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

    private lateinit var pref: SharedPreferences

    private var currentUsername = ""
    private var currentRole = ""

    private val serialPrefix = "serial_"

    // =========================================================
    // SERIAL DATA
    // =========================================================

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
    // REFRESH
    // =========================================================

    private val refreshHandler = Handler(Looper.getMainLooper())

    private val refreshIntervalMs = 20_000L

    private var dashboardVisible = false

    private var lastRefreshText: TextView? = null

    private val refreshRunnable = object : Runnable {

        override fun run() {

            if (
                dashboardVisible &&
                currentUsername.isNotEmpty()
            ) {

                updateDashboardRefreshTime()

                refreshHandler.postDelayed(
                    this,
                    refreshIntervalMs
                )
            }
        }
    }

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

        if (pref.getBoolean("logged_in", false)) {

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
    // TEXT
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

        t.setPadding(
            8,
            4,
            8,
            4
        )

        if (bold) {

            t.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
            )
        }

        return t
    }

    // =========================================================
    // MAIN CONTAINER
    // =========================================================

    private fun verticalContainer(): LinearLayout {

        val layout = LinearLayout(this)

        layout.orientation =
            LinearLayout.VERTICAL

        layout.setPadding(
            12,
            18,
            12,
            30
        )

        layout.setBackgroundColor(BG)

        return layout
    }

    // =========================================================
    // SCROLL
    // =========================================================

    private fun scrollScreen(
        content: View
    ): ScrollView {

        val scroll = ScrollView(this)

        scroll.setBackgroundColor(BG)

        scroll.isFillViewport = true

        scroll.addView(content)

        return scroll
    }

    // =========================================================
    // BACKGROUND
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

        val button =
            label(
                text,
                17f,
                WHITE,
                true
            )

        button.background =
            background(
                color,
                14f
            )

        button.setPadding(
            10,
            0,
            10,
            0
        )

        button.elevation = 3f

        val params =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                height
            )

        params.setMargins(
            6,
            5,
            6,
            5
        )

        button.layoutParams = params

        button.setOnClickListener {

            onClick()
        }

        return button
    }

    // =========================================================
    // GRID BUTTON
    // =========================================================
    //
    // এইটাই নতুন Dashboard-এর গুরুত্বপূর্ণ অংশ।
    // দুই পাশে ২টা করে button থাকবে।
    // =========================================================

    private fun gridButton(
        text: String,
        color: Int = BLUE,
        onClick: () -> Unit
    ): TextView {

        val button =
            label(
                text,
                16f,
                WHITE,
                true
            )

        button.background =
            background(
                color,
                14f
            )

        button.gravity =
            Gravity.CENTER

        button.setPadding(
            8,
            8,
            8,
            8
        )

        button.elevation = 3f

        val params =
            LinearLayout.LayoutParams(
                0,
                92,
                1f
            )

        params.setMargins(
            4,
            4,
            4,
            4
        )

        button.layoutParams = params

        button.setOnClickListener {

            onClick()
        }

        return button
    }

    // =========================================================
    // INPUT
    // =========================================================

    private fun input(
        hint: String,
        password: Boolean = false
    ): EditText {

        val edit =
            EditText(this)

        edit.hint = hint

        edit.textSize = 18f

        edit.setTextColor(DARK)

        edit.setHintTextColor(
            Color.rgb(
                125,
                130,
                135
            )
        )

        edit.setPadding(
            18,
            0,
            18,
            0
        )

        edit.background =
            background(
                WHITE,
                18f,
                TEAL
            )

        if (password) {

            edit.inputType =
                InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_VARIATION_PASSWORD

        } else {

            edit.inputType =
                InputType.TYPE_CLASS_TEXT
        }

        val params =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                68
            )

        params.setMargins(
            6,
            7,
            6,
            7
        )

        edit.layoutParams = params

        return edit
    }

    // =========================================================
    // LOGIN
    // =========================================================

    private fun showLogin() {

        dashboardVisible = false

        refreshHandler.removeCallbacks(
            refreshRunnable
        )

        currentUsername = ""
        currentRole = ""

        val root =
            verticalContainer()

        root.setPadding(
            16,
            25,
            16,
            30
        )

        root.gravity =
            Gravity.CENTER_HORIZONTAL

        root.addView(space(25))

        root.addView(
            label(
                "MDC",
                58f,
                BLUE,
                true
            )
        )

        root.addView(
            label(
                "মুন ডায়াগনস্টিক সেন্টার",
                27f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            label(
                "সঠিক নির্ণয়, সুস্থ জীবনের প্রত্যয়",
                14f,
                GRAY
            )
        )

        root.addView(space(20))

        val card =
            LinearLayout(this)

        card.orientation =
            LinearLayout.VERTICAL

        card.setPadding(
            16,
            24,
            16,
            24
        )

        card.background =
            background(
                WHITE,
                22f,
                LIGHT_BORDER
            )

        card.elevation = 6f

        root.addView(
            card,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        card.addView(
            label(
                "লগইন করুন",
                29f,
                DARK_BLUE,
                true
            )
        )

        card.addView(space(15))

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
                64
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

        root.addView(space(18))

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
    // LOGIN USER
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

        dashboardVisible = true

        refreshHandler.removeCallbacks(
            refreshRunnable
        )

        val root =
            verticalContainer()

        // -----------------------------------------------------
        // LOGO
        // -----------------------------------------------------

        root.addView(
            label(
                "MDC",
                54f,
                BLUE,
                true
            )
        )

        root.addView(
            label(
                "স্বাগতম, $currentUsername",
                25f,
                DARK,
                true
            )
        )

        root.addView(
            label(
                "Role: $currentRole",
                16f,
                TEAL,
                true
            )
        )

        root.addView(space(7))

        // -----------------------------------------------------
        // LOGOUT
        // -----------------------------------------------------

        root.addView(
            actionButton(
                "🚪   Logout",
                RED,
                60
            ) {

                logout()
            }
        )

        root.addView(space(10))

        // -----------------------------------------------------
        // DATE
        // -----------------------------------------------------

        root.addView(
            label(
                "আজকের তারিখ",
                28f,
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
                20f,
                DARK
            )
        )

        root.addView(space(15))

        // =====================================================
        // STATISTICS GRID
        // =====================================================

        val records =
            readSerials()

        val waiting =
            records.count {
                it.status == "Waiting"
            }

        val completed =
            records.count {
                it.status == "Completed"
            }

        val cancelled =
            records.count {
                it.status == "Cancelled"
            }

        // ROW 1

        val statRow1 =
            LinearLayout(this)

        statRow1.orientation =
            LinearLayout.HORIZONTAL

        statRow1.layoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                145
            )

        statRow1.addView(
            statCard(
                "👥",
                "মোট সিরিয়াল",
                "${records.size} জন",
                BLUE
            )
        )

        statRow1.addView(
            statCard(
                "⏳",
                "অপেক্ষমাণ",
                "$waiting জন",
                ORANGE
            )
        )

        root.addView(statRow1)

        // ROW 2

        val statRow2 =
            LinearLayout(this)

        statRow2.orientation =
            LinearLayout.HORIZONTAL

        statRow2.layoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                145
            )

        statRow2.addView(
            statCard(
                "✓",
                "সম্পন্ন",
                "$completed জন",
                GREEN
            )
        )

        statRow2.addView(
            statCard(
                "✕",
                "বাতিল",
                "$cancelled জন",
                RED
            )
        )

        root.addView(statRow2)

        root.addView(space(18))

        // =====================================================
        // QUICK ACTION
        // =====================================================

        root.addView(
            label(
                "দ্রুত অ্যাকশন",
                28f,
                DARK_BLUE,
                true
            )
        )

        root.addView(space(5))

        // QUICK ROW 1

        val quickRow1 =
            LinearLayout(this)

        quickRow1.orientation =
            LinearLayout.HORIZONTAL

        quickRow1.layoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                100
            )

        quickRow1.addView(
            gridButton(
                "📋\nটোটাল সিরিয়াল",
                BLUE
            ) {

                showTotalSerial()
            }
        )

        quickRow1.addView(
            gridButton(
                "＋\nঅ্যাড সিরিয়াল",
                BLUE
            ) {

                showAddSerial()
            }
        )

        root.addView(quickRow1)

        // QUICK ROW 2

        val quickRow2 =
            LinearLayout(this)

        quickRow2.orientation =
            LinearLayout.HORIZONTAL

        quickRow2.layoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                100
            )

        quickRow2.addView(
            gridButton(
                "👨‍⚕️\nঅ্যাড ডাক্তার",
                BLUE
            ) {

                if (
                    currentRole.equals(
                        "Admin",
                        true
                    )
                ) {

                    toast(
                        "অ্যাড ডাক্তার"
                    )

                } else {

                    toast(
                        "শুধুমাত্র Admin ডাক্তার যোগ করতে পারবেন"
                    )
                }
            }
        )

        quickRow2.addView(
            gridButton(
                "👨‍👩‍👧\nঅ্যাড কেয়ার অফ",
                BLUE
            ) {

                toast(
                    "অ্যাড কেয়ার অফ"
                )
            }
        )

        root.addView(quickRow2)

        root.addView(space(18))

        // =====================================================
        // DOCTOR WISE
        // =====================================================

        root.addView(
            label(
                "ডাক্তার ওয়াইজ সিরিয়াল",
                27f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            label(
                "ডাক্তার নির্বাচন করে তার সিরিয়ালগুলো দেখা যাবে",
                15f,
                GRAY
            )
        )

        root.addView(space(15))

        // =====================================================
        // CARE WISE
        // =====================================================

        root.addView(
            label(
                "কেয়ার ওয়াইজ সিরিয়াল",
                27f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            label(
                "কেয়ার অফ নির্বাচন করে সংশ্লিষ্ট সিরিয়ালগুলো দেখা যাবে",
                15f,
                GRAY
            )
        )

        // =====================================================
        // ADMIN
        // =====================================================

        if (
            currentRole.equals(
                "Admin",
                true
            )
        ) {

            root.addView(space(20))

            root.addView(
                label(
                    "👑 Admin Control Panel",
                    23f,
                    PURPLE,
                    true
                )
            )

            root.addView(
                label(
                    "User এবং Operator পরিচালনা করুন",
                    15f,
                    GRAY
                )
            )

            root.addView(space(5))

            root.addView(
                actionButton(
                    "⚙   Admin Control Panel",
                    PURPLE,
                    62
                ) {

                    showAdminPanel()
                }
            )
        }

        // =====================================================
        // REFRESH INFO
        // =====================================================

        root.addView(space(20))

        val refreshBox =
            LinearLayout(this)

        refreshBox.orientation =
            LinearLayout.VERTICAL

        refreshBox.gravity =
            Gravity.CENTER

        refreshBox.setPadding(
            14,
            14,
            14,
            14
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

        refreshBox.addView(
            label(
                "🔄  ডাটা প্রতি ২০ সেকেন্ড পর পর রিফ্রেশ হবে",
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
            refreshBox
        )

        root.addView(space(20))

        root.addView(
            label(
                "মুন ডায়াগনস্টিক সেন্টার",
                16f,
                GRAY,
                true
            )
        )

        root.addView(
            label(
                "আপনার বিশ্বস্ত স্বাস্থ্যসেবা কেন্দ্র",
                14f,
                GRAY
            )
        )

        setContentView(
            scrollScreen(root)
        )

        // -----------------------------------------------------
        // IMPORTANT
        // -----------------------------------------------------
        // এখানে আর showDashboard() কল হচ্ছে না।
        // তাই ২০ সেকেন্ড পর Add Serial page ভেঙে Dashboard-এ
        // ফিরে যাবে না।
        // -----------------------------------------------------

        refreshHandler.postDelayed(
            refreshRunnable,
            refreshIntervalMs
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
                17f,
                LIGHT_BORDER
            )

        card.elevation = 4f

        val params =
            LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1f
            )

        params.setMargins(
            4,
            5,
            4,
            5
        )

        card.layoutParams = params

        card.addView(
            label(
                icon,
                36f,
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
                19f,
                color,
                true
            )
        )

        return card
    }

    // =========================================================
    // REFRESH TIME
    // =========================================================

    private fun updateDashboardRefreshTime() {

        if (!dashboardVisible) {
            return
        }

        lastRefreshText?.text =
            "সর্বশেষ আপডেট: ${currentTime()}"
    }

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

        if (currentUsername.isEmpty()) {

            toast(
                "আগে Login করুন"
            )

            return
        }

        // Dashboard refresh বন্ধ।
        // Add Serial page-এ থাকলে ২০ সেকেন্ড পর
        // আর Dashboard খুলবে না।

        dashboardVisible = false

        refreshHandler.removeCallbacks(
            refreshRunnable
        )

        val root =
            verticalContainer()

        root.addView(
            label(
                "➕ নতুন সিরিয়াল",
                29f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            label(
                "রোগীর তথ্য দিয়ে নতুন সিরিয়াল তৈরি করুন",
                15f,
                GRAY
            )
        )

        root.addView(space(15))

        val card =
            LinearLayout(this)

        card.orientation =
            LinearLayout.VERTICAL

        card.setPadding(
            18,
            22,
            18,
            22
        )

        card.background =
            background(
                WHITE,
                22f,
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
                16f,
                DARK_BLUE,
                true
            )
        )

        card.addView(patient)

        card.addView(
            label(
                "Care Of",
                16f,
                DARK_BLUE,
                true
            )
        )

        card.addView(careOf)

        card.addView(
            label(
                "ডাক্তার",
                16f,
                DARK_BLUE,
                true
            )
        )

        card.addView(doctor)

        card.addView(space(10))

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

        card.addView(space(10))

        card.addView(
            actionButton(
                "✅   সিরিয়াল তৈরি করুন",
                GREEN,
                66
            ) {

                saveSerial(
                    patient.text.toString().trim(),
                    careOf.text.toString().trim(),
                    doctor.text.toString().trim()
                )
            }
        )

        root.addView(card)

        root.addView(space(15))

        root.addView(
            actionButton(
                "←   Dashboard-এ ফিরে যান",
                BLUE,
                64
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

        for (key in pref.all.keys) {

            if (
                key.startsWith(
                    serialPrefix + today + "_"
                )
            ) {

                val number =
                    key.substringAfterLast(
                        "_"
                    ).toIntOrNull() ?: 0

                if (number >= next) {

                    next =
                        number + 1
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
            "সিরিয়াল #$next তৈরি হয়েছে"
        )

        showTotalSerial()
    }

    // =========================================================
    // READ SERIAL
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

        for (key in pref.all.keys.sorted()) {

            if (
                !key.startsWith(
                    serialPrefix + today + "_"
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

            if (parts.size >= 7) {

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

        dashboardVisible = false

        refreshHandler.removeCallbacks(
            refreshRunnable
        )

        val root =
            verticalContainer()

        val records =
            readSerials()

        root.addView(
            label(
                "📋 আজকের মোট সিরিয়াল",
                28f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            label(
                "মোট ${records.size} জন",
                18f,
                TEAL,
                true
            )
        )

        root.addView(space(10))

        if (records.isEmpty()) {

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
                    14,
                    16,
                    14
                )

                card.background =
                    background(
                        WHITE,
                        17f,
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
                            if (r.careOf.isEmpty())
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

                root.addView(card)
            }
        }

        root.addView(space(15))

        root.addView(
            actionButton(
                "＋   নতুন সিরিয়াল",
                GREEN,
                64
            ) {

                showAddSerial()
            }
        )

        root.addView(
            actionButton(
                "←   Dashboard-এ ফিরে যান",
                BLUE,
                64
            ) {

                showDashboard()
            }
        )

        setContentView(
            scrollScreen(root)
        )
    }

    // =========================================================
    // ADMIN PANEL
    // =========================================================

    private fun showAdminPanel() {

        if (
            !currentRole.equals(
                "Admin",
                true
            )
        ) {

            toast(
                "শুধুমাত্র Admin ব্যবহার করতে পারবেন"
            )

            return
        }

        dashboardVisible = false

        refreshHandler.removeCallbacks(
            refreshRunnable
        )

        val root =
            verticalContainer()

        root.addView(
            label(
                "👑 Admin Control Panel",
                27f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            label(
                "User এবং Operator পরিচালনা করুন",
                15f,
                GRAY
            )
        )

        root.addView(space(15))

        val username =
            input(
                "নতুন Username"
            )

        val password =
            input(
                "নতুন Password",
                true
            )

        root.addView(username)

        root.addView(password)

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

        root.addView(
            roleSpinner,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                64
            )
        )

        root.addView(space(10))

        root.addView(
            actionButton(
                "＋   নতুন User / Operator তৈরি করুন",
                TEAL,
                64
            ) {

                createUser(
                    username.text.toString().trim(),
                    password.text.toString(),
                    roleSpinner.selectedItem.toString()
                )
            }
        )

        root.addView(space(18))

        root.addView(
            label(
                "বর্তমান User / Operator",
                23f,
                DARK_BLUE,
                true
            )
        )

        showUserList(root)

        root.addView(space(18))

        root.addView(
            actionButton(
                "←   Dashboard-এ ফিরে যান",
                BLUE,
                64
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

        if (password.length < 4) {

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

        var count = 0

        for (key in pref.all.keys) {

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
                            68
                        )

                    params.setMargins(
                        6,
                        5,
                        6,
                        5
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
                            82,
                            44
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

        dashboardVisible = false

        refreshHandler.removeCallbacks(
            refreshRunnable
        )

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
                    .getInstance("SHA-256")
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
    // LIFECYCLE
    // =========================================================

    override fun onPause() {

        super.onPause()

        refreshHandler.removeCallbacks(
            refreshRunnable
        )
    }

    override fun onResume() {

        super.onResume()

        if (
            dashboardVisible &&
            currentUsername.isNotEmpty()
        ) {

            updateDashboardRefreshTime()

            refreshHandler.removeCallbacks(
                refreshRunnable
            )

            refreshHandler.postDelayed(
                refreshRunnable,
                refreshIntervalMs
            )
        }
    }

    override fun onDestroy() {

        refreshHandler.removeCallbacks(
            refreshRunnable
        )

        super.onDestroy()
    }

    // =========================================================
    // BACK
    // =========================================================

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {

        if (
            currentRole.isNotEmpty()
        ) {

            showDashboard()

        } else {

            super.onBackPressed()
        }
    }
}
