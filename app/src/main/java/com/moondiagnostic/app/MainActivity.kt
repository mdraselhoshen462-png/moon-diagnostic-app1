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
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {

    // =========================================================
    // COLORS
    // =========================================================

    private val BG = Color.rgb(241, 248, 253)
    private val BLUE = Color.rgb(27, 91, 148)
    private val DARK_BLUE = Color.rgb(18, 65, 108)
    private val TEAL = Color.rgb(15, 137, 128)
    private val GREEN = Color.rgb(35, 143, 91)
    private val RED = Color.rgb(198, 55, 55)
    private val ORANGE = Color.rgb(225, 143, 37)
    private val PURPLE = Color.rgb(105, 77, 165)
    private val WHITE = Color.WHITE
    private val DARK = Color.rgb(42, 48, 54)
    private val GRAY = Color.rgb(105, 112, 118)
    private val BORDER = Color.rgb(202, 218, 231)

    // =========================================================
    // STORAGE
    // =========================================================

    private lateinit var pref: SharedPreferences

    private val PREF_NAME = "MDC_DATA"

    private var currentUsername = ""
    private var currentRole = ""

    // =========================================================
    // REFRESH
    // =========================================================

    private val handler = Handler(Looper.getMainLooper())

    private val refreshInterval = 20_000L

    private var dashboardVisible = false

    private var formOpen = false

    private val refreshRunnable = object : Runnable {
        override fun run() {

            if (
                dashboardVisible &&
                currentUsername.isNotEmpty() &&
                !formOpen
            ) {

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
        val totalNumber: Int,
        val doctorNumber: Int,
        val careNumber: Int,
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

        val logged =
            pref.getBoolean(
                "logged_in",
                false
            )

        if (logged) {

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
        content: View
    ): ScrollView {

        val s = ScrollView(this)

        s.setBackgroundColor(BG)

        s.isFillViewport = true

        s.addView(content)

        return s
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

        d.cornerRadius =
            radius

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
        title: String,
        color: Int,
        onClick: () -> Unit
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

        b.elevation = 5f

        b.setPadding(
            10,
            8,
            10,
            8
        )

        val p =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                70
            )

        p.setMargins(
            6,
            6,
            6,
            6
        )

        b.layoutParams = p

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
    // LOGIN
    // =========================================================

    private fun showLogin() {

        dashboardVisible = false
        formOpen = false

        handler.removeCallbacks(
            refreshRunnable
        )

        val root =
            rootLayout()

        root.gravity =
            Gravity.CENTER_HORIZONTAL

        root.addView(space(35))

        root.addView(
            text(
                "MDC",
                68f,
                BLUE,
                true
            )
        )

        root.addView(space(5))

        root.addView(
            text(
                "মুন ডায়াগনস্টিক সেন্টার",
                31f,
                DARK_BLUE,
                true
            )
        )

        root.addView(space(4))

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

        card.gravity =
            Gravity.CENTER_HORIZONTAL

        card.setPadding(
            18,
            25,
            18,
            28
        )

        card.background =
            bg(
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
                "🔐",
                48f,
                BLUE,
                true
            )
        )

        card.addView(space(4))

        card.addView(
            text(
                "লগইন করুন",
                32f,
                DARK_BLUE,
                true
            )
        )

        card.addView(space(12))

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

        card.addView(space(10))

        card.addView(
            bigButton(
                "🔓   লগইন করুন",
                BLUE
            ) {

                login(
                    username.text
                        .toString()
                        .trim(),

                    password.text
                        .toString()
                )
            }
        )

        root.addView(space(22))

        root.addView(
            text(
                "শুধুমাত্র অনুমোদিত User / Operator / Admin",
                15f,
                GRAY,
                true
            )
        )

        root.addView(space(18))

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
        formOpen = false

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
                28f,
                DARK_BLUE,
                true
            )
        )

        root.addView(space(8))

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

        root.addView(space(10))

        root.addView(
            bigButton(
                "🚪   Logout",
                RED
            ) {
                logout()
            }
        )

        root.addView(space(14))

        // =====================================================
        // STATISTICS
        // =====================================================

        val records =
            readSerials(
                today()
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

        val statRow1 =
            LinearLayout(this)

        statRow1.orientation =
            LinearLayout.HORIZONTAL

        statRow1.addView(
            statCard(
                "📋",
                "মোট সিরিয়াল",
                "${records.size}",
                BLUE
            )
        )

        statRow1.addView(
            statCard(
                "⏳",
                "অপেক্ষমাণ",
                "$waiting",
                ORANGE
            )
        )

        root.addView(
            statRow1
        )

        val statRow2 =
            LinearLayout(this)

        statRow2.orientation =
            LinearLayout.HORIZONTAL

        statRow2.addView(
            statCard(
                "✅",
                "সম্পন্ন",
                "$completed",
                GREEN
            )
        )

        statRow2.addView(
            statCard(
                "❌",
                "বাতিল",
                "$cancelled",
                RED
            )
        )

        root.addView(
            statRow2
        )

        root.addView(space(18))

        root.addView(
            text(
                "⚡ দ্রুত অ্যাকশন",
                27f,
                DARK_BLUE,
                true
            )
        )

        root.addView(space(8))

        // =====================================================
        // QUICK ACTION 2 x 2
        // =====================================================

        val row1 =
            LinearLayout(this)

        row1.orientation =
            LinearLayout.HORIZONTAL

        row1.addView(
            dashboardTile(
                "📋",
                "টোটাল সিরিয়াল",
                BLUE
            ) {
                showTotalSerial()
            }
        )

        row1.addView(
            dashboardTile(
                "➕",
                "অ্যাড সিরিয়াল",
                GREEN
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
            dashboardTile(
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
                        "শুধুমাত্র Admin ডাক্তার যোগ করতে পারবেন"
                    )
                }
            }
        )

        row2.addView(
            dashboardTile(
                "👤",
                "অ্যাড কেয়ার অফ",
                TEAL
            ) {
                showCareManager()
            }
        )

        root.addView(row2)

        root.addView(space(18))

        // =====================================================
        // DOCTOR WISE
        // =====================================================

        root.addView(
            text(
                "👨‍⚕️ ডাক্তার ওয়াইজ সিরিয়াল",
                25f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            bigButton(
                "🔎   ডাক্তার অনুযায়ী সিরিয়াল দেখুন",
                BLUE
            ) {
                showDoctorWise()
            }
        )

        root.addView(space(10))

        // =====================================================
        // CARE WISE
        // =====================================================

        root.addView(
            text(
                "👤 কেয়ার অফ ওয়াইজ সিরিয়াল",
                25f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            bigButton(
                "🔎   কেয়ার অফ অনুযায়ী সিরিয়াল দেখুন",
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

            root.addView(space(18))

            root.addView(
                text(
                    "👑 Admin Control",
                    25f,
                    PURPLE,
                    true
                )
            )

            root.addView(
                bigButton(
                    "⚙️   User / Operator Management",
                    PURPLE
                ) {
                    showAdminPanel()
                }
            )
        }

        root.addView(space(20))

        root.addView(
            text(
                "🔄 ডাটা প্রতি ২০ সেকেন্ডে অটো রিফ্রেশ হবে",
                15f,
                TEAL,
                true
            )
        )

        root.addView(space(16))

        root.addView(
            text(
                "মুন ডায়াগনস্টিক সেন্টার",
                16f,
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
            10,
            16,
            10,
            16
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
                145,
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
                38f,
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
                23f,
                color,
                true
            )
        )

        return card
    }

    // =========================================================
    // DASHBOARD TILE
    // =========================================================

    private fun dashboardTile(
        icon: String,
        title: String,
        color: Int,
        click: () -> Unit
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
            bg(
                WHITE,
                22f,
                BORDER
            )

        card.elevation = 7f

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
                58f,
                color,
                true
            )
        )

        card.addView(space(5))

        card.addView(
            text(
                title,
                19f,
                DARK_BLUE,
                true
            )
        )

        card.setOnClickListener {
            click()
        }

        return card
    }

    // =========================================================
    // ADD SERIAL
    // =========================================================

    private fun showAddSerial(
        editId: String? = null
    ) {

        formOpen = true

        val root =
            rootLayout()

        val editing =
            editId != null

        root.addView(
            text(
                if (editing)
                    "✏️ সিরিয়াল এডিট"
                else
                    "➕ নতুন সিরিয়াল",

                30f,
                DARK_BLUE,
                true
            )
        )

        root.addView(space(10))

        var oldRecord: SerialRecord? = null

        if (editing) {
            oldRecord =
                findSerial(editId!!)
        }

        val dateButton =
            text(
                "📅   তারিখ নির্বাচন করুন",
                18f,
                DARK_BLUE,
                true
            )

        dateButton.background =
            bg(
                WHITE,
                16f,
                TEAL
            )

        dateButton.setPadding(
            15,
            0,
            15,
            0
        )

        val dateParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                68
            )

        dateParams.setMargins(
            6,
            6,
            6,
            6
        )

        root.addView(
            dateButton,
            dateParams
        )

        var selectedDate =
            oldRecord?.date
                ?: today()

        dateButton.text =
            "📅   তারিখ: $selectedDate"

        dateButton.setOnClickListener {

            val cal =
                Calendar.getInstance()

            DatePickerDialog(
                this,
                { _, y, m, d ->

                    selectedDate =
                        String.format(
                            Locale.getDefault(),
                            "%04d-%02d-%02d",
                            y,
                            m + 1,
                            d
                        )

                    dateButton.text =
                        "📅   তারিখ: $selectedDate"
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
            ).show()
        }

        val patient =
            input(
                "রোগীর নাম"
            )

        patient.setText(
            oldRecord?.patient ?: ""
        )

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
                "🤝 Care Of",
                17f,
                DARK_BLUE,
                true
            )
        )

        val careRow =
            LinearLayout(this)

        careRow.orientation =
            LinearLayout.HORIZONTAL

        val careInput =
            input(
                "Care Of নাম লিখুন"
            )

        careInput.setText(
            oldRecord?.careOf ?: ""
        )

        val careSpinner =
            Spinner(this)

        setupSpinner(
            careSpinner,
            getCareList()
        )

        careSpinner.setOnItemSelectedListener(
            object :
                AdapterView.OnItemSelectedListener {

                override fun onNothingSelected(
                    parent: AdapterView<*>?
                ) {}

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    if (position > 0) {

                        careInput.setText(
                            parent
                                ?.getItemAtPosition(
                                    position
                                )
                                ?.toString()
                                ?: ""
                        )
                    }
                }
            }
        )

        careRow.addView(
            careInput,
            LinearLayout.LayoutParams(
                0,
                68,
                1f
            )
        )

        careRow.addView(
            careSpinner,
            LinearLayout.LayoutParams(
                75,
                68
            )
        )

        root.addView(
            careRow
        )

        root.addView(
            text(
                "👨‍⚕️ ডাক্তার",
                17f,
                DARK_BLUE,
                true
            )
        )

        val doctorRow =
            LinearLayout(this)

        doctorRow.orientation =
            LinearLayout.HORIZONTAL

        val doctorInput =
            input(
                "ডাক্তারের নাম লিখুন"
            )

        doctorInput.setText(
            oldRecord?.doctor ?: ""
        )

        val doctorSpinner =
            Spinner(this)

        setupSpinner(
            doctorSpinner,
            getDoctorList()
        )

        doctorSpinner.setOnItemSelectedListener(
            object :
                AdapterView.OnItemSelectedListener {

                override fun onNothingSelected(
                    parent: AdapterView<*>?
                ) {}

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    if (position > 0) {

                        doctorInput.setText(
                            parent
                                ?.getItemAtPosition(
                                    position
                                )
                                ?.toString()
                                ?: ""
                        )
                    }
                }
            }
        )

        doctorRow.addView(
            doctorInput,
            LinearLayout.LayoutParams(
                0,
                68,
                1f
            )
        )

        doctorRow.addView(
            doctorSpinner,
            LinearLayout.LayoutParams(
                75,
                68
            )
        )

        root.addView(
            doctorRow
        )

        root.addView(space(10))

        root.addView(
            bigButton(
                if (editing)
                    "💾   পরিবর্তন সংরক্ষণ করুন"
                else
                    "✅   সিরিয়াল তৈরি করুন",

                GREEN
            ) {

                if (patient.text
                        .toString()
                        .trim()
                        .isEmpty()
                ) {

                    toast(
                        "রোগীর নাম লিখুন"
                    )
                    return@bigButton
                }

                if (
                    doctorInput.text
                        .toString()
                        .trim()
                        .isEmpty()
                ) {

                    toast(
                        "ডাক্তারের নাম নির্বাচন বা লিখুন"
                    )
                    return@bigButton
                }

                if (editing) {

                    updateSerial(
                        editId!!,
                        selectedDate,
                        patient.text
                            .toString()
                            .trim(),
                        careInput.text
                            .toString()
                            .trim(),
                        doctorInput.text
                            .toString()
                            .trim()
                    )

                } else {

                    addSerial(
                        selectedDate,
                        patient.text
                            .toString()
                            .trim(),
                        careInput.text
                            .toString()
                            .trim(),
                        doctorInput.text
                            .toString()
                            .trim()
                    )
                }
            }
        )

        root.addView(space(8))

        root.addView(
            bigButton(
                "←   Dashboard",
                BLUE
            ) {
                formOpen = false
                showDashboard()
            }
        )

        setContentView(
            scroll(root)
        )
    }

    // =========================================================
    // SPINNER
    // =========================================================

    private fun setupSpinner(
        spinner: Spinner,
        list: List<String>
    ) {

        val data =
            mutableListOf(
                "▼ নির্বাচন করুন"
            )

        data.addAll(list)

        val adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                data
            )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spinner.adapter =
            adapter
    }

    // =========================================================
    // ADD SERIAL
    // =========================================================

    private fun addSerial(
        date: String,
        patient: String,
        careOf: String,
        doctor: String
    ) {

        val total =
            readSerials(date).size + 1

        val doctorNumber =
            readSerials(date)
                .count {
                    it.doctor
                        .equals(
                            doctor,
                            true
                        )
                } + 1

        val careNumber =
            if (careOf.isEmpty()) {

                0

            } else {

                readSerials(date)
                    .count {
                        it.careOf
                            .equals(
                                careOf,
                                true
                            )
                    } + 1
            }

        val id =
            "SERIAL_" +
                    System.currentTimeMillis()

        val record =
            SerialRecord(
                id,
                date,
                total,
                doctorNumber,
                careNumber,
                patient,
                careOf,
                doctor,
                "Waiting",
                currentUsername,
                currentRole,
                currentTime()
            )

        saveRecord(record)

        formOpen = false

        toast(
            "সিরিয়াল #$total তৈরি হয়েছে"
        )

        showTotalSerial(date)
    }

    // =========================================================
    // SAVE RECORD
    // =========================================================

    private fun saveRecord(
        r: SerialRecord
    ) {

        val value =
            listOf(
                r.date,
                r.totalNumber,
                r.doctorNumber,
                r.careNumber,
                r.patient,
                r.careOf,
                r.doctor,
                r.status,
                r.createdBy,
                r.createdRole,
                r.createdAt
            ).joinToString("|||")

        pref.edit()
            .putString(
                "serial_${r.id}",
                value
            )
            .apply()
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
                    "serial_"
                )
            ) continue

            val id =
                key.removePrefix(
                    "serial_"
                )

            val raw =
                pref.getString(
                    key,
                    null
                ) ?: continue

            val p =
                raw.split("|||")

            if (p.size < 11)
                continue

            if (p[0] != date)
                continue

            list.add(
                SerialRecord(
                    id,
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

        return list.sortedBy {
            it.totalNumber
        }
    }

    // =========================================================
    // FIND
    // =========================================================

    private fun findSerial(
        id: String
    ): SerialRecord? {

        val all =
            pref.getString(
                "serial_$id",
                null
            ) ?: return null

        val p =
            all.split("|||")

        if (p.size < 11)
            return null

        return SerialRecord(
            id,
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
    }

    // =========================================================
    // TOTAL SERIAL
    // =========================================================

    private fun showTotalSerial(
        selectedDate: String = today()
    ) {

        formOpen = false

        val root =
            rootLayout()

        root.addView(
            text(
                "📋 মোট সিরিয়াল",
                31f,
                DARK_BLUE,
                true
            )
        )

        root.addView(space(8))

        val dateButton =
            bigButton(
                "📅   তারিখ: $selectedDate",
                BLUE
            ) {

                chooseDate(
                    selectedDate
                ) {
                    showTotalSerial(it)
                }
            }

        root.addView(
            dateButton
        )

        root.addView(space(10))

        val records =
            readSerials(
                selectedDate
            )

        root.addView(
            text(
                "মোট ${records.size} টি সিরিয়াল",
                20f,
                TEAL,
                true
            )
        )

        root.addView(space(8))

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
                    serialCard(
                        it
                    )
                )
            }
        }

        root.addView(space(15))

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

    private fun serialCard(
        r: SerialRecord
    ): LinearLayout {

        val card =
            LinearLayout(this)

        card.orientation =
            LinearLayout.VERTICAL

        card.setPadding(
            17,
            16,
            17,
            16
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
                "সিরিয়াল #${r.totalNumber}",
                24f,
                BLUE,
                true
            )
        )

        card.addView(
            text(
                "👤 রোগী: ${r.patient}",
                20f,
                DARK,
                true
            )
        )

        card.addView(
            text(
                "🤝 Care Of: ${
                    if (
                        r.careOf.isEmpty()
                    ) "—"
                    else r.careOf
                }",
                17f,
                DARK
            )
        )

        card.addView(
            text(
                "👨‍⚕️ ডাক্তার: ${r.doctor}",
                18f,
                DARK
            )
        )

        card.addView(
            text(
                "ডাক্তার সিরিয়াল: #${r.doctorNumber}",
                16f,
                PURPLE,
                true
            )
        )

        if (r.careNumber > 0) {

            card.addView(
                text(
                    "Care Of সিরিয়াল: #${r.careNumber}",
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
                "🕐 ${r.createdAt}",
                14f,
                GRAY
            )
        )

        card.addView(
            text(
                "স্ট্যাটাস: ${statusBangla(r.status)}",
                18f,
                statusColor(r.status),
                true
            )
        )

        card.addView(space(8))

        val canEditDelete =
            r.createdBy.equals(
                currentUsername,
                true
            )

        if (canEditDelete) {

            val row =
                LinearLayout(this)

            row.orientation =
                LinearLayout.HORIZONTAL

            row.addView(
                smallButton(
                    "✏️ এডিট",
                    BLUE
                ) {
                    showAddSerial(
                        r.id
                    )
                }
            )

            row.addView(
                smallButton(
                    "🗑 ডিলিট",
                    RED
                ) {
                    confirmDelete(
                        r
                    )
                }
            )

            card.addView(
                row
            )
        }

        // Operator + Admin only
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
                smallButton(
                    if (
                        r.status ==
                        "Completed"
                    )
                        "↩ অসম্পন্ন করুন"
                    else
                        "✅ সম্পন্ন করুন",

                    if (
                        r.status ==
                        "Completed"
                    )
                        ORANGE
                    else
                        GREEN
                ) {

                    toggleComplete(
                        r
                    )
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
        click: () -> Unit
    ): TextView {

        val b =
            text(
                title,
                16f,
                WHITE,
                true
            )

        b.background =
            bg(
                color,
                13f
            )

        b.setPadding(
            10,
            0,
            10,
            0
        )

        val p =
            LinearLayout.LayoutParams(
                0,
                58,
                1f
            )

        p.setMargins(
            4,
            4,
            4,
            4
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

    private fun updateSerial(
        id: String,
        date: String,
        patient: String,
        care: String,
        doctor: String
    ) {

        val old =
            findSerial(id)

        if (old == null) {
            toast("সিরিয়াল পাওয়া যায়নি")
            return
        }

        if (
            !old.createdBy.equals(
                currentUsername,
                true
            )
        ) {

            toast(
                "এই সিরিয়াল আপনি Edit করতে পারবেন না"
            )

            return
        }

        val newRecord =
            old.copy(
                date = date,
                patient = patient,
                careOf = care,
                doctor = doctor
            )

        saveRecord(
            newRecord
        )

        formOpen = false

        toast(
            "সিরিয়াল পরিবর্তন করা হয়েছে"
        )

        showTotalSerial(
            date
        )
    }

    // =========================================================
    // DELETE
    // =========================================================

    private fun confirmDelete(
        r: SerialRecord
    ) {

        if (
            !r.createdBy.equals(
                currentUsername,
                true
            )
        ) {

            toast(
                "শুধুমাত্র সিরিয়াল প্রদানকারী ব্যক্তি Delete করতে পারবেন"
            )

            return
        }

        AlertDialog.Builder(this)
            .setTitle(
                "সিরিয়াল Delete"
            )
            .setMessage(
                "সিরিয়াল #${r.totalNumber} Delete করতে চান?"
            )
            .setNegativeButton(
                "না",
                null
            )
            .setPositiveButton(
                "হ্যাঁ"
            ) { _, _ ->

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
            .show()
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
                "User সিরিয়াল Complete করতে পারবেন না"
            )

            return
        }

        val updated =
            r.copy(
                status =
                    if (
                        r.status ==
                        "Completed"
                    )
                        "Waiting"
                    else
                        "Completed"
            )

        saveRecord(
            updated
        )

        showTotalSerial(
            r.date
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
                30f,
                DARK_BLUE,
                true
            )
        )

        root.addView(space(8))

        val dateButton =
            bigButton(
                "📅   আজ: ${today()}",
                BLUE
            ) {

                chooseDate(
                    today()
                ) {

                    showDoctorWiseForDate(
                        it
                    )
                }
            }

        root.addView(
            dateButton
        )

        root.addView(space(10))

        val doctors =
            getDoctorList()

        if (doctors.isEmpty()) {

            root.addView(
                text(
                    "কোনো ডাক্তার যোগ করা হয়নি",
                    18f,
                    GRAY
                )
            )

        } else {

            doctors.forEach { doctor ->

                root.addView(
                    bigButton(
                        "👨‍⚕️   $doctor",
                        PURPLE
                    ) {

                        showDoctorSerialList(
                            doctor,
                            today()
                        )
                    }
                )
            }
        }

        root.addView(space(15))

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

    private fun showDoctorWiseForDate(
        date: String
    ) {

        val root =
            rootLayout()

        root.addView(
            text(
                "👨‍⚕️ ডাক্তার ওয়াইজ",
                30f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            text(
                "তারিখ: $date",
                18f,
                TEAL,
                true
            )
        )

        root.addView(space(10))

        getDoctorList()
            .forEach { doctor ->

                root.addView(
                    bigButton(
                        "👨‍⚕️   $doctor",
                        PURPLE
                    ) {

                        showDoctorSerialList(
                            doctor,
                            date
                        )
                    }
                )
            }

        root.addView(
            bigButton(
                "←   ফিরে যান",
                BLUE
            ) {
                showDoctorWise()
            }
        )

        setContentView(
            scroll(root)
        )
    }

    private fun showDoctorSerialList(
        doctor: String,
        date: String
    ) {

        val root =
            rootLayout()

        root.addView(
            text(
                "👨‍⚕️ $doctor",
                28f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            text(
                "তারিখ: $date",
                17f,
                TEAL,
                true
            )
        )

        val list =
            readSerials(date)
                .filter {
                    it.doctor.equals(
                        doctor,
                        true
                    )
                }

        if (list.isEmpty()) {

            root.addView(
                text(
                    "এই দিনে কোনো সিরিয়াল নেই",
                    18f,
                    GRAY
                )
            )

        } else {

            list.forEach {

                root.addView(
                    serialCard(
                        it
                    )
                )
            }
        }

        root.addView(
            bigButton(
                "←   ফিরে যান",
                BLUE
            ) {
                showDoctorWise()
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
                "🤝 Care Of ওয়াইজ সিরিয়াল",
                30f,
                DARK_BLUE,
                true
            )
        )

        root.addView(space(8))

        root.addView(
            bigButton(
                "📅   তারিখ নির্বাচন",
                BLUE
            ) {

                chooseDate(
                    today()
                ) {

                    showCareWiseForDate(
                        it
                    )
                }
            }
        )

        root.addView(space(10))

        getCareList()
            .forEach { care ->

                root.addView(
                    bigButton(
                        "🤝   $care",
                        TEAL
                    ) {

                        showCareSerialList(
                            care,
                            today()
                        )
                    }
                )
            }

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

    private fun showCareWiseForDate(
        date: String
    ) {

        val root =
            rootLayout()

        root.addView(
            text(
                "🤝 Care Of ওয়াইজ",
                30f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            text(
                "তারিখ: $date",
                18f,
                TEAL,
                true
            )
        )

        getCareList()
            .forEach { care ->

                root.addView(
                    bigButton(
                        "🤝   $care",
                        TEAL
                    ) {

                        showCareSerialList(
                            care,
                            date
                        )
                    }
                )
            }

        root.addView(
            bigButton(
                "←   ফিরে যান",
                BLUE
            ) {
                showCareWise()
            }
        )

        setContentView(
            scroll(root)
        )
    }

    private fun showCareSerialList(
        care: String,
        date: String
    ) {

        val root =
            rootLayout()

        root.addView(
            text(
                "🤝 $care",
                28f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            text(
                "তারিখ: $date",
                17f,
                TEAL,
                true
            )
        )

        val list =
            readSerials(date)
                .filter {
                    it.careOf.equals(
                        care,
                        true
                    )
                }

        list.forEach {

            root.addView(
                serialCard(
                    it
                )
            )
        }

        if (list.isEmpty()) {

            root.addView(
                text(
                    "এই দিনে কোনো সিরিয়াল নেই",
                    18f,
                    GRAY
                )
            )
        }

        root.addView(
            bigButton(
                "←   ফিরে যান",
                BLUE
            ) {
                showCareWise()
            }
        )

        setContentView(
            scroll(root)
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

        formOpen = true

        val root =
            rootLayout()

        root.addView(
            text(
                "👨‍⚕️ ডাক্তার ম্যানেজমেন্ট",
                29f,
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
            bigButton(
                "➕   ডাক্তার যোগ করুন",
                PURPLE
            ) {

                val n =
                    name.text
                        .toString()
                        .trim()

                if (n.isEmpty()) {

                    toast(
                        "ডাক্তারের নাম লিখুন"
                    )

                } else {

                    addDoctor(n)

                    name.setText("")

                    toast(
                        "ডাক্তার যোগ হয়েছে"
                    )
                }
            }
        )

        root.addView(space(15))

        getDoctorList()
            .forEach { doctor ->

                root.addView(
                    text(
                        "👨‍⚕️ $doctor",
                        18f,
                        DARK,
                        true
                    )
                )
            }

        root.addView(
            bigButton(
                "←   Dashboard",
                BLUE
            ) {

                formOpen = false
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

        val list =
            getDoctorList()
                .toMutableList()

        if (
            list.none {
                it.equals(
                    name,
                    true
                )
            }
        ) {

            list.add(name)

            pref.edit()
                .putString(
                    "doctors",
                    list.joinToString("|||")
                )
                .apply()
        }
    }

    private fun getDoctorList():
            List<String> {

        val raw =
            pref.getString(
                "doctors",
                ""
            ) ?: ""

        if (raw.isEmpty())
            return emptyList()

        return raw
            .split("|||")
            .filter {
                it.isNotBlank()
            }
    }

    // =========================================================
    // CARE MANAGER
    // =========================================================

    private fun showCareManager() {

        formOpen = true

        val root =
            rootLayout()

        root.addView(
            text(
                "🤝 Care Of ম্যানেজমেন্ট",
                29f,
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

        val name =
            input(
                "Care Of নাম"
            )

        root.addView(name)

        root.addView(
            bigButton(
                "➕   Care Of যোগ করুন",
                TEAL
            ) {

                val n =
                    name.text
                        .toString()
                        .trim()

                if (n.isEmpty()) {

                    toast(
                        "Care Of নাম লিখুন"
                    )

                } else {

                    addCare(n)

                    name.setText("")

                    toast(
                        "Care Of যোগ হয়েছে"
                    )
                }
            }
        )

        root.addView(space(15))

        getCareList()
            .forEach { care ->

                root.addView(
                    text(
                        "🤝 $care",
                        18f,
                        DARK,
                        true
                    )
                )
            }

        root.addView(
            bigButton(
                "←   Dashboard",
                BLUE
            ) {

                formOpen = false
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

        val list =
            getCareList()
                .toMutableList()

        if (
            list.none {
                it.equals(
                    name,
                    true
                )
            }
        ) {

            list.add(name)

            pref.edit()
                .putString(
                    "cares",
                    list.joinToString("|||")
                )
                .apply()
        }
    }

    private fun getCareList():
            List<String> {

        val raw =
            pref.getString(
                "cares",
                ""
            ) ?: ""

        if (raw.isEmpty())
            return emptyList()

        return raw
            .split("|||")
            .filter {
                it.isNotBlank()
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
                "শুধুমাত্র Admin"
            )

            return
        }

        formOpen = true

        val root =
            rootLayout()

        root.addView(
            text(
                "👑 Admin Control Panel",
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

        setupSpinner(
            spinner,
            listOf(
                "Operator",
                "User"
            )
        )

        root.addView(
            spinner,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                68
            )
        )

        root.addView(
            bigButton(
                "➕   User / Operator তৈরি করুন",
                PURPLE
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

        root.addView(space(18))

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

                    root.addView(
                        text(
                            "👤 $u   •   $role",
                            18f,
                            DARK,
                            true
                        )
                    )

                    root.addView(
                        smallButton(
                            "🗑 User Delete",
                            RED
                        ) {
                            deleteUser(u)
                        }
                    )
                }
            }

        root.addView(space(15))

        root.addView(
            bigButton(
                "←   Dashboard",
                BLUE
            ) {

                formOpen = false
                showDashboard()
            }
        )

        setContentView(
            scroll(root)
        )
    }

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
    // DATE
    // =========================================================

    private fun chooseDate(
        current: String,
        result: (String) -> Unit
    ) {

        val parts =
            current.split("-")

        val y =
            parts.getOrNull(0)
                ?.toIntOrNull()
                ?: Calendar.getInstance()
                    .get(Calendar.YEAR)

        val m =
            (parts.getOrNull(1)
                ?.toIntOrNull()
                ?: (
                    Calendar.getInstance()
                        .get(Calendar.MONTH) + 1
                    )
                ) - 1

        val d =
            parts.getOrNull(2)
                ?.toIntOrNull()
                ?: Calendar.getInstance()
                    .get(Calendar.DAY_OF_MONTH)

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

                result(
                    selected
                )
            },
            y,
            m,
            d
        ).show()
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

        } catch (e: Exception) {

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
        formOpen = false

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
    // LIFECYCLE
    // =========================================================

    override fun onResume() {

        super.onResume()

        if (
            dashboardVisible &&
            currentUsername.isNotEmpty() &&
            !formOpen
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

    override fun onPause() {

        handler.removeCallbacks(
            refreshRunnable
        )

        super.onPause()
    }

    override fun onDestroy() {

        handler.removeCallbacks(
            refreshRunnable
        )

        super.onDestroy()
    }

    override fun onBackPressed() {

        if (formOpen) {

            formOpen = false
            showDashboard()

        } else if (
            currentUsername.isNotEmpty()
        ) {

            showDashboard()

        } else {

            super.onBackPressed()
        }
    }
}
