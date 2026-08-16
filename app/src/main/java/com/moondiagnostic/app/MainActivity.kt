package com.moondiagnostic.app

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
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

class MainActivity : Activity() {

    // =========================================================
    // COLORS
    // =========================================================

    private val BG = Color.rgb(238, 247, 253)
    private val WHITE = Color.WHITE

    private val BLUE = Color.rgb(25, 91, 150)
    private val DARK_BLUE = Color.rgb(12, 54, 94)

    private val TEAL = Color.rgb(15, 137, 125)
    private val GREEN = Color.rgb(38, 142, 91)
    private val RED = Color.rgb(205, 55, 55)
    private val ORANGE = Color.rgb(225, 139, 35)
    private val PURPLE = Color.rgb(105, 75, 165)

    private val DARK = Color.rgb(35, 40, 45)
    private val GRAY = Color.rgb(100, 108, 115)
    private val BORDER = Color.rgb(190, 212, 228)

    // =========================================================
    // STORAGE
    // =========================================================

    private lateinit var pref: SharedPreferences

    private val PREF_NAME = "MDC_DATA_V2"

    private var currentUsername = ""
    private var currentRole = ""

    // =========================================================
    // PAGE
    // =========================================================

    private var currentPage = "LOGIN"

    // =========================================================
    // AUTO REFRESH
    // =========================================================

    private val handler = Handler(Looper.getMainLooper())

    private val REFRESH_TIME = 20_000L

    private val refreshRunnable = object : Runnable {

        override fun run() {

            if (currentUsername.isEmpty()) {
                return
            }

            when (currentPage) {

                "DASHBOARD" -> showDashboard()

                "TOTAL" -> showTotalSerial()

                "DOCTOR" -> showDoctorWise()

                "CARE" -> showCareWise()
            }

            if (
                currentPage == "DASHBOARD" ||
                currentPage == "TOTAL" ||
                currentPage == "DOCTOR" ||
                currentPage == "CARE"
            ) {

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
            Context.MODE_PRIVATE
        )

        createDefaultAdmin()

        val loggedIn =
            pref.getBoolean(
                "logged_in",
                false
            )

        if (loggedIn) {

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
    // DP
    // =========================================================

    private fun dp(value: Int): Int {

        return (
            value *
                resources.displayMetrics.density
            ).toInt()
    }

    // =========================================================
    // TEXT VIEW
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
            dp(10),
            dp(8),
            dp(10),
            dp(8)
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
    // ROUNDED BACKGROUND
    // =========================================================

    private fun rounded(
        color: Int,
        radius: Float = 18f,
        stroke: Int? = null
    ): GradientDrawable {

        val d = GradientDrawable()

        d.setColor(color)

        d.cornerRadius = dp(radius.toInt()).toFloat()

        if (stroke != null) {

            d.setStroke(
                dp(2),
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
            dp(12),
            dp(18),
            dp(12),
            dp(18)
        )

        return root
    }

    // =========================================================
    // SCREEN
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
                dp(height)
            )

        return s
    }

    // =========================================================
    // HEADER
    // =========================================================

    private fun addHeader(
        root: LinearLayout,
        icon: String,
        title: String,
        subtitle: String = "",
        color: Int = BLUE
    ) {

        root.addView(
            text(
                icon,
                58f,
                color,
                true
            )
        )

        root.addView(
            text(
                title,
                30f,
                DARK_BLUE,
                true
            )
        )

        if (subtitle.isNotEmpty()) {

            root.addView(
                text(
                    subtitle,
                    17f,
                    GRAY,
                    true
                )
            )
        }
    }

    // =========================================================
    // LARGE ACTION BUTTON
    // =========================================================

    private fun actionButton(
        title: String,
        color: Int,
        onClick: () -> Unit
    ): TextView {

        val b =
            text(
                title,
                19f,
                WHITE,
                true
            )

        b.background =
            rounded(
                color,
                16f
            )

        b.elevation = dp(4).toFloat()

        b.setPadding(
            dp(15),
            dp(8),
            dp(15),
            dp(8)
        )

        b.layoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(70)
            ).apply {

                setMargins(
                    dp(5),
                    dp(6),
                    dp(5),
                    dp(6)
                )
            }

        b.setOnClickListener {

            onClick()
        }

        return b
    }

    // =========================================================
    // LARGE DASHBOARD BOX
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
            dp(8),
            dp(12),
            dp(8),
            dp(12)
        )

