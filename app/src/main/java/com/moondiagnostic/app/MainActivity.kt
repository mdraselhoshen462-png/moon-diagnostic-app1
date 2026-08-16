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
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : Activity() {

    // =========================================================
    // COLORS
    // =========================================================

    private val BG = Color.rgb(242, 248, 253)
    private val BLUE = Color.rgb(25, 91, 145)
    private val DARK_BLUE = Color.rgb(15, 65, 110)
    private val TEAL = Color.rgb(15, 135, 125)
    private val GREEN = Color.rgb(35, 140, 90)
    private val RED = Color.rgb(200, 55, 55)
    private val ORANGE = Color.rgb(225, 140, 35)
    private val PURPLE = Color.rgb(105, 75, 165)
    private val DARK = Color.rgb(40, 45, 50)
    private val GRAY = Color.rgb(105, 110, 115)
    private val LIGHT_BORDER = Color.rgb(200, 218, 232)
    private val WHITE = Color.WHITE

    // =========================================================
    // STORAGE
    // =========================================================

    private lateinit var pref: android.content.SharedPreferences

    private val PREF = "MDC_DATA"

    private var currentUsername = ""
    private var currentRole = ""

    private var currentScreen = "login"

    // =========================================================
    // AUTO REFRESH
    // =========================================================

    private val handler = Handler(Looper.getMainLooper())

    private val refreshTime = 20_000L

    private val refreshRunnable = object : Runnable {

        override fun run() {

            if (
                currentUsername.isNotEmpty() &&
                currentScreen == "dashboard"
            ) {

                showDashboard()

                handler.postDelayed(
                    this,
                    refreshTime
                )
            }
        }
    }

    // =========================================================
    // SERIAL MODEL
    // =========================================================

    data class SerialRecord(

        var id: String,

        var date: String,

        var serial: Int,

        var doctorSerial: Int,

        var careSerial: Int,

        var patient: String,

        var careOf: String,

        var doctor: String,

        var status: String,

        var createdBy: String,

        var createdRole: String,

        var createdTime: String
    )

    // =========================================================
    // ACTIVITY
    // =========================================================

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        pref = getSharedPreferences(
            PREF,
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
                currentUsername.isNotEmpty()
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

        if (
            !pref.contains("user_admin")
        ) {

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
            12,
            12,
            12
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
    // ROOT
    // =========================================================

    private fun rootLayout(): LinearLayout {

        val l = LinearLayout(this)

        l.orientation =
            LinearLayout.VERTICAL

        l.setPadding(
            12,
            18,
            12,
            35
        )

        l.setBackgroundColor(BG)

        return l
    }

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
    // SPACE
    // =========================================================

    private fun gap(
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

    private fun button(
        title: String,
        color: Int = BLUE,
        height: Int = 70,
        click: () -> Unit
    ): TextView {

        val b = text(
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

        b.elevation = 4f

        val p =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                height
            )

        p.setMargins(
            7,
            7,
            7,
            7
        )

        b.layoutParams = p

        b.setOnClickListener {

            click()
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

        e.textSize = 20f

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
            7,
            7,
            7,
            7
        )

        e.layoutParams = p

        return e
    }

    // =========================================================
    // LOGIN
    // =========================================================

    private fun showLogin() {

        currentScreen = "login"

        handler.removeCallbacks(
            refreshRunnable
        )

        val root =
            rootLayout()

        root.gravity =
            Gravity.CENTER_HORIZONTAL

        root.addView(
            gap(45)
        )

        root.addView(
            text(
                "MDC",
                65f,
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
            gap(25)
        )

        val card =
            LinearLayout(this)

        card.orientation =
            LinearLayout.VERTICAL

        card.setPadding(
            18,
            25,
            18,
            25
        )

        card.background =
            bg(
                WHITE,
                22f,
                LIGHT_BORDER
            )

        card.elevation = 7f

        root.addView(
            card,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        card.addView(
            text(
                "🔐  লগইন করুন",
                31f,
                DARK_BLUE,
                true
            )
        )

        card.addView(
            gap(15)
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
            gap(10)
        )

        card.addView(
            button(
                "🔐   LOGIN",
                BLUE,
                72
            ) {

                login(
                    username.text.toString()
                        .trim(),

                    password.text.toString()
                )
            }
        )

        root.addView(
            gap(22)
        )

        root.addView(
            text(
                "শুধুমাত্র Admin অনুমোদিত User / Operator / Admin লগইন করতে পারবেন",
                15f,
                GRAY,
                true
            )
        )

        root.addView(
            gap(20)
        )

        root.addView(
            text(
                "Moon Diagnostic Center",
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
            savedPass ==
            hashPassword(password) &&
            savedRole != null
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
                "সফলভাবে Login হয়েছে"
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

        currentScreen =
            "dashboard"

        handler.removeCallbacks(
            refreshRunnable
        )

        val root =
            rootLayout()

        root.addView(
            text(
                "MDC",
                60f,
                BLUE,
                true
            )
        )

        root.addView(
            text(
                "মুন ডায়াগনস্টিক সেন্টার",
                30f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            text(
                "স্বাগতম, $currentUsername",
                25f,
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
            gap(10)
        )

        root.addView(
            button(
                "🚪   LOGOUT",
                RED,
                68
            ) {

                logout()
            }
        )

        root.addView(
            gap(12)
        )

        val records =
            getSerials()

        val today =
            today()

        val todayRecords =
            records.filter {
                it.date == today
            }

        val waiting =
            todayRecords.count {
                it.status == "Waiting"
            }

        val completed =
            todayRecords.count {
                it.status == "Completed"
            }

        val cancelled =
            todayRecords.count {
                it.status == "Cancelled"
            }

        // =====================================================
        // STAT CARDS
        // =====================================================

        root.addView(
            text(
                "📊 আজকের পরিসংখ্যান",
                25f,
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
                "👥",
                "মোট সিরিয়াল",
                todayRecords.size.toString(),
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
                "✓",
                "সম্পন্ন",
                completed.toString(),
                GREEN
            )
        )

        row2.addView(
            stat(
                "✕",
                "বাতিল",
                cancelled.toString(),
                RED
            )
        )

        root.addView(row2)

        root.addView(
            gap(20)
        )

        // =====================================================
        // QUICK ACTION
        // =====================================================

        root.addView(
            text(
                "⚡ দ্রুত অ্যাকশন",
                27f,
                DARK_BLUE,
                true
            )
        )

        val q1 =
            LinearLayout(this)

        q1.orientation =
            LinearLayout.HORIZONTAL

        q1.addView(
            quick(
                "📋\nটোটাল সিরিয়াল"
            ) {

                showTotalSerial()
            }
        )

        q1.addView(
            quick(
                "➕\nঅ্যাড সিরিয়াল"
            ) {

                showAddSerial()
            }
        )

        root.addView(q1)

        val q2 =
            LinearLayout(this)

        q2.orientation =
            LinearLayout.HORIZONTAL

        q2.addView(
            quick(
                "👨‍⚕️\nঅ্যাড ডাক্তার"
            ) {

                if (
                    currentRole.equals(
                        "Admin",
                        true
                    )
                ) {

                    showDoctorPage()

                } else {

                    toast(
                        "শুধুমাত্র Admin ডাক্তার Add করতে পারবেন"
                    )
                }
            }
        )

        q2.addView(
            quick(
                "👤\nঅ্যাড কেয়ার অফ"
            ) {

                showCarePage()
            }
        )

        root.addView(q2)

        root.addView(
            gap(20)
        )

        // =====================================================
        // DOCTOR / CARE
        // =====================================================

        root.addView(
            button(
                "👨‍⚕️   ডাক্তার ওয়াইজ সিরিয়াল",
                PURPLE,
                72
            ) {

                showDoctorWise()
            }
        )

        root.addView(
            button(
                "👤   কেয়ার অফ ওয়াইজ সিরিয়াল",
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
                gap(10)
            )

            root.addView(
                button(
                    "👑   ADMIN CONTROL PANEL",
                    PURPLE,
                    72
                ) {

                    showAdminPanel()
                }
            )
        }

        root.addView(
            gap(25)
        )

        root.addView(
            text(
                "🔄 ড্যাশবোর্ড প্রতি ২০ সেকেন্ডে স্বয়ংক্রিয়ভাবে Refresh হবে",
                15f,
                TEAL,
                true
            )
        )

        root.addView(
            text(
                "অন্য পেজে কাজ করার সময় Auto Refresh পেজ পরিবর্তন করবে না",
                14f,
                GRAY
            )
        )

        root.addView(
            gap(20)
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
            refreshTime
        )
    }

    // =========================================================
    // STAT
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
            8,
            15,
            8,
            15
        )

        card.background =
            bg(
                WHITE,
                18f,
                LIGHT_BORDER
            )

        card.elevation = 4f

        val p =
            LinearLayout.LayoutParams(
                0,
                150,
                1f
            )

        p.setMargins(
            5,
            5,
            5,
            5
        )

        card.layoutParams = p

        card.addView(
            text(
                icon,
                35f,
                color,
                true
            )
        )

        card.addView(
            text(
                title,
                18f,
                DARK,
                true
            )
        )

        card.addView(
            text(
                value,
                25f,
                color,
                true
            )
        )

        return card
    }

    // =========================================================
    // QUICK ACTION
    // =========================================================

    private fun quick(
        value: String,
        click: () -> Unit
    ): TextView {

        val b =
            text(
                value,
                18f,
                DARK_BLUE,
                true
            )

        b.background =
            bg(
                WHITE,
                18f,
                LIGHT_BORDER
            )

        b.elevation = 5f

        val p =
            LinearLayout.LayoutParams(
                0,
                150,
                1f
            )

        p.setMargins(
            6,
            6,
            6,
            6
        )

        b.layoutParams = p

        b.setOnClickListener {
            click()
        }

        return b
    }

    // =========================================================
    // ADD SERIAL
    // =========================================================

    private fun showAddSerial(
        editId: String? = null
    ) {

        currentScreen =
            "add_serial"

        val editing =
            editId != null

        val old =
            if (editing)
                getSerials()
                    .find {
                        it.id == editId
                    }
            else null

        if (
            editing &&
            old != null &&
            old.createdBy != currentUsername
        ) {

            toast(
                "আপনি এই সিরিয়াল Edit করতে পারবেন না"
            )

            return
        }

        val root =
            rootLayout()

        root.addView(
            text(
                if (editing)
                    "✏️ সিরিয়াল Edit করুন"
                else
                    "➕ নতুন সিরিয়াল",
                29f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            text(
                "তারিখ, রোগী, Care Of এবং ডাক্তার নির্বাচন করুন",
                15f,
                GRAY
            )
        )

        root.addView(
            gap(12)
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
            bg(
                WHITE,
                20f,
                LIGHT_BORDER
            )

        card.elevation = 5f

        // DATE

        card.addView(
            text(
                "📅 সিরিয়ালের তারিখ",
                18f,
                DARK_BLUE,
                true
            )
        )

        val dateBox =
            EditText(this)

        dateBox.textSize = 19f

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
                TEAL
            )

        val selectedDate =
            arrayOf(
                old?.date ?: today()
            )

        dateBox.setText(
            selectedDate[0]
        )

        dateBox.setOnClickListener {

            showDatePicker(
                selectedDate[0]
            ) {

                selectedDate[0] =
                    it

                dateBox.setText(it)
            }
        }

        dateBox.layoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                70
            )

        card.addView(dateBox)

        card.addView(gap(10))

        // PATIENT

        card.addView(
            text(
                "👤 রোগীর নাম",
                18f,
                DARK_BLUE,
                true
            )
        )

        val patient =
            input(
                "রোগীর নাম লিখুন"
            )

        patient.setText(
            old?.patient ?: ""
        )

        card.addView(patient)

        // CARE

        card.addView(
            text(
                "👤 Care Of",
                18f,
                DARK_BLUE,
                true
            )
        )

        val careOf =
            autoComplete(
                "Care Of লিখুন বা নিচের Arrow থেকে নির্বাচন করুন",
                getCareList()
            )

        careOf.setText(
            old?.careOf ?: "",
            false
        )

        card.addView(careOf)

        // DOCTOR

        card.addView(
            text(
                "👨‍⚕️ ডাক্তার",
                18f,
                DARK_BLUE,
                true
            )
        )

        val doctor =
            autoComplete(
                "ডাক্তারের নাম লিখুন বা Arrow থেকে নির্বাচন করুন",
                getDoctorList()
            )

        doctor.setText(
            old?.doctor ?: "",
            false
        )

        card.addView(doctor)

        card.addView(
            gap(12)
        )

        card.addView(
            text(
                if (editing)
                    "সিরিয়াল দিয়েছেন: ${old?.createdBy}"
                else
                    "সিরিয়াল দেবেন: $currentUsername",
                16f,
                TEAL,
                true
            )
        )

        card.addView(
            gap(8)
        )

        card.addView(
            button(
                if (editing)
                    "💾   পরিবর্তন সংরক্ষণ"
                else
                    "✅   সিরিয়াল তৈরি করুন",
                GREEN,
                74
            ) {

                if (editing) {

                    updateSerial(
                        editId!!,
                        selectedDate[0],
                        patient.text.toString().trim(),
                        careOf.text.toString().trim(),
                        doctor.text.toString().trim()
                    )

                } else {

                    addSerial(
                        selectedDate[0],
                        patient.text.toString().trim(),
                        careOf.text.toString().trim(),
                        doctor.text.toString().trim()
                    )
                }
            }
        )

        root.addView(card)

        root.addView(
            gap(12)
        )

        root.addView(
            button(
                "←   Dashboard",
                BLUE
            ) {

                showDashboard()
            }
        )

        setContentView(
            scroll(root)
        )
    }

    // =========================================================
    // AUTO COMPLETE
    // =========================================================

    private fun autoComplete(
        hint: String,
        values: List<String>
    ): AutoCompleteTextView {

        val a =
            AutoCompleteTextView(this)

        a.hint = hint

        a.textSize = 17f

        a.setTextColor(DARK)

        a.setHintTextColor(
            Color.rgb(
                125,
                130,
                135
            )
        )

        a.setPadding(
            18,
            0,
            18,
            0
        )

        a.threshold = 0

        a.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                values
            )
        )

        a.setOnClickListener {

            a.showDropDown()
        }

        a.setOnFocusChangeListener { _, hasFocus ->

            if (hasFocus) {
                a.showDropDown()
            }
        }

        a.background =
            bg(
                WHITE,
                16f,
                TEAL
            )

        a.layoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                70
            )

        return a
    }

    // =========================================================
    // ADD SERIAL
    // =========================================================

    private fun addSerial(
        date: String,
        patient: String,
        care: String,
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
                "ডাক্তার নির্বাচন করুন"
            )

            return
        }

        if (care.isEmpty()) {

            toast(
                "Care Of নির্বাচন করুন"
            )

            return
        }

        val all =
            getSerials()
                .toMutableList()

        val overall =
            all.filter {
                it.date == date
            }.maxOfOrNull {
                it.serial
            }?.plus(1) ?: 1

        val doctorNo =
            all.filter {
                it.date == date &&
                it.doctor.equals(
                    doctor,
                    true
                )
            }.maxOfOrNull {
                it.doctorSerial
            }?.plus(1) ?: 1

        val careNo =
            all.filter {
                it.date == date &&
                it.careOf.equals(
                    care,
                    true
                )
            }.maxOfOrNull {
                it.careSerial
            }?.plus(1) ?: 1

        val record =
            SerialRecord(
                id = System.currentTimeMillis()
                    .toString(),

                date = date,

                serial = overall,

                doctorSerial = doctorNo,

                careSerial = careNo,

                patient = patient,

                careOf = care,

                doctor = doctor,

                status = "Waiting",

                createdBy =
                    currentUsername,

                createdRole =
                    currentRole,

                createdTime =
                    currentTime()
            )

        all.add(record)

        saveSerials(all)

        toast(
            "সিরিয়াল #$overall তৈরি হয়েছে"
        )

        showTotalSerial(date)
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

        val all =
            getSerials()
                .toMutableList()

        val index =
            all.indexOfFirst {
                it.id == id
            }

        if (index < 0) {

            toast(
                "সিরিয়াল পাওয়া যায়নি"
            )

            return
        }

        val old =
            all[index]

        if (
            old.createdBy !=
            currentUsername
        ) {

            toast(
                "এই সিরিয়াল আপনি Edit করতে পারবেন না"
            )

            return
        }

        if (
            patient.isEmpty() ||
            care.isEmpty() ||
            doctor.isEmpty()
        ) {

            toast(
                "সব তথ্য পূরণ করুন"
            )

            return
        }

        val doctorNo =
            all.filter {
                it.id != id &&
                it.date == date &&
                it.doctor.equals(
                    doctor,
                    true
                )
            }.maxOfOrNull {
                it.doctorSerial
            }?.plus(1) ?: old.doctorSerial

        val careNo =
            all.filter {
                it.id != id &&
                it.date == date &&
                it.careOf.equals(
                    care,
                    true
                )
            }.maxOfOrNull {
                it.careSerial
            }?.plus(1) ?: old.careSerial

        old.date = date
        old.patient = patient
        old.careOf = care
        old.doctor = doctor
        old.doctorSerial = doctorNo
        old.careSerial = careNo

        saveSerials(all)

        toast(
            "সিরিয়াল পরিবর্তন হয়েছে"
        )

        showTotalSerial(date)
    }

    // =========================================================
    // TOTAL SERIAL
    // =========================================================

    private fun showTotalSerial(
        selectedDate: String = today()
    ) {

        currentScreen =
            "total"

        val root =
            rootLayout()

        root.addView(
            text(
                "📋 টোটাল সিরিয়াল",
                30f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            gap(10)
        )

        val dateButton =
            button(
                "📅   তারিখ: $selectedDate",
                BLUE,
                70
            ) {

                showDatePicker(
                    selectedDate
                ) {

                    showTotalSerial(it)
                }
            }

        root.addView(
            dateButton
        )

        val records =
            getSerials()
                .filter {
                    it.date == selectedDate
                }
                .sortedBy {
                    it.serial
                }

        root.addView(
            text(
                "মোট ${records.size} জন",
                21f,
                TEAL,
                true
            )
        )

        root.addView(
            gap(8)
        )

        if (records.isEmpty()) {

            root.addView(
                text(
                    "এই তারিখে কোনো সিরিয়াল নেই",
                    18f,
                    GRAY,
                    true
                )
            )

        } else {

            records.forEach {

                addSerialCard(
                    root,
                    it
                )
            }
        }

        root.addView(
            gap(15)
        )

        root.addView(
            button(
                "➕   নতুন সিরিয়াল",
                GREEN
            ) {

                showAddSerial()
            }
        )

        root.addView(
            button(
                "←   Dashboard",
                BLUE
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

    private fun addSerialCard(
        root: LinearLayout,
        r: SerialRecord
    ) {

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
            bg(
                WHITE,
                18f,
                LIGHT_BORDER
            )

        card.elevation = 3f

        val p =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

        p.setMargins(
            5,
            6,
            5,
            6
        )

        card.layoutParams = p

        card.addView(
            text(
                "সিরিয়াল #${r.serial}   •   ${statusBangla(r.status)}",
                21f,
                BLUE,
                true
            )
        )

        card.addView(
            text(
                "👤 রোগী: ${r.patient}",
                19f,
                DARK,
                true
            )
        )

        card.addView(
            text(
                "👤 Care Of: ${r.careOf}",
                17f,
                DARK
            )
        )

        card.addView(
            text(
                "👨‍⚕️ ডাক্তার: ${r.doctor}",
                17f,
                DARK
            )
        )

        card.addView(
            text(
                "ডাক্তার সিরিয়াল: #${r.doctorSerial}    |    Care সিরিয়াল: #${r.careSerial}",
                16f,
                PURPLE,
                true
            )
        )

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
                "সময়: ${r.createdTime}",
                14f,
                GRAY
            )
        )

        // =====================================================
        // EDIT / DELETE
        // =====================================================

        if (
            r.createdBy ==
            currentUsername
        ) {

            card.addView(
                button(
                    "✏️   EDIT",
                    BLUE,
                    58
                ) {

                    showAddSerial(
                        r.id
                    )
                }
            )

            card.addView(
                button(
                    "🗑   DELETE",
                    RED,
                    58
                ) {

                    confirmDelete(
                        r
                    )
                }
            )
        }

        // =====================================================
        // COMPLETE
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

            if (
                r.status !=
                "Completed"
            ) {

                card.addView(
                    button(
                        "✅   সম্পন্ন করুন",
                        GREEN,
                        60
                    ) {

                        setStatus(
                            r.id,
                            "Completed"
                        )
                    }
                )

            } else {

                card.addView(
                    button(
                        "↩   অসম্পন্ন করুন",
                        ORANGE,
                        60
                    ) {

                        setStatus(
                            r.id,
                            "Waiting"
                        )
                    }
                )
            }
        }

        root.addView(
            card
        )
    }

    // =========================================================
    // STATUS
    // =========================================================

    private fun setStatus(
        id: String,
        status: String
    ) {

        val all =
            getSerials()
                .toMutableList()

        val item =
            all.find {
                it.id == id
            }

        if (item == null) {

            toast(
                "সিরিয়াল পাওয়া যায়নি"
            )

            return
        }

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
                "আপনার এই অনুমতি নেই"
            )

            return
        }

        item.status =
            status

        saveSerials(all)

        toast(
            if (
                status ==
                "Completed"
            )
                "সিরিয়াল সম্পন্ন হয়েছে"
            else
                "সিরিয়াল আবার অসম্পন্ন হয়েছে"
        )

        showTotalSerial(
            item.date
        )
    }

    // =========================================================
    // DELETE
    // =========================================================

    private fun confirmDelete(
        r: SerialRecord
    ) {

        if (
            r.createdBy !=
            currentUsername
        ) {

            toast(
                "আপনি এই সিরিয়াল Delete করতে পারবেন না"
            )

            return
        }

        AlertDialogBuilder(
            "সিরিয়াল #${r.serial} Delete করবেন?",
            "এই কাজটি ফিরিয়ে নেওয়া যাবে না।"
        ) {

            val all =
                getSerials()
                    .filter {
                        it.id != r.id
                    }

            saveSerials(all)

            toast(
                "সিরিয়াল Delete হয়েছে"
            )

            showTotalSerial(
                r.date
            )
        }
    }

    // =========================================================
    // SIMPLE CONFIRM DIALOG
    // =========================================================

    private fun AlertDialogBuilder(
        title: String,
        message: String,
        yes: () -> Unit
    ) {

        android.app.AlertDialog.Builder(
            this
        )
            .setTitle(title)
            .setMessage(message)
            .setNegativeButton(
                "না",
                null
            )
            .setPositiveButton(
                "হ্যাঁ",
            ) { _, _ ->

                yes()
            }
            .show()
    }

    // =========================================================
    // DOCTOR WISE
    // =========================================================

    private fun showDoctorWise(
        selectedDate: String = today()
    ) {

        currentScreen =
            "doctor_wise"

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
            button(
                "📅   তারিখ: $selectedDate",
                BLUE,
                70
            ) {

                showDatePicker(
                    selectedDate
                ) {

                    showDoctorWise(it)
                }
            }
        )

        val records =
            getSerials()
                .filter {
                    it.date ==
                    selectedDate
                }

        val doctors =
            records.map {
                it.doctor
            }.distinct()

        if (doctors.isEmpty()) {

            root.addView(
                text(
                    "এই দিনে কোনো ডাক্তার সিরিয়াল নেই",
                    18f,
                    GRAY,
                    true
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

                records
                    .filter {
                        it.doctor ==
                        doctor
                    }
                    .sortedBy {
                        it.doctorSerial
                    }
                    .forEach {

                        root.addView(
                            text(
                                "ডাক্তার সিরিয়াল #${it.doctorSerial}  |  রোগী: ${it.patient}  |  Care: ${it.careOf}  |  ${statusBangla(it.status)}",
                                17f,
                                DARK,
                                true
                            )
                        )
                    }

                root.addView(
                    gap(10)
                )
            }
        }

        root.addView(
            button(
                "←   Dashboard",
                BLUE
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

    private fun showCareWise(
        selectedDate: String = today()
    ) {

        currentScreen =
            "care_wise"

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
            button(
                "📅   তারিখ: $selectedDate",
                BLUE,
                70
            ) {

                showDatePicker(
                    selectedDate
                ) {

                    showCareWise(it)
                }
            }
        )

        val records =
            getSerials()
                .filter {
                    it.date ==
                    selectedDate
                }

        val cares =
            records.map {
                it.careOf
            }.distinct()

        if (cares.isEmpty()) {

            root.addView(
                text(
                    "এই দিনে কোনো Care Of সিরিয়াল নেই",
                    18f,
                    GRAY,
                    true
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

                records
                    .filter {
                        it.careOf ==
                        care
                    }
                    .sortedBy {
                        it.careSerial
                    }
                    .forEach {

                        root.addView(
                            text(
                                "Care সিরিয়াল #${it.careSerial}  |  রোগী: ${it.patient}  |  ডাক্তার: ${it.doctor}  |  ${statusBangla(it.status)}",
                                17f,
                                DARK,
                                true
                            )
                        )
                    }

                root.addView(
                    gap(10)
                )
            }
        }

        root.addView(
            button(
                "←   Dashboard",
                BLUE
            ) {

                showDashboard()
            }
        )

        setContentView(
            scroll(root)
        )
    }

    // =========================================================
    // DOCTOR PAGE
    // =========================================================

    private fun showDoctorPage() {

        if (
            !currentRole.equals(
                "Admin",
                true
            )
        ) {

            toast(
                "শুধুমাত্র Admin ডাক্তার Add করতে পারবেন"
            )

            return
        }

        currentScreen =
            "doctor"

        val root =
            rootLayout()

        root.addView(
            text(
                "👨‍⚕️ ডাক্তার পরিচালনা",
                29f,
                DARK_BLUE,
                true
            )
        )

        val doctor =
            input(
                "ডাক্তারের নাম"
            )

        root.addView(
            doctor
        )

        root.addView(
            button(
                "➕   ডাক্তার Add করুন",
                GREEN,
                70
            ) {

                val name =
                    doctor.text.toString()
                        .trim()

                if (name.isEmpty()) {

                    toast(
                        "ডাক্তারের নাম লিখুন"
                    )

                    return@button
                }

                val list =
                    getDoctorList()
                        .toMutableList()

                if (
                    list.any {
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

                list.add(name)

                saveList(
                    "doctors",
                    list
                )

                toast(
                    "ডাক্তার Add হয়েছে"
                )

                showDoctorPage()
            }
        )

        root.addView(
            gap(15)
        )

        root.addView(
            text(
                "বর্তমান ডাক্তার",
                22f,
                DARK_BLUE,
                true
            )
        )

        getDoctorList()
            .forEach { name ->

                val row =
                    LinearLayout(this)

                row.orientation =
                    LinearLayout.HORIZONTAL

                row.setPadding(
                    10,
                    10,
                    10,
                    10
                )

                row.background =
                    bg(
                        WHITE,
                        14f,
                        LIGHT_BORDER
                    )

                val info =
                    text(
                        "👨‍⚕️ $name",
                        18f,
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

                val del =
                    text(
                        "মুছুন",
                        14f,
                        WHITE,
                        true
                    )

                del.background =
                    bg(
                        RED,
                        10f
                    )

                del.setOnClickListener {

                    val list =
                        getDoctorList()
                            .toMutableList()

                    list.remove(name)

                    saveList(
                        "doctors",
                        list
                    )

                    showDoctorPage()
                }

                row.addView(
                    del,
                    LinearLayout.LayoutParams(
                        90,
                        55
                    )
                )

                root.addView(row)

                root.addView(
                    gap(5)
                )
            }

        root.addView(
            button(
                "←   Dashboard",
                BLUE
            ) {

                showDashboard()
            }
        )

        setContentView(
            scroll(root)
        )
    }

    // =========================================================
    // CARE PAGE
    // =========================================================

    private fun showCarePage() {

        currentScreen =
            "care"

        val root =
            rootLayout()

        root.addView(
            text(
                "👤 Care Of পরিচালনা",
                29f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            text(
                "User / Operator / Admin সবাই Care Of Add করতে পারবেন",
                15f,
                GRAY
            )
        )

        val care =
            input(
                "Care Of নাম"
            )

        root.addView(
            care
        )

        root.addView(
            button(
                "➕   Care Of Add করুন",
                GREEN,
                70
            ) {

                val name =
                    care.text.toString()
                        .trim()

                if (name.isEmpty()) {

                    toast(
                        "Care Of নাম লিখুন"
                    )

                    return@button
                }

                val list =
                    getCareList()
                        .toMutableList()

                if (
                    list.any {
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

                list.add(name)

                saveList(
                    "cares",
                    list
                )

                toast(
                    "Care Of Add হয়েছে"
                )

                showCarePage()
            }
        )

        root.addView(
            gap(15)
        )

        root.addView(
            text(
                "বর্তমান Care Of",
                22f,
                DARK_BLUE,
                true
            )
        )

        getCareList()
            .forEach { name ->

                val row =
                    LinearLayout(this)

                row.orientation =
                    LinearLayout.HORIZONTAL

                row.setPadding(
                    10,
                    10,
                    10,
                    10
                )

                row.background =
                    bg(
                        WHITE,
                        14f,
                        LIGHT_BORDER
                    )

                row.addView(
                    text(
                        "👤 $name",
                        18f,
                        DARK,
                        true
                    ),
                    LinearLayout.LayoutParams(
                        0,
                        65,
                        1f
                    )
                )

                // Only Admin can delete Care Of

                if (
                    currentRole.equals(
                        "Admin",
                        true
                    )
                ) {

                    val del =
                        text(
                            "মুছুন",
                            14f,
                            WHITE,
                            true
                        )

                    del.background =
                        bg(
                            RED,
                            10f
                        )

                    del.setOnClickListener {

                        val list =
                            getCareList()
                                .toMutableList()

                        list.remove(name)

                        saveList(
                            "cares",
                            list
                        )

                        showCarePage()
                    }

                    row.addView(
                        del,
                        LinearLayout.LayoutParams(
                            90,
                            55
                        )
                    )
                }

                root.addView(row)

                root.addView(
                    gap(5)
                )
            }

        root.addView(
            button(
                "←   Dashboard",
                BLUE
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

        currentScreen =
            "admin"

        val root =
            rootLayout()

        root.addView(
            text(
                "👑 Admin Control Panel",
                29f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            text(
                "User এবং Operator তৈরি ও মুছে ফেলুন",
                15f,
                GRAY
            )
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
                "User",
                "Operator"
            )

        spinner.adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                roles
            )

        spinner.setBackgroundColor(
            WHITE
        )

        root.addView(
            spinner,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                70
            )
        )

        root.addView(
            button(
                "➕   User / Operator তৈরি করুন",
                TEAL,
                70
            ) {

                createUser(
                    username.text.toString()
                        .trim(),

                    password.text.toString(),

                    spinner.selectedItem
                        .toString()
                )
            }
        )

        root.addView(
            gap(18)
        )

        root.addView(
            text(
                "বর্তমান User / Operator",
                23f,
                DARK_BLUE,
                true
            )
        )

        pref.all.keys
            .filter {
                it.startsWith(
                    "user_"
                )
            }
            .forEach { key ->

                val name =
                    pref.getString(
                        key,
                        ""
                    ) ?: ""

                if (
                    name.isEmpty() ||
                    name.equals(
                        "admin",
                        true
                    )
                ) return@forEach

                val role =
                    pref.getString(
                        "role_$name",
                        ""
                    ) ?: ""

                val row =
                    LinearLayout(this)

                row.orientation =
                    LinearLayout.HORIZONTAL

                row.gravity =
                    Gravity.CENTER_VERTICAL

                row.background =
                    bg(
                        WHITE,
                        15f,
                        LIGHT_BORDER
                    )

                row.setPadding(
                    12,
                    8,
                    8,
                    8
                )

                row.addView(
                    text(
                        "$name\nRole: $role",
                        17f,
                        DARK,
                        true
                    ),
                    LinearLayout.LayoutParams(
                        0,
                        75,
                        1f
                    )
                )

                val del =
                    text(
                        "মুছুন",
                        14f,
                        WHITE,
                        true
                    )

                del.background =
                    bg(
                        RED,
                        10f
                    )

                del.setOnClickListener {

                    pref.edit()
                        .remove(
                            "user_$name"
                        )
                        .remove(
                            "pass_$name"
                        )
                        .remove(
                            "role_$name"
                        )
                        .apply()

                    showAdminPanel()
                }

                row.addView(
                    del,
                    LinearLayout.LayoutParams(
                        90,
                        55
                    )
                )

                root.addView(row)

                root.addView(
                    gap(5)
                )
            }

        root.addView(
            gap(15)
        )

        root.addView(
            button(
                "←   Dashboard",
                BLUE
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
                "Username লিখুন"
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
            "$role তৈরি হয়েছে"
        )

        showAdminPanel()
    }

    // =========================================================
    // DATE PICKER
    // =========================================================

    private fun showDatePicker(
        initial: String,
        selected: (String) -> Unit
    ) {

        val parts =
            initial.split("-")

        val year =
            parts.getOrNull(0)
                ?.toIntOrNull()
                ?: Calendar.getInstance()
                    .get(Calendar.YEAR)

        val month =
            parts.getOrNull(1)
                ?.toIntOrNull()
                ?.minus(1)
                ?: Calendar.getInstance()
                    .get(Calendar.MONTH)

        val day =
            parts.getOrNull(2)
                ?.toIntOrNull()
                ?: Calendar.getInstance()
                    .get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            this,
            { _, y, m, d ->

                selected(
                    String.format(
                        Locale.getDefault(),
                        "%04d-%02d-%02d",
                        y,
                        m + 1,
                        d
                    )
                )

            },
            year,
            month,
            day
        ).show()
    }

    // =========================================================
    // SERIAL STORAGE
    // =========================================================

    private fun getSerials():
            List<SerialRecord> {

        val result =
            mutableListOf<SerialRecord>()

        val raw =
            pref.getString(
                "serials",
                "[]"
            ) ?: "[]"

        try {

            val array =
                JSONArray(raw)

            for (
                i in 0 until array.length()
            ) {

                val o =
                    array.getJSONObject(i)

                result.add(
                    SerialRecord(
                        id =
                            o.optString(
                                "id"
                            ),

                        date =
                            o.optString(
                                "date"
                            ),

                        serial =
                            o.optInt(
                                "serial"
                            ),

                        doctorSerial =
                            o.optInt(
                                "doctorSerial"
                            ),

                        careSerial =
                            o.optInt(
                                "careSerial"
                            ),

                        patient =
                            o.optString(
                                "patient"
                            ),

                        careOf =
                            o.optString(
                                "careOf"
                            ),

                        doctor =
                            o.optString(
                                "doctor"
                            ),

                        status =
                            o.optString(
                                "status",
                                "Waiting"
                            ),

                        createdBy =
                            o.optString(
                                "createdBy"
                            ),

                        createdRole =
                            o.optString(
                                "createdRole"
                            ),

                        createdTime =
                            o.optString(
                                "createdTime"
                            )
                    )
                )
            }

        } catch (_: Exception) {
        }

        return result
    }

    private fun saveSerials(
        list: List<SerialRecord>
    ) {

        val array =
            JSONArray()

        list.forEach {

            val o =
                JSONObject()

            o.put(
                "id",
                it.id
            )

            o.put(
                "date",
                it.date
            )

            o.put(
                "serial",
                it.serial
            )

            o.put(
                "doctorSerial",
                it.doctorSerial
            )

            o.put(
                "careSerial",
                it.careSerial
            )

            o.put(
                "patient",
                it.patient
            )

            o.put(
                "careOf",
                it.careOf
            )

            o.put(
                "doctor",
                it.doctor
            )

            o.put(
                "status",
                it.status
            )

            o.put(
                "createdBy",
                it.createdBy
            )

            o.put(
                "createdRole",
                it.createdRole
            )

            o.put(
                "createdTime",
                it.createdTime
            )

            array.put(o)
        }

        pref.edit()
            .putString(
                "serials",
                array.toString()
            )
            .apply()
    }

    // =========================================================
    // DOCTOR / CARE STORAGE
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
            "cares"
        )
    }

    private fun getStringList(
        key: String
    ): List<String> {

        val raw =
            pref.getString(
                key,
                "[]"
            ) ?: "[]"

        val result =
            mutableListOf<String>()

        try {

            val array =
                JSONArray(raw)

            for (
                i in 0 until array.length()
            ) {

                result.add(
                    array.getString(i)
                )
            }

        } catch (_: Exception) {
        }

        return result.sorted()
    }

    private fun saveList(
        key: String,
        list: List<String>
    ) {

        val array =
            JSONArray()

        list.forEach {
            array.put(it)
        }

        pref.edit()
            .putString(
                key,
                array.toString()
            )
            .apply()
    }

    // =========================================================
    // DATE / TIME
    // =========================================================

    private fun today():
            String {

        return SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        ).format(
            Date()
        )
    }

    private fun currentTime():
            String {

        return SimpleDateFormat(
            "hh:mm:ss a",
            Locale.getDefault()
        ).format(
            Date()
        )
    }

    // =========================================================
    // STATUS BANGLA
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
    // PASSWORD
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
    // LOGOUT
    // =========================================================

    private fun logout() {

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
            "Logout হয়েছে"
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
            currentScreen ==
            "dashboard" &&
            currentUsername.isNotEmpty()
        ) {

            handler.removeCallbacks(
                refreshRunnable
            )

            handler.postDelayed(
                refreshRunnable,
                refreshTime
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

        when (currentScreen) {

            "login" -> {
                super.onBackPressed()
            }

            else -> {
                showDashboard()
            }
        }
    }
}
