package com.moondiagnostic.app

import android.app.Activity
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

    private val BG = Color.rgb(241, 247, 252)
    private val BLUE = Color.rgb(25, 91, 145)
    private val DARK_BLUE = Color.rgb(18, 65, 108)
    private val TEAL = Color.rgb(14, 135, 125)
    private val GREEN = Color.rgb(35, 140, 88)
    private val RED = Color.rgb(195, 55, 55)
    private val ORANGE = Color.rgb(225, 137, 32)
    private val PURPLE = Color.rgb(105, 75, 165)
    private val WHITE = Color.WHITE
    private val DARK = Color.rgb(40, 45, 50)
    private val GRAY = Color.rgb(105, 112, 118)
    private val BORDER = Color.rgb(205, 220, 232)
    private val LIGHT_GREEN = Color.rgb(232, 247, 240)
    private val LIGHT_BLUE = Color.rgb(232, 243, 252)

    // =========================================================
    // STORAGE
    // =========================================================

    private lateinit var pref: android.content.SharedPreferences

    private val PREF = "MDC_DATABASE"

    private var currentUsername = ""
    private var currentRole = ""

    // =========================================================
    // REFRESH
    // =========================================================

    private val handler = Handler(Looper.getMainLooper())

    private val refreshInterval = 20_000L

    private var dashboardVisible = false

    private val refreshRunnable = object : Runnable {

        override fun run() {

            if (dashboardVisible && currentUsername.isNotEmpty()) {

                showDashboard()

                handler.postDelayed(
                    this,
                    refreshInterval
                )
            }
        }
    }

    // =========================================================
    // SERIAL MODEL
    // =========================================================

    private data class SerialRecord(

        val id: String,
        val date: String,
        val serial: Int,
        val doctorSerial: Int,
        val careSerial: Int,

        val patient: String,
        val careOf: String,
        val doctor: String,

        val status: String,

        val createdBy: String,
        val createdRole: String,

        val time: String
    )

    // =========================================================
    // ACTIVITY START
    // =========================================================

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        pref = getSharedPreferences(
            PREF,
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

            if (currentUsername.isNotEmpty()) {

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

    private fun text(
        value: String,
        size: Float,
        color: Int = DARK,
        bold: Boolean = false
    ): TextView {

        val t = TextView(this)

        t.text = value
        t.textSize = size
        t.setTextColor(color)

        t.gravity = Gravity.CENTER

        t.includeFontPadding = true

        t.setPadding(
            12,
            10,
            12,
            10
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
    // BACKGROUND
    // =========================================================

    private fun bg(
        color: Int,
        radius: Float = 18f,
        stroke: Int? = null
    ): GradientDrawable {

        val d = GradientDrawable()

        d.setColor(color)

        d.cornerRadius = radius

        if (stroke != null) {

            d.setStroke(
                2,
                stroke
            )
        }

        return d
    }

    // =========================================================
    // SPACE
    // =========================================================

    private fun space(
        h: Int
    ): Space {

        val s = Space(this)

        s.layoutParams =
            LinearLayout.LayoutParams(
                1,
                h
            )

        return s
    }

    // =========================================================
    // ROOT
    // =========================================================

    private fun rootLayout(): LinearLayout {

        val l = LinearLayout(this)

        l.orientation =
            LinearLayout.VERTICAL

        l.setPadding(
            14,
            18,
            14,
            30
        )

        l.setBackgroundColor(BG)

        return l
    }

    // =========================================================
    // SCROLL
    // =========================================================

    private fun scroll(
        view: View
    ): ScrollView {

        val s = ScrollView(this)

        s.isFillViewport = true

        s.setBackgroundColor(BG)

        s.addView(view)

        return s
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

        e.textSize = 19f

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
            bg(
                WHITE,
                16f,
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

        val p =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                70
            )

        p.setMargins(
            6,
            7,
            6,
            7
        )

        e.layoutParams = p

        return e
    }

    // =========================================================
    // BUTTON
    // =========================================================

    private fun button(
        title: String,
        color: Int = BLUE,
        height: Int = 66,
        action: () -> Unit
    ): TextView {

        val b =
            text(
                title,
                18f,
                WHITE,
                true
            )

        b.background =
            bg(
                color,
                16f
            )

        b.elevation = 5f

        val p =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                height
            )

        p.setMargins(
            6,
            6,
            6,
            6
        )

        b.layoutParams = p

        b.setOnClickListener {

            action()
        }

        return b
    }

    // =========================================================
    // BIG QUICK ACTION
    // =========================================================

    private fun quickAction(
        icon: String,
        title: String,
        color: Int,
        action: () -> Unit
    ): LinearLayout {

        val card = LinearLayout(this)

        card.orientation =
            LinearLayout.VERTICAL

        card.gravity =
            Gravity.CENTER

        card.setPadding(
            10,
            14,
            10,
            14
        )

        card.background =
            bg(
                WHITE,
                20f,
                BORDER
            )

        card.elevation = 5f

        val p =
            LinearLayout.LayoutParams(
                0,
                175,
                1f
            )

        p.setMargins(
            6,
            6,
            6,
            6
        )

        card.layoutParams = p

        card.addView(
            text(
                icon,
                45f,
                color,
                true
            )
        )

        card.addView(
            text(
                title,
                18f,
                DARK_BLUE,
                true
            )
        )

        card.setOnClickListener {

            action()
        }

        return card
    }

    // =========================================================
    // LOGIN
    // =========================================================

    private fun showLogin() {

        dashboardVisible = false

        handler.removeCallbacks(
            refreshRunnable
        )

        currentUsername = ""
        currentRole = ""

        val root = rootLayout()

        root.gravity =
            Gravity.CENTER_HORIZONTAL

        root.addView(
            space(25)
        )

        root.addView(
            text(
                "MDC",
                64f,
                BLUE,
                true
            )
        )

        root.addView(
            text(
                "মুন ডায়াগনস্টিক সেন্টার",
                31f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            text(
                "সঠিক নির্ণয়, সুস্থ জীবনের প্রত্যয়",
                16f,
                GRAY
            )
        )

        root.addView(
            space(20)
        )

        val card = LinearLayout(this)

        card.orientation =
            LinearLayout.VERTICAL

        card.setPadding(
            20,
            28,
            20,
            30
        )

        card.background =
            bg(
                WHITE,
                24f,
                BORDER
            )

        card.elevation = 8f

        root.addView(
            card,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        card.addView(
            text(
                "🔐",
                50f,
                BLUE,
                true
            )
        )

        card.addView(
            text(
                "লগইন করুন",
                31f,
                DARK_BLUE,
                true
            )
        )

        card.addView(
            space(14)
        )

        val username =
            input(
                "ইউজারনেম লিখুন"
            )

        val password =
            input(
                "পাসওয়ার্ড লিখুন",
                true
            )

        card.addView(username)

        card.addView(password)

        card.addView(
            space(12)
        )

        card.addView(
            button(
                "🔐   লগইন করুন",
                BLUE,
                72
            ) {

                login(
                    username.text.toString().trim(),
                    password.text.toString()
                )
            }
        )

        card.addView(
            space(12)
        )

        card.addView(
            text(
                "অনুমোদিত Admin / Operator / User-ই প্রবেশ করতে পারবেন",
                14f,
                GRAY
            )
        )

        root.addView(
            space(25)
        )

        root.addView(
            text(
                "মুন ডায়াগনস্টিক সেন্টার",
                17f,
                GRAY,
                true
            )
        )

        root.addView(
            text(
                "আপনার বিশ্বস্ত স্বাস্থ্যসেবা কেন্দ্র",
                14f,
                GRAY
            )
        )

        setContentView(
            scroll(root)
        )
    }

    // =========================================================
    // LOGIN FUNCTION
    // =========================================================

    private fun login(
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

        val savedUser =
            pref.getString(
                "user_$username",
                null
            )

        val savedPass =
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
            savedUser != null &&
            savedPass != null &&
            savedRole != null &&
            savedPass ==
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
                "লগইন সফল হয়েছে"
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

        handler.removeCallbacks(
            refreshRunnable
        )

        val root =
            rootLayout()

        root.addView(
            text(
                "MDC",
                58f,
                BLUE,
                true
            )
        )

        root.addView(
            text(
                "মুন ডায়াগনস্টিক সেন্টার",
                27f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            text(
                "স্বাগতম, $currentUsername",
                23f,
                DARK,
                true
            )
        )

        root.addView(
            text(
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
            button(
                "🚪   লগআউট",
                RED,
                68
            ) {

                logout()
            }
        )

        root.addView(
            space(10)
        )

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

        // =====================================================
        // STAT CARDS
        // =====================================================

        root.addView(
            text(
                "আজকের পরিসংখ্যান",
                23f,
                DARK_BLUE,
                true
            )
        )

        val row1 =
            LinearLayout(this)

        row1.orientation =
            LinearLayout.HORIZONTAL

        row1.addView(
            stat(
                "📋",
                "মোট সিরিয়াল",
                records.size.toString(),
                BLUE
            )
        )

        row1.addView(
            stat(
                "⏳",
                "অপেক্ষমাণ",
                waiting.toString(),
                ORANGE
            )
        )

        root.addView(row1)

        val row2 =
            LinearLayout(this)

        row2.orientation =
            LinearLayout.HORIZONTAL

        row2.addView(
            stat(
                "✅",
                "সম্পন্ন",
                completed.toString(),
                GREEN
            )
        )

        row2.addView(
            stat(
                "❌",
                "বাতিল",
                cancelled.toString(),
                RED
            )
        )

        root.addView(row2)

        root.addView(
            space(14)
        )

        // =====================================================
        // QUICK ACTION
        // =====================================================

        root.addView(
            text(
                "দ্রুত অ্যাকশন",
                26f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            text(
                "নিচের অপশন থেকে কাজ নির্বাচন করুন",
                15f,
                GRAY
            )
        )

        root.addView(
            space(6)
        )

        val actions1 =
            LinearLayout(this)

        actions1.orientation =
            LinearLayout.HORIZONTAL

        actions1.addView(
            quickAction(
                "📋",
                "টোটাল সিরিয়াল",
                BLUE
            ) {

                showTotalSerial()
            }
        )

        actions1.addView(
            quickAction(
                "➕",
                "অ্যাড সিরিয়াল",
                GREEN
            ) {

                showAddSerial()
            }
        )

        root.addView(actions1)

        val actions2 =
            LinearLayout(this)

        actions2.orientation =
            LinearLayout.HORIZONTAL

        actions2.addView(
            quickAction(
                "👨‍⚕️",
                "অ্যাড ডাক্তার",
                PURPLE
            ) {

                if (
                    currentRole.equals(
                        "Admin",
                        true
                    )
                ) {

                    showAddDoctor()

                } else {

                    toast(
                        "শুধুমাত্র Admin ডাক্তার যোগ করতে পারবেন"
                    )
                }
            }
        )

        actions2.addView(
            quickAction(
                "👤",
                "অ্যাড কেয়ার অফ",
                TEAL
            ) {

                showAddCare()
            }
        )

        root.addView(actions2)

        root.addView(
            space(12)
        )

        // =====================================================
        // VIEW BUTTONS
        // =====================================================

        root.addView(
            button(
                "👨‍⚕️   ডাক্তার ওয়াইজ সিরিয়াল দেখুন",
                PURPLE,
                72
            ) {

                showDoctorWise()
            }
        )

        root.addView(
            button(
                "👤   কেয়ার অফ ওয়াইজ সিরিয়াল দেখুন",
                TEAL,
                72
            ) {

                showCareWise()
            }
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

            root.addView(
                space(12)
            )

            root.addView(
                button(
                    "👑   Admin Control Panel",
                    PURPLE,
                    72
                ) {

                    showAdminPanel()
                }
            )
        }

        root.addView(
            space(15)
        )

        root.addView(
            text(
                "🔄 ড্যাশবোর্ড ডাটা প্রতি ২০ সেকেন্ডে আপডেট হবে",
                14f,
                TEAL,
                true
            )
        )

        root.addView(
            text(
                "সর্বশেষ আপডেট: ${
                    SimpleDateFormat(
                        "hh:mm:ss a",
                        Locale.getDefault()
                    ).format(Date())
                }",
                13f,
                GRAY
            )
        )

        root.addView(
            space(18)
        )

        root.addView(
            text(
                "মুন ডায়াগনস্টিক সেন্টার",
                17f,
                GRAY,
                true
            )
        )

        root.addView(
            text(
                "আপনার বিশ্বস্ত স্বাস্থ্যসেবা কেন্দ্র",
                14f,
                GRAY
            )
        )

        setContentView(
            scroll(root)
        )

        handler.postDelayed(
            refreshRunnable,
            refreshInterval
        )
    }

    // =========================================================
    // STAT CARD
    // =========================================================

    private fun stat(
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
            10,
            18,
            10,
            18
        )

        card.background =
            bg(
                WHITE,
                20f,
                BORDER
            )

        card.elevation = 5f

        val p =
            LinearLayout.LayoutParams(
                0,
                165,
                1f
            )

        p.setMargins(
            6,
            6,
            6,
            6
        )

        card.layoutParams = p

        card.addView(
            text(
                icon,
                42f,
                color,
                true
            )
        )

        card.addView(
            text(
                title,
                17f,
                DARK,
                true
            )
        )

        card.addView(
            text(
                value + " জন",
                22f,
                color,
                true
            )
        )

        return card
    }

    // =========================================================
    // ADD SERIAL
    // =========================================================

    private fun showAddSerial(
        editId: String? = null
    ) {

        dashboardVisible = false

        handler.removeCallbacks(
            refreshRunnable
        )

        val root =
            rootLayout()

        root.addView(
            text(
                if (editId == null)
                    "➕ নতুন সিরিয়াল"
                else
                    "✏️ সিরিয়াল এডিট",
                30f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            space(12)
        )

        val patient =
            input(
                "রোগীর নাম লিখুন"
            )

        val care =
            AutoCompleteTextView(this)

        care.hint =
            "Care Of নির্বাচন করুন / লিখুন"

        care.textSize = 19f

        care.setTextColor(DARK)

        care.setPadding(
            18,
            0,
            18,
            0
        )

        care.background =
            bg(
                WHITE,
                16f,
                TEAL
            )

        val careList =
            getCareList()

        care.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                careList
            )
        )

        care.threshold = 0

        val careParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                70
            )

        careParams.setMargins(
            6,
            7,
            6,
            7
        )

        care.layoutParams =
            careParams

        care.setOnClickListener {
            care.showDropDown()
        }

        val doctor =
            AutoCompleteTextView(this)

        doctor.hint =
            "ডাক্তার নির্বাচন করুন / লিখুন"

        doctor.textSize = 19f

        doctor.setTextColor(DARK)

        doctor.setPadding(
            18,
            0,
            18,
            0
        )

        doctor.background =
            bg(
                WHITE,
                16f,
                PURPLE
            )

        val doctors =
            getDoctorList()

        doctor.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                doctors
            )
        )

        doctor.threshold = 0

        val doctorParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                70
            )

        doctorParams.setMargins(
            6,
            7,
            6,
            7
        )

        doctor.layoutParams =
            doctorParams

        doctor.setOnClickListener {
            doctor.showDropDown()
        }

        // DATE

        val dateBox =
            EditText(this)

        dateBox.hint =
            "সিরিয়ালের তারিখ নির্বাচন করুন"

        dateBox.textSize = 18f

        dateBox.setTextColor(DARK)

        dateBox.setPadding(
            18,
            0,
            18,
            0
        )

        dateBox.isFocusable = false

        dateBox.background =
            bg(
                WHITE,
                16f,
                ORANGE
            )

        val dateParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                70
            )

        dateParams.setMargins(
            6,
            7,
            6,
            7
        )

        dateBox.layoutParams =
            dateParams

        var selectedDate =
            todayKey()

        dateBox.setText(
            displayDate(selectedDate)
        )

        dateBox.setOnClickListener {

            chooseDate(
                selectedDate
            ) {

                selectedDate = it

                dateBox.setText(
                    displayDate(it)
                )
            }
        }

        root.addView(
            text(
                "👤 রোগীর নাম",
                17f,
                DARK_BLUE,
                true
            )
        )

        root.addView(patient)

        root.addView(
            text(
                "👤 Care Of নাম",
                17f,
                DARK_BLUE,
                true
            )
        )

        root.addView(care)

        root.addView(
            text(
                "👨‍⚕️ ডাক্তার",
                17f,
                DARK_BLUE,
                true
            )
        )

        root.addView(doctor)

        root.addView(
            text(
                "📅 সিরিয়ালের তারিখ",
                17f,
                DARK_BLUE,
                true
            )
        )

        root.addView(dateBox)

        root.addView(
            space(10)
        )

        root.addView(
            text(
                "সিরিয়াল প্রদানকারী: $currentUsername ($currentRole)",
                15f,
                TEAL,
                true
            )
        )

        root.addView(
            space(10)
        )

        root.addView(
            button(
                if (editId == null)
                    "✅   সিরিয়াল তৈরি করুন"
                else
                    "💾   পরিবর্তন সংরক্ষণ করুন",
                GREEN,
                74
            ) {

                if (patient.text.toString().trim().isEmpty()) {

                    toast(
                        "রোগীর নাম লিখুন"
                    )

                    return@button
                }

                if (doctor.text.toString().trim().isEmpty()) {

                    toast(
                        "ডাক্তার নির্বাচন করুন"
                    )

                    return@button
                }

                if (editId == null) {

                    saveSerial(
                        selectedDate,
                        patient.text.toString().trim(),
                        care.text.toString().trim(),
                        doctor.text.toString().trim()
                    )

                } else {

                    updateSerial(
                        editId,
                        selectedDate,
                        patient.text.toString().trim(),
                        care.text.toString().trim(),
                        doctor.text.toString().trim()
                    )
                }
            }
        )

        root.addView(
            button(
                "←   Dashboard-এ ফিরে যান",
                BLUE,
                70
            ) {

                showDashboard()
            }
        )

        setContentView(
            scroll(root)
        )
    }

    // =========================================================
    // SAVE SERIAL
    // =========================================================

    private fun saveSerial(
        date: String,
        patient: String,
        care: String,
        doctor: String
    ) {

        val records =
            readSerials(date)

        val serial =
            records.maxOfOrNull {
                it.serial
            }?.plus(1) ?: 1

        val doctorSerial =
            records.filter {
                it.doctor.equals(
                    doctor,
                    true
                )
            }.maxOfOrNull {
                it.doctorSerial
            }?.plus(1) ?: 1

        val careSerial =
            if (care.isEmpty()) {

                0

            } else {

                records.filter {
                    it.careOf.equals(
                        care,
                        true
                    )
                }.maxOfOrNull {
                    it.careSerial
                }?.plus(1) ?: 1
            }

        val id =
            "serial_${System.currentTimeMillis()}"

        val record =
            listOf(
                date,
                serial,
                doctorSerial,
                careSerial,
                patient,
                care,
                doctor,
                "Waiting",
                currentUsername,
                currentRole,
                currentTime()
            ).joinToString("|||")

        pref.edit()
            .putString(
                id,
                record
            )
            .apply()

        toast(
            "সিরিয়াল #$serial তৈরি হয়েছে"
        )

        showTotalSerial(
            date
        )
    }

    // =========================================================
    // READ SERIAL
    // =========================================================

    private fun readSerials(
        date: String
    ): List<SerialRecord> {

        val result =
            mutableListOf<SerialRecord>()

        for (key in pref.all.keys) {

            if (!key.startsWith("serial_")) {
                continue
            }

            val raw =
                pref.getString(
                    key,
                    null
                ) ?: continue

            val p =
                raw.split("|||")

            if (p.size < 11) {
                continue
            }

            if (p[0] != date) {
                continue
            }

            result.add(
                SerialRecord(
                    key,
                    p[0],
                    p[1].toIntOrNull() ?: 0,
                    p[2].toIntOrNull() ?: 0,
                    p[3].toIntOrNull() ?: 0,
                    p[4],
                    p[5],
                    p[6],
                    p[7],
                    p[8],
                    p[9],
                    p[10]
                )
            )
        }

        return result.sortedBy {
            it.serial
        }
    }

    // =========================================================
    // TOTAL SERIAL
    // =========================================================

    private fun showTotalSerial(
        selected: String = todayKey()
    ) {

        dashboardVisible = false

        handler.removeCallbacks(
            refreshRunnable
        )

        val root =
            rootLayout()

        root.addView(
            text(
                "📋 মোট সিরিয়াল",
                30f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            text(
                "তারিখ নির্বাচন করে সেই দিনের সব সিরিয়াল দেখুন",
                15f,
                GRAY
            )
        )

        root.addView(
            space(10)
        )

        val date =
            EditText(this)

        date.isFocusable = false

        date.textSize = 19f

        date.gravity = Gravity.CENTER

        date.setText(
            displayDate(selected)
        )

        date.background =
            bg(
                WHITE,
                16f,
                ORANGE
            )

        val dp =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                70
            )

        dp.setMargins(
            6,
            6,
            6,
            10
        )

        root.addView(
            date,
            dp
        )

        date.setOnClickListener {

            chooseDate(
                selected
            ) {

                showTotalSerial(it)
            }
        }

        val records =
            readSerials(selected)

        root.addView(
            text(
                "মোট ${records.size} জন",
                22f,
                TEAL,
                true
            )
        )

        root.addView(
            space(8)
        )

        if (records.isEmpty()) {

            root.addView(
                text(
                    "এই তারিখে কোনো সিরিয়াল নেই",
                    18f,
                    GRAY
                )
            )

        } else {

            records.forEach {

                root.addView(
                    serialCard(it)
                )
            }
        }

        root.addView(
            space(12)
        )

        root.addView(
            button(
                "＋   নতুন সিরিয়াল",
                GREEN,
                72
            ) {

                showAddSerial()
            }
        )

        root.addView(
            button(
                "←   Dashboard",
                BLUE,
                72
            ) {

                showDashboard()
            }
        )

        setContentView(
            scroll(root)
        )
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
            16,
            16,
            16,
            16
        )

        card.background =
            bg(
                WHITE,
                20f,
                BORDER
            )

        card.elevation = 4f

        val p =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

        p.setMargins(
            5,
            7,
            5,
            7
        )

        card.layoutParams = p

        card.addView(
            text(
                "সিরিয়াল #${r.serial}",
                23f,
                BLUE,
                true
            )
        )

        card.addView(
            text(
                "👤 ${r.patient}",
                20f,
                DARK,
                true
            )
        )

        card.addView(
            text(
                "Care Of: ${
                    if (r.careOf.isEmpty())
                        "—"
                    else
                        r.careOf
                }",
                17f,
                DARK
            )
        )

        card.addView(
            text(
                "ডাক্তার: ${r.doctor}",
                17f,
                DARK
            )
        )

        card.addView(
            text(
                "ডাক্তার সিরিয়াল: #${r.doctorSerial}",
                16f,
                PURPLE,
                true
            )
        )

        if (r.careSerial > 0) {

            card.addView(
                text(
                    "Care Of সিরিয়াল: #${r.careSerial}",
                    16f,
                    TEAL,
                    true
                )
            )
        }

        card.addView(
            text(
                "✍ দিয়েছেন: ${r.createdBy} (${r.createdRole})",
                16f,
                TEAL,
                true
            )
        )

        card.addView(
            text(
                "সময়: ${r.time}",
                14f,
                GRAY
            )
        )

        card.addView(
            text(
                "স্ট্যাটাস: ${statusBangla(r.status)}",
                17f,
                statusColor(r.status),
                true
            )
        )

        // =====================================================
        // CREATOR EDIT DELETE
        // =====================================================

        if (
            r.createdBy.equals(
                currentUsername,
                true
            )
        ) {

            val row =
                LinearLayout(this)

            row.orientation =
                LinearLayout.HORIZONTAL

            val edit =
                smallButton(
                    "✏️ Edit",
                    BLUE
                ) {

                    editSerial(r)
                }

            val del =
                smallButton(
                    "🗑 Delete",
                    RED
                ) {

                    deleteSerial(r)
                }

            row.addView(edit)

            row.addView(del)

            card.addView(row)
        }

        // =====================================================
        // COMPLETE BUTTON
        // =====================================================

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

            val title =
                if (
                    r.status ==
                    "Completed"
                )
                    "↩ অসম্পন্ন করুন"
                else
                    "✅ সম্পন্ন করুন"

            card.addView(
                button(
                    title,
                    if (
                        r.status ==
                        "Completed"
                    )
                        ORANGE
                    else
                        GREEN,
                    62
                ) {

                    toggleComplete(r)
                }
            )
        }

        return card
    }

    // =========================================================
    // SMALL BUTTON
    // =========================================================

    private fun smallButton(
        title: String,
        color: Int,
        action: () -> Unit
    ): TextView {

        val b =
            text(
                title,
                15f,
                WHITE,
                true
            )

        b.background =
            bg(
                color,
                12f
            )

        val p =
            LinearLayout.LayoutParams(
                0,
                58,
                1f
            )

        p.setMargins(
            5,
            6,
            5,
            6
        )

        b.layoutParams = p

        b.setOnClickListener {
            action()
        }

        return b
    }

    // =========================================================
    // EDIT SERIAL
    // =========================================================

    private fun editSerial(
        r: SerialRecord
    ) {

        if (
            !r.createdBy.equals(
                currentUsername,
                true
            )
        ) {

            toast(
                "আপনি এই সিরিয়াল Edit করতে পারবেন না"
            )

            return
        }

        showAddSerial(
            r.id
        )
    }

    // =========================================================
    // UPDATE SERIAL
    // =========================================================

    private fun updateSerial(
        id: String,
        date: String,
        patient: String,
        care: String,
        doctor: String
    ) {

        val old =
            pref.getString(
                id,
                null
            ) ?: return

        val p =
            old.split("|||")

        if (p.size < 11) {
            return
        }

        if (
            !p[8].equals(
                currentUsername,
                true
            )
        ) {

            toast(
                "শুধুমাত্র যে User সিরিয়াল দিয়েছে সে Edit করতে পারবে"
            )

            return
        }

        val records =
            readSerials(date)

        val serial =
            if (p[0] == date) {

                p[1].toIntOrNull() ?: 1

            } else {

                records.maxOfOrNull {
                    it.serial
                }?.plus(1) ?: 1
            }

        val doctorSerial =
            records.filter {
                it.doctor.equals(
                    doctor,
                    true
                ) && it.id != id
            }.maxOfOrNull {
                it.doctorSerial
            }?.plus(1) ?: 1

        val careSerial =
            if (care.isEmpty()) {

                0

            } else {

                records.filter {
                    it.careOf.equals(
                        care,
                        true
                    ) && it.id != id
                }.maxOfOrNull {
                    it.careSerial
                }?.plus(1) ?: 1
            }

        val value =
            listOf(
                date,
                serial,
                doctorSerial,
                careSerial,
                patient,
                care,
                doctor,
                p[7],
                p[8],
                p[9],
                currentTime()
            ).joinToString("|||")

        pref.edit()
            .putString(
                id,
                value
            )
            .apply()

        toast(
            "সিরিয়াল আপডেট হয়েছে"
        )

        showTotalSerial(
            date
        )
    }

    // =========================================================
    // DELETE SERIAL
    // =========================================================

    private fun deleteSerial(
        r: SerialRecord
    ) {

        if (
            !r.createdBy.equals(
                currentUsername,
                true
            )
        ) {

            toast(
                "আপনি এই সিরিয়াল Delete করতে পারবেন না"
            )

            return
        }

        AlertDialog.Builder(this)
            .setTitle("সিরিয়াল Delete")
            .setMessage(
                "সিরিয়াল #${r.serial} Delete করতে চান?"
            )
            .setNegativeButton(
                "না",
                null
            )
            .setPositiveButton(
                "হ্যাঁ"
            ) { _, _ ->

                pref.edit()
                    .remove(r.id)
                    .apply()

                toast(
                    "সিরিয়াল Delete হয়েছে"
                )

                showTotalSerial(
                    r.date
                )
            }
            .show()
    }

    // =========================================================
    // COMPLETE
    // =========================================================

    private fun toggleComplete(
        r: SerialRecord
    ) {

        if (
            currentRole.equals(
                "User",
                true
            )
        ) {

            toast(
                "User সিরিয়াল সম্পন্ন করতে পারবেন না"
            )

            return
        }

        val old =
            pref.getString(
                r.id,
                null
            ) ?: return

        val p =
            old.split("|||")

        if (p.size < 11) {
            return
        }

        val newStatus =
            if (
                p[7] == "Completed"
            )
                "Waiting"
            else
                "Completed"

        val value =
            listOf(
                p[0],
                p[1],
                p[2],
                p[3],
                p[4],
                p[5],
                p[6],
                newStatus,
                p[8],
                p[9],
                p[10]
            ).joinToString("|||")

        pref.edit()
            .putString(
                r.id,
                value
            )
            .apply()

        showTotalSerial(
            r.date
        )
    }

    // =========================================================
    // DOCTOR LIST
    // =========================================================

    private fun getDoctorList(): List<String> {

        val result =
            mutableListOf<String>()

        for (key in pref.all.keys) {

            if (
                key.startsWith(
                    "doctor_"
                )
            ) {

                val d =
                    pref.getString(
                        key,
                        ""
                    ) ?: ""

                if (d.isNotEmpty()) {
                    result.add(d)
                }
            }
        }

        return result.distinct().sorted()
    }

    // =========================================================
    // CARE LIST
    // =========================================================

    private fun getCareList(): List<String> {

        val result =
            mutableListOf<String>()

        for (key in pref.all.keys) {

            if (
                key.startsWith(
                    "care_"
                )
            ) {

                val c =
                    pref.getString(
                        key,
                        ""
                    ) ?: ""

                if (c.isNotEmpty()) {
                    result.add(c)
                }
            }
        }

        return result.distinct().sorted()
    }

    // =========================================================
    // ADD DOCTOR
    // =========================================================

    private fun showAddDoctor() {

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
            rootLayout()

        root.addView(
            text(
                "👨‍⚕️ ডাক্তার যোগ করুন",
                30f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            space(15)
        )

        val doctor =
            input(
                "ডাক্তারের নাম লিখুন"
            )

        root.addView(
            doctor
        )

        root.addView(
            button(
                "➕   ডাক্তার যোগ করুন",
                PURPLE,
                72
            ) {

                val name =
                    doctor.text.toString().trim()

                if (name.isEmpty()) {

                    toast(
                        "ডাক্তারের নাম লিখুন"
                    )

                    return@button
                }

                if (
                    getDoctorList().any {
                        it.equals(
                            name,
                            true
                        )
                    }
                ) {

                    toast(
                        "এই ডাক্তার আগে থেকেই আছে"
                    )

                    return@button
                }

                pref.edit()
                    .putString(
                        "doctor_${System.currentTimeMillis()}",
                        name
                    )
                    .apply()

                toast(
                    "ডাক্তার যোগ হয়েছে"
                )

                showDashboard()
            }
        )

        root.addView(
            space(12)
        )

        root.addView(
            text(
                "বর্তমান ডাক্তার",
                22f,
                DARK_BLUE,
                true
            )
        )

        getDoctorList().forEach {

            root.addView(
                text(
                    "👨‍⚕️ $it",
                    18f,
                    DARK,
                    true
                )
            )
        }

        root.addView(
            space(12)
        )

        root.addView(
            button(
                "←   Dashboard",
                BLUE,
                70
            ) {

                showDashboard()
            }
        )

        setContentView(
            scroll(root)
        )
    }

    // =========================================================
    // ADD CARE
    // =========================================================

    private fun showAddCare() {

        val root =
            rootLayout()

        root.addView(
            text(
                "👤 Care Of যোগ করুন",
                30f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            text(
                "User / Operator / Admin সবাই Care Of যোগ করতে পারবেন",
                15f,
                GRAY
            )
        )

        root.addView(
            space(15)
        )

        val care =
            input(
                "Care Of নাম লিখুন"
            )

        root.addView(
            care
        )

        root.addView(
            button(
                "➕   Care Of যোগ করুন",
                TEAL,
                72
            ) {

                val name =
                    care.text.toString().trim()

                if (name.isEmpty()) {

                    toast(
                        "Care Of নাম লিখুন"
                    )

                    return@button
                }

                if (
                    getCareList().any {
                        it.equals(
                            name,
                            true
                        )
                    }
                ) {

                    toast(
                        "এই Care Of আগে থেকেই আছে"
                    )

                    return@button
                }

                pref.edit()
                    .putString(
                        "care_${System.currentTimeMillis()}",
                        name
                    )
                    .apply()

                toast(
                    "Care Of যোগ হয়েছে"
                )

                showDashboard()
            }
        )

        root.addView(
            space(15)
        )

        root.addView(
            text(
                "বর্তমান Care Of",
                22f,
                DARK_BLUE,
                true
            )
        )

        getCareList().forEach {

            root.addView(
                text(
                    "👤 $it",
                    18f,
                    DARK,
                    true
                )
            )
        }

        root.addView(
            space(15)
        )

        root.addView(
            button(
                "←   Dashboard",
                BLUE,
                70
            ) {

                showDashboard()
            }
        )

        setContentView(
            scroll(root)
        )
    }

    // =========================================================
    // DOCTOR WISE
    // =========================================================

    private fun showDoctorWise() {

        val root =
            rootLayout()

        root.addView(
            text(
                "👨‍⚕️ ডাক্তার ওয়াইজ সিরিয়াল",
                29f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            text(
                "সব ডাক্তার এবং তাদের আজকের সিরিয়াল",
                15f,
                GRAY
            )
        )

        root.addView(
            space(10)
        )

        val date =
            todayKey()

        val records =
            readSerials(date)

        val doctors =
            records.map {
                it.doctor
            }.distinct()

        if (doctors.isEmpty()) {

            root.addView(
                text(
                    "আজ কোনো ডাক্তার সিরিয়াল নেই",
                    18f,
                    GRAY
                )
            )

        } else {

            doctors.forEach { doctor ->

                root.addView(
                    text(
                        "👨‍⚕️ $doctor",
                        22f,
                        PURPLE,
                        true
                    )
                )

                records.filter {
                    it.doctor.equals(
                        doctor,
                        true
                    )
                }.forEach {

                    root.addView(
                        serialCard(it)
                    )
                }

                root.addView(
                    space(10)
                )
            }
        }

        root.addView(
            button(
                "←   Dashboard",
                BLUE,
                70
            ) {

                showDashboard()
            }
        )

        setContentView(
            scroll(root)
        )
    }

    // =========================================================
    // CARE WISE
    // =========================================================

    private fun showCareWise() {

        val root =
            rootLayout()

        root.addView(
            text(
                "👤 Care Of ওয়াইজ সিরিয়াল",
                29f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            text(
                "সব Care Of এবং তাদের আজকের সিরিয়াল",
                15f,
                GRAY
            )
        )

        root.addView(
            space(10)
        )

        val records =
            readSerials(
                todayKey()
            )

        val cares =
            records.map {
                it.careOf
            }.filter {
                it.isNotEmpty()
            }.distinct()

        if (cares.isEmpty()) {

            root.addView(
                text(
                    "আজ কোনো Care Of সিরিয়াল নেই",
                    18f,
                    GRAY
                )
            )

        } else {

            cares.forEach { care ->

                root.addView(
                    text(
                        "👤 $care",
                        22f,
                        TEAL,
                        true
                    )
                )

                records.filter {
                    it.careOf.equals(
                        care,
                        true
                    )
                }.forEach {

                    root.addView(
                        serialCard(it)
                    )
                }

                root.addView(
                    space(10)
                )
            }
        }

        root.addView(
            button(
                "←   Dashboard",
                BLUE,
                70
            ) {

                showDashboard()
            }
        )

        setContentView(
            scroll(root)
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
                "শুধুমাত্র Admin"
            )

            return
        }

        val root =
            rootLayout()

        root.addView(
            text(
                "👑 Admin Control Panel",
                30f,
                PURPLE,
                true
            )
        )

        root.addView(
            text(
                "User এবং Operator তৈরি ও পরিচালনা করুন",
                15f,
                GRAY
            )
        )

        root.addView(
            space(12)
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

        root.addView(username)

        root.addView(password)

        val spinner =
            Spinner(this)

        val roles =
            arrayOf(
                "User",
                "Operator"
            )

        spinner.adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                roles
            )

        spinner.layoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                65
            )

        root.addView(spinner)

        root.addView(
            button(
                "➕   User / Operator তৈরি করুন",
                PURPLE,
                72
            ) {

                createUser(
                    username.text.toString().trim(),
                    password.text.toString(),
                    spinner.selectedItem.toString()
                )
            }
        )

        root.addView(
            space(15)
        )

        root.addView(
            text(
                "বর্তমান User / Operator",
                23f,
                DARK_BLUE,
                true
            )
        )

        for (key in pref.all.keys) {

            if (
                key.startsWith(
                    "user_"
                )
            ) {

                val u =
                    pref.getString(
                        key,
                        ""
                    ) ?: ""

                if (
                    u.isNotEmpty() &&
                    !u.equals(
                        "admin",
                        true
                    )
                ) {

                    val role =
                        pref.getString(
                            "role_$u",
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
                        bg(
                            WHITE,
                            15f,
                            BORDER
                        )

                    val info =
                        text(
                            "$u\nRole: $role",
                            17f,
                            DARK,
                            true
                        )

                    row.addView(
                        info,
                        LinearLayout.LayoutParams(
                            0,
                            70,
                            1f
                        )
                    )

                    val del =
                        smallButton(
                            "মুছুন",
                            RED
                        ) {

                            pref.edit()
                                .remove(
                                    "user_$u"
                                )
                                .remove(
                                    "pass_$u"
                                )
                                .remove(
                                    "role_$u"
                                )
                                .apply()

                            showAdminPanel()
                        }

                    row.addView(
                        del
                    )

                    root.addView(
                        row
                    )
                }
            }
        }

        root.addView(
            space(15)
        )

        root.addView(
            button(
                "←   Dashboard",
                BLUE,
                70
            ) {

                showDashboard()
            }
        )

        setContentView(
            scroll(root)
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
                "Password কমপক্ষে ৪ অক্ষর"
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
            "$role তৈরি হয়েছে"
        )

        showAdminPanel()
    }

    // =========================================================
    // DATE PICKER
    // =========================================================

    private fun chooseDate(
        selected: String,
        callback: (String) -> Unit
    ) {

        val parts =
            selected.split("-")

        val year =
            parts.getOrNull(0)
                ?.toIntOrNull()
                ?: Calendar.getInstance()
                    .get(Calendar.YEAR)

        val month =
            (
                    parts.getOrNull(1)
                        ?.toIntOrNull()
                        ?: 1
                    ) - 1

        val day =
            parts.getOrNull(2)
                ?.toIntOrNull()
                ?: 1

        val picker =
            DatePickerDialog(
                this,
                { _, y, m, d ->

                    val value =
                        String.format(
                            Locale.getDefault(),
                            "%04d-%02d-%02d",
                            y,
                            m + 1,
                            d
                        )

                    callback(
                        value
                    )
                },
                year,
                month,
                day
            )

        picker.show()
    }

    // =========================================================
    // DATE
    // =========================================================

    private fun todayKey(): String {

        return SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        ).format(Date())
    }

    private fun displayDate(
        value: String
    ): String {

        return try {

            val input =
                SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
                )

            val output =
                SimpleDateFormat(
                    "dd-MM-yyyy",
                    Locale.getDefault()
                )

            output.format(
                input.parse(value)!!
            )

        } catch (e: Exception) {

            value
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

    private fun statusColor(
        status: String
    ): Int {

        return when (status) {

            "Completed" ->
                GREEN

            "Cancelled" ->
                RED

            else ->
                ORANGE
        }
    }

    // =========================================================
    // TIME
    // =========================================================

    private fun currentTime(): String {

        return SimpleDateFormat(
            "hh:mm:ss a",
            Locale.getDefault()
        ).format(Date())
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

        } catch (e: Exception) {

            password
        }
    }

    // =========================================================
    // LOGOUT
    // =========================================================

    private fun logout() {

        dashboardVisible = false

        handler.removeCallbacks(
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

        handler.removeCallbacks(
            refreshRunnable
        )
    }

    override fun onResume() {

        super.onResume()

        if (
            dashboardVisible &&
            currentUsername.isNotEmpty()
        ) {

            handler.removeCallbacks(
                refreshRunnable
            )

            handler.postDelayed(
                refreshRunnable,
                refreshInterval
            )
        }
    }

    override fun onDestroy() {

        handler.removeCallbacks(
            refreshRunnable
        )

        super.onDestroy()
    }

    // =========================================================
    // BACK
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
