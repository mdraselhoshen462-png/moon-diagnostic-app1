package com.moondiagnostic.app

import android.app.Activity
import android.app.DatePickerDialog
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import org.json.JSONObject
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class MainActivity : Activity() {

    // =========================================================
    // COLORS
    // =========================================================

    private val BG = Color.rgb(242, 248, 253)
    private val BLUE = Color.rgb(27, 92, 150)
    private val DARK_BLUE = Color.rgb(18, 65, 108)
    private val TEAL = Color.rgb(17, 135, 126)
    private val GREEN = Color.rgb(38, 139, 91)
    private val RED = Color.rgb(198, 57, 57)
    private val ORANGE = Color.rgb(225, 143, 38)
    private val PURPLE = Color.rgb(103, 76, 165)
    private val DARK = Color.rgb(40, 45, 50)
    private val GRAY = Color.rgb(105, 110, 115)
    private val LIGHT_BORDER = Color.rgb(202, 218, 231)
    private val WHITE = Color.WHITE

    // =========================================================
    // STORAGE
    // =========================================================

    private lateinit var pref: SharedPreferences

    private val PREF = "MDC_DATA"

    private var currentUsername = ""
    private var currentRole = ""

    // =========================================================
    // AUTO REFRESH
    // =========================================================

    private val handler = Handler(Looper.getMainLooper())

    private val REFRESH_TIME = 20_000L

    private var dashboardVisible = false

    private val refreshRunnable = object : Runnable {
        override fun run() {

            if (dashboardVisible && currentUsername.isNotEmpty()) {

                showDashboard()

                handler.postDelayed(
                    this,
                    REFRESH_TIME
                )
            }
        }
    }

    // =========================================================
    // SERIAL MODEL
    // =========================================================

    data class SerialRecord(

        val id: String,

        val date: String,

        val globalNumber: Int,

        val doctorNumber: Int,

        val careNumber: Int,

        val patient: String,

        val care: String,

        val doctor: String,

        val status: String,

        val createdBy: String,

        val createdRole: String,

        val createdTime: String
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
    // BASIC TEXT
    // =========================================================

    private fun tv(
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
    // ROOT
    // =========================================================

    private fun rootLayout(): LinearLayout {

        val root = LinearLayout(this)

        root.orientation =
            LinearLayout.VERTICAL

        root.setPadding(
            14,
            18,
            14,
            30
        )

        root.setBackgroundColor(BG)

        return root
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
    // BACKGROUND
    // =========================================================

    private fun box(
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
    // BIG BUTTON
    // =========================================================

    private fun bigButton(
        text: String,
        color: Int = BLUE,
        height: Int = 70,
        click: () -> Unit
    ): TextView {

        val b = tv(
            text,
            18f,
            WHITE,
            true
        )

        b.background =
            box(
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
            6,
            7,
            6,
            7
        )

        b.layoutParams = p

        b.setOnClickListener {

            click()
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

        e.textSize = 19f

        e.setTextColor(DARK)

        e.setHintTextColor(
            Color.rgb(
                130,
                135,
                140
            )
        )

        e.setPadding(
            18,
            0,
            18,
            0
        )

        e.background =
            box(
                WHITE,
                16f,
                TEAL
            )

        e.inputType =
            if (password) {

                InputType.TYPE_CLASS_TEXT or
                        InputType.TYPE_TEXT_VARIATION_PASSWORD

            } else {

                InputType.TYPE_CLASS_TEXT
            }

        val p =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                68
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
    // LOGIN PAGE
    // =========================================================

    private fun showLogin() {

        dashboardVisible = false

        handler.removeCallbacks(
            refreshRunnable
        )

        val root = rootLayout()

        root.gravity =
            Gravity.CENTER_HORIZONTAL

        root.addView(gap(45))

        root.addView(
            tv(
                "MDC",
                62f,
                BLUE,
                true
            )
        )

        root.addView(
            tv(
                "মুন ডায়াগনস্টিক সেন্টার",
                30f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            tv(
                "সঠিক নির্ণয়, সুস্থ জীবনের প্রত্যয়",
                16f,
                GRAY
            )
        )

        root.addView(gap(25))

        val card = LinearLayout(this)

        card.orientation =
            LinearLayout.VERTICAL

        card.setPadding(
            18,
            25,
            18,
            28
        )

        card.background =
            box(
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
            tv(
                "🔐  লগইন করুন",
                30f,
                DARK_BLUE,
                true
            )
        )

        card.addView(gap(15))

        val username =
            input("ইউজারনেম")

        val password =
            input(
                "পাসওয়ার্ড",
                true
            )

        card.addView(username)

        card.addView(password)

        card.addView(gap(12))

        card.addView(
            bigButton(
                "🔐   LOGIN",
                BLUE,
                70
            ) {

                login(
                    username.text.toString().trim(),
                    password.text.toString()
                )
            }
        )

        root.addView(gap(22))

        root.addView(
            tv(
                "শুধুমাত্র অনুমোদিত User / Operator / Admin ব্যবহার করতে পারবেন",
                14f,
                GRAY
            )
        )

        root.addView(gap(20))

        root.addView(
            tv(
                "Moon Diagnostic Center",
                17f,
                GRAY,
                true
            )
        )

        root.addView(
            tv(
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
    // LOGIN
    // =========================================================

    private fun login(
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

        val role =
            pref.getString(
                "role_$username",
                null
            )

        if (
            savedUser != null &&
            savedPass == hashPassword(password) &&
            role != null
        ) {

            currentUsername =
                username

            currentRole =
                role

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
                    role
                )

                .apply()

            toast("সফলভাবে Login হয়েছে")

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

        val root = rootLayout()

        // HEADER

        root.addView(
            tv(
                "MDC",
                58f,
                BLUE,
                true
            )
        )

        root.addView(
            tv(
                "মুন ডায়াগনস্টিক সেন্টার",
                29f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            tv(
                "স্বাগতম, $currentUsername",
                22f,
                DARK,
                true
            )
        )

        root.addView(
            tv(
                "Role: $currentRole",
                17f,
                TEAL,
                true
            )
        )

        root.addView(gap(12))

        root.addView(
            bigButton(
                "🚪   LOGOUT",
                RED,
                65
            ) {

                logout()
            }
        )

        root.addView(gap(15))

        val today =
            todayString()

        root.addView(
            tv(
                "📅  আজকের তারিখ",
                22f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            tv(
                formatDate(today),
                20f,
                DARK,
                true
            )
        )

        root.addView(gap(15))

        val records =
            readSerials(today)

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
        // LARGE STAT CARDS
        // =====================================================

        val row1 =
            LinearLayout(this)

        row1.orientation =
            LinearLayout.HORIZONTAL

        row1.addView(
            stat(
                "👥",
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

        root.addView(gap(20))

        root.addView(
            tv(
                "⚡  দ্রুত অ্যাকশন",
                26f,
                DARK_BLUE,
                true
            )
        )

        root.addView(gap(8))

        // =====================================================
        // QUICK ACTION 2 x 2
        // =====================================================

        val q1 =
            LinearLayout(this)

        q1.orientation =
            LinearLayout.HORIZONTAL

        q1.addView(
            gridButton(
                "📋\nটোটাল সিরিয়াল",
                BLUE
            ) {

                showTotalSerial()
            }
        )

        q1.addView(
            gridButton(
                "➕\nঅ্যাড সিরিয়াল",
                GREEN
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
            gridButton(
                "👨‍⚕️\nঅ্যাড ডাক্তার",
                PURPLE
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
                        "শুধুমাত্র Admin ডাক্তার যোগ করতে পারবেন"
                    )
                }
            }
        )

        q2.addView(
            gridButton(
                "👤\nকেয়ার অফ",
                TEAL
            ) {

                showCarePage()
            }
        )

        root.addView(q2)

        root.addView(gap(20))

        // =====================================================
        // DOCTOR WISE
        // =====================================================

        root.addView(
            tv(
                "👨‍⚕️  ডাক্তার ওয়াইজ সিরিয়াল",
                25f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            tv(
                "প্রতিটি ডাক্তার এবং তার আজকের সিরিয়াল",
                15f,
                GRAY
            )
        )

        root.addView(
            bigButton(
                "ডাক্তার অনুযায়ী সিরিয়াল দেখুন",
                PURPLE
            ) {

                showDoctorWise()
            }
        )

        root.addView(gap(15))

        // =====================================================
        // CARE WISE
        // =====================================================

        root.addView(
            tv(
                "👤  কেয়ার অফ ওয়াইজ সিরিয়াল",
                25f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            tv(
                "প্রতিটি Care Of এবং তার আজকের সিরিয়াল",
                15f,
                GRAY
            )
        )

        root.addView(
            bigButton(
                "কেয়ার অফ অনুযায়ী সিরিয়াল দেখুন",
                TEAL
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

            root.addView(gap(18))

            root.addView(
                bigButton(
                    "👑   ADMIN CONTROL PANEL",
                    PURPLE
                ) {

                    showAdminPanel()
                }
            )
        }

        root.addView(gap(25))

        root.addView(
            tv(
                "🔄 ড্যাশবোর্ড প্রতি ২০ সেকেন্ডে Refresh হবে",
                14f,
                TEAL,
                true
            )
        )

        root.addView(
            tv(
                "অন্য পেজে কাজ করার সময় অটো Refresh হবে না",
                13f,
                GRAY
            )
        )

        root.addView(gap(20))

        root.addView(
            tv(
                "মুন ডায়াগনস্টিক সেন্টার",
                17f,
                GRAY,
                true
            )
        )

        setContentView(
            scroll(root)
        )

        handler.postDelayed(
            refreshRunnable,
            REFRESH_TIME
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
            15,
            10,
            15
        )

        card.background =
            box(
                WHITE,
                18f,
                LIGHT_BORDER
            )

        card.elevation = 5f

        val p =
            LinearLayout.LayoutParams(
                0,
                155,
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
            tv(
                icon,
                34f,
                color,
                true
            )
        )

        card.addView(
            tv(
                title,
                18f,
                DARK,
                true
            )
        )

        card.addView(
            tv(
                value + " জন",
                22f,
                color,
                true
            )
        )

        return card
    }

    // =========================================================
    // GRID BUTTON
    // =========================================================

    private fun gridButton(
        text: String,
        color: Int,
        click: () -> Unit
    ): TextView {

        val b =
            tv(
                text,
                20f,
                DARK,
                true
            )

        b.background =
            box(
                WHITE,
                18f,
                LIGHT_BORDER
            )

        b.elevation = 5f

        val p =
            LinearLayout.LayoutParams(
                0,
                155,
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

    private fun showAddSerial() {

        dashboardVisible = false

        handler.removeCallbacks(
            refreshRunnable
        )

        val root =
            rootLayout()

        root.addView(
            tv(
                "➕  নতুন সিরিয়াল",
                30f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            tv(
                "রোগীর তথ্য দিয়ে সিরিয়াল তৈরি করুন",
                15f,
                GRAY
            )
        )

        root.addView(gap(12))

        val patient =
            input("রোগীর নাম")

        root.addView(
            tv(
                "রোগীর নাম",
                17f,
                DARK_BLUE,
                true
            )
        )

        root.addView(patient)

        // =====================================================
        // CARE AUTOCOMPLETE
        // =====================================================

        root.addView(
            tv(
                "কেয়ার অফ",
                17f,
                DARK_BLUE,
                true
            )
        )

        val care =
            autoCompleteInput(
                "কেয়ার অফ লিখুন বা Arrow থেকে নির্বাচন করুন",
                getCareList()
            )

        root.addView(care)

        // =====================================================
        // DOCTOR AUTOCOMPLETE
        // =====================================================

        root.addView(
            tv(
                "ডাক্তার",
                17f,
                DARK_BLUE,
                true
            )
        )

        val doctor =
            autoCompleteInput(
                "ডাক্তারের নাম লিখুন বা Arrow থেকে নির্বাচন করুন",
                getDoctorList()
            )

        root.addView(doctor)

        // =====================================================
        // DATE
        // =====================================================

        root.addView(
            tv(
                "সিরিয়ালের তারিখ",
                17f,
                DARK_BLUE,
                true
            )
        )

        val date =
            input(
                "তারিখ নির্বাচন করুন"
            )

        date.isFocusable = false

        date.isClickable = true

        date.setText(
            formatDate(
                todayString()
            )
        )

        root.addView(date)

        date.setOnClickListener {

            chooseDate(date)
        }

        root.addView(gap(10))

        root.addView(
            tv(
                "সিরিয়াল দিচ্ছেন:",
                15f,
                GRAY
            )
        )

        root.addView(
            tv(
                "$currentUsername  •  $currentRole",
                19f,
                TEAL,
                true
            )
        )

        root.addView(gap(10))

        root.addView(
            bigButton(
                "✅   সিরিয়াল তৈরি করুন",
                GREEN,
                72
            ) {

                val selectedDate =
                    parseDisplayDate(
                        date.text.toString()
                    )

                saveSerial(
                    patient.text.toString().trim(),
                    care.text.toString().trim(),
                    doctor.text.toString().trim(),
                    selectedDate
                )
            }
        )

        root.addView(gap(10))

        root.addView(
            bigButton(
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

    private fun autoCompleteInput(
        hint: String,
        list: List<String>
    ): AutoCompleteTextView {

        val a =
            AutoCompleteTextView(this)

        a.hint = hint

        a.textSize = 18f

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

        a.background =
            box(
                WHITE,
                16f,
                TEAL
            )

        a.threshold = 0

        val adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                list
            )

        a.setAdapter(adapter)

        a.setOnClickListener {

            a.showDropDown()
        }

        val p =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                68
            )

        p.setMargins(
            6,
            7,
            6,
            7
        )

        a.layoutParams = p

        return a
    }

    // =========================================================
    // DATE PICKER
    // =========================================================

    private fun chooseDate(
        field: EditText
    ) {

        val cal =
            Calendar.getInstance()

        val dialog =
            DatePickerDialog(
                this,
                { _, year, month, day ->

                    val selected =
                        String.format(
                            Locale.getDefault(),
                            "%04d-%02d-%02d",
                            year,
                            month + 1,
                            day
                        )

                    field.setText(
                        formatDate(
                            selected
                        )
                    )
                },
                cal.get(
                    Calendar.YEAR
                ),
                cal.get(
                    Calendar.MONTH
                ),
                cal.get(
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
        care: String,
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
                "ডাক্তারের নাম নির্বাচন করুন"
            )

            return
        }

        val records =
            readSerials(date)

        val globalNumber =
            records.maxOfOrNull {
                it.globalNumber
            }?.plus(1) ?: 1

        val doctorNumber =
            records.filter {
                it.doctor.equals(
                    doctor,
                    true
                )
            }.maxOfOrNull {
                it.doctorNumber
            }?.plus(1) ?: 1

        val careNumber =
            if (care.isEmpty()) {

                0

            } else {

                records.filter {
                    it.care.equals(
                        care,
                        true
                    )
                }.maxOfOrNull {
                    it.careNumber
                }?.plus(1) ?: 1
            }

        val id =
            UUID.randomUUID().toString()

        val record =
            JSONObject()

        record.put(
            "id",
            id
        )

        record.put(
            "date",
            date
        )

        record.put(
            "global",
            globalNumber
        )

        record.put(
            "doctorNumber",
            doctorNumber
        )

        record.put(
            "careNumber",
            careNumber
        )

        record.put(
            "patient",
            patient
        )

        record.put(
            "care",
            care
        )

        record.put(
            "doctor",
            doctor
        )

        record.put(
            "status",
            "Waiting"
        )

        record.put(
            "createdBy",
            currentUsername
        )

        record.put(
            "createdRole",
            currentRole
        )

        record.put(
            "createdTime",
            currentTime()
        )

        pref.edit()
            .putString(
                "serial_$id",
                record.toString()
            )
            .apply()

        toast(
            "সিরিয়াল #$globalNumber তৈরি হয়েছে"
        )

        showTotalSerial(
            date
        )
    }

    // =========================================================
    // READ SERIALS
    // =========================================================

    private fun readSerials(
        date: String
    ): List<SerialRecord> {

        val list =
            mutableListOf<SerialRecord>()

        for (key in pref.all.keys) {

            if (!key.startsWith("serial_")) {
                continue
            }

            try {

                val json =
                    JSONObject(
                        pref.getString(
                            key,
                            ""
                        ) ?: ""
                    )

                if (
                    json.optString(
                        "date"
                    ) != date
                ) {
                    continue
                }

                list.add(
                    SerialRecord(

                        json.optString(
                            "id"
                        ),

                        json.optString(
                            "date"
                        ),

                        json.optInt(
                            "global"
                        ),

                        json.optInt(
                            "doctorNumber"
                        ),

                        json.optInt(
                            "careNumber"
                        ),

                        json.optString(
                            "patient"
                        ),

                        json.optString(
                            "care"
                        ),

                        json.optString(
                            "doctor"
                        ),

                        json.optString(
                            "status",
                            "Waiting"
                        ),

                        json.optString(
                            "createdBy"
                        ),

                        json.optString(
                            "createdRole"
                        ),

                        json.optString(
                            "createdTime"
                        )
                    )
                )

            } catch (_: Exception) {
            }
        }

        return list.sortedBy {
            it.globalNumber
        }
    }

    // =========================================================
    // TOTAL SERIAL
    // =========================================================

    private fun showTotalSerial(
        selectedDate: String = todayString()
    ) {

        dashboardVisible = false

        handler.removeCallbacks(
            refreshRunnable
        )

        val root =
            rootLayout()

        root.addView(
            tv(
                "📋  টোটাল সিরিয়াল",
                30f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            tv(
                "নির্বাচিত দিনের সবার সিরিয়াল",
                15f,
                GRAY
            )
        )

        val dateField =
            input(
                "তারিখ নির্বাচন করুন"
            )

        dateField.isFocusable =
            false

        dateField.setText(
            formatDate(
                selectedDate
            )
        )

        root.addView(dateField)

        dateField.setOnClickListener {

            chooseDate(
                dateField
            )

            dateField.setOnFocusChangeListener {
                    _, hasFocus ->

                if (!hasFocus) {

                    val date =
                        parseDisplayDate(
                            dateField.text.toString()
                        )

                    showTotalSerial(
                        date
                    )
                }
            }
        }

        root.addView(gap(12))

        val records =
            readSerials(
                selectedDate
            )

        root.addView(
            tv(
                "মোট ${records.size} জন",
                21f,
                TEAL,
                true
            )
        )

        root.addView(gap(5))

        if (records.isEmpty()) {

            root.addView(
                tv(
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

        root.addView(gap(15))

        root.addView(
            bigButton(
                "➕   নতুন সিরিয়াল",
                GREEN
            ) {

                showAddSerial()
            }
        )

        root.addView(
            bigButton(
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
            16,
            15,
            16,
            17
        )

        card.background =
            box(
                WHITE,
                18f,
                LIGHT_BORDER
            )

        card.elevation = 4f

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
            tv(
                "সিরিয়াল #${r.globalNumber}",
                23f,
                BLUE,
                true
            )
        )

        card.addView(
            tv(
                "👤 রোগী: ${r.patient}",
                20f,
                DARK,
                true
            )
        )

        card.addView(
            tv(
                "👤 Care Of: ${
                    if (r.care.isEmpty()) "—"
                    else r.care
                }",
                17f,
                DARK
            )
        )

        card.addView(
            tv(
                "👨‍⚕️ ডাক্তার: ${r.doctor}",
                18f,
                DARK
            )
        )

        card.addView(
            tv(
                "ডাক্তার সিরিয়াল: #${r.doctorNumber}",
                16f,
                PURPLE,
                true
            )
        )

        if (r.careNumber > 0) {

            card.addView(
                tv(
                    "Care সিরিয়াল: #${r.careNumber}",
                    16f,
                    TEAL,
                    true
                )
            )
        }

        card.addView(
            tv(
                "✍ দিয়েছেন: ${r.createdBy} (${r.createdRole})",
                16f,
                TEAL,
                true
            )
        )

        card.addView(
            tv(
                "সময়: ${r.createdTime}",
                14f,
                GRAY
            )
        )

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
            tv(
                when (r.status) {

                    "Completed" ->
                        "✓ সম্পন্ন"

                    "Cancelled" ->
                        "✕ বাতিল"

                    else ->
                        "⏳ অপেক্ষমাণ"
                },
                18f,
                statusColor,
                true
            )
        )

        // =====================================================
        // OWNER EDIT / DELETE
        // =====================================================

        if (
            r.createdBy.equals(
                currentUsername,
                true
            )
        ) {

            val ownerRow =
                LinearLayout(this)

            ownerRow.orientation =
                LinearLayout.HORIZONTAL

            val edit =
                smallButton(
                    "✏ Edit",
                    BLUE
                ) {

                    showEditSerial(
                        r
                    )
                }

            val delete =
                smallButton(
                    "🗑 Delete",
                    RED
                ) {

                    deleteSerial(
                        r
                    )
                }

            ownerRow.addView(edit)

            ownerRow.addView(delete)

            card.addView(
                ownerRow
            )
        }

        // =====================================================
        // COMPLETE / INCOMPLETE
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

            val text =
                if (
                    r.status ==
                    "Completed"
                ) {

                    "↩ অসম্পন্ন করুন"

                } else {

                    "✓ সম্পন্ন করুন"
                }

            val color =
                if (
                    r.status ==
                    "Completed"
                ) {

                    ORANGE

                } else {

                    GREEN
                }

            card.addView(
                bigButton(
                    text,
                    color,
                    60
                ) {

                    toggleComplete(
                        r
                    )
                }
            )
        }

        root.addView(card)
    }

    // =========================================================
    // SMALL BUTTON
    // =========================================================

    private fun smallButton(
        text: String,
        color: Int,
        click: () -> Unit
    ): TextView {

        val b =
            tv(
                text,
                15f,
                WHITE,
                true
            )

        b.background =
            box(
                color,
                12f
            )

        val p =
            LinearLayout.LayoutParams(
                0,
                55,
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

            click()
        }

        return b
    }

    // =========================================================
    // EDIT SERIAL
    // =========================================================

    private fun showEditSerial(
        r: SerialRecord
    ) {

        val root =
            rootLayout()

        root.addView(
            tv(
                "✏  সিরিয়াল Edit",
                28f,
                DARK_BLUE,
                true
            )
        )

        val patient =
            input(
                "রোগীর নাম"
            )

        patient.setText(
            r.patient
        )

        val care =
            autoCompleteInput(
                "Care Of",
                getCareList()
            )

        care.setText(
            r.care
        )

        val doctor =
            autoCompleteInput(
                "ডাক্তার",
                getDoctorList()
            )

        doctor.setText(
            r.doctor
        )

        root.addView(
            tv(
                "রোগীর নাম",
                17f,
                DARK_BLUE,
                true
            )
        )

        root.addView(patient)

        root.addView(
            tv(
                "Care Of",
                17f,
                DARK_BLUE,
                true
            )
        )

        root.addView(care)

        root.addView(
            tv(
                "ডাক্তার",
                17f,
                DARK_BLUE,
                true
            )
        )

        root.addView(doctor)

        root.addView(gap(12))

        root.addView(
            bigButton(
                "💾   পরিবর্তন সংরক্ষণ",
                GREEN
            ) {

                updateSerial(
                    r,
                    patient.text.toString().trim(),
                    care.text.toString().trim(),
                    doctor.text.toString().trim()
                )
            }
        )

        root.addView(
            bigButton(
                "← ফিরে যান",
                BLUE
            ) {

                showTotalSerial(
                    r.date
                )
            }
        )

        setContentView(
            scroll(root)
        )
    }

    private fun updateSerial(
        r: SerialRecord,
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

        val json =
            JSONObject()

        json.put(
            "id",
            r.id
        )

        json.put(
            "date",
            r.date
        )

        json.put(
            "global",
            r.globalNumber
        )

        json.put(
            "doctorNumber",
            r.doctorNumber
        )

        json.put(
            "careNumber",
            r.careNumber
        )

        json.put(
            "patient",
            patient
        )

        json.put(
            "care",
            care
        )

        json.put(
            "doctor",
            doctor
        )

        json.put(
            "status",
            r.status
        )

        json.put(
            "createdBy",
            r.createdBy
        )

        json.put(
            "createdRole",
            r.createdRole
        )

        json.put(
            "createdTime",
            r.createdTime
        )

        pref.edit()
            .putString(
                "serial_${r.id}",
                json.toString()
            )
            .apply()

        toast(
            "সিরিয়াল পরিবর্তন হয়েছে"
        )

        showTotalSerial(
            r.date
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

        AlertDialogBuilder(
            "সিরিয়াল Delete করবেন?",
            "সিরিয়াল #${r.globalNumber} মুছে ফেলা হবে।",
            "Delete"
        ) {

            pref.edit()
                .remove(
                    "serial_${r.id}"
                )
                .apply()

            toast(
                "সিরিয়াল Delete হয়েছে"
            )

            showTotalSerial(
                r.date
            )
        }
    }

    // =========================================================
    // COMPLETE
    // =========================================================

    private fun toggleComplete(
        r: SerialRecord
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
                "User Completed করতে পারবেন না"
            )

            return
        }

        val json =
            JSONObject()

        json.put(
            "id",
            r.id
        )

        json.put(
            "date",
            r.date
        )

        json.put(
            "global",
            r.globalNumber
        )

        json.put(
            "doctorNumber",
            r.doctorNumber
        )

        json.put(
            "careNumber",
            r.careNumber
        )

        json.put(
            "patient",
            r.patient
        )

        json.put(
            "care",
            r.care
        )

        json.put(
            "doctor",
            r.doctor
        )

        json.put(
            "status",
            if (
                r.status ==
                "Completed"
            ) {

                "Waiting"

            } else {

                "Completed"
            }
        )

        json.put(
            "createdBy",
            r.createdBy
        )

        json.put(
            "createdRole",
            r.createdRole
        )

        json.put(
            "createdTime",
            r.createdTime
        )

        pref.edit()
            .putString(
                "serial_${r.id}",
                json.toString()
            )
            .apply()

        showTotalSerial(
            r.date
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
                "শুধুমাত্র Admin ডাক্তার যোগ করতে পারবেন"
            )

            return
        }

        val root =
            rootLayout()

        root.addView(
            tv(
                "👨‍⚕️  ডাক্তার পরিচালনা",
                28f,
                DARK_BLUE,
                true
            )
        )

        val doctor =
            input(
                "ডাক্তারের নাম"
            )

        root.addView(doctor)

        root.addView(
            bigButton(
                "➕   ডাক্তার যোগ করুন",
                PURPLE
            ) {

                addDoctor(
                    doctor.text.toString().trim()
                )
            }
        )

        root.addView(gap(15))

        root.addView(
            tv(
                "বর্তমান ডাক্তার",
                23f,
                DARK_BLUE,
                true
            )
        )

        getDoctorList().forEach {

            root.addView(
                itemCard(
                    "👨‍⚕️ $it"
                )
            )
        }

        root.addView(
            bigButton(
                "← Dashboard",
                BLUE
            ) {

                showDashboard()
            }
        )

        setContentView(
            scroll(root)
        )
    }

    private fun addDoctor(
        name: String
    ) {

        if (name.isEmpty()) {

            toast(
                "ডাক্তারের নাম লিখুন"
            )

            return
        }

        val list =
            getDoctorList().toMutableList()

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

            return
        }

        list.add(name)

        saveStringList(
            "doctors",
            list
        )

        toast(
            "ডাক্তার যোগ হয়েছে"
        )

        showDoctorPage()
    }

    // =========================================================
    // CARE PAGE
    // =========================================================

    private fun showCarePage() {

        val root =
            rootLayout()

        root.addView(
            tv(
                "👤  Care Of পরিচালনা",
                28f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            tv(
                "User / Operator / Admin সবাই Care Of যোগ করতে পারবেন",
                15f,
                GRAY
            )
        )

        val care =
            input(
                "Care Of নাম"
            )

        root.addView(care)

        root.addView(
            bigButton(
                "➕   Care Of যোগ করুন",
                TEAL
            ) {

                addCare(
                    care.text.toString().trim()
                )
            }
        )

        root.addView(gap(15))

        root.addView(
            tv(
                "বর্তমান Care Of",
                23f,
                DARK_BLUE,
                true
            )
        )

        getCareList().forEach {

            val row =
                LinearLayout(this)

            row.orientation =
                LinearLayout.HORIZONTAL

            row.background =
                box(
                    WHITE,
                    15f,
                    LIGHT_BORDER
                )

            val name =
                tv(
                    "👤 $it",
                    18f,
                    DARK,
                    true
                )

            row.addView(
                name,
                LinearLayout.LayoutParams(
                    0,
                    65,
                    1f
                )
            )

            // শুধু Admin delete করতে পারবে
            if (
                currentRole.equals(
                    "Admin",
                    true
                )
            ) {

                val del =
                    smallButton(
                        "Delete",
                        RED
                    ) {

                        deleteCare(
                            it
                        )
                    }

                row.addView(
                    del,
                    LinearLayout.LayoutParams(
                        100,
                        55
                    )
                )
            }

            val p =
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    70
                )

            p.setMargins(
                5,
                5,
                5,
                5
            )

            root.addView(
                row,
                p
            )
        }

        root.addView(
            bigButton(
                "← Dashboard",
                BLUE
            ) {

                showDashboard()
            }
        )

        setContentView(
            scroll(root)
        )
    }

    private fun addCare(
        name: String
    ) {

        if (name.isEmpty()) {

            toast(
                "Care Of নাম লিখুন"
            )

            return
        }

        val list =
            getCareList().toMutableList()

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

            return
        }

        list.add(name)

        saveStringList(
            "care_list",
            list
        )

        toast(
            "Care Of যোগ হয়েছে"
        )

        showCarePage()
    }

    private fun deleteCare(
        name: String
    ) {

        if (
            !currentRole.equals(
                "Admin",
                true
            )
        ) {

            toast(
                "শুধু Admin Care Of Delete করতে পারবেন"
            )

            return
        }

        val list =
            getCareList().toMutableList()

        list.removeAll {
            it.equals(
                name,
                true
            )
        }

        saveStringList(
            "care_list",
            list
        )

        showCarePage()
    }

    // =========================================================
    // DOCTOR WISE
    // =========================================================

    private fun showDoctorWise() {

        val root =
            rootLayout()

        root.addView(
            tv(
                "👨‍⚕️  ডাক্তার ওয়াইজ সিরিয়াল",
                29f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            tv(
                "আজকের প্রতিটি ডাক্তারের সিরিয়াল",
                15f,
                GRAY
            )
        )

        val records =
            readSerials(
                todayString()
            )

        val doctors =
            getDoctorList()

        if (doctors.isEmpty()) {

            root.addView(
                tv(
                    "কোনো ডাক্তার যোগ করা হয়নি",
                    18f,
                    GRAY,
                    true
                )
            )

        } else {

            doctors.forEach { doctor ->

                val list =
                    records.filter {
                        it.doctor.equals(
                            doctor,
                            true
                        )
                    }

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
                    box(
                        WHITE,
                        18f,
                        LIGHT_BORDER
                    )

                card.addView(
                    tv(
                        "👨‍⚕️ $doctor",
                        21f,
                        PURPLE,
                        true
                    )
                )

                if (list.isEmpty()) {

                    card.addView(
                        tv(
                            "আজ কোনো সিরিয়াল নেই",
                            15f,
                            GRAY
                        )
                    )

                } else {

                    list.sortedBy {
                        it.doctorNumber
                    }.forEach {

                        card.addView(
                            tv(
                                "#${it.doctorNumber}  ${it.patient}  •  ${it.status}",
                                17f,
                                DARK,
                                true
                            )
                        )
                    }
                }

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

                root.addView(
                    card,
                    p
                )
            }
        }

        root.addView(
            bigButton(
                "← Dashboard",
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

    private fun showCareWise() {

        val root =
            rootLayout()

        root.addView(
            tv(
                "👤  Care Of ওয়াইজ সিরিয়াল",
                29f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            tv(
                "আজকের প্রতিটি Care Of-এর সিরিয়াল",
                15f,
                GRAY
            )
        )

        val records =
            readSerials(
                todayString()
            )

        val cares =
            getCareList()

        if (cares.isEmpty()) {

            root.addView(
                tv(
                    "কোনো Care Of যোগ করা হয়নি",
                    18f,
                    GRAY,
                    true
                )
            )

        } else {

            cares.forEach { care ->

                val list =
                    records.filter {
                        it.care.equals(
                            care,
                            true
                        )
                    }

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
                    box(
                        WHITE,
                        18f,
                        LIGHT_BORDER
                    )

                card.addView(
                    tv(
                        "👤 $care",
                        21f,
                        TEAL,
                        true
                    )
                )

                if (list.isEmpty()) {

                    card.addView(
                        tv(
                            "আজ কোনো সিরিয়াল নেই",
                            15f,
                            GRAY
                        )
                    )

                } else {

                    list.sortedBy {
                        it.careNumber
                    }.forEach {

                        card.addView(
                            tv(
                                "#${it.careNumber}  ${it.patient}  •  ${it.status}",
                                17f,
                                DARK,
                                true
                            )
                        )
                    }
                }

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

                root.addView(
                    card,
                    p
                )
            }
        }

        root.addView(
            bigButton(
                "← Dashboard",
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
                "শুধু Admin ব্যবহার করতে পারবেন"
            )

            return
        }

        val root =
            rootLayout()

        root.addView(
            tv(
                "👑  Admin Control Panel",
                29f,
                PURPLE,
                true
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

        root.addView(username)

        root.addView(password)

        val spinner =
            Spinner(this)

        val roles =
            arrayOf(
                "User",
                "Operator"
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

        spinner.adapter =
            adapter

        root.addView(
            spinner,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                65
            )
        )

        root.addView(
            bigButton(
                "➕  User / Operator তৈরি করুন",
                PURPLE
            ) {

                createUser(
                    username.text.toString().trim(),
                    password.text.toString(),
                    spinner.selectedItem.toString()
                )
            }
        )

        root.addView(gap(15))

        root.addView(
            tv(
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

                val user =
                    pref.getString(
                        key,
                        ""
                    ) ?: ""

                if (
                    user.isNotEmpty() &&
                    !user.equals(
                        "admin",
                        true
                    )
                ) {

                    val role =
                        pref.getString(
                            "role_$user",
                            ""
                        ) ?: ""

                    val row =
                        LinearLayout(this)

                    row.orientation =
                        LinearLayout.HORIZONTAL

                    row.background =
                        box(
                            WHITE,
                            15f,
                            LIGHT_BORDER
                        )

                    row.addView(
                        tv(
                            "$user\nRole: $role",
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

                    row.addView(
                        smallButton(
                            "Delete",
                            RED
                        ) {

                            deleteUser(
                                user
                            )
                        },
                        LinearLayout.LayoutParams(
                            100,
                            55
                        )
                    )

                    val p =
                        LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            80
                        )

                    p.setMargins(
                        5,
                        5,
                        5,
                        5
                    )

                    root.addView(
                        row,
                        p
                    )
                }
            }
        }

        root.addView(
            bigButton(
                "← Dashboard",
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
    // USER CREATE
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
                "Password কমপক্ষে ৪ অক্ষরের দিন"
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
                "Admin মুছা যাবে না"
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
            "$username Delete হয়েছে"
        )

        showAdminPanel()
    }

    // =========================================================
    // LIST STORAGE
    // =========================================================

    private fun getDoctorList(): List<String> {

        return getStringList(
            "doctors"
        )
    }

    private fun getCareList(): List<String> {

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

        if (raw.isEmpty()) {

            return emptyList()
        }

        return raw.split("|||")
            .filter {
                it.isNotBlank()
            }
            .distinct()
    }

    private fun saveStringList(
        key: String,
        list: List<String>
    ) {

        pref.edit()
            .putString(
                key,
                list.distinct().joinToString("|||")
            )
            .apply()
    }

    // =========================================================
    // ITEM CARD
    // =========================================================

    private fun itemCard(
        text: String
    ): TextView {

        val t =
            tv(
                text,
                18f,
                DARK,
                true
            )

        t.background =
            box(
                WHITE,
                15f,
                LIGHT_BORDER
            )

        val p =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                70
            )

        p.setMargins(
            5,
            5,
            5,
            5
        )

        t.layoutParams = p

        return t
    }

    // =========================================================
    // DATE
    // =========================================================

    private fun todayString(): String {

        return SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        ).format(
            Date()
        )
    }

    private fun formatDate(
        date: String
    ): String {

        return try {

            val d =
                SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
                ).parse(date)

            SimpleDateFormat(
                "dd-MM-yyyy",
                Locale.getDefault()
            ).format(d!!)

        } catch (_: Exception) {

            date
        }
    }

    private fun parseDisplayDate(
        display: String
    ): String {

        return try {

            val d =
                SimpleDateFormat(
                    "dd-MM-yyyy",
                    Locale.getDefault()
                ).parse(display)

            SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            ).format(d!!)

        } catch (_: Exception) {

            todayString()
        }
    }

    // =========================================================
    // ALERT
    // =========================================================

    private fun AlertDialogBuilder(
        title: String,
        message: String,
        positive: String,
        action: () -> Unit
    ) {

        android.app.AlertDialog.Builder(this)

            .setTitle(title)

            .setMessage(message)

            .setNegativeButton(
                "Cancel",
                null
            )

            .setPositiveButton(
                positive
            ) { _, _ ->

                action()
            }

            .show()
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
    // HASH
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

        } catch (_: Exception) {

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
                REFRESH_TIME
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