        box.background =
            rounded(
                WHITE,
                20f,
                BORDER
            )

        box.elevation =
            dp(5).toFloat()

        val iconView =
            text(
                icon,
                45f,
                color,
                true
            )

        val titleView =
            text(
                title,
                18f,
                DARK_BLUE,
                true
            )

        box.addView(
            iconView
        )

        box.addView(
            titleView
        )

        box.setOnClickListener {

            onClick()
        }

        return box
    }

    private fun gridParams():
        LinearLayout.LayoutParams {

        return LinearLayout.LayoutParams(
            0,
            dp(185),
            1f
        ).apply {

            setMargins(
                dp(6),
                dp(6),
                dp(6),
                dp(6)
            )
        }
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
            dp(18),
            0,
            dp(18),
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

        e.layoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(68)
            ).apply {

                setMargins(
                    dp(5),
                    dp(7),
                    dp(5),
                    dp(7)
                )
            }

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

        root.addView(
            space(35)
        )

        root.addView(
            text(
                "🏥",
                78f,
                BLUE,
                true
            )
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
                17f,
                GRAY,
                true
            )
        )

        root.addView(
            space(25)
        )

        val card =
            LinearLayout(this)

        card.orientation =
            LinearLayout.VERTICAL

        card.setPadding(
            dp(18),
            dp(28),
            dp(18),
            dp(30)
        )

        card.background =
            rounded(
                WHITE,
                24f,
                BORDER
            )

        card.elevation =
            dp(8).toFloat()

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
                32f,
                DARK_BLUE,
                true
            )
        )

        card.addView(
            space(12)
        )

        card.addView(
            text(
                "Admin কর্তৃক দেওয়া Username ও Password ব্যবহার করুন",
                16f,
                GRAY,
                true
            )
        )

        card.addView(
            space(12)
        )

        val username =
            input(
                "👤  ইউজারনেম লিখুন"
            )

        val password =
            input(
                "🔑  পাসওয়ার্ড লিখুন",
                true
            )

        card.addView(
            username
        )

        card.addView(
            password
        )

        card.addView(
            space(12)
        )

        card.addView(
            actionButton(
                "🔐   LOGIN করুন",
                BLUE
            ) {

                login(
                    username.text.toString().trim(),
                    password.text.toString()
                )
            }
        )

        root.addView(
            space(25)
        )

        root.addView(
            text(
                "👑 Admin অনুমোদন ছাড়া User / Operator অ্যাপ ব্যবহার করতে পারবে না",
                16f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            space(15)
        )

        root.addView(
            text(
                "Moon Diagnostic Center",
                18f,
                GRAY,
                true
            )
        )

        root.addView(
            text(
                "আপনার বিশ্বস্ত স্বাস্থ্যসেবা কেন্দ্র",
                15f,
                GRAY
            )
        )

        root.addView(
            space(20)
        )

        setContentView(
            screen(root)
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

        addHeader(
            root,
            "🏥",
            "মুন ডায়াগনস্টিক সেন্টার",
            "Medical Serial Management System",
            BLUE
        )

        root.addView(
            space(8)
        )

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
                18f,
                TEAL,
                true
            )
        )

        root.addView(
            space(10)
        )

        root.addView(
            actionButton(
                "🚪   LOGOUT",
                RED
            ) {

                logout()
            }
        )

        root.addView(
            space(18)
        )

        root.addView(
            text(
                "📊  আজকের পরিসংখ্যান",
                27f,
                DARK_BLUE,
                true
            )
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

        root.addView(
            space(20)
        )

        root.addView(
            text(
                "⚡  দ্রুত অ্যাকশন",
                27f,
                DARK_BLUE,
                true
            )
        )

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

        root.addView(
            actionRow1
        )

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

        root.addView(
            actionRow2
        )

        root.addView(
            space(15)
        )

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
                "👤   কেয়ার অফ ওয়াইজ সিরিয়াল",
                TEAL
            ) {
                showCareWise()
            }
        )

        if (
            currentRole.equals(
                "Admin",
                true
            )
        ) {

            root.addView(
                space(8)
            )

            root.addView(
                actionButton(
                    "👑   ADMIN CONTROL PANEL",
                    PURPLE
                ) {
                    showAdminPanel()
                }
            )
        }

        root.addView(
            space(18)
        )

        root.addView(
            text(
                "🔄 ডাটা প্রতি ২০ সেকেন্ডে Auto Refresh হবে",
                15f,
                TEAL,
                true
            )
        )

        root.addView(
            space(15)
        )

        root.addView(
            text(
                "মুন ডায়াগনস্টিক সেন্টার",
                18f,
                GRAY,
                true
            )
        )

        root.addView(
            text(
                "আপনার বিশ্বস্ত স্বাস্থ্যসেবা কেন্দ্র",
                15f,
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
            dp(6),
            dp(16),
            dp(6),
            dp(16)
        )

        card.background =
            rounded(
                WHITE,
                20f,
                BORDER
            )

        card.elevation =
            dp(4).toFloat()

        card.layoutParams =
            LinearLayout.LayoutParams(
                0,
                dp(155),
                1f
            ).apply {

                setMargins(
                    dp(5),
                    dp(5),
                    dp(5),
                    dp(5)
                )
            }

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
                value,
                27f,
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

        currentPage = "ADD_SERIAL"

        handler.removeCallbacks(
            refreshRunnable
        )

        val root =
            rootLayout()

        addHeader(
            root,
            "➕",
            "নতুন সিরিয়াল",
            "রোগীর তথ্য দিয়ে নতুন সিরিয়াল তৈরি করুন",
            GREEN
        )

        root.addView(
            space(15)
        )

        val card =
            LinearLayout(this)

        card.orientation =
            LinearLayout.VERTICAL

        card.setPadding(
            dp(16),
            dp(22),
            dp(16),
            dp(25)
        )

        card.background =
            rounded(
                WHITE,
                22f,
                BORDER
            )

        card.elevation =
            dp(6).toFloat()

        root.addView(card)

        card.addView(
            text(
                "📅  সিরিয়ালের তারিখ",
                20f,
                DARK_BLUE,
                true
            )
        )

        val selectedDate =
            Calendar.getInstance()

        val dateButton =
            text(
                formatDateForUser(
                    selectedDate
                ),
                20f,
                DARK_BLUE,
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

        card.addView(
            dateButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(70)
            ).apply {

                setMargins(
                    dp(5),
                    dp(7),
                    dp(5),
                    dp(14)
                )
            }
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
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        card.addView(
            text(
                "👤  রোগীর নাম",
                20f,
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
                20f,
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
                AdapterView.OnItemSelectedListener {

                override fun onNothingSelected(
                    parent: AdapterView<*>?
                ) {
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
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
                20f,
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
                AdapterView.OnItemSelectedListener {

                override fun onNothingSelected(
                    parent: AdapterView<*>?
                ) {
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
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

        card.addView(
            space(10)
        )

        card.addView(
            text(
                "সিরিয়াল তৈরি করবে:",
                16f,
                GRAY,
                true
            )
        )

        card.addView(
            text(
                "$currentUsername  •  $currentRole",
                19f,
                TEAL,
                true
            )
        )

        card.addView(
            space(10)
        )

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

                } else if (care.isEmpty()) {

                    toast(
                        "Care Of নির্বাচন বা লিখুন"
                    )

                } else if (doctor.isEmpty()) {

                    toast(
                        "ডাক্তার নির্বাচন বা লিখুন"
                    )

                } else {

                    saveSerial(
                        selectedDate,
                        patientName,
                        care,
                        doctor
                    )
                }
            }
        )

        root.addView(
            space(15)
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

        val obj =
            JSONObject()

        obj.put(
            "date",
            dateKey
        )

        obj.put(
            "totalNumber",
            totalNumber
        )

        obj.put(
            "doctorNumber",
            doctorNumber
        )

        obj.put(
            "careNumber",
            careNumber
        )

        obj.put(
            "patient",
            patient
        )

        obj.put(
            "careOf",
            care
        )

        obj.put(
            "doctor",
            doctor
        )

        obj.put(
            "status",
            "Waiting"
        )

        obj.put(
            "createdBy",
            currentUsername
        )

        obj.put(
            "createdRole",
            currentRole
        )

        obj.put(
            "createdTime",
            currentTime()
        )

        pref.edit()
            .putString(
                id,
                obj.toString()
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

        addHeader(
            root,
            "📋",
            "টোটাল সিরিয়াল",
            "তারিখ অনুযায়ী সকল রোগীর সিরিয়াল",
            BLUE
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
                BLUE
            )

        root.addView(
            dateButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(70)
            ).apply {

                setMargins(
                    dp(5),
                    dp(15),
                    dp(5),
                    dp(10)
                )
            }
        )

        val listContainer =
            LinearLayout(this)

        listContainer.orientation =
            LinearLayout.VERTICAL

        root.addView(
            listContainer
        )

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
                    space(25)
                )

                listContainer.addView(
                    text(
                        "এই তারিখে কোনো সিরিয়াল নেই",
                        19f,
                        GRAY,
                        true
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
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH),
                date.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        loadList()

        root.addView(
            space(15)
        )

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
            dp(16),
            dp(17),
            dp(16),
            dp(18)
        )

        card.background =
            rounded(
                WHITE,
                20f,
                BORDER
            )

        card.elevation =
            dp(5).toFloat()

        card.layoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {

                setMargins(
                    dp(5),
                    dp(7),
                    dp(5),
                    dp(7)
                )
            }

        card.addView(
            text(
                "🔢  মোট সিরিয়াল #${r.totalNumber}",
                23f,
                BLUE,
                true
            )
        )

        card.addView(
            text(
                "👨‍⚕️  ${r.doctor}",
                19f,
                DARK_BLUE,
                true
            )
        )

        card.addView(
            text(
                "ডাক্তার সিরিয়াল #${r.doctorNumber}",
                17f,
                PURPLE,
                true
            )
        )

        card.addView(
            text(
                "👤  ${r.careOf}",
                19f,
                TEAL,
                true
            )
        )

        card.addView(
            text(
                "Care Serial #${r.careNumber}",
                17f,
                TEAL,
                true
            )
        )

        card.addView(
            text(
                "🧑  রোগী: ${r.patient}",
                21f,
                DARK,
                true
            )
        )

        card.addView(
            text(
                "✍  সিরিয়াল দিয়েছেন: ${r.createdBy}",
                16f,
                PURPLE,
                true
            )
        )

        card.addView(
            text(
                "Role: ${r.createdRole}   •   🕐 ${r.createdTime}",
                15f,
                GRAY,
                true
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
                    "✅  সম্পন্ন"

                "Cancelled" ->
                    "❌  বাতিল"

                else ->
                    "⏳  অপেক্ষমাণ"
            }

        card.addView(
            text(
                statusText,
                19f,
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

            val row =
                LinearLayout(this)

            row.orientation =
                LinearLayout.HORIZONTAL

            row.addView(
                smallButton(
                    "✏️  Edit",
                    BLUE
                ) {
                    showEditSerial(r)
                },
                smallParams()
            )

            row.addView(
                smallButton(
                    "🗑️  Delete",
                    RED
                ) {
                    deleteSerial(r)
                },
                smallParams()
            )

            card.addView(row)
        }

        // =====================================================
        // ADMIN / OPERATOR STATUS CONTROL
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

            val row =
                LinearLayout(this)

            row.orientation =
                LinearLayout.HORIZONTAL

            if (
                r.status != "Completed"
            ) {

                row.addView(
                    smallButton(
                        "✅  সম্পন্ন",
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

                row.addView(
                    smallButton(
                        "↩  Waiting",
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

            if (
                r.status != "Cancelled"
            ) {

                row.addView(
                    smallButton(
                        "❌  বাতিল",
                        RED
                    ) {
                        updateStatus(
                            r.id,
                            "Cancelled"
                        )
                    },
                    smallParams()
                )
            }

            card.addView(row)
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

        b.setPadding(
            dp(5),
            dp(4),
            dp(5),
            dp(4)
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
            dp(55),
            1f
        ).apply {

            setMargins(
                dp(4),
                dp(5),
                dp(4),
                dp(5)
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

        addHeader(
            root,
            "✏️",
            "Serial Edit",
            "নিজের তৈরি Serial পরিবর্তন করুন",
            BLUE
        )

        val patient =
            input(
                "রোগীর নাম"
            )

        patient.setText(
            record.patient
        )

        val care =
            input(
                "Care Of"
            )

        care.setText(
            record.careOf
        )

        val doctor =
            input(
                "ডাক্তার"
            )

        doctor.setText(
            record.doctor
        )

        root.addView(patient)
        root.addView(care)
        root.addView(doctor)

        root.addView(
            space(10)
        )

        root.addView(
            actionButton(
                "💾   পরিবর্তন Save করুন",
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

        val obj =
            JSONObject()

        obj.put(
            "date",
            record.date
        )

        obj.put(
            "totalNumber",
            record.totalNumber
        )

        obj.put(
            "doctorNumber",
            record.doctorNumber
        )

        obj.put(
            "careNumber",
            record.careNumber
        )

        obj.put(
            "patient",
            patient
        )

        obj.put(
            "careOf",
            care
        )

        obj.put(
            "doctor",
            doctor
        )

        obj.put(
            "status",
            record.status
        )

        obj.put(
            "createdBy",
            record.createdBy
        )

        obj.put(
            "createdRole",
            record.createdRole
        )

        obj.put(
            "createdTime",
            record.createdTime
        )

        pref.edit()
            .putString(
                record.id,
                obj.toString()
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
            .remove(
                record.id
            )
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

        try {

            val obj =
                JSONObject(raw)

            obj.put(
                "status",
                status
            )

            pref.edit()
                .putString(
                    id,
                    obj.toString()
                )
                .apply()

            toast(
                when (status) {

                    "Completed" ->
                        "Serial সম্পন্ন হয়েছে"

                    "Cancelled" ->
                        "Serial বাতিল হয়েছে"

                    else ->
                        "Serial আবার অপেক্ষমাণ হয়েছে"
                }
            )

            showTotalSerial()

        } catch (
            e: Exception
        ) {

            toast(
                "Serial data error"
            )
        }
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

        addHeader(
            root,
            "👨‍⚕️",
            "ডাক্তার ওয়াইজ সিরিয়াল",
            "প্রতিটি ডাক্তারের সিরিয়াল আলাদাভাবে দেখুন",
            PURPLE
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
                dp(70)
            ).apply {

                setMargins(
                    dp(5),
                    dp(15),
                    dp(5),
                    dp(10)
                )
            }
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
                        19f,
                        GRAY,
                        true
                    )
                )

            } else {

                groups
                    .toSortedMap()
                    .forEach { entry ->

                        val doctor =
                            entry.key

                        val doctorRecords =
                            entry.value

                        list.addView(
                            text(
                                "👨‍⚕️  $doctor",
                                23f,
                                PURPLE,
                                true
                            )
                        )

                        doctorRecords
                            .sortedBy {
                                it.doctorNumber
                            }
                            .forEach { record ->

                                list.addView(
                                    serialCard(record)
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

        root.addView(
            space(15)
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
    // CARE WISE
    // =========================================================

    private fun showCareWise() {

        currentPage = "CARE"

        handler.removeCallbacks(
            refreshRunnable
        )

        val root =
            rootLayout()

        addHeader(
            root,
            "👤",
            "Care Of ওয়াইজ সিরিয়াল",
            "প্রতিটি Care Of অনুযায়ী সিরিয়াল দেখুন",
            TEAL
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
                dp(70)
            ).apply {

                setMargins(
                    dp(5),
                    dp(15),
                    dp(5),
                    dp(10)
                )
            }
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
                        19f,
                        GRAY,
                        true
                    )
                )

            } else {

                groups
                    .toSortedMap()
                    .forEach { entry ->

                        val care =
                            entry.key

                        val careRecords =
                            entry.value

                        list.addView(
                            text(
                                "👤  $care",
                                23f,
                                TEAL,
                                true
                            )
                        )

                        careRecords
                            .sortedBy {
                                it.careNumber
                            }
                            .forEach { record ->

                                list.addView(
                                    serialCard(record)
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

        root.addView(
            space(15)
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
                "শুধুমাত্র Admin"
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

        addHeader(
            root,
            "👨‍⚕️",
            "ডাক্তার ম্যানেজমেন্ট",
            "শুধুমাত্র Admin ডাক্তার পরিচালনা করতে পারবেন",
            PURPLE
        )

        val name =
            input(
                "ডাক্তারের নাম লিখুন"
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

        root.addView(
            space(18)
        )

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

        root.addView(
            space(15)
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
                "শুধুমাত্র Admin"
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

        addHeader(
            root,
            "👤",
            "Care Of ম্যানেজমেন্ট",
            "User / Operator / Admin Care Of Add করতে পারবে",
            TEAL
        )

        val name =
            input(
                "Care Of নাম লিখুন"
            )

        root.addView(name)

        root.addView(
            actionButton(
                "➕   Care Of Add করুন",
                TEAL
            ) {

                addCare(
                    name.text.toString().trim()
                )
            }
        )

        root.addView(
            space(18)
        )

        root.addView(
            text(
                "বর্তমান Care Of",
                23f,
                DARK_BLUE,
                true
            )
        )

        getCareList().forEach {

            if (it.isNotEmpty()) {

                root.addView(
                    managerRow(
                        it,
                        TEAL
                    ) {
                        deleteCare(it)
                    }
                )
            }
        }

        root.addView(
            space(15)
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
    }

    // =========================================================
    // ADD CARE
    // =========================================================

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

        saveList(
            "CARES",
            list
        )

        toast(
            "Care Of Add হয়েছে"
        )

        showCareManager()
    }

    // =========================================================
    // DELETE CARE
    // =========================================================

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
                "শুধুমাত্র Admin Care Of Delete করতে পারবেন"
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

        saveList(
            "CARES",
            list
        )

        toast(
            "Care Of Delete হয়েছে"
        )

        showCareManager()
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

        currentPage = "ADMIN"

        handler.removeCallbacks(
            refreshRunnable
        )

        val root =
            rootLayout()

        addHeader(
            root,
            "👑",
            "Admin Control Panel",
            "User এবং Operator পরিচালনা করুন",
            PURPLE
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

        setupSpinner(
            spinner,
            listOf(
                "Operator",
                "User"
            ),
            "Role নির্বাচন করুন"
        )

        root.addView(
            spinner,
            spinnerParams()
        )

        root.addView(
            actionButton(
                "➕   User / Operator তৈরি করুন",
                PURPLE
            ) {

                createUser(
                    username.text.toString().trim(),
                    password.text.toString(),
                    spinner.selectedItem.toString()
                )
            }
        )

        root.addView(
            space(20)
        )

        root.addView(
            text(
                "👥  বর্তমান User / Operator",
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

                    root.addView(
                        managerRow(
                            "$user\nRole: $role",
                            PURPLE
                        ) {
                            deleteUser(user)
                        }
                    )
                }
            }
        }

        root.addView(
            space(15)
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
            username.equals(
                "admin",
                true
            )
        ) {

            toast(
                "এই Username ব্যবহার করা যাবে না"
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
    // DELETE USER
    // =========================================================

    private fun deleteUser(
        username: String
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

        if (
            username.equals(
                "admin",
                true
            )
        ) {

            toast(
                "Admin Delete করা যাবে না"
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
    // MANAGER ROW
    // =========================================================

    private fun managerRow(
        title: String,
        color: Int,
        deleteAction: () -> Unit
    ): LinearLayout {

        val row =
            LinearLayout(this)

        row.orientation =
            LinearLayout.HORIZONTAL

        row.gravity =
            Gravity.CENTER_VERTICAL

        row.setPadding(
            dp(14),
            dp(10),
            dp(10),
            dp(10)
        )

        row.background =
            rounded(
                WHITE,
                16f,
                BORDER
            )

        row.layoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(78)
            ).apply {

                setMargins(
                    dp(5),
                    dp(4),
                    dp(5),
                    dp(4)
                )
            }

        row.addView(
            text(
                title,
                17f,
                DARK,
                true
            ),
            LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1f
            )
        )

        if (
            currentRole.equals(
                "Admin",
                true
            )
        ) {

            row.addView(
                smallButton(
                    "🗑️",
                    RED
                ) {
                    deleteAction()
                },
                LinearLayout.LayoutParams(
                    dp(65),
                    dp(52)
                )
            )
        }

        return row
    }

    // =========================================================
    // LIST STORAGE
    // =========================================================

    private fun saveList(
        key: String,
        list: List<String>
    ) {

        pref.edit()
            .putString(
                key,
                list.joinToString(
                    "\n"
                )
            )
            .apply()
    }

    private fun getList(
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

        return raw
            .split("\n")
            .map {
                it.trim()
            }
            .filter {
                it.isNotEmpty()
            }
    }

    private fun getDoctorList():
        List<String> {

        return getList(
            "DOCTORS"
        )
    }

    private fun getCareList():
        List<String> {

        return getList(
            "CARES"
        )
    }

    // =========================================================
    // SPINNER
    // =========================================================

    private fun setupSpinner(
        spinner: Spinner,
        list: List<String>,
        first: String
    ) {

        val items =
            mutableListOf<String>()

        items.add(first)

        items.addAll(list)

        val adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                items
            )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spinner.adapter = adapter
    }

    private fun spinnerParams():
        LinearLayout.LayoutParams {

        return LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(68)
        ).apply {

            setMargins(
                dp(5),
                dp(7),
                dp(5),
                dp(12)
            )
        }
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

            if (
                !key.startsWith(
                    "SERIAL_"
                )
            ) {
                continue
            }

            val raw =
                pref.getString(
                    key,
                    null
                ) ?: continue

            try {

                val obj =
                    JSONObject(raw)

                if (
                    obj.optString(
                        "date"
                    ) != date
                ) {
                    continue
                }

                val record =
                    SerialRecord(

                        id = key,

                        date =
                            obj.optString(
                                "date"
                            ),

                        totalNumber =
                            obj.optInt(
                                "totalNumber",
                                0
                            ),

                        doctorNumber =
                            obj.optInt(
                                "doctorNumber",
                                0
                            ),

                        careNumber =
                            obj.optInt(
                                "careNumber",
                                0
                            ),

                        patient =
                            obj.optString(
                                "patient"
                            ),

                        careOf =
                            obj.optString(
                                "careOf"
                            ),

                        doctor =
                            obj.optString(
                                "doctor"
                            ),

                        status =
                            obj.optString(
                                "status",
                                "Waiting"
                            ),

                        createdBy =
                            obj.optString(
                                "createdBy"
                            ),

                        createdRole =
                            obj.optString(
                                "createdRole"
                            ),

                        createdTime =
                            obj.optString(
                                "createdTime"
                            )
                    )

                list.add(record)

            } catch (
                e: Exception
            ) {

                // পুরনো/ভাঙা record হলে skip করবে
            }
        }

        return list.sortedBy {
            it.totalNumber
        }
    }

    // =========================================================
    // DATE
    // =========================================================

    private fun todayKey():
        String {

        return SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        ).format(
            Date()
        )
    }

    private fun formatDateForUser(
        calendar: Calendar
    ): String {

        return SimpleDateFormat(
            "dd-MM-yyyy",
            Locale.getDefault()
        ).format(
            calendar.time
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
                        password.toByteArray(
                            Charsets.UTF_8
                        )
                    )

            bytes.joinToString("") {

                "%02x".format(
                    it
                )
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
    // LOGOUT
    // =========================================================

    private fun logout() {

        handler.removeCallbacks(
            refreshRunnable
        )

        currentUsername = ""

        currentRole = ""

        currentPage = "LOGIN"

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

        toast(
            "Logout হয়েছে"
        )

        showLogin()
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
            currentUsername.isNotEmpty() &&
            (
                currentPage == "DASHBOARD" ||
                currentPage == "TOTAL" ||
                currentPage == "DOCTOR" ||
                currentPage == "CARE"
            )
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

    @Suppress("DEPRECATION")
    override fun onBackPressed() {

        when (currentPage) {

            "LOGIN" -> {

                super.onBackPressed()
            }

            "DASHBOARD" -> {

                super.onBackPressed()
            }

            else -> {

                showDashboard()
            }
        }
    }
}
