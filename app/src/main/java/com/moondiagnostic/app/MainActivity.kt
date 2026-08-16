package com.moondiagnostic.app

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
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
import java.util.Calendar
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

    // =========================================================
    // SERIAL RECORD
    // =========================================================

    private data class SerialRecord(
        val number: Int,
        val patient: String,
        val careOf: String,
        val doctor: String,
        val status: String,
        val createdBy: String,
        val createdRole: String,
        val createdAt: String,
        val serialDate: String
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

                refreshDashboardData()

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

        val b = label(
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
    // LOGIN
    // =========================================================

    private fun showLogin() {

        currentUsername = ""
        currentRole = ""

        dashboardVisible = false

        refreshHandler.removeCallbacks(
            refreshRunnable
        )

        val root =
            verticalContainer()

        root.gravity =
            Gravity.CENTER_HORIZONTAL

        root.addView(space(35))

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
                BLUE
            ) {

                loginUser(
                    username.text
                        .toString()
                        .trim(),

                    password.text
                        .toString()
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

        root.addView(space(15))

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

        root.setPadding(
            12,
            18,
            12,
            28
        )

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

        root.addView(
            label(
                "আজকের তারিখ",
                24f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            label(
                SimpleDateFormat(
                    "dd-MM-yyyy",
                    Locale.getDefault()
                ).format(Date()),
                19f,
                DARK
            )
        )

        root.addView(space(12))

        val records =
            readSerials(
                todayKey()
            )

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

        val stats1 =
            LinearLayout(this)

        stats1.orientation =
            LinearLayout.HORIZONTAL

        stats1.addView(
            statCard(
                "👥",
                "মোট সিরিয়াল",
                "${records.size} জন",
                BLUE
            )
        )

        stats1.addView(
            statCard(
                "⏳",
                "অপেক্ষমাণ",
                "$waiting জন",
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
                "$completed জন",
                GREEN
            )
        )

        stats2.addView(
            statCard(
                "✕",
                "বাতিল",
                "$cancelled জন",
                RED
            )
        )

        root.addView(stats2)

        root.addView(space(14))

        root.addView(
            label(
                "দ্রুত অ্যাকশন",
                25f,
                DARK_BLUE,
                true
            )
        )

        root.addView(space(5))

        // দুই পাশে দুইটি
        val row1 =
            LinearLayout(this)

        row1.orientation =
            LinearLayout.HORIZONTAL

        row1.addView(
            gridButton(
                "📋\nটোটাল সিরিয়াল",
                BLUE
            ) {

                showTotalSerial(
                    todayKey()
                )
            }
        )

        row1.addView(
            gridButton(
                "＋\nঅ্যাড সিরিয়াল",
                BLUE
            ) {

                showAddSerial()
            }
        )

        root.addView(row1)

        val row2 =
            LinearLayout(this)

        row2.orientation =
            LinearLayout.HORIZONTAL

        row2.addView(
            gridButton(
                "👨‍⚕️\nঅ্যাড ডাক্তার",
                BLUE
            ) {

                showManageDoctors()
            }
        )

        row2.addView(
            gridButton(
                "👨‍👩‍👧\nঅ্যাড কেয়ার অফ",
                BLUE
            ) {

                showManageCareOf()
            }
        )

        root.addView(row2)

        root.addView(space(14))

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
                14f,
                GRAY
            )
        )

        root.addView(space(12))

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
                14f,
                GRAY
            )
        )

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
                    23f,
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

            root.addView(
                actionButton(
                    "⚙   Admin Control Panel",
                    PURPLE
                ) {

                    showAdminPanel()
                }
            )
        }

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

        refreshBox.addView(
            label(
                "🔄  ডাটা প্রতি ২০ সেকেন্ড পর পর রিফ্রেশ হচ্ছে",
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

        refreshHandler.postDelayed(
            refreshRunnable,
            refreshIntervalMs
        )
    }

    // =========================================================
    // GRID BUTTON
    // =========================================================

    private fun gridButton(
        text: String,
        color: Int,
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

        b.gravity =
            Gravity.CENTER

        b.setPadding(
            5,
            5,
            5,
            5
        )

        b.elevation = 3f

        val params =
            LinearLayout.LayoutParams(
                0,
                82,
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
                112,
                1f
            )

        params.setMargins(
            4,
            4,
            4,
            4
        )

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
                16f,
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
    // ADD SERIAL
    // =========================================================

    private fun showAddSerial() {

        if (
            currentUsername.isEmpty()
        ) {

            toast(
                "আগে Login করুন"
            )

            return
        }

        // এই পেজে থাকলে Dashboard refresh হবে না
        dashboardVisible = false

        refreshHandler.removeCallbacks(
            refreshRunnable
        )

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

        // -----------------------------------------------------
        // PATIENT
        // -----------------------------------------------------

        card.addView(
            label(
                "রোগীর নাম",
                17f,
                DARK_BLUE,
                true
            )
        )

        val patient =
            input(
                "রোগীর নাম"
            )

        card.addView(patient)

        // -----------------------------------------------------
        // CARE OF
        // -----------------------------------------------------

        card.addView(
            label(
                "Care Of / অভিভাবকের নাম",
                17f,
                DARK_BLUE,
                true
            )
        )

        val careList =
            getCareOfList()

        val careSpinner =
            createSpinner(
                careList
            )

        card.addView(
            careSpinner
        )

        // -----------------------------------------------------
        // DOCTOR
        // -----------------------------------------------------

        card.addView(
            label(
                "ডাক্তার নির্বাচন করুন",
                17f,
                DARK_BLUE,
                true
            )
        )

        val doctorList =
            getDoctorList()

        val doctorSpinner =
            createSpinner(
                doctorList
            )

        card.addView(
            doctorSpinner
        )

        // -----------------------------------------------------
        // DATE
        // -----------------------------------------------------

        card.addView(
            label(
                "সিরিয়ালের তারিখ",
                17f,
                DARK_BLUE,
                true
            )
        )

        val selectedCalendar =
            Calendar.getInstance()

        val dateButton =
            TextView(this)

        dateButton.text =
            formatDisplayDate(
                selectedCalendar
            )

        dateButton.textSize =
            18f

        dateButton.setTextColor(
            DARK
        )

        dateButton.gravity =
            Gravity.CENTER

        dateButton.setTypeface(
            Typeface.DEFAULT,
            Typeface.BOLD
        )

        dateButton.background =
            background(
                WHITE,
                14f,
                TEAL
            )

        dateButton.setPadding(
            12,
            0,
            12,
            0
        )

        val dateParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                62
            )

        dateParams.setMargins(
            8,
            7,
            8,
            7
        )

        card.addView(
            dateButton,
            dateParams
        )

        dateButton.setOnClickListener {

            showDatePicker(
                selectedCalendar
            ) {

                dateButton.text =
                    formatDisplayDate(
                        selectedCalendar
                    )
            }
        }

        card.addView(space(8))

        // -----------------------------------------------------
        // CREATED BY
        // -----------------------------------------------------

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

        // -----------------------------------------------------
        // SAVE
        // -----------------------------------------------------

        card.addView(
            actionButton(
                "✅   সিরিয়াল তৈরি করুন",
                GREEN,
                68
            ) {

                val selectedCare =
                    careSpinner
                        .selectedItem
                        ?.toString()
                        ?: ""

                val selectedDoctor =
                    doctorSpinner
                        .selectedItem
                        ?.toString()
                        ?: ""

                val dateKey =
                    formatKeyDate(
                        selectedCalendar
                    )

                saveSerial(
                    patient.text
                        .toString()
                        .trim(),

                    selectedCare,

                    selectedDoctor,

                    dateKey,

                    formatDisplayDate(
                        selectedCalendar
                    )
                )
            }
        )

        root.addView(card)

        root.addView(space(14))

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
    // SPINNER
    // =========================================================

    private fun createSpinner(
        items: List<String>
    ): Spinner {

        val spinner =
            Spinner(this)

        val finalItems =
            if (items.isEmpty()) {

                listOf(
                    "নির্বাচন করুন"
                )

            } else {

                items
            }

        val adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                finalItems
            )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spinner.adapter =
            adapter

        spinner.background =
            background(
                WHITE,
                14f,
                TEAL
            )

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

        spinner.layoutParams =
            params

        return spinner
    }

    // =========================================================
    // DATE PICKER
    // =========================================================

    private fun showDatePicker(
        calendar: Calendar,
        onSelected: () -> Unit
    ) {

        val dialog =
            DatePickerDialog(
                this,

                { _, year, month, day ->

                    calendar.set(
                        Calendar.YEAR,
                        year
                    )

                    calendar.set(
                        Calendar.MONTH,
                        month
                    )

                    calendar.set(
                        Calendar.DAY_OF_MONTH,
                        day
                    )

                    onSelected()
                },

                calendar.get(
                    Calendar.YEAR
                ),

                calendar.get(
                    Calendar.MONTH
                ),

                calendar.get(
                    Calendar.DAY_OF_MONTH
                )
            )

        dialog.show()
    }

    // =========================================================
    // DATE FORMAT
    // =========================================================

    private fun formatKeyDate(
        calendar: Calendar
    ): String {

        return SimpleDateFormat(
            "yyyyMMdd",
            Locale.getDefault()
        ).format(
            calendar.time
        )
    }

    private fun formatDisplayDate(
        calendar: Calendar
    ): String {

        return SimpleDateFormat(
            "dd-MM-yyyy",
            Locale.getDefault()
        ).format(
            calendar.time
        )
    }

    private fun todayKey(): String {

        return SimpleDateFormat(
            "yyyyMMdd",
            Locale.getDefault()
        ).format(
            Date()
        )
    }

    // =========================================================
    // SAVE SERIAL
    // =========================================================

    private fun saveSerial(
        patient: String,
        careOf: String,
        doctor: String,
        dateKey: String,
        displayDate: String
    ) {

        if (patient.isEmpty()) {

            toast(
                "রোগীর নাম লিখুন"
            )

            return
        }

        if (
            careOf.isEmpty() ||
            careOf == "নির্বাচন করুন" ||
            careOf == "কেয়ার অফ নির্বাচন করুন"
        ) {

            toast(
                "Care Of নির্বাচন করুন"
            )

            return
        }

        if (
            doctor.isEmpty() ||
            doctor == "নির্বাচন করুন" ||
            doctor == "ডাক্তার নির্বাচন করুন"
        ) {

            toast(
                "ডাক্তার নির্বাচন করুন"
            )

            return
        }

        // নির্বাচিত তারিখের সিরিয়াল নম্বর
        val next =
            getNextSerialNumber(
                dateKey
            )

        val key =
            serialPrefix +
            dateKey +
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

                currentTime(),

                displayDate

            ).joinToString("||")

        pref.edit()
            .putString(
                key,
                value
            )
            .apply()

        toast(
            "সিরিয়াল #$next তৈরি হয়েছে\n$displayDate"
        )

        // Save করার পরে নির্বাচিত তারিখের serial দেখাবে
        showTotalSerial(
            dateKey
        )
    }

    // =========================================================
    // NEXT SERIAL NUMBER
    // =========================================================

    private fun getNextSerialNumber(
        dateKey: String
    ): Int {

        var next = 1

        for (
            key in pref.all.keys
        ) {

            if (
                key.startsWith(
                    serialPrefix +
                    dateKey +
                    "_"
                )
            ) {

                val number =
                    key.substringAfterLast(
                        "_"
                    ).toIntOrNull()
                        ?: 0

                if (
                    number >= next
                ) {

                    next =
                        number + 1
                }
            }
        }

        return next
    }

    // =========================================================
    // READ SERIALS
    // =========================================================

    private fun readSerials(
        dateKey: String
    ): List<SerialRecord> {

        val result =
            mutableListOf<SerialRecord>()

        for (
            key in pref.all.keys.sorted()
        ) {

            if (
                !key.startsWith(
                    serialPrefix +
                    dateKey +
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
                parts.size >= 8
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

                        parts[6],

                        parts[7]
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

    private fun showTotalSerial(
        dateKey: String
    ) {

        dashboardVisible = false

        refreshHandler.removeCallbacks(
            refreshRunnable
        )

        val root =
            verticalContainer()

        val records =
            readSerials(
                dateKey
            )

        val displayDate =
            if (
                dateKey == todayKey()
            ) {

                "আজকের সিরিয়াল"

            } else {

                try {

                    val d =
                        SimpleDateFormat(
                            "yyyyMMdd",
                            Locale.getDefault()
                        ).parse(
                            dateKey
                        )

                    SimpleDateFormat(
                        "dd-MM-yyyy",
                        Locale.getDefault()
                    ).format(
                        d ?: Date()
                    )

                } catch (
                    e: Exception
                ) {

                    dateKey
                }
            }

        root.addView(
            label(
                "📋 $displayDate",
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
                    "এই তারিখে কোনো সিরিয়াল তৈরি হয়নি",
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
                        "Care Of: ${r.careOf}",
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
                        "📅 তারিখ: ${r.serialDate}",
                        14f,
                        DARK_BLUE,
                        true
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
    // DOCTOR LIST
    // =========================================================

    private fun getDoctorList(): List<String> {

        val list =
            mutableListOf<String>()

        for (
            key in pref.all.keys.sorted()
        ) {

            if (
                key.startsWith(
                    "doctor_"
                )
            ) {

                val name =
                    pref.getString(
                        key,
                        ""
                    ) ?: ""

                if (
                    name.isNotEmpty()
                ) {

                    list.add(name)
                }
            }
        }

        return list
    }

    // =========================================================
    // CARE OF LIST
    // =========================================================

    private fun getCareOfList(): List<String> {

        val list =
            mutableListOf<String>()

        for (
            key in pref.all.keys.sorted()
        ) {

            if (
                key.startsWith(
                    "care_"
                )
            ) {

                val name =
                    pref.getString(
                        key,
                        ""
                    ) ?: ""

                if (
                    name.isNotEmpty()
                ) {

                    list.add(name)
                }
            }
        }

        return list
    }

    // =========================================================
    // MANAGE DOCTORS
    // =========================================================

    private fun showManageDoctors() {

        if (
            currentRole.equals(
                "User",
                true
            )
        ) {

            toast(
                "User ডাক্তার যোগ করতে পারবেন না"
            )

            return
        }

        showManageNames(
            "ডাক্তার পরিচালনা করুন",
            "doctor_",
            "ডাক্তারের নাম লিখুন"
        )
    }

    // =========================================================
    // MANAGE CARE OF
    // =========================================================

    private fun showManageCareOf() {

        if (
            currentRole.equals(
                "User",
                true
            )
        ) {

            toast(
                "User Care Of যোগ করতে পারবেন না"
            )

            return
        }

        showManageNames(
            "Care Of পরিচালনা করুন",
            "care_",
            "Care Of-এর নাম লিখুন"
        )
    }

    // =========================================================
    // MANAGE NAME SCREEN
    // =========================================================

    private fun showManageNames(
        title: String,
        prefix: String,
        hint: String
    ) {

        dashboardVisible = false

        refreshHandler.removeCallbacks(
            refreshRunnable
        )

        val root =
            verticalContainer()

        root.addView(
            label(
                title,
                26f,
                DARK_BLUE,
                true
            )
        )

        root.addView(space(12))

        val nameInput =
            input(hint)

        root.addView(
            nameInput
        )

        root.addView(
            actionButton(
                "＋   নতুন যোগ করুন",
                TEAL
            ) {

                val name =
                    nameInput.text
                        .toString()
                        .trim()

                if (
                    name.isEmpty()
                ) {

                    toast(
                        "নাম লিখুন"
                    )

                    return@actionButton
                }

                addNamedItem(
                    prefix,
                    name
                )

                toast(
                    "$name যোগ করা হয়েছে"
                )

                showManageNames(
                    title,
                    prefix,
                    hint
                )
            }
        )

        root.addView(space(12))

        root.addView(
            label(
                "বর্তমান তালিকা",
                21f,
                DARK_BLUE,
                true
            )
        )

        val items =
            if (
                prefix == "doctor_"
            ) {

                getDoctorList()

            } else {

                getCareOfList()
            }

        if (
            items.isEmpty()
        ) {

            root.addView(
                label(
                    "এখনো কোনো নাম যোগ করা হয়নি",
                    14f,
                    GRAY
                )
            )

        } else {

            items.forEach { name ->

                val row =
                    LinearLayout(this)

                row.orientation =
                    LinearLayout.HORIZONTAL

                row.gravity =
                    Gravity.CENTER_VERTICAL

                row.setPadding(
                    12,
                    8,
                    8,
                    8
                )

                row.background =
                    background(
                        WHITE,
                        14f,
                        LIGHT_BORDER
                    )

                val info =
                    label(
                        name,
                        16f,
                        DARK,
                        true
                    )

                row.addView(
                    info,
                    LinearLayout.LayoutParams(
                        0,
                        55,
                        1f
                    )
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

                    deleteNamedItem(
                        prefix,
                        name
                    )

                    showManageNames(
                        title,
                        prefix,
                        hint
                    )
                }

                row.addView(
                    delete,
                    LinearLayout.LayoutParams(
                        80,
                        42
                    )
                )

                val p =
                    LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )

                p.setMargins(
                    5,
                    4,
                    5,
                    4
                )

                root.addView(
                    row,
                    p
                )
            }
        }

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
    // ADD NAME
    // =========================================================

    private fun addNamedItem(
        prefix: String,
        name: String
    ) {

        val key =
            prefix +
            System.currentTimeMillis()

        pref.edit()
            .putString(
                key,
                name
            )
            .apply()
    }

    // =========================================================
    // DELETE NAME
    // =========================================================

    private fun deleteNamedItem(
        prefix: String,
        name: String
    ) {

        for (
            key in pref.all.keys
        ) {

            if (
                key.startsWith(
                    prefix
                )
            ) {

                val value =
                    pref.getString(
                        key,
                        ""
                    )

                if (
                    value == name
                ) {

                    pref.edit()
                        .remove(key)
                        .apply()

                    toast(
                        "$name মুছে ফেলা হয়েছে"
                    )

                    return
                }
            }
        }
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
                62
            )
        )

        root.addView(space(8))

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

        if (
            username.isEmpty()
        ) {

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

        var count = 0

        for (
            key in pref.all.keys
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

                    val info =
                        label(
                            "$username\nRole: $role",
                            14f,
                            DARK,
                            true
                        )

                    card.addView(
                        info,
                        LinearLayout.LayoutParams(
                            0,
                            58,
                            1f
                        )
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

                    val params =
                        LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )

                    params.setMargins(
                        5,
                        4,
                        5,
                        4
                    )

                    root.addView(
                        card,
                        params
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

        lastRefreshText?.text =
            "সর্বশেষ আপডেট: ${currentTime()}"

        /*
         * গুরুত্বপূর্ণ:
         *
         * এই refresh শুধুমাত্র Dashboard-এ।
         * Add Serial / Admin / Doctor / Care Of পেজে
         * ঢুকলে dashboardVisible = false করা হয়।
         *
         * তাই ২০ সেকেন্ড পরপর Add Serial পেজ আর
         * নিজে থেকে Dashboard-এ চলে যাবে না।
         */

        if (dashboardVisible) {

            showDashboard()
        }
    }

    private fun currentTime(): String {

        return SimpleDateFormat(
            "hh:mm:ss a",
            Locale.getDefault()
        ).format(
            Date()
        )
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

        refreshHandler.removeCallbacks(
            refreshRunnable
        )
    }

    // =========================================================
    // RESUME
    // =========================================================

    override fun onResume() {

        super.onResume()

        if (
            dashboardVisible &&
            currentUsername.isNotEmpty()
        ) {

            refreshDashboardData()

            refreshHandler.removeCallbacks(
                refreshRunnable
            )

            refreshHandler.postDelayed(
                refreshRunnable,
                refreshIntervalMs
            )
        }
    }

    // =========================================================
    // DESTROY
    // =========================================================

    override fun onDestroy() {

        refreshHandler.removeCallbacks(
            refreshRunnable
        )

        super.onDestroy()
    }

    // =========================================================
    // BACK BUTTON
    // =========================================================

    override fun onBackPressed() {

        if (
            currentUsername.isNotEmpty()
        ) {

            showDashboard()

        } else {

            super.onBackPressed()
        }
    }
}
