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

    private val BG = Color.rgb(238, 247, 253)
    private val WHITE = Color.WHITE

    private val BLUE = Color.rgb(25, 91, 150)
    private val DARK_BLUE = Color.rgb(17, 61, 103)

    private val TEAL = Color.rgb(15, 137, 125)
    private val GREEN = Color.rgb(38, 142, 91)
    private val RED = Color.rgb(205, 55, 55)
    private val ORANGE = Color.rgb(225, 139, 35)
    private val PURPLE = Color.rgb(105, 75, 165)

    private val DARK = Color.rgb(40, 45, 50)
    private val GRAY = Color.rgb(105, 110, 115)
    private val BORDER = Color.rgb(198, 216, 229)

    // =========================================================
    // STORAGE
    // =========================================================

    private lateinit var pref: android.content.SharedPreferences

    private val PREF_NAME = "MDC_DATA"

    private var currentUsername = ""
    private var currentRole = ""

    // =========================================================
    // REFRESH
    // =========================================================

    private val handler = Handler(Looper.getMainLooper())

    private val REFRESH_TIME = 20_000L

    private var currentPage = "LOGIN"

    private val refreshRunnable = object : Runnable {

        override fun run() {

            if (
                currentUsername.isNotEmpty() &&
                (
                    currentPage == "DASHBOARD" ||
                    currentPage == "TOTAL" ||
                    currentPage == "DOCTOR" ||
                    currentPage == "CARE"
                )
            ) {

                when (currentPage) {

                    "DASHBOARD" -> showDashboard()

                    "TOTAL" -> showTotalSerial()

                    "DOCTOR" -> showDoctorWise()

                    "CARE" -> showCareWise()
                }

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

    private data class SerialRecord(

        val id: String,

        val date: String,

        val totalNumber: Int,

        val doctorNumber: Int,

        val careNumber: Int,

        val patient: String,

        val careOf: String,

        val doctor: String,

        val status: String,

        val createdBy: String,

        val createdRole: String,

        val createdTime: String
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

    private fun rounded(
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

        val root = LinearLayout(this)

        root.orientation =
            LinearLayout.VERTICAL

        root.gravity =
            Gravity.CENTER_HORIZONTAL

        root.setBackgroundColor(BG)

        root.setPadding(
            14,
            18,
            14,
            30
        )

        return root
    }

    // =========================================================
    // SCROLL
    // =========================================================

    private fun screen(
        view: View
    ): ScrollView {

        val scroll = ScrollView(this)

        scroll.setBackgroundColor(BG)
        scroll.isFillViewport = true

        scroll.addView(view)

        return scroll
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
    // LARGE BUTTON
    // =========================================================

    private fun bigButton(
        icon: String,
        title: String,
        color: Int,
        onClick: () -> Unit
    ): LinearLayout {

        val box =
            LinearLayout(this)

        box.orientation =
            LinearLayout.VERTICAL

        box.gravity =
            Gravity.CENTER

        box.setPadding(
            8,
            12,
            8,
            12
        )

        box.background =
            rounded(
                WHITE,
                20f,
                BORDER
            )

        box.elevation = 5f

        val iconView =
            text(
                icon,
                40f,
                color,
                true
            )

        val titleView =
            text(
                title,
                17f,
                DARK_BLUE,
                true
            )

        box.addView(iconView)
        box.addView(titleView)

        box.setOnClickListener {
            onClick()
        }

        return box
    }

    // =========================================================
    // ACTION BUTTON
    // =========================================================

    private fun actionButton(
        title: String,
        color: Int,
        onClick: () -> Unit
    ): TextView {

        val b =
            text(
                title,
                17f,
                WHITE,
                true
            )

        b.background =
            rounded(
                color,
                15f
            )

        b.elevation = 4f

        val params =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                68
            )

        params.setMargins(
            7,
            6,
            7,
            6
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
            rounded(
                WHITE,
                15f,
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
                68
            )

        params.setMargins(
            7,
            7,
            7,
            7
        )

        e.layoutParams = params

        return e
    }

    // =========================================================
    // LOGIN
    // =========================================================

    private fun showLogin() {

        currentPage = "LOGIN"

        handler.removeCallbacks(
            refreshRunnable
        )

        val root =
            rootLayout()

        root.gravity =
            Gravity.CENTER_HORIZONTAL

        root.addView(space(45))

        root.addView(
            text(
                "🏥",
                62f,
                BLUE,
                true
            )
        )

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
                30f,
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

        root.addView(space(22))

        val card =
            LinearLayout(this)

        card.orientation =
            LinearLayout.VERTICAL

        card.setPadding(
            16,
            25,
            16,
            28
        )

        card.background =
            rounded(
                WHITE,
                22f,
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
                "🔐  লগইন করুন",
                31f,
                DARK_BLUE,
                true
            )
        )

        card.addView(space(15))

        val username =
            input(
                "👤  ইউজারনেম"
            )

        val password =
            input(
                "🔑  পাসওয়ার্ড",
                true
            )

        card.addView(username)
        card.addView(password)

        card.addView(space(12))

        card.addView(
            actionButton(
                "🔐   LOGIN",
                BLUE
            ) {

                login(
                    username.text.toString().trim(),
                    password.text.toString()
                )
            }
        )

        root.addView(space(25))

        root.addView(
            text(
                "Admin অনুমোদন ছাড়া User / Operator অ্যাপ ব্যবহার করতে পারবে না",
                15f,
                GRAY,
                true
            )
        )

        root.addView(space(20))

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
            screen(root)
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

        val savedRole =
            pref.getString(
                "role_$username",
                null
            )

        if (
            savedUser != null &&
            savedPass != null &&
            savedRole != null &&
            savedPass == hashPassword(password)
        ) {

            currentUsername = username
            currentRole = savedRole

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

        currentPage = "DASHBOARD"

        handler.removeCallbacks(
            refreshRunnable
        )

        val root =
            rootLayout()

        root.addView(
            text(
                "🏥",
                55f,
                BLUE,
                true
            )
        )

        root.addView(
            text(
                "MDC",
                50f,
                BLUE,
                true
            )
        )

        root.addView(
            text(
                "মুন ডায়াগনস্টিক সেন্টার",
                28f,
                DARK_BLUE,
                true
            )
        )

        root.addView(space(8))

        root.addView(
            text(
                "স্বাগতম, $currentUsername",
                24f,
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

        root.addView(space(12))

        root.addView(
            actionButton(
                "🚪   LOGOUT",
                RED
            ) {
                logout()
            }
        )

        root.addView(space(16))

        val today =
            todayKey()

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

        root.addView(
            text(
                "📊  আজকের পরিসংখ্যান",
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
            statCard(
                "📋",
                "মোট সিরিয়াল",
                records.size.toString(),
                BLUE
            )
        )

        row1.addView(
            statCard(
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
            statCard(
                "✅",
                "সম্পন্ন",
                completed.toString(),
                GREEN
            )
        )

        row2.addView(
            statCard(
                "❌",
                "বাতিল",
                cancelled.toString(),
                RED
            )
        )

        root.addView(row2)

        root.addView(space(20))

        root.addView(
            text(
                "⚡  দ্রুত অ্যাকশন",
                27f,
                DARK_BLUE,
                true
            )
        )

        root.addView(space(8))

        val actionRow1 =
            LinearLayout(this)

        actionRow1.orientation =
            LinearLayout.HORIZONTAL

        actionRow1.addView(
            bigButton(
                "📋",
                "টোটাল সিরিয়াল",
                BLUE
            ) {
                showTotalSerial()
            },
            gridParams()
        )

        actionRow1.addView(
            bigButton(
                "➕",
                "অ্যাড সিরিয়াল",
                GREEN
            ) {
                showAddSerial()
            },
            gridParams()
        )

        root.addView(actionRow1)

        val actionRow2 =
            LinearLayout(this)

        actionRow2.orientation =
            LinearLayout.HORIZONTAL

        actionRow2.addView(
            bigButton(
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

                    showDoctorManager()

                } else {

                    toast(
                        "শুধুমাত্র Admin ডাক্তার Add করতে পারবেন"
                    )
                }

            },
            gridParams()
        )

        actionRow2.addView(
            bigButton(
                "👤",
                "অ্যাড কেয়ার অফ",
                TEAL
            ) {
                showCareManager()
            },
            gridParams()
        )

        root.addView(actionRow2)

        root.addView(space(20))

        root.addView(
            actionButton(
                "👨‍⚕️   ডাক্তার ওয়াইজ সিরিয়াল",
                BLUE
            ) {
                showDoctorWise()
            }
        )

        root.addView(
            actionButton(
                "👤   কেয়ার ওয়াইজ সিরিয়াল",
                TEAL
            ) {
                showCareWise()
            }
        )

        root.addView(space(20))

        if (
            currentRole.equals(
                "Admin",
                true
            )
        ) {

            root.addView(
                actionButton(
                    "👑   ADMIN CONTROL PANEL",
                    PURPLE
                ) {
                    showAdminPanel()
                }
            )

            root.addView(space(8))
        }

        root.addView(
            text(
                "🔄 ডাটা প্রতি ২০ সেকেন্ড পর পর Auto Refresh হবে",
                15f,
                TEAL,
                true
            )
        )

        root.addView(space(18))

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
            screen(root)
        )

        handler.postDelayed(
            refreshRunnable,
            REFRESH_TIME
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
            18,
            8,
            18
        )

        card.background =
            rounded(
                WHITE,
                20f,
                BORDER
            )

        card.elevation = 5f

        card.layoutParams =
            LinearLayout.LayoutParams(
                0,
                160,
                1f
            ).apply {

                setMargins(
                    5,
                    5,
                    5,
                    5
                )
            }

        card.addView(
            text(
                icon,
                43f,
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
    // GRID
    // =========================================================

    private fun gridParams():
        LinearLayout.LayoutParams {

        return LinearLayout.LayoutParams(
            0,
            190,
            1f
        ).apply {

            setMargins(
                6,
                6,
                6,
                6
            )
        }
    }

    // =========================================================
    // ADD SERIAL
    // =========================================================

    private fun showAddSerial() {

        currentPage = "ADD_SERIAL"

        handler.removeCallbacks(
            refreshRunnable
        )

        val root =
            rootLayout()

        root.addView(
            text(
                "➕",
                55f,
                GREEN,
                true
            )
        )

        root.addView(
            text(
                "নতুন সিরিয়াল",
                30f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            text(
                "রোগীর তথ্য দিয়ে সিরিয়াল তৈরি করুন",
                16f,
                GRAY
            )
        )

        root.addView(space(15))

        val card =
            LinearLayout(this)

        card.orientation =
            LinearLayout.VERTICAL

        card.setPadding(
            16,
            22,
            16,
            25
        )

        card.background =
            rounded(
                WHITE,
                22f,
                BORDER
            )

        card.elevation = 6f

        root.addView(card)

        card.addView(
            text(
                "📅  সিরিয়ালের তারিখ",
                19f,
                DARK_BLUE,
                true
            )
        )

        val dateButton =
            text(
                "তারিখ নির্বাচন করুন",
                18f,
                DARK,
                true
            )

        dateButton.background =
            rounded(
                Color.rgb(
                    237,
                    247,
                    255
                ),
                15f,
                BLUE
            )

        val selectedDate =
            Calendar.getInstance()

        dateButton.text =
            formatDateForUser(
                selectedDate
            )

        dateButton.setOnClickListener {

            DatePickerDialog(
                this,
                { _, year, month, day ->

                    selectedDate.set(
                        year,
                        month,
                        day
                    )

                    dateButton.text =
                        formatDateForUser(
                            selectedDate
                        )
                },
                selectedDate.get(
                    Calendar.YEAR
                ),
                selectedDate.get(
                    Calendar.MONTH
                ),
                selectedDate.get(
                    Calendar.DAY_OF_MONTH
                )
            ).show()
        }

        card.addView(
            dateButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                70
            ).apply {

                setMargins(
                    7,
                    7,
                    7,
                    12
                )
            }
        )

        card.addView(
            text(
                "👤  রোগীর নাম",
                19f,
                DARK_BLUE,
                true
            )
        )

        val patient =
            input(
                "রোগীর নাম লিখুন"
            )

        card.addView(patient)

        card.addView(
            text(
                "👥  Care Of",
                19f,
                DARK_BLUE,
                true
            )
        )

        val careInput =
            input(
                "Care Of নাম লিখুন"
            )

        card.addView(careInput)

        val careSpinner =
            Spinner(this)

        setupSpinner(
            careSpinner,
            getCareList(),
            "Care Of নির্বাচন করুন"
        )

        card.addView(
            careSpinner,
            spinnerParams()
        )

        careSpinner.onItemSelectedListener =
            object :
                android.widget.AdapterView.OnItemSelectedListener {

                override fun onNothingSelected(
                    parent: android.widget.AdapterView<*>?
                ) {
                }

                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    if (position > 0) {

                        careInput.setText(
                            careSpinner.selectedItem.toString()
                        )
                    }
                }
            }

        card.addView(
            text(
                "👨‍⚕️  ডাক্তার",
                19f,
                DARK_BLUE,
                true
            )
        )

        val doctorInput =
            input(
                "ডাক্তারের নাম লিখুন"
            )

        card.addView(doctorInput)

        val doctorSpinner =
            Spinner(this)

        setupSpinner(
            doctorSpinner,
            getDoctorList(),
            "ডাক্তার নির্বাচন করুন"
        )

        card.addView(
            doctorSpinner,
            spinnerParams()
        )

        doctorSpinner.onItemSelectedListener =
            object :
                android.widget.AdapterView.OnItemSelectedListener {

                override fun onNothingSelected(
                    parent: android.widget.AdapterView<*>?
                ) {
                }

                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    if (position > 0) {

                        doctorInput.setText(
                            doctorSpinner.selectedItem.toString()
                        )
                    }
                }
            }

        card.addView(space(12))

        card.addView(
            text(
                "এই সিরিয়াল তৈরি করবে:",
                15f,
                GRAY
            )
        )

        card.addView(
            text(
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
                GREEN
            ) {

                val patientName =
                    patient.text.toString().trim()

                val care =
                    careInput.text.toString().trim()

                val doctor =
                    doctorInput.text.toString().trim()

                if (patientName.isEmpty()) {

                    toast(
                        "রোগীর নাম লিখুন"
                    )

                    return@actionButton
                }

                if (care.isEmpty()) {

                    toast(
                        "Care Of নির্বাচন বা লিখুন"
                    )

                    return@actionButton
                }

                if (doctor.isEmpty()) {

                    toast(
                        "ডাক্তার নির্বাচন বা লিখুন"
                    )

                    return@actionButton
                }

                saveSerial(
                    selectedDate,
                    patientName,
                    care,
                    doctor
                )
            }
        )

        root.addView(space(15))

        root.addView(
            actionButton(
                "←   Dashboard",
                BLUE
            ) {
                showDashboard()
            }
        )

        setContentView(
            screen(root)
        )
    }

    // =========================================================
    // SAVE SERIAL
    // =========================================================

    private fun saveSerial(
        date: Calendar,
        patient: String,
        care: String,
        doctor: String
    ) {

        val dateKey =
            SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            ).format(date.time)

        val records =
            readSerials(dateKey)

        val totalNumber =
            if (records.isEmpty()) {
                1
            } else {
                records.maxOf {
                    it.totalNumber
                } + 1
            }

        val doctorRecords =
            records.filter {
                it.doctor.equals(
                    doctor,
                    true
                )
            }

        val doctorNumber =
            if (doctorRecords.isEmpty()) {
                1
            } else {
                doctorRecords.maxOf {
                    it.doctorNumber
                } + 1
            }

        val careRecords =
            records.filter {
                it.careOf.equals(
                    care,
                    true
                )
            }

        val careNumber =
            if (careRecords.isEmpty()) {
                1
            } else {
                careRecords.maxOf {
                    it.careNumber
                } + 1
            }

        val id =
            "SERIAL_" +
            System.currentTimeMillis()

        val raw =
            listOf(
                dateKey,
                totalNumber,
                doctorNumber,
                careNumber,
                patient,
                care,
                doctor,
                "Waiting",
                currentUsername,
                currentRole,
                currentTime()
            ).joinToString(
                "|||"
            )

        pref.edit()
            .putString(
                id,
                raw
            )
            .apply()

        toast(
            "Serial #$totalNumber তৈরি হয়েছে"
        )

        showTotalSerial()
    }

    // =========================================================
    // TOTAL SERIAL
    // =========================================================

    private fun showTotalSerial() {

        currentPage = "TOTAL"

        handler.removeCallbacks(
            refreshRunnable
        )

        val root =
            rootLayout()

        root.addView(
            text(
                "📋",
                55f,
                BLUE,
                true
            )
        )

        root.addView(
            text(
                "টোটাল সিরিয়াল",
                30f,
                DARK_BLUE,
                true
            )
        )

        root.addView(space(10))

        val date =
            Calendar.getInstance()

        val dateButton =
            text(
                formatDateForUser(date),
                20f,
                DARK_BLUE,
                true
            )

        dateButton.background =
            rounded(
                WHITE,
                16f,
                BLUE
            )

        root.addView(
            dateButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                70
            ).apply {

                setMargins(
                    7,
                    7,
                    7,
                    12
                )
            }
        )

        val listContainer =
            LinearLayout(this)

        listContainer.orientation =
            LinearLayout.VERTICAL

        root.addView(listContainer)

        fun loadList() {

            listContainer.removeAllViews()

            val key =
                SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
                ).format(date.time)

            val records =
                readSerials(key)

            listContainer.addView(
                text(
                    "📅 ${formatDateForUser(date)}   •   মোট ${records.size} জন",
                    19f,
                    TEAL,
                    true
                )
            )

            if (records.isEmpty()) {

                listContainer.addView(
                    space(20)
                )

                listContainer.addView(
                    text(
                        "এই তারিখে কোনো সিরিয়াল নেই",
                        18f,
                        GRAY
                    )
                )

            } else {

                records.forEach {

                    listContainer.addView(
                        serialCard(it)
                    )
                }
            }
        }

        dateButton.setOnClickListener {

            DatePickerDialog(
                this,
                { _, year, month, day ->

                    date.set(
                        year,
                        month,
                        day
                    )

                    dateButton.text =
                        formatDateForUser(date)

                    loadList()
                },
                date.get(
                    Calendar.YEAR
                ),
                date.get(
                    Calendar.MONTH
                ),
                date.get(
                    Calendar.DAY_OF_MONTH
                )
            ).show()
        }

        loadList()

        root.addView(space(15))

        root.addView(
            actionButton(
                "➕   নতুন সিরিয়াল",
                GREEN
            ) {
                showAddSerial()
            }
        )

        root.addView(
            actionButton(
                "←   Dashboard",
                BLUE
            ) {
                showDashboard()
            }
        )

        setContentView(
            screen(root)
        )

        handler.postDelayed(
            refreshRunnable,
            REFRESH_TIME
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
            18
        )

        card.background =
            rounded(
                WHITE,
                20f,
                BORDER
            )

        card.elevation = 5f

        card.layoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {

                setMargins(
                    5,
                    7,
                    5,
                    7
                )
            }

        card.addView(
            text(
                "🔢 মোট সিরিয়াল #${r.totalNumber}",
                22f,
                BLUE,
                true
            )
        )

        card.addView(
            text(
                "👨‍⚕️ ${r.doctor}  •  ডাক্তার সিরিয়াল #${r.doctorNumber}",
                17f,
                DARK_BLUE,
                true
            )
        )

        card.addView(
            text(
                "👤 ${r.careOf}  •  Care Serial #${r.careNumber}",
                17f,
                TEAL,
                true
            )
        )

        card.addView(
            text(
                "🧑 রোগী: ${r.patient}",
                20f,
                DARK,
                true
            )
        )

        card.addView(
            text(
                "✍ সিরিয়াল দিয়েছেন: ${r.createdBy} (${r.createdRole})",
                16f,
                PURPLE,
                true
            )
        )

        card.addView(
            text(
                "🕐 ${r.createdTime}",
                14f,
                GRAY
            )
        )

        val statusColor =
            when (r.status) {

                "Completed" -> GREEN

                "Cancelled" -> RED

                else -> ORANGE
            }

        val statusText =
            when (r.status) {

                "Completed" ->
                    "✅ সম্পন্ন"

                "Cancelled" ->
                    "❌ বাতিল"

                else ->
                    "⏳ অপেক্ষমাণ"
            }

        card.addView(
            text(
                statusText,
                18f,
                statusColor,
                true
            )
        )

        // =====================================================
        // OWN EDIT DELETE
        // =====================================================

        if (
            r.createdBy.equals(
                currentUsername,
                true
            )
        ) {

            val ownRow =
                LinearLayout(this)

            ownRow.orientation =
                LinearLayout.HORIZONTAL

            ownRow.addView(
                smallButton(
                    "✏️ Edit",
                    BLUE
                ) {
                    showEditSerial(r)
                },
                smallParams()
            )

            ownRow.addView(
                smallButton(
                    "🗑️ Delete",
                    RED
                ) {
                    deleteSerial(r)
                },
                smallParams()
            )

            card.addView(ownRow)
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

            val completeRow =
                LinearLayout(this)

            completeRow.orientation =
                LinearLayout.HORIZONTAL

            if (
                r.status != "Completed"
            ) {

                completeRow.addView(
                    smallButton(
                        "✅ সম্পন্ন করুন",
                        GREEN
                    ) {

                        updateStatus(
                            r.id,
                            "Completed"
                        )

                    },
                    smallParams()
                )

            } else {

                completeRow.addView(
                    smallButton(
                        "↩ অসম্পন্ন করুন",
                        ORANGE
                    ) {

                        updateStatus(
                            r.id,
                            "Waiting"
                        )

                    },
                    smallParams()
                )
            }

            card.addView(completeRow)
        }

        return card
    }

    // =========================================================
    // SMALL BUTTON
    // =========================================================

    private fun smallButton(
        title: String,
        color: Int,
        onClick: () -> Unit
    ): TextView {

        val b =
            text(
                title,
                14f,
                WHITE,
                true
            )

        b.background =
            rounded(
                color,
                12f
            )

        b.setOnClickListener {
            onClick()
        }

        return b
    }

    private fun smallParams():
        LinearLayout.LayoutParams {

        return LinearLayout.LayoutParams(
            0,
            52,
            1f
        ).apply {

            setMargins(
                4,
                5,
                4,
                5
            )
        }
    }

    // =========================================================
    // EDIT SERIAL
    // =========================================================

    private fun showEditSerial(
        record: SerialRecord
    ) {

        if (
            !record.createdBy.equals(
                currentUsername,
                true
            )
        ) {

            toast(
                "এই Serial আপনি Edit করতে পারবেন না"
            )

            return
        }

        currentPage = "EDIT"

        handler.removeCallbacks(
            refreshRunnable
        )

        val root =
            rootLayout()

        root.addView(
            text(
                "✏️",
                55f,
                BLUE,
                true
            )
        )

        root.addView(
            text(
                "Serial Edit",
                30f,
                DARK_BLUE,
                true
            )
        )

        val patient =
            input(
                "রোগীর নাম"
            )

        patient.setText(record.patient)

        val care =
            input(
                "Care Of"
            )

        care.setText(record.careOf)

        val doctor =
            input(
                "ডাক্তার"
            )

        doctor.setText(record.doctor)

        root.addView(patient)
        root.addView(care)
        root.addView(doctor)

        root.addView(space(10))

        root.addView(
            actionButton(
                "💾   Save Changes",
                GREEN
            ) {

                updateSerial(
                    record,
                    patient.text.toString().trim(),
                    care.text.toString().trim(),
                    doctor.text.toString().trim()
                )
            }
        )

        root.addView(
            actionButton(
                "←   ফিরে যান",
                BLUE
            ) {
                showTotalSerial()
            }
        )

        setContentView(
            screen(root)
        )
    }

    // =========================================================
    // UPDATE SERIAL
    // =========================================================

    private fun updateSerial(
        record: SerialRecord,
        patient: String,
        care: String,
        doctor: String
    ) {

        if (
            !record.createdBy.equals(
                currentUsername,
                true
            )
        ) {

            toast(
                "আপনি এই Serial Edit করতে পারবেন না"
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

        val raw =
            listOf(
                record.date,
                record.totalNumber,
                record.doctorNumber,
                record.careNumber,
                patient,
                care,
                doctor,
                record.status,
                record.createdBy,
                record.createdRole,
                record.createdTime
            ).joinToString(
                "|||"
            )

        pref.edit()
            .putString(
                record.id,
                raw
            )
            .apply()

        toast(
            "Serial আপডেট হয়েছে"
        )

        showTotalSerial()
    }

    // =========================================================
    // DELETE SERIAL
    // =========================================================

    private fun deleteSerial(
        record: SerialRecord
    ) {

        if (
            !record.createdBy.equals(
                currentUsername,
                true
            )
        ) {

            toast(
                "আপনি এই Serial Delete করতে পারবেন না"
            )

            return
        }

        pref.edit()
            .remove(record.id)
            .apply()

        toast(
            "Serial Delete হয়েছে"
        )

        showTotalSerial()
    }

    // =========================================================
    // STATUS
    // =========================================================

    private fun updateStatus(
        id: String,
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
                "আপনার এই permission নেই"
            )

            return
        }

        val raw =
            pref.getString(
                id,
                null
            ) ?: return

        val parts =
            raw.split(
                "|||"
            ).toMutableList()

        if (parts.size < 11) {
            return
        }

        parts[7] = status

        pref.edit()
            .putString(
                id,
                parts.joinToString(
                    "|||"
                )
            )
            .apply()

        toast(
            if (status == "Completed") {
                "Serial সম্পন্ন হয়েছে"
            } else {
                "Serial আবার অপেক্ষমাণ হয়েছে"
            }
        )

        showTotalSerial()
    }

    // =========================================================
    // DOCTOR WISE
    // =========================================================

    private fun showDoctorWise() {

        currentPage = "DOCTOR"

        handler.removeCallbacks(
            refreshRunnable
        )

        val root =
            rootLayout()

        root.addView(
            text(
                "👨‍⚕️",
                55f,
                PURPLE,
                true
            )
        )

        root.addView(
            text(
                "ডাক্তার ওয়াইজ সিরিয়াল",
                30f,
                DARK_BLUE,
                true
            )
        )

        val date =
            Calendar.getInstance()

        val dateButton =
            text(
                formatDateForUser(date),
                20f,
                DARK_BLUE,
                true
            )

        dateButton.background =
            rounded(
                WHITE,
                16f,
                PURPLE
            )

        root.addView(
            dateButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                70
            )
        )

        val list =
            LinearLayout(this)

        list.orientation =
            LinearLayout.VERTICAL

        root.addView(list)

        fun load() {

            list.removeAllViews()

            val key =
                SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
                ).format(date.time)

            val records =
                readSerials(key)

            val groups =
                records.groupBy {
                    it.doctor
                }

            if (groups.isEmpty()) {

                list.addView(
                    text(
                        "এই তারিখে কোনো Doctor Serial নেই",
                        18f,
                        GRAY
                    )
                )

            } else {

                // FIXED KOTLIN SYNTAX
                groups.forEach { (doctor, doctorRecords) ->

                    list.addView(
                        text(
                            "👨‍⚕️ $doctor",
                            23f,
                            PURPLE,
                            true
                        )
                    )

                    doctorRecords
                        .sortedBy {
                            it.doctorNumber
                        }
                        .forEach { r ->

                            list.addView(
                                serialCard(r)
                            )
                        }
                }
            }
        }

        dateButton.setOnClickListener {

            DatePickerDialog(
                this,
                { _, year, month, day ->

                    date.set(
                        year,
                        month,
                        day
                    )

                    dateButton.text =
                        formatDateForUser(date)

                    load()
                },
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH),
                date.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        load()

        root.addView(space(15))

        root.addView(
            actionButton(
                "←   Dashboard",
                BLUE
            ) {
                showDashboard()
            }
        )

        setContentView(
            screen(root)
        )

        handler.postDelayed(
            refreshRunnable,
            REFRESH_TIME
        )
    }

    // =========================================================
    // CARE WISE
    // =========================================================

    private fun showCareWise() {

        currentPage = "CARE"

        handler.removeCallbacks(
            refreshRunnable
        )

        val root =
            rootLayout()

        root.addView(
            text(
                "👤",
                55f,
                TEAL,
                true
            )
        )

        root.addView(
            text(
                "Care Of ওয়াইজ সিরিয়াল",
                30f,
                DARK_BLUE,
                true
            )
        )

        val date =
            Calendar.getInstance()

        val dateButton =
            text(
                formatDateForUser(date),
                20f,
                DARK_BLUE,
                true
            )

        dateButton.background =
            rounded(
                WHITE,
                16f,
                TEAL
            )

        root.addView(
            dateButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                70
            )
        )

        val list =
            LinearLayout(this)

        list.orientation =
            LinearLayout.VERTICAL

        root.addView(list)

        fun load() {

            list.removeAllViews()

            val key =
                SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
                ).format(date.time)

            val records =
                readSerials(key)

            val groups =
                records.groupBy {
                    it.careOf
                }

            if (groups.isEmpty()) {

                list.addView(
                    text(
                        "এই তারিখে কোনো Care Of Serial নেই",
                        18f,
                        GRAY
                    )
                )

            } else {

                // FIXED KOTLIN SYNTAX
                groups.forEach { (care, careRecords) ->

                    list.addView(
                        text(
                            "👤 $care",
                            23f,
                            TEAL,
                            true
                        )
                    )

                    careRecords
                        .sortedBy {
                            it.careNumber
                        }
                        .forEach { r ->

                            list.addView(
                                serialCard(r)
                            )
                        }
                }
            }
        }

        dateButton.setOnClickListener {

            DatePickerDialog(
                this,
                { _, year, month, day ->

                    date.set(
                        year,
                        month,
                        day
                    )

                    dateButton.text =
                        formatDateForUser(date)

                    load()
                },
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH),
                date.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        load()

        root.addView(space(15))

        root.addView(
            actionButton(
                "←   Dashboard",
                BLUE
            ) {
                showDashboard()
            }
        )

        setContentView(
            screen(root)
        )

        handler.postDelayed(
            refreshRunnable,
            REFRESH_TIME
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
                "শুধুমাত্র Admin ডাক্তার Add করতে পারবেন"
            )

            return
        }

        currentPage =
            "DOCTOR_MANAGER"

        handler.removeCallbacks(
            refreshRunnable
        )

        val root =
            rootLayout()

        root.addView(
            text(
                "👨‍⚕️",
                55f,
                PURPLE,
                true
            )
        )

        root.addView(
            text(
                "ডাক্তার ম্যানেজমেন্ট",
                30f,
                DARK_BLUE,
                true
            )
        )

        val name =
            input(
                "ডাক্তারের নাম"
            )

        root.addView(name)

        root.addView(
            actionButton(
                "➕   ডাক্তার Add করুন",
                PURPLE
            ) {

                addDoctor(
                    name.text.toString().trim()
                )
            }
        )

        root.addView(space(18))

        root.addView(
            text(
                "বর্তমান ডাক্তার",
                23f,
                DARK_BLUE,
                true
            )
        )

        getDoctorList().forEach {

            if (it.isNotEmpty()) {

                root.addView(
                    managerRow(
                        it,
                        PURPLE
                    ) {
                        deleteDoctor(it)
                    }
                )
            }
        }

        root.addView(space(15))

        root.addView(
            actionButton(
                "←   Dashboard",
                BLUE
            ) {
                showDashboard()
            }
        )

        setContentView(
            screen(root)
        )
    }

    // =========================================================
    // ADD DOCTOR
    // =========================================================

    private fun addDoctor(
        name: String
    ) {

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

        saveList(
            "DOCTORS",
            list
        )

        toast(
            "ডাক্তার Add হয়েছে"
        )

        showDoctorManager()
    }

    // =========================================================
    // DELETE DOCTOR
    // =========================================================

    private fun deleteDoctor(
        name: String
    ) {

        if (
            !currentRole.equals(
                "Admin",
                true
            )
        ) {

            toast(
                "শুধুমাত্র Admin Delete করতে পারবেন"
            )

            return
        }

        val list =
            getDoctorList().toMutableList()

        list.removeAll {
            it.equals(
                name,
                true
            )
        }

        saveList(
            "DOCTORS",
            list
        )

        toast(
            "ডাক্তার Delete হয়েছে"
        )

        showDoctorManager()
    }

    // =========================================================
    // CARE MANAGER
    // =========================================================

    private fun showCareManager() {

        currentPage =
            "CARE_MANAGER"

        handler.removeCallbacks(
            refreshRunnable
        )

        val root =
            rootLayout()

        root.addView(
            text(
                "👤",
                55f,
                TEAL,
                true
            )
        )

        root.addView(
            text(
                "Care Of ম্যানেজমেন্ট",
                30f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            text(
                "User / Operator / Admin সবাই Care Of Add করতে পারবে
