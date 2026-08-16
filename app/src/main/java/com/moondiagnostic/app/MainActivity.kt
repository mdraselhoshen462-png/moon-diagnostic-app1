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
import java.util.UUID
import android.util.Base64

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
    // AUTO REFRESH
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
    // SERIAL DATA
    // =========================================================

    private data class SerialRecord(

        val id: String,

        val globalNumber: Int,

        val doctorNumber: Int,

        val date: String,

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
            14,
            16,
            14,
            30
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
    // NORMAL BUTTON
    // =========================================================

    private fun actionButton(
        text: String,
        color: Int = BLUE,
        height: Int = 64,
        onClick: () -> Unit
    ): TextView {

        val b = label(
            text,
            18f,
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

        b.elevation = 4f

        val params =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                height
            )

        params.setMargins(
            5,
            6,
            5,
            6
        )

        b.layoutParams = params

        b.setOnClickListener {

            onClick()
        }

        return b
    }

    // =========================================================
    // DASHBOARD GRID BUTTON
    // =========================================================

    private fun dashboardGridButton(
        icon: String,
        text: String,
        color: Int,
        onClick: () -> Unit
    ): TextView {

        val button =
            TextView(this)

        button.text =
            "$icon\n$text"

        button.textSize = 18f

        button.setTextColor(
            WHITE
        )

        button.gravity =
            Gravity.CENTER

        button.includeFontPadding =
            true

        button.setTypeface(
            Typeface.DEFAULT,
            Typeface.BOLD
        )

        button.background =
            background(
                color,
                16f
            )

        button.elevation = 5f

        button.setPadding(
            8,
            12,
            8,
            12
        )

        val params =
            LinearLayout.LayoutParams(
                0,
                125,
                1f
            )

        params.setMargins(
            5,
            5,
            5,
            5
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

        val e =
            EditText(this)

        e.hint = hint

        e.textSize = 18f

        e.setTextColor(
            DARK
        )

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
                18f,
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
            8,
            8,
            8,
            8
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

        root.setPadding(
            16,
            30,
            16,
            30
        )

        root.gravity =
            Gravity.CENTER_HORIZONTAL

        root.addView(
            space(30)
        )

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
                28f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            label(
                "সঠিক নির্ণয়, সুস্থ জীবনের প্রত্যয়",
                15f,
                GRAY
            )
        )

        root.addView(
            space(20)
        )

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

        card.elevation = 7f

        val cardParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

        cardParams.setMargins(
            5,
            0,
            5,
            0
        )

        root.addView(
            card,
            cardParams
        )

        card.addView(
            label(
                "লগইন করুন",
                30f,
                DARK_BLUE,
                true
            )
        )

        card.addView(
            space(14)
        )

        val username =
            input(
                "ইউজারনেম"
            )

        val password =
            input(
                "পাসওয়ার্ড",
                true
            )

        card.addView(
            username
        )

        card.addView(
            password
        )

        card.addView(
            space(10)
        )

        card.addView(
            actionButton(
                "🔐   লগইন",
                BLUE,
                68
            ) {

                loginUser(
                    username.text.toString()
                        .trim(),
                    password.text.toString()
                )
            }
        )

        root.addView(
            space(20)
        )

        root.addView(
            label(
                "অ্যাক্সেস শুধুমাত্র অনুমোদিত User / Operator / Admin-এর জন্য",
                13f,
                GRAY
            )
        )

        root.addView(
            space(16)
        )

        root.addView(
            label(
                "Moon Diagnostic Center",
                16f,
                GRAY,
                true
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

            toast(
                "Username লিখুন"
            )

            return
        }

        if (password.isEmpty()) {

            toast(
                "Password লিখুন"
            )

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
            10,
            20,
            10,
            35
        )

        // -----------------------------------------------------
        // HEADER
        // -----------------------------------------------------

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
                "স্বাগতম, $currentUsername",
                25f,
                DARK,
                true
            )
        )

        root.addView(
            label(
                "Role: $currentRole",
                17f,
                TEAL,
                true
            )
        )

        root.addView(
            space(8)
        )

        root.addView(
            actionButton(
                "🚪   Logout",
                RED,
                64
            ) {

                logout()
            }
        )

        root.addView(
            space(10)
        )

        // -----------------------------------------------------
        // DATE
        // -----------------------------------------------------

        root.addView(
            label(
                "আজকের তারিখ",
                30f,
                DARK_BLUE,
                true
            )
        )

        val todayDisplay =
            SimpleDateFormat(
                "dd-MM-yyyy",
                Locale.getDefault()
            ).format(
                Date()
            )

        root.addView(
            label(
                todayDisplay,
                22f,
                DARK
            )
        )

        root.addView(
            space(12)
        )

        // -----------------------------------------------------
        // STATISTICS
        // -----------------------------------------------------

        val today =
            dateKeyToday()

        val records =
            readSerialsForDate(
                today
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

        val row1 =
            LinearLayout(this)

        row1.orientation =
            LinearLayout.HORIZONTAL

        row1.addView(
            statCard(
                "👥",
                "মোট সিরিয়াল",
                "${records.size} জন",
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

        root.addView(
            row1
        )

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

        root.addView(
            row2
        )

        root.addView(
            space(15)
        )

        // -----------------------------------------------------
        // QUICK ACTION
        // -----------------------------------------------------

        root.addView(
            label(
                "দ্রুত অ্যাকশন",
                30f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            space(8)
        )

        val quickRow1 =
            LinearLayout(this)

        quickRow1.orientation =
            LinearLayout.HORIZONTAL

        quickRow1.addView(
            dashboardGridButton(
                "📋",
                "টোটাল সিরিয়াল",
                BLUE
            ) {

                showTotalSerial()
            }
        )

        quickRow1.addView(
            dashboardGridButton(
                "＋",
                "অ্যাড সিরিয়াল",
                BLUE
            ) {

                showAddSerial()
            }
        )

        root.addView(
            quickRow1
        )

        val quickRow2 =
            LinearLayout(this)

        quickRow2.orientation =
            LinearLayout.HORIZONTAL

        quickRow2.addView(
            dashboardGridButton(
                "👨‍⚕️",
                "অ্যাড ডাক্তার",
                BLUE
            ) {

                if (
                    currentRole.equals(
                        "Admin",
                        true
                    )
                ) {

                    showDoctorManager()

                } else {

                    toast(
                        "শুধুমাত্র Admin ডাক্তার যোগ করতে পারবেন"
                    )
                }
            }
        )

        quickRow2.addView(
            dashboardGridButton(
                "👨‍👩‍👧",
                "অ্যাড কেয়ার অফ",
                BLUE
            ) {

                showCareManager()
            }
        )

        root.addView(
            quickRow2
        )

        root.addView(
            space(18)
        )

        // -----------------------------------------------------
        // DOCTOR WISE
        // -----------------------------------------------------

        root.addView(
            label(
                "ডাক্তার ওয়াইজ সিরিয়াল",
                28f,
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

        root.addView(
            space(8)
        )

        root.addView(
            actionButton(
                "👨‍⚕️   ডাক্তার অনুযায়ী সিরিয়াল দেখুন",
                TEAL,
                65
            ) {

                showDoctorWise()
            }
        )

        root.addView(
            space(12)
        )

        // -----------------------------------------------------
        // CARE WISE
        // -----------------------------------------------------

        root.addView(
            label(
                "কেয়ার ওয়াইজ সিরিয়াল",
                28f,
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

        root.addView(
            space(8)
        )

        root.addView(
            actionButton(
                "👨‍👩‍👧   কেয়ার অফ অনুযায়ী দেখুন",
                TEAL,
                65
            ) {

                showCareWise()
            }
        )

        // -----------------------------------------------------
        // ADMIN
        // -----------------------------------------------------

        if (
            currentRole.equals(
                "Admin",
                true
            )
        ) {

            root.addView(
                space(20)
            )

            root.addView(
                label(
                    "👑 Admin Control Panel",
                    27f,
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

            root.addView(
                space(7)
            )

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

        root.addView(
            space(18)
        )

        // -----------------------------------------------------
        // REFRESH INFORMATION
        // -----------------------------------------------------

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
                16f,
                Color.rgb(
                    181,
                    224,
                    216
                )
            )

        refreshBox.addView(
            label(
                "🔄  ডাটা স্বয়ংক্রিয়ভাবে আপডেট হচ্ছে",
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

        root.addView(
            space(18)
        )

        root.addView(
            label(
                "মুন ডায়াগনস্টিক সেন্টার",
                17f,
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
            15,
            8,
            15
        )

        card.background =
            background(
                WHITE,
                18f,
                LIGHT_BORDER
            )

        card.elevation = 5f

        val params =
            LinearLayout.LayoutParams(
                0,
                155,
                1f
            )

        params.setMargins(
            5,
            5,
            5,
            5
        )

        card.layoutParams =
            params

        card.addView(
            label(
                icon,
                38f,
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
                20f,
                color,
                true
            )
        )

        return card
    }

    // =========================================================
    // REFRESH
    // =========================================================

    private fun currentTime(): String {

        return SimpleDateFormat(
            "hh:mm:ss a",
            Locale.getDefault()
        ).format(
            Date()
        )
    }

    private fun refreshDashboardData() {

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
         * এখানে showDashboard() করা হচ্ছে না।
         *
         * তাই ২০ সেকেন্ড পর পর পুরো Activity
         * rebuild হবে না।
         *
         * ফলে Add Serial form-এ থাকা অবস্থায়
         * user-এর input হারিয়ে যাবে না।
         */
    }

    // =========================================================
    // ADD SERIAL
    // =========================================================

    private fun showAddSerial(
        editId: String? = null
    ) {

        if (currentUsername.isEmpty()) {

            toast(
                "আগে Login করুন"
            )

            return
        }

        val editing =
            editId != null

        val existing =
            if (editing)
                getSerial(editId!!)
            else
                null

        if (
            editing &&
            existing == null
        ) {

            toast(
                "সিরিয়াল পাওয়া যায়নি"
            )

            return
        }

        if (
            editing &&
            existing!!.createdBy !=
            currentUsername
        ) {

            toast(
                "শুধুমাত্র যিনি সিরিয়াল দিয়েছেন তিনিই Edit করতে পারবেন"
            )

            return
        }

        val root =
            verticalContainer()

        root.setPadding(
            16,
            20,
            16,
            30
        )

        root.addView(
            label(
                if (editing)
                    "✏️ সিরিয়াল Edit করুন"
                else
                    "＋ নতুন সিরিয়াল",
                30f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            label(
                if (editing)
                    "সিরিয়ালের তথ্য পরিবর্তন করুন"
                else
                    "রোগীর তথ্য দিয়ে নতুন সিরিয়াল তৈরি করুন",
                15f,
                GRAY
            )
        )

        root.addView(
            space(15)
        )

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
                22f,
                LIGHT_BORDER
            )

        card.elevation = 7f

        // -----------------------------------------------------
        // PATIENT
        // -----------------------------------------------------

        card.addView(
            label(
                "রোগীর নাম",
                18f,
                DARK_BLUE,
                true
            )
        )

        val patient =
            input(
                "রোগীর নাম"
            )

        if (existing != null) {

            patient.setText(
                existing.patient
            )
        }

        card.addView(
            patient
        )

        // -----------------------------------------------------
        // CARE OF
        // -----------------------------------------------------

        card.addView(
            label(
                "Care Of",
                18f,
                DARK_BLUE,
                true
            )
        )

        val careOf =
            AutoCompleteTextView(
                this
            )

        careOf.hint =
            "Care Of / অভিভাবকের নাম লিখুন বা নির্বাচন করুন"

        careOf.textSize = 17f

        careOf.setTextColor(
            DARK
        )

        careOf.setHintTextColor(
            GRAY
        )

        careOf.setPadding(
            18,
            0,
            18,
            0
        )

        careOf.background =
            background(
                WHITE,
                18f,
                TEAL
            )

        val careParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                70
            )

        careParams.setMargins(
            8,
            8,
            8,
            8
        )

        careOf.layoutParams =
            careParams

        val careList =
            getCareList()

        val careAdapter =
            ArrayAdapter<String>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                careList
            )

        careOf.setAdapter(
            careAdapter
        )

        careOf.threshold = 0

        careOf.setOnClickListener {

            careOf.showDropDown()
        }

        if (existing != null) {

            careOf.setText(
                existing.careOf,
                false
            )
        }

        card.addView(
            careOf
        )

        // -----------------------------------------------------
        // DOCTOR
        // -----------------------------------------------------

        card.addView(
            label(
                "ডাক্তার",
                18f,
                DARK_BLUE,
                true
            )
        )

        val doctor =
            AutoCompleteTextView(
                this
            )

        doctor.hint =
            "ডাক্তারের নাম লিখুন বা নির্বাচন করুন"

        doctor.textSize = 17f

        doctor.setTextColor(
            DARK
        )

        doctor.setHintTextColor(
            GRAY
        )

        doctor.setPadding(
            18,
            0,
            18,
            0
        )

        doctor.background =
            background(
                WHITE,
                18f,
                TEAL
            )

        val doctorParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                70
            )

        doctorParams.setMargins(
            8,
            8,
            8,
            8
        )

        doctor.layoutParams =
            doctorParams

        val doctorList =
            getDoctorList()

        val doctorAdapter =
            ArrayAdapter<String>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                doctorList
            )

        doctor.setAdapter(
            doctorAdapter
        )

        doctor.threshold = 0

        doctor.setOnClickListener {

            doctor.showDropDown()
        }

        if (existing != null) {

            doctor.setText(
                existing.doctor,
                false
            )
        }

        card.addView(
            doctor
        )

        // -----------------------------------------------------
        // DATE
        // -----------------------------------------------------

        card.addView(
            label(
                "সিরিয়ালের তারিখ",
                18f,
                DARK_BLUE,
                true
            )
        )

        val dateBox =
            TextView(this)

        dateBox.textSize = 18f

        dateBox.setTextColor(
            DARK
        )

        dateBox.gravity =
            Gravity.CENTER_VERTICAL

        dateBox.setPadding(
            18,
            0,
            18,
            0
        )

        dateBox.background =
            background(
                WHITE,
                18f,
                TEAL
            )

        val initialDate =
            existing?.date
                ?: dateKeyToday()

        dateBox.text =
            displayDate(
                initialDate
            )

        val dateParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                70
            )

        dateParams.setMargins(
            8,
            8,
            8,
            8
        )

        dateBox.layoutParams =
            dateParams

        dateBox.setOnClickListener {

            chooseDate(
                initialDate,
                dateBox
            )
        }

        card.addView(
            dateBox
        )

        // -----------------------------------------------------
        // USER INFO
        // -----------------------------------------------------

        card.addView(
            space(8)
        )

        card.addView(
            label(
                "সিরিয়ালটি ${if (editing) "সম্পাদনা হচ্ছে" else "তৈরি হবে"} আপনার Login করা নামের অধীনে:",
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

        card.addView(
            space(10)
        )

        // -----------------------------------------------------
        // SAVE
        // -----------------------------------------------------

        card.addView(
            actionButton(
                if (editing)
                    "💾   পরিবর্তন সংরক্ষণ করুন"
                else
                    "✅   সিরিয়াল তৈরি করুন",
                GREEN,
                70
            ) {

                val selectedDate =
                    dateKeyFromDisplay(
                        dateBox.text.toString()
                    )

                if (editing) {

                    updateSerial(
                        existing!!,
                        patient.text.toString()
                            .trim(),
                        careOf.text.toString()
                            .trim(),
                        doctor.text.toString()
                            .trim(),
                        selectedDate
                    )

                } else {

                    saveSerial(
                        patient.text.toString()
                            .trim(),
                        careOf.text.toString()
                            .trim(),
                        doctor.text.toString()
                            .trim(),
                        selectedDate
                    )
                }
            }
        )

        root.addView(
            card
        )

        root.addView(
            space(15)
        )

        root.addView(
            actionButton(
                "←   Dashboard-এ ফিরে যান",
                BLUE,
                70
            ) {

                showDashboard()
            }
        )

        setContentView(
            scrollScreen(root)
        )
    }

    // =========================================================
    // CHOOSE DATE
    // =========================================================

    private fun chooseDate(
        currentDate: String,
        target: TextView
    ) {

        val calendar =
            Calendar.getInstance()

        try {

            val parsed =
                SimpleDateFormat(
                    "yyyyMMdd",
                    Locale.getDefault()
                ).parse(
                    currentDate
                )

            if (parsed != null) {

                calendar.time =
                    parsed
            }

        } catch (_: Exception) {
        }

        val dialog =
            DatePickerDialog(
                this,
                { _, year, month, day ->

                    val selected =
                        String.format(
                            Locale.getDefault(),
                            "%04d%02d%02d",
                            year,
                            month + 1,
                            day
                        )

                    target.text =
                        displayDate(
                            selected
                        )
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
    // SAVE SERIAL
    // =========================================================

    private fun saveSerial(
        patient: String,
        careOf: String,
        doctor: String,
        date: String
    ) {

        if (patient.isEmpty()) {

            toast(
                "রোগীর নাম লিখুন"
            )

            return
        }

        if (doctor.isEmpty()) {

            toast(
                "ডাক্তারের নাম লিখুন বা নির্বাচন করুন"
            )

            return
        }

        val globalNumber =
            nextGlobalNumber(
                date
            )

        val doctorNumber =
            nextDoctorNumber(
                date,
                doctor
            )

        val id =
            UUID.randomUUID()
                .toString()

        val record =
            SerialRecord(
                id = id,
                globalNumber = globalNumber,
                doctorNumber = doctorNumber,
                date = date,
                patient = patient,
                careOf = careOf,
                doctor = doctor,
                status = "Waiting",
                createdBy = currentUsername,
                createdRole = currentRole,
                createdAt = currentTime()
            )

        saveRecord(
            record
        )

        toast(
            "সিরিয়াল #$globalNumber তৈরি হয়েছে"
        )

        showTotalSerial(
            date
        )
    }

    // =========================================================
    // UPDATE SERIAL
    // =========================================================

    private fun updateSerial(
        old: SerialRecord,
        patient: String,
        careOf: String,
        doctor: String,
        date: String
    ) {

        if (
            old.createdBy !=
            currentUsername
        ) {

            toast(
                "এই সিরিয়াল Edit করার অনুমতি আপনার নেই"
            )

            return
        }

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

        val sameDate =
            old.date == date

        val sameDoctor =
            old.doctor.equals(
                doctor,
                true
            )

        val newGlobalNumber =
            if (
                sameDate
            ) {

                old.globalNumber

            } else {

                nextGlobalNumber(
                    date
                )
            }

        val newDoctorNumber =
            if (
                sameDate &&
                sameDoctor
            ) {

                old.doctorNumber

            } else {

                nextDoctorNumber(
                    date,
                    doctor
                )
            }

        val updated =
            old.copy(

                globalNumber =
                    newGlobalNumber,

                doctorNumber =
                    newDoctorNumber,

                date =
                    date,

                patient =
                    patient,

                careOf =
                    careOf,

                doctor =
                    doctor
            )

        saveRecord(
            updated
        )

        toast(
            "সিরিয়াল পরিবর্তন করা হয়েছে"
        )

        showTotalSerial(
            date
        )
    }

    // =========================================================
    // TOTAL SERIAL
    // =========================================================

    private fun showTotalSerial(
        selectedDate: String = dateKeyToday()
    ) {

        val root =
            verticalContainer()

        root.setPadding(
            12,
            18,
            12,
            30
        )

        root.addView(
            label(
                "📋 মোট সিরিয়াল",
                30f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            label(
                "তারিখ নির্বাচন করে ওই দিনের সিরিয়াল দেখুন",
                15f,
                GRAY
            )
        )

        root.addView(
            space(12)
        )

        // -----------------------------------------------------
        // DATE SELECTOR
        // -----------------------------------------------------

        val dateButton =
            TextView(this)

        dateButton.text =
            "📅   ${displayDate(selectedDate)}"

        dateButton.textSize = 19f

        dateButton.setTextColor(
            DARK_BLUE
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
                18f,
                TEAL
            )

        dateButton.elevation = 4f

        val dateParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                70
            )

        dateParams.setMargins(
            6,
            5,
            6,
            10
        )

        dateButton.layoutParams =
            dateParams

        dateButton.setOnClickListener {

            chooseDateForTotal(
                selectedDate
            )
        }

        root.addView(
            dateButton
        )

        val records =
            readSerialsForDate(
                selectedDate
            )

        root.addView(
            label(
                "মোট ${records.size} জন",
                20f,
                TEAL,
                true
            )
        )

        root.addView(
            space(8)
        )

        if (records.isEmpty()) {

            root.addView(
                label(
                    "এই তারিখে কোনো সিরিয়াল নেই",
                    17f,
                    GRAY
                )
            )

        } else {

            records.forEach { record ->

                root.addView(
                    serialCard(
                        record
                    )
                )
            }
        }

        root.addView(
            space(14)
        )

        root.addView(
            actionButton(
                "＋   নতুন সিরিয়াল",
                GREEN,
                70
            ) {

                showAddSerial()
            }
        )

        root.addView(
            actionButton(
                "←   Dashboard-এ ফিরে যান",
                BLUE,
                70
            ) {

                showDashboard()
            }
        )

        setContentView(
            scrollScreen(root)
        )
    }

    // =========================================================
    // TOTAL DATE
    // =========================================================

    private fun chooseDateForTotal(
        currentDate: String
    ) {

        val calendar =
            Calendar.getInstance()

        try {

            val parsed =
                SimpleDateFormat(
                    "yyyyMMdd",
                    Locale.getDefault()
                ).parse(
                    currentDate
                )

            if (parsed != null) {

                calendar.time =
                    parsed
            }

        } catch (_: Exception) {
        }

        DatePickerDialog(
            this,
            { _, year, month, day ->

                val selected =
                    String.format(
                        Locale.getDefault(),
                        "%04d%02d%02d",
                        year,
                        month + 1,
                        day
                    )

                showTotalSerial(
                    selected
                )
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
        ).show()
    }

    // =========================================================
    // SERIAL CARD
    // =========================================================

    private fun serialCard(
        r: SerialRecord
    ): LinearLayout {

        val card =
            LinearLayout(this)

        card.orientation =
            LinearLayout.VERTICAL

        card.setPadding(
            15,
            15,
            15,
            15
        )

        card.background =
            background(
                WHITE,
                18f,
                LIGHT_BORDER
            )

        card.elevation = 4f

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

        val statusColor =
            when (r.status) {

                "Completed" ->
                    GREEN

                "Cancelled" ->
                    RED

                else ->
                    ORANGE
            }

        card.addView(
            label(
                "সিরিয়াল #${r.globalNumber}   •   ${statusBangla(r.status)}",
                20f,
                statusColor,
                true
            )
        )

        card.addView(
            label(
                "ডাক্তার সিরিয়াল: #${r.doctorNumber}",
                15f,
                PURPLE,
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
                "সময়: ${r.createdAt}",
                13f,
                GRAY
            )
        )

        card.addView(
            space(8)
        )

        // -----------------------------------------------------
        // EDIT / DELETE
        // -----------------------------------------------------

        if (
            r.createdBy ==
            currentUsername
        ) {

            val editDeleteRow =
                LinearLayout(this)

            editDeleteRow.orientation =
                LinearLayout.HORIZONTAL

            editDeleteRow.addView(
                smallButton(
                    "✏ Edit",
                    BLUE
                ) {

                    showAddSerial(
                        r.id
                    )
                }
            )

            editDeleteRow.addView(
                smallButton(
                    "🗑 Delete",
                    RED
                ) {

                    confirmDeleteSerial(
                        r
                    )
                }
            )

            card.addView(
                editDeleteRow
            )
        }

        // -----------------------------------------------------
        // COMPLETE / WAITING
        // -----------------------------------------------------

        if (
            currentRole.equals(
                "Admin",
                true
            ) ||
            currentRole.equals(
                "Operator",
                true
            )
        ) {

            card.addView(
                space(7)
            )

            val statusRow =
                LinearLayout(this)

            statusRow.orientation =
                LinearLayout.HORIZONTAL

            if (
                r.status !=
                "Completed"
            ) {

                statusRow.addView(
                    smallButton(
                        "✓ সম্পন্ন",
                        GREEN
                    ) {

                        changeSerialStatus(
                            r,
                            "Completed"
                        )
                    }
                )
            }

            if (
                r.status ==
                "Completed"
            ) {

                statusRow.addView(
                    smallButton(
                        "↩ অসম্পন্ন",
                        ORANGE
                    ) {

                        changeSerialStatus(
                            r,
                            "Waiting"
                        )
                    }
                )
            }

            card.addView(
                statusRow
            )
        }

        return card
    }

    // =========================================================
    // SMALL BUTTON
    // =========================================================

    private fun smallButton(
        text: String,
        color: Int,
        onClick: () -> Unit
    ): TextView {

        val button =
            label(
                text,
                14f,
                WHITE,
                true
            )

        button.background =
            background(
                color,
                11f
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
                0,
                48,
                1f
            )

        params.setMargins(
            4,
            4,
            4,
            4
        )

        button.layoutParams =
            params

        button.setOnClickListener {

            onClick()
        }

        return button
    }

    // =========================================================
    // DELETE SERIAL
    // =========================================================

    private fun confirmDeleteSerial(
        record: SerialRecord
    ) {

        if (
            record.createdBy !=
            currentUsername
        ) {

            toast(
                "এই সিরিয়াল Delete করার অনুমতি আপনার নেই"
            )

            return
        }

        AlertDialog.Builder(this)

            .setTitle(
                "সিরিয়াল Delete"
            )

            .setMessage(
                "সিরিয়াল #${record.globalNumber} কি Delete করতে চান?"
            )

            .setNegativeButton(
                "না",
                null
            )

            .setPositiveButton(
                "হ্যাঁ, Delete",
            ) { _, _ ->

                pref.edit()
                    .remove(
                        serialKey(
                            record.id
                        )
                    )
                    .apply()

                toast(
                    "সিরিয়াল Delete হয়েছে"
                )

                showTotalSerial(
                    record.date
                )
            }

            .show()
    }

    // =========================================================
    // CHANGE STATUS
    // =========================================================

    private fun changeSerialStatus(
        record: SerialRecord,
        status: String
    ) {

        if (
            !currentRole.equals(
                "Admin",
                true
            ) &&
            !currentRole.equals(
                "Operator",
                true
            )
        ) {

            toast(
                "শুধুমাত্র Admin এবং Operator এটি করতে পারবেন"
            )

            return
        }

        saveRecord(
            record.copy(
                status = status
            )
        )

        toast(
            if (
                status ==
                "Completed"
            )
                "সিরিয়াল সম্পন্ন হয়েছে"
            else
                "সিরিয়াল আবার অপেক্ষমাণ"
        )

        showTotalSerial(
            record.date
        )
    }

    // =========================================================
    // DOCTOR MANAGER
    // =========================================================

    private fun showDoctorManager() {

        if (
            !currentRole.equals(
                "Admin",
                true
            )
        ) {

            toast(
                "শুধুমাত্র Admin ডাক্তার যোগ করতে পারবেন"
            )

            return
        }

        val root =
            verticalContainer()

        root.addView(
            label(
                "👨‍⚕️ ডাক্তার পরিচালনা",
                28f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            label(
                "শুধুমাত্র Admin ডাক্তার যোগ / Delete করতে পারবেন",
                14f,
                GRAY
            )
        )

        root.addView(
            space(15)
        )

        val doctorInput =
            input(
                "ডাক্তারের নাম"
            )

        root.addView(
            doctorInput
        )

        root.addView(
            actionButton(
                "＋   ডাক্তার যোগ করুন",
                TEAL,
                68
            ) {

                val name =
                    doctorInput.text
                        .toString()
                        .trim()

                if (name.isEmpty()) {

                    toast(
                        "ডাক্তারের নাম লিখুন"
                    )

                } else {

                    addDoctor(
                        name
                    )

                    doctorInput.text.clear()

                    showDoctorManager()
                }
            }
        )

        root.addView(
            space(15)
        )

        root.addView(
            label(
                "বর্তমান ডাক্তার",
                23f,
                DARK_BLUE,
                true
            )
        )

        getDoctorList()
            .forEach { doctor ->

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

                val name =
                    label(
                        doctor,
                        16f,
                        DARK,
                        true
                    )

                row.addView(
                    name,
                    LinearLayout.LayoutParams(
                        0,
                        55,
                        1f
                    )
                )

                val delete =
                    smallButton(
                        "মুছুন",
                        RED
                    ) {

                        deleteDoctor(
                            doctor
                        )
                    }

                row.addView(
                    delete
                )

                root.addView(
                    row
                )
            }

        root.addView(
            space(15)
        )

        root.addView(
            actionButton(
                "←   Dashboard",
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

    private fun addDoctor(
        doctor: String
    ) {

        val list =
            getDoctorList()
                .toMutableList()

        if (
            list.any {
                it.equals(
                    doctor,
                    true
                )
            }
        ) {

            toast(
                "এই ডাক্তার আগে থেকেই আছে"
            )

            return
        }

        list.add(
            doctor
        )

        saveStringList(
            "doctors",
            list
        )

        toast(
            "ডাক্তার যোগ হয়েছে"
        )
    }

    private fun deleteDoctor(
        doctor: String
    ) {

        if (
            !currentRole.equals(
                "Admin",
                true
            )
        ) {

            toast(
                "শুধুমাত্র Admin ডাক্তার Delete করতে পারবেন"
            )

            return
        }

        val list =
            getDoctorList()
                .toMutableList()

        list.removeAll {
            it.equals(
                doctor,
                true
            )
        }

        saveStringList(
            "doctors",
            list
        )

        toast(
            "ডাক্তার মুছে ফেলা হয়েছে"
        )

        showDoctorManager()
    }

    // =========================================================
    // CARE MANAGER
    // =========================================================

    private fun showCareManager() {

        val root =
            verticalContainer()

        root.addView(
            label(
                "👨‍👩‍👧 Care Of পরিচালনা",
                28f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            label(
                "User / Operator / Admin সবাই Care Of যোগ করতে পারবেন",
                14f,
                GRAY
            )
        )

        root.addView(
            space(15)
        )

        val careInput =
            input(
                "Care Of নাম"
            )

        root.addView(
            careInput
        )

        root.addView(
            actionButton(
                "＋   Care Of যোগ করুন",
                TEAL,
                68
            ) {

                val name =
                    careInput.text
                        .toString()
                        .trim()

                if (name.isEmpty()) {

                    toast(
                        "Care Of নাম লিখুন"
                    )

                } else {

                    addCare(
                        name
                    )

                    careInput.text.clear()

                    showCareManager()
                }
            }
        )

        root.addView(
            space(15)
        )

        root.addView(
            label(
                "বর্তমান Care Of",
                23f,
                DARK_BLUE,
                true
            )
        )

        getCareList()
            .forEach { care ->

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

                row.addView(
                    label(
                        care,
                        16f,
                        DARK,
                        true
                    ),
                    LinearLayout.LayoutParams(
                        0,
                        55,
                        1f
                    )
                )

                row.addView(
                    smallButton(
                        "মুছুন",
                        RED
                    ) {

                        deleteCare(
                            care
                        )
                    }
                )

                root.addView(
                    row
                )
            }

        root.addView(
            space(15)
        )

        root.addView(
            actionButton(
                "←   Dashboard",
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

    private fun addCare(
        care: String
    ) {

        val list =
            getCareList()
                .toMutableList()

        if (
            list.any {
                it.equals(
                    care,
                    true
                )
            }
        ) {

            toast(
                "এই Care Of আগে থেকেই আছে"
            )

            return
        }

        list.add(
            care
        )

        saveStringList(
            "care_list",
            list
        )

        toast(
            "Care Of যোগ হয়েছে"
        )
    }

    private fun deleteCare(
        care: String
    ) {

        val list =
            getCareList()
                .toMutableList()

        list.removeAll {
            it.equals(
                care,
                true
            )
        }

        saveStringList(
            "care_list",
            list
        )

        toast(
            "Care Of মুছে ফেলা হয়েছে"
        )

        showCareManager()
    }

    // =========================================================
    // DOCTOR WISE
    // =========================================================

    private fun showDoctorWise() {

        val root =
            verticalContainer()

        root.addView(
            label(
                "👨‍⚕️ ডাক্তার ওয়াইজ সিরিয়াল",
                28f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            space(10)
        )

        val spinner =
            Spinner(this)

        val doctors =
            getDoctorList()

        val items =
            if (doctors.isEmpty())
                arrayOf("কোনো ডাক্তার নেই")
            else
                doctors.toTypedArray()

        spinner.adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                items
            )

        root.addView(
            spinner,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                65
            )
        )

        root.addView(
            space(10)
        )

        root.addView(
            actionButton(
                "🔎   আজকের সিরিয়াল দেখুন",
                TEAL,
                68
            ) {

                if (
                    doctors.isNotEmpty()
                ) {

                    val doctor =
                        spinner.selectedItem
                            .toString()

                    showDoctorWiseList(
                        doctor,
                        dateKeyToday()
                    )
                }
            }
        )

        root.addView(
            space(10)
        )

        root.addView(
            actionButton(
                "←   Dashboard",
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

    private fun showDoctorWiseList(
        doctor: String,
        date: String
    ) {

        val root =
            verticalContainer()

        root.addView(
            label(
                "ডাক্তার: $doctor",
                26f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            label(
                displayDate(date),
                17f,
                TEAL,
                true
            )
        )

        val records =
            readSerialsForDate(
                date
            ).filter {
                it.doctor.equals(
                    doctor,
                    true
                )
            }

        root.addView(
            label(
                "মোট ${records.size} জন",
                18f,
                DARK,
                true
            )
        )

        records.forEach {

            root.addView(
                serialCard(it)
            )
        }

        root.addView(
            actionButton(
                "←   ফিরে যান",
                BLUE,
                68
            ) {

                showDoctorWise()
            }
        )

        setContentView(
            scrollScreen(root)
        )
    }

    // =========================================================
    // CARE WISE
    // =========================================================

    private fun showCareWise() {

        val root =
            verticalContainer()

        root.addView(
            label(
                "👨‍👩‍👧 Care Of ওয়াইজ সিরিয়াল",
                28f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            space(10)
        )

        val spinner =
            Spinner(this)

        val cares =
            getCareList()

        val items =
            if (cares.isEmpty())
                arrayOf("কোনো Care Of নেই")
            else
                cares.toTypedArray()

        spinner.adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                items
            )

        root.addView(
            spinner,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                65
            )
        )

        root.addView(
            space(10)
        )

        root.addView(
            actionButton(
                "🔎   আজকের সিরিয়াল দেখুন",
                TEAL,
                68
            ) {

                if (
                    cares.isNotEmpty()
                ) {

                    val care =
                        spinner.selectedItem
                            .toString()

                    showCareWiseList(
                        care,
                        dateKeyToday()
                    )
                }
            }
        )

        root.addView(
            actionButton(
                "←   Dashboard",
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

    private fun showCareWiseList(
        care: String,
        date: String
    ) {

        val root =
            verticalContainer()

        root.addView(
            label(
                "Care Of: $care",
                26f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            label(
                displayDate(date),
                17f,
                TEAL,
                true
            )
        )

        val records =
            readSerialsForDate(
                date
            ).filter {
                it.careOf.equals(
                    care,
                    true
                )
            }

        root.addView(
            label(
                "মোট ${records.size} জন",
                18f,
                DARK,
                true
            )
        )

        records.forEach {

            root.addView(
                serialCard(it)
            )
        }

        root.addView(
            actionButton(
                "←   ফিরে যান",
                BLUE,
                68
            ) {

                showCareWise()
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
                "শুধুমাত্র Admin এই পেজ ব্যবহার করতে পারবেন"
            )

            return
        }

        val root =
            verticalContainer()

        root.addView(
            label(
                "👑 Admin Control Panel",
                28f,
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

        root.addView(
            space(15)
        )

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

        val spinner =
            Spinner(this)

        val roles =
            arrayOf(
                "Operator",
                "User"
            )

        spinner.adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                roles
            )

        root.addView(
            spinner,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                65
            )
        )

        root.addView(
            space(8)
        )

        root.addView(
            actionButton(
                "＋   নতুন User / Operator তৈরি করুন",
                TEAL,
                68
            ) {

                createUser(
                    username.text
                        .toString()
                        .trim(),
                    password.text
                        .toString(),
                    spinner.selectedItem
                        .toString()
                )
            }
        )

        root.addView(
            space(18)
        )

        root.addView(
            label(
                "বর্তমান User / Operator",
                23f,
                DARK_BLUE,
                true
            )
        )

        showUserList(
            root
        )

        root.addView(
            space(15)
        )

        root.addView(
            actionButton(
                "←   Dashboard",
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

        pref.all.keys.forEach { key ->

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

                if (
                    username.isNotEmpty() &&
                    !username.equals(
                        "admin",
                        true
                    )
                ) {

                    val role =
                        pref.getString(
                            "role_$username",
                            ""
                        ) ?: ""

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
                            "$username\nRole: $role",
                            15f,
                            DARK,
                            true
                        )

                    row.addView(
                        info,
                        LinearLayout.LayoutParams(
                            0,
                            65,
                            1f
                        )
                    )

                    row.addView(
                        smallButton(
                            "মুছুন",
                            RED
                        ) {

                            deleteUser(
                                username
                            )
                        }
                    )

                    root.addView(
                        row
                    )

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
    // SERIAL STORAGE
    // =========================================================

    private fun serialKey(
        id: String
    ): String {

        return "serial_record_$id"
    }

    private fun saveRecord(
        record: SerialRecord
    ) {

        val encoded =
            listOf(
                record.globalNumber.toString(),
                record.doctorNumber.toString(),
                record.date,
                encode(record.patient),
                encode(record.careOf),
                encode(record.doctor),
                record.status,
                encode(record.createdBy),
                record.createdRole,
                encode(record.createdAt)
            ).joinToString("|")

        pref.edit()
            .putString(
                serialKey(
                    record.id
                ),
                encoded
            )
            .apply()
    }

    private fun getSerial(
        id: String
    ): SerialRecord? {

        val raw =
            pref.getString(
                serialKey(id),
                null
            ) ?: return null

        val parts =
            raw.split("|")

        if (parts.size < 10)
            return null

        return try {

            SerialRecord(

                id = id,

                globalNumber =
                    parts[0].toInt(),

                doctorNumber =
                    parts[1].toInt(),

                date =
                    parts[2],

                patient =
                    decode(parts[3]),

                careOf =
                    decode(parts[4]),

                doctor =
                    decode(parts[5]),

                status =
                    parts[6],

                createdBy =
                    decode(parts[7]),

                createdRole =
                    parts[8],

                createdAt =
                    decode(parts[9])
            )

        } catch (
            _: Exception
        ) {

            null
        }
    }

    private fun readAllSerials():
            List<SerialRecord> {

        val list =
            mutableListOf<SerialRecord>()

        pref.all.keys.forEach { key ->

            if (
                key.startsWith(
                    "serial_record_"
                )
            ) {

                val id =
                    key.removePrefix(
                        "serial_record_"
                    )

                val record =
                    getSerial(id)

                if (record != null) {

                    list.add(
                        record
                    )
                }
            }
        }

        return list
    }

    private fun readSerialsForDate(
        date: String
    ): List<SerialRecord> {

        return readAllSerials()
            .filter {
                it.date == date
            }
            .sortedBy {
                it.globalNumber
            }
    }

    // =========================================================
    // NUMBER GENERATION
    // =========================================================

    private fun nextGlobalNumber(
        date: String
    ): Int {

        val key =
            "counter_global_$date"

        val current =
            pref.getInt(
                key,
                0
            )

        val next =
            current + 1

        pref.edit()
            .putInt(
                key,
                next
            )
            .apply()

        return next
    }

    private fun doctorCounterKey(
        date: String,
        doctor: String
    ): String {

        return "counter_doctor_${date}_${hashPassword(
            doctor.lowercase(
                Locale.getDefault()
            )
        )}"
    }

    private fun nextDoctorNumber(
        date: String,
        doctor: String
    ): Int {

        val key =
            doctorCounterKey(
                date,
                doctor
            )

        val current =
            pref.getInt(
                key,
                0
            )

        val next =
            current + 1

        pref.edit()
            .putInt(
                key,
                next
            )
            .apply()

        return next
    }

    // =========================================================
    // DOCTORS / CARE LIST
    // =========================================================

    private fun getDoctorList():
            List<String> {

        return getStringList(
            "doctors"
        )
    }

    private fun getCareList():
            List<String> {

        return getStringList(
            "care_list"
        )
    }

    private fun getStringList(
        key: String
    ): List<String> {

        val raw =
            pref.getString(
                key,
                ""
            ) ?: ""

        if (raw.isEmpty())
            return emptyList()

        return raw
            .split("\n")
            .filter {
                it.isNotBlank()
            }
            .map {
                decode(it)
            }
    }

    private fun saveStringList(
        key: String,
        list: List<String>
    ) {

        val raw =
            list
                .map {
                    encode(it)
                }
                .joinToString("\n")

        pref.edit()
            .putString(
                key,
                raw
            )
            .apply()
    }

    // =========================================================
    // ENCODE / DECODE
    // =========================================================

    private fun encode(
        value: String
    ): String {

        return Base64.encodeToString(
            value.toByteArray(
                Charsets.UTF_8
            ),
            Base64.NO_WRAP
        )
    }

    private fun decode(
        value: String
    ): String {

        return try {

            String(
                Base64.decode(
                    value,
                    Base64.NO_WRAP
                ),
                Charsets.UTF_8
            )

        } catch (
            _: Exception
        ) {

            ""
        }
    }

    // =========================================================
    // DATE HELPERS
    // =========================================================

    private fun dateKeyToday():
            String {

        return SimpleDateFormat(
            "yyyyMMdd",
            Locale.getDefault()
        ).format(
            Date()
        )
    }

    private fun displayDate(
        date: String
    ): String {

        return try {

            val parsed =
                SimpleDateFormat(
                    "yyyyMMdd",
                    Locale.getDefault()
                ).parse(
                    date
                )

            if (parsed != null) {

                SimpleDateFormat(
                    "dd-MM-yyyy",
                    Locale.getDefault()
                ).format(
                    parsed
                )

            } else {

                date
            }

        } catch (
            _: Exception
        ) {

            date
        }
    }

    private fun dateKeyFromDisplay(
        display: String
    ): String {

        return try {

            val parsed =
                SimpleDateFormat(
                    "dd-MM-yyyy",
                    Locale.getDefault()
                ).parse(
                    display
                )

            if (parsed != null) {

                SimpleDateFormat(
                    "yyyyMMdd",
                    Locale.getDefault()
                ).format(
                    parsed
                )

            } else {

                dateKeyToday()
            }

        } catch (
            _: Exception
        ) {

            dateKeyToday()
        }
    }

    // =========================================================
    // STATUS
    // =========================================================

    private fun statusBangla(
        status: String
    ): String {

        return when (status) {

            "Completed" ->
                "সম্পন্ন"

            "Cancelled" ->
                "বাতিল"

            else ->
                "অপেক্ষমাণ"
        }
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

                "%02x".format(
                    it
                )
            }

        } catch (
            _: Exception
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
