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
    // AUTO REFRESH
    // =========================================================

    private val refreshHandler =
        Handler(Looper.getMainLooper())

    private val refreshIntervalMs =
        20_000L

    /*
     * খুব গুরুত্বপূর্ণ:
     *
     * dashboardVisible = true
     * শুধু Dashboard-এ থাকলেই refresh চলবে।
     *
     * Add Serial / Total Serial / Admin Panel-এ
     * গেলে refresh বন্ধ হয়ে যাবে।
     */
    private var dashboardVisible = false

    private var lastRefreshText: TextView? = null

    private val refreshRunnable =
        object : Runnable {

            override fun run() {

                if (
                    dashboardVisible &&
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
    // ON CREATE
    // =========================================================

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

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

    private fun verticalContainer():
            LinearLayout {

        val l = LinearLayout(this)

        l.orientation =
            LinearLayout.VERTICAL

        l.setPadding(
            14,
            16,
            14,
            28
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
    // NORMAL BUTTON
    // =========================================================

    private fun actionButton(
        text: String,
        color: Int = BLUE,
        height: Int = 66,
        onClick: () -> Unit
    ): TextView {

        val b =
            label(
                text,
                17f,
                WHITE,
                true
            )

        b.background =
            background(
                color,
                14f
            )

        b.setPadding(
            10,
            0,
            10,
            0
        )

        b.elevation = 3f

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

        b.layoutParams = params

        b.setOnClickListener {
            onClick()
        }

        return b
    }

    // =========================================================
    // DASHBOARD 2-COLUMN BUTTON
    // =========================================================

    private fun dashboardGridButton(
        text: String,
        color: Int = BLUE,
        onClick: () -> Unit
    ): TextView {

        val b =
            label(
                text,
                17f,
                WHITE,
                true
            )

        b.background =
            background(
                color,
                16f
            )

        b.setPadding(
            8,
            8,
            8,
            8
        )

        b.elevation = 4f

        val params =
            LinearLayout.LayoutParams(
                0,
                88,
                1f
            )

        params.setMargins(
            5,
            5,
            5,
            5
        )

        b.layoutParams = params

        b.setOnClickListener {
            onClick()
        }

        return b
    }

    // =========================================================
    // LARGE INPUT
    // =========================================================

    private fun input(
        hint: String,
        password: Boolean = false
    ): EditText {

        val e = EditText(this)

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
            18,
            0,
            18,
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
                70
            )

        params.setMargins(
            6,
            7,
            6,
            7
        )

        e.layoutParams = params

        return e
    }

    // =========================================================
    // LOGIN
    // =========================================================

    private fun showLogin() {

        dashboardVisible = false

        stopDashboardRefresh()

        currentUsername = ""
        currentRole = ""

        val root =
            verticalContainer()

        root.setPadding(
            14,
            24,
            14,
            32
        )

        root.gravity =
            Gravity.CENTER_HORIZONTAL

        root.addView(space(24))

        root.addView(
            label(
                "MDC",
                58f,
                BLUE,
                true
            )
        )

        root.addView(space(5))

        root.addView(
            label(
                "মুন ডায়াগনস্টিক সেন্টার",
                28f,
                DARK_BLUE,
                true
            )
        )

        root.addView(space(6))

        root.addView(
            label(
                "সঠিক নির্ণয়, সুস্থ জীবনের প্রত্যয়",
                15f,
                GRAY
            )
        )

        root.addView(space(22))

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

        root.addView(card)

        card.addView(
            label(
                "লগইন করুন",
                30f,
                DARK_BLUE,
                true
            )
        )

        card.addView(space(16))

        val username =
            input("ইউজারনেম")

        val password =
            input(
                "পাসওয়ার্ড",
                true
            )

        card.addView(username)
        card.addView(password)

        card.addView(space(12))

        card.addView(
            actionButton(
                "🔐   লগইন",
                BLUE,
                68
            ) {

                loginUser(
                    username.text.toString().trim(),
                    password.text.toString()
                )
            }
        )

        root.addView(space(20))

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

        stopDashboardRefresh()

        val root =
            verticalContainer()

        root.setPadding(
            10,
            18,
            10,
            32
        )

        // HEADER

        root.addView(
            label(
                "MDC",
                52f,
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

        root.addView(space(8))

        root.addView(
            actionButton(
                "🚪   Logout",
                RED,
                62
            ) {

                logout()
            }
        )

        root.addView(space(10))

        // DATE

        root.addView(
            label(
                "আজকের তারিখ",
                24f,
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

        root.addView(space(14))

        // STATISTICS

        val serials =
            readSerials()

        val waiting =
            serials.count {
                it.status == "Waiting"
            }

        val completed =
            serials.count {
                it.status == "Completed"
            }

        val cancelled =
            serials.count {
                it.status == "Cancelled"
            }

        val row1 =
            LinearLayout(this)

        row1.orientation =
            LinearLayout.HORIZONTAL

        row1.addView(
            statCard(
                "👥",
                "মোট সিরিয়াল",
                "${serials.size} জন",
                BLUE
            )
        )

        row1.addView(
            statCard(
                "⏳",
                "অপেক্ষমাণ",
                "$waiting জন",
                ORANGE
            )
        )

        root.addView(row1)

        val row2 =
            LinearLayout(this)

        row2.orientation =
            LinearLayout.HORIZONTAL

        row2.addView(
            statCard(
                "✓",
                "সম্পন্ন",
                "$completed জন",
                GREEN
            )
        )

        row2.addView(
            statCard(
                "✕",
                "বাতিল",
                "$cancelled জন",
                RED
            )
        )

        root.addView(row2)

        root.addView(space(16))

        // QUICK ACTION

        root.addView(
            label(
                "দ্রুত অ্যাকশন",
                27f,
                DARK_BLUE,
                true
            )
        )

        root.addView(space(6))

        // FIRST TWO BUTTONS

        val quickRow1 =
            LinearLayout(this)

        quickRow1.orientation =
            LinearLayout.HORIZONTAL

        quickRow1.addView(
            dashboardGridButton(
                "📋\nটোটাল সিরিয়াল"
            ) {

                showTotalSerial()
            }
        )

        quickRow1.addView(
            dashboardGridButton(
                "＋\nঅ্যাড সিরিয়াল"
            ) {

                showAddSerial()
            }
        )

        root.addView(quickRow1)

        // SECOND TWO BUTTONS

        val quickRow2 =
            LinearLayout(this)

        quickRow2.orientation =
            LinearLayout.HORIZONTAL

        quickRow2.addView(
            dashboardGridButton(
                "👨‍⚕️\nঅ্যাড ডাক্তার"
            ) {

                showAddDoctor()
            }
        )

        quickRow2.addView(
            dashboardGridButton(
                "🧑‍🤝‍🧑\nঅ্যাড কেয়ার অফ"
            ) {

                showAddCareOf()
            }
        )

        root.addView(quickRow2)

        root.addView(space(18))

        // DOCTOR WISE

        root.addView(
            label(
                "ডাক্তার ওয়াইজ সিরিয়াল",
                25f,
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

        root.addView(space(16))

        // CARE WISE

        root.addView(
            label(
                "কেয়ার ওয়াইজ সিরিয়াল",
                25f,
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

        // ADMIN PANEL

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

            root.addView(space(6))

            root.addView(
                actionButton(
                    "⚙   Admin Control Panel",
                    PURPLE,
                    68
                ) {

                    showAdminPanel()
                }
            )
        }

        root.addView(space(18))

        // REFRESH BOX

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
                15f,
                Color.rgb(
                    181,
                    224,
                    216
                )
            )

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

        root.addView(refreshBox)

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

        // Start 20 second refresh.

        dashboardVisible = true

        refreshHandler.removeCallbacks(
            refreshRunnable
        )

        refreshHandler.postDelayed(
            refreshRunnable,
            refreshIntervalMs
        )
    }

    // =========================================================
    // REFRESH
    // =========================================================

    private fun refreshDashboardData() {

        if (!dashboardVisible) {
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
         * এখানে শুধু Dashboard rebuild হবে।
         *
         * Add Serial screen-এ timer ঢুকতে পারবে না,
         * কারণ showAddSerial() dashboardVisible=false করে।
         */

        showDashboard()
    }

    private fun stopDashboardRefresh() {

        refreshHandler.removeCallbacks(
            refreshRunnable
        )
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

        /*
         * সবচেয়ে গুরুত্বপূর্ণ FIX:
         * Add Serial পেজে ঢুকলে Dashboard refresh বন্ধ।
         */

        dashboardVisible = false

        stopDashboardRefresh()

        if (currentUsername.isEmpty()) {

            toast(
                "আগে Login করুন"
            )

            return
        }

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

        root.addView(space(16))

        val card =
            LinearLayout(this)

        card.orientation =
            LinearLayout.VERTICAL

        card.setPadding(
            16,
            20,
            16,
            20
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
                14f,
                GRAY
            )
        )

        card.addView(
            label(
                "$currentUsername  •  $currentRole",
                18f,
                TEAL,
                true
            )
        )

        card.addView(space(10))

        card.addView(
            actionButton(
                "✅   সিরিয়াল তৈরি করুন",
                GREEN,
                70
            ) {

                saveSerial(
                    patient.text.toString().trim(),
                    careOf.text.toString().trim(),
                    doctor.text.toString().trim()
                )
            }
        )

        root.addView(card)

        root.addView(space(16))

        root.addView(
            actionButton(
                "←   Dashboard-এ ফিরে যান",
                BLUE,
                68
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
                    serialPrefix +
                    today +
                    "_"
                )
            ) {

                val n =
                    key.substringAfterLast(
                        "_"
                    ).toIntOrNull() ?: 0

                if (n >= next) {
                    next = n + 1
                }
            }
        }

        val key =
            serialPrefix +
            today +
            "_" +
            next

        /*
         * সিরিয়ালের সাথে অবশ্যই User/Operator
         * নাম এবং Role সংরক্ষণ করা হচ্ছে।
         */

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

        dashboardVisible = false

        stopDashboardRefresh()

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

        for (key in pref.all.keys.sorted()) {

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

        stopDashboardRefresh()

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

        root.addView(space(12))

        if (records.isEmpty()) {

            root.addView(
                label(
                    "আজ এখনো কোনো সিরিয়াল তৈরি হয়নি",
                    17f,
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
                    5,
                    6,
                    5,
                    6
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
                        18f,
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
                        15f,
                        GRAY
                    )
                )

                card.addView(
                    label(
                        "ডাক্তার: ${r.doctor}",
                        16f,
                        DARK
                    )
                )

                /*
                 * এখানে User / Operator-এর নাম
                 * পরিষ্কারভাবে দেখাবে।
                 */

                card.addView(
                    label(
                        "✍ দিয়েছেন: ${r.createdBy} (${r.createdRole})",
                        15f,
                        TEAL,
                        true
                    )
                )

                card.addView(
                    label(
                        "সময়: ${r.createdAt}",
                        13f,
                        GRAY
                    )
                )

                root.addView(card)
            }
        }

        root.addView(space(14))

        root.addView(
            actionButton(
                "＋   নতুন সিরিয়াল",
                GREEN,
                68
            ) {

                showAddSerial()
            }
        )

        root.addView(
            actionButton(
                "←   Dashboard-এ ফিরে যান",
                BLUE,
                68
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
                17f,
                LIGHT_BORDER
            )

        card.elevation = 4f

        val params =
            LinearLayout.LayoutParams(
                0,
                142,
                1f
            )

        params.setMargins(
            5,
            5,
            5,
            5
        )

        card.layoutParams = params

        card.addView(
            label(
                icon,
                34f,
                color,
                true
            )
        )

        card.addView(
            label(
                title,
                18f,
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
    // ADD DOCTOR
    // =========================================================

    private fun showAddDoctor() {

        dashboardVisible = false

        stopDashboardRefresh()

        val root =
            verticalContainer()

        root.addView(
            label(
                "👨‍⚕️ অ্যাড ডাক্তার",
                28f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            label(
                "ডাক্তারের নাম সংরক্ষণ করার পেজ",
                15f,
                GRAY
            )
        )

        root.addView(space(18))

        val doctor =
            input(
                "ডাক্তারের নাম"
            )

        root.addView(doctor)

        root.addView(
            actionButton(
                "💾   ডাক্তার সংরক্ষণ করুন",
                GREEN,
                68
            ) {

                val name =
                    doctor.text.toString().trim()

                if (name.isEmpty()) {

                    toast(
                        "ডাক্তারের নাম লিখুন"
                    )

                } else {

                    toast(
                        "ডাক্তার \"$name\" সংরক্ষণ হয়েছে"
                    )
                }
            }
        )

        root.addView(space(12))

        root.addView(
            actionButton(
                "←   Dashboard-এ ফিরে যান",
                BLUE,
                68
            ) {

                showDashboard()
            }
        )

        setContentView(
            scrollScreen(root)
        )
    }

    // =========================================================
    // ADD CARE OF
    // =========================================================

    private fun showAddCareOf() {

        dashboardVisible = false

        stopDashboardRefresh()

        val root =
            verticalContainer()

        root.addView(
            label(
                "🧑‍🤝‍🧑 অ্যাড কেয়ার অফ",
                28f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            label(
                "Care Of / অভিভাবকের নাম সংরক্ষণ করার পেজ",
                15f,
                GRAY
            )
        )

        root.addView(space(18))

        val careOf =
            input(
                "Care Of / অভিভাবকের নাম"
            )

        root.addView(careOf)

        root.addView(
            actionButton(
                "💾   Care Of সংরক্ষণ করুন",
                GREEN,
                68
            ) {

                val name =
                    careOf.text.toString().trim()

                if (name.isEmpty()) {

                    toast(
                        "Care Of নাম লিখুন"
                    )

                } else {

                    toast(
                        "Care Of \"$name\" সংরক্ষণ হয়েছে"
                    )
                }
            }
        )

        root.addView(space(12))

        root.addView(
            actionButton(
                "←   Dashboard-এ ফিরে যান",
                BLUE,
                68
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

        dashboardVisible = false

        stopDashboardRefresh()

        if (
            !currentRole.equals(
                "Admin",
                true
            )
        ) {

            toast(
                "শুধুমাত্র Admin এই পেজ ব্যবহার করতে পারবেন"
            )

            return
        }

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

        root.addView(space(14))

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

        val spinnerParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                66
            )

        spinnerParams.setMargins(
            6,
            7,
            6,
            7
        )

        root.addView(
            roleSpinner,
            spinnerParams
        )

        root.addView(space(6))

        root.addView(
            actionButton(
                "＋   নতুন User / Operator তৈরি করুন",
                TEAL,
                70
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
                24f,
                DARK_BLUE,
                true
            )
        )

        root.addView(space(6))

        showUserList(root)

        root.addView(space(16))

        root.addView(
            actionButton(
                "←   Dashboard-এ ফিরে যান",
                BLUE,
                68
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
                key.startsWith("user_")
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
                        4,
                        6,
                        4
                    )

                    card.layoutParams =
                        params

                    val info =
                        label(
                            "$username\nRole: $role",
                            15f,
                            DARK,
                            true
                        )

                    card.addView(
                        info,
                        LinearLayout.LayoutParams(
                            0,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            1f
                        )
                    )

                    val delete =
                        label(
                            "মুছুন",
                            14f,
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
                            46
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
                    15f,
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

        stopDashboardRefresh()

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

        stopDashboardRefresh()
    }

    override fun onResume() {

        super.onResume()

        if (
            dashboardVisible &&
            currentUsername.isNotEmpty()
        ) {

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

        stopDashboardRefresh()

        super.onDestroy()
    }

    // =========================================================
    // BACK BUTTON
    // =========================================================

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
