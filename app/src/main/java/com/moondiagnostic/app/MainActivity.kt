package com.moondiagnostic.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MainActivity : Activity() {

    // =========================================================
    // COLORS
    // =========================================================

    private val BG = Color.rgb(241, 248, 253)
    private val BLUE = Color.rgb(25, 91, 150)
    private val DARK_BLUE = Color.rgb(15, 62, 105)
    private val TEAL = Color.rgb(15, 135, 125)
    private val GREEN = Color.rgb(35, 140, 85)
    private val RED = Color.rgb(200, 55, 55)
    private val ORANGE = Color.rgb(225, 140, 35)
    private val PURPLE = Color.rgb(105, 75, 165)
    private val WHITE = Color.WHITE
    private val DARK = Color.rgb(40, 45, 50)
    private val GRAY = Color.rgb(100, 108, 115)
    private val BORDER = Color.rgb(195, 215, 230)

    // =========================================================
    // LOCAL SESSION
    // =========================================================

    private val PREF_NAME = "MDC_APP_DATA"

    private lateinit var pref: android.content.SharedPreferences

    private var currentUsername = ""
    private var currentRole = ""

    // =========================================================
    // SIMPLE DATA MODEL
    // =========================================================

    private data class SerialRecord(
        val id: String,
        val date: String,
        val serial: Int,
        val doctorSerial: Int,
        val careOfSerial: Int,
        val patient: String,
        val careOf: String,
        val doctor: String,
        val status: String,
        val createdBy: String,
        val createdRole: String
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
                .putString("user_admin", "admin")
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
            d.setStroke(2, stroke)
        }

        return d
    }

    // =========================================================
    // SPACING
    // =========================================================

    private fun gap(height: Int): Space {

        val s = Space(this)

        s.layoutParams =
            LinearLayout.LayoutParams(
                1,
                height
            )

        return s
    }

    // =========================================================
    // VERTICAL CONTAINER
    // =========================================================

    private fun container(): LinearLayout {

        val l = LinearLayout(this)

        l.orientation =
            LinearLayout.VERTICAL

        l.setPadding(
            16,
            18,
            16,
            30
        )

        return l
    }

    // =========================================================
    // REFRESH SCREEN
    // =========================================================

    private fun refreshScreen(
        content: View,
        onRefresh: () -> Unit
    ): SwipeRefreshLayout {

        val refresh =
            SwipeRefreshLayout(this)

        refresh.setBackgroundColor(BG)

        refresh.isEnabled = true

        refresh.setOnRefreshListener {

            onRefresh()

            refresh.isRefreshing = false
        }

        refresh.addView(content)

        return refresh
    }

    // =========================================================
    // BIG BUTTON
    // =========================================================

    private fun bigButton(
        icon: String,
        title: String,
        color: Int = BLUE,
        onClick: () -> Unit
    ): LinearLayout {

        val card = LinearLayout(this)

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
            box(
                WHITE,
                18f,
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
            7,
            7,
            7,
            7
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
            gap(6)
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
            onClick()
        }

        return card
    }

    // =========================================================
    // BIG ACTION BUTTON
    // =========================================================

    private fun action(
        title: String,
        color: Int = BLUE,
        onClick: () -> Unit
    ): TextView {

        val b =
            text(
                title,
                18f,
                WHITE,
                true
            )

        b.background =
            box(
                color,
                16f
            )

        b.setPadding(
            10,
            0,
            10,
            0
        )

        b.elevation = 4f

        val p =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                70
            )

        p.setMargins(
            7,
            6,
            7,
            6
        )

        b.layoutParams = p

        b.setOnClickListener {
            onClick()
        }

        return b
    }

    // =========================================================
    // BIG INPUT
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

        if (password) {

            e.inputType =
                android.text.InputType.TYPE_CLASS_TEXT or
                        android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD

        } else {

            e.inputType =
                android.text.InputType.TYPE_CLASS_TEXT
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

        val root = container()

        root.gravity =
            Gravity.CENTER_HORIZONTAL

        root.setPadding(
            14,
            30,
            14,
            40
        )

        root.addView(
            text(
                "MDC",
                70f,
                BLUE,
                true
            )
        )

        root.addView(
            gap(5)
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
            gap(6)
        )

        root.addView(
            text(
                "সঠিক নির্ণয়, সুস্থ জীবনের প্রত্যয়",
                16f,
                GRAY
            )
        )

        root.addView(
            gap(22)
        )

        val card =
            LinearLayout(this)

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
                BORDER
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
                32f,
                DARK_BLUE,
                true
            )
        )

        card.addView(
            gap(18)
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

        card.addView(username)
        card.addView(password)

        card.addView(
            gap(12)
        )

        card.addView(
            action(
                "🔑   লগইন",
                BLUE
            ) {

                loginUser(
                    username.text.toString().trim(),
                    password.text.toString()
                )
            }
        )

        root.addView(
            gap(25)
        )

        root.addView(
            text(
                "অনুমোদিত Admin / Operator / User-এর জন্য",
                15f,
                GRAY
            )
        )

        root.addView(
            gap(18)
        )

        root.addView(
            text(
                "Moon Diagnostic Center",
                17f,
                GRAY,
                true
            )
        )

        setContentView(
            refreshScreen(
                root
            ) {
                // Login page refresh
            }
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
            toast("ইউজারনেম লিখুন")
            return
        }

        if (password.isEmpty()) {
            toast("পাসওয়ার্ড লিখুন")
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
                "ইউজারনেম অথবা পাসওয়ার্ড ভুল"
            )
        }
    }

    // =========================================================
    // DASHBOARD
    // =========================================================

    private fun showDashboard() {

        val root = container()

        root.addView(
            text(
                "MDC",
                62f,
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
            gap(8)
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
            gap(12)
        )

        root.addView(
            action(
                "🚪   Logout",
                RED
            ) {
                logout()
            }
        )

        root.addView(
            gap(15)
        )

        val stats1 =
            LinearLayout(this)

        stats1.orientation =
            LinearLayout.HORIZONTAL

        stats1.addView(
            stat(
                "👥",
                "মোট সিরিয়াল",
                countToday().toString(),
                BLUE
            )
        )

        stats1.addView(
            stat(
                "⏳",
                "অপেক্ষমাণ",
                waitingToday().toString(),
                ORANGE
            )
        )

        root.addView(stats1)

        val stats2 =
            LinearLayout(this)

        stats2.orientation =
            LinearLayout.HORIZONTAL

        stats2.addView(
            stat(
                "✓",
                "সম্পন্ন",
                completedToday().toString(),
                GREEN
            )
        )

        stats2.addView(
            stat(
                "✕",
                "বাতিল",
                cancelledToday().toString(),
                RED
            )
        )

        root.addView(stats2)

        root.addView(
            gap(20)
        )

        root.addView(
            text(
                "দ্রুত অ্যাকশন",
                28f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            gap(5)
        )

        // TWO BY TWO

        val row1 =
            LinearLayout(this)

        row1.orientation =
            LinearLayout.HORIZONTAL

        row1.addView(
            bigButton(
                "📋",
                "টোটাল সিরিয়াল",
                BLUE
            ) {
                showTotalSerial()
            }
        )

        row1.addView(
            bigButton(
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
                    showDoctorPage()
                } else {
                    toast(
                        "শুধুমাত্র Admin ডাক্তার যোগ করতে পারবেন"
                    )
                }
            }
        )

        row2.addView(
            bigButton(
                "👤",
                "অ্যাড কেয়ার অফ",
                TEAL
            ) {
                showCarePage()
            }
        )

        root.addView(row2)

        if (
            currentRole.equals(
                "Admin",
                true
            )
        ) {

            root.addView(
                gap(15)
            )

            root.addView(
                action(
                    "👑   Admin Control Panel",
                    PURPLE
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
                "উপর থেকে টেনে ধরলে ডাটা Refresh হবে",
                15f,
                GRAY
            )
        )

        root.addView(
            gap(25)
        )

        root.addView(
            text(
                "মুন ডায়াগনস্টিক সেন্টার",
                17f,
                GRAY,
                true
            )
        )

        setContentView(
            refreshScreen(
                root
            ) {

                // Manual pull-to-refresh
                showDashboard()
            }
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
            8,
            15,
            8,
            15
        )

        card.background =
            box(
                WHITE,
                18f,
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
            7,
            7,
            7,
            7
        )

        card.layoutParams = p

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
                23f,
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

        val root =
            container()

        root.addView(
            text(
                "➕  নতুন সিরিয়াল",
                31f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            gap(15)
        )

        val patient =
            input(
                "রোগীর নাম"
            )

        val careOf =
            input(
                "Care Of নাম"
            )

        val doctor =
            input(
                "ডাক্তারের নাম"
            )

        root.addView(patient)
        root.addView(careOf)
        root.addView(doctor)

        root.addView(
            gap(8)
        )

        root.addView(
            text(
                "সিরিয়ালের তারিখ",
                18f,
                DARK_BLUE,
                true
            )
        )

        val dateButton =
            action(
                "📅   আজকের তারিখ নির্বাচন করুন",
                TEAL
            ) {

                showDatePicker(
                    dateButton
                )
            }

        root.addView(
            dateButton
        )

        root.addView(
            gap(18)
        )

        root.addView(
            action(
                "✅   সিরিয়াল যোগ করুন",
                GREEN
            ) {

                addSerial(
                    patient.text.toString().trim(),
                    careOf.text.toString().trim(),
                    doctor.text.toString().trim()
                )
            }
        )

        setContentView(
            refreshScreen(
                root
            ) {
                // Add page intentionally does not reload data.
            }
        )
    }

    // =========================================================
    // DATE PICKER
    // =========================================================

    private var selectedDate =
        todayKey()

    private fun showDatePicker(
        button: TextView
    ) {

        val calendar =
            java.util.Calendar.getInstance()

        android.app.DatePickerDialog(
            this,
            { _, year, month, day ->

                selectedDate =
                    String.format(
                        "%04d-%02d-%02d",
                        year,
                        month + 1,
                        day
                    )

                button.text =
                    "📅   তারিখ: $selectedDate"

            },
            calendar.get(
                java.util.Calendar.YEAR
            ),
            calendar.get(
                java.util.Calendar.MONTH
            ),
            calendar.get(
                java.util.Calendar.DAY_OF_MONTH
            )
        ).show()
    }

    // =========================================================
    // ADD SERIAL
    // =========================================================

    private fun addSerial(
        patient: String,
        careOf: String,
        doctor: String
    ) {

        if (patient.isEmpty()) {
            toast("রোগীর নাম লিখুন")
            return
        }

        if (doctor.isEmpty()) {
            toast("ডাক্তার নির্বাচন করুন")
            return
        }

        val total =
            nextSerial(
                selectedDate,
                "total"
            )

        val doctorNumber =
            nextSerial(
                selectedDate,
                "doctor_$doctor"
            )

        val careNumber =
            nextSerial(
                selectedDate,
                "care_$careOf"
            )

        val id =
            System.currentTimeMillis()
                .toString()

        val value =
            listOf(
                selectedDate,
                total,
                doctorNumber,
                careNumber,
                patient,
                careOf,
                doctor,
                "Waiting",
                currentUsername,
                currentRole
            ).joinToString("|||")

        pref.edit()
            .putString(
                "serial_$id",
                value
            )
            .apply()

        toast(
            "সিরিয়াল #$total যোগ হয়েছে"
        )

        selectedDate =
            todayKey()

        showDashboard()
    }

    // =========================================================
    // SERIAL NUMBER
    // =========================================================

    private fun nextSerial(
        date: String,
        type: String
    ): Int {

        var max = 0

        for (key in pref.all.keys) {

            if (!key.startsWith("serial_"))
                continue

            val raw =
                pref.getString(
                    key,
                    ""
                ) ?: ""

            val p =
                raw.split("|||")

            if (p.size < 10)
                continue

            if (p[0] != date)
                continue

            val number =
                when {

                    type == "total" ->
                        p[1].toIntOrNull()
                            ?: 0

                    type.startsWith(
                        "doctor_"
                    ) &&
                            p[6] ==
                            type.removePrefix(
                                "doctor_"
                            ) ->
                        p[2].toIntOrNull()
                            ?: 0

                    type.startsWith(
                        "care_"
                    ) &&
                            p[5] ==
                            type.removePrefix(
                                "care_"
                            ) ->
                        p[3].toIntOrNull()
                            ?: 0

                    else -> 0
                }

            if (number > max)
                max = number
        }

        return max + 1
    }

    // =========================================================
    // TOTAL SERIAL
    // =========================================================

    private fun showTotalSerial() {

        val root =
            container()

        root.addView(
            text(
                "📋  টোটাল সিরিয়াল",
                31f,
                DARK_BLUE,
                true
            )
        )

        root.addView(
            gap(15)
        )

        val dateButton =
            action(
                "📅   তারিখ নির্বাচন করুন",
                TEAL
            ) {

                chooseSerialDate(
                    dateButton,
                    root
                )
            }

        root.addView(
            dateButton
        )

        root.addView(
            gap(15)
        )

        // TWO INNER TABS

        val tabs =
            LinearLayout(this)

        tabs.orientation =
            LinearLayout.HORIZONTAL

        val allButton =
            tabButton(
                "📋\nসব সিরিয়াল",
                BLUE
            )

        val doctorButton =
            tabButton(
                "👨‍⚕️\nডাক্তার ওয়াইজ",
                PURPLE
            )

        val careButton =
            tabButton(
                "👤\nকেয়ার অফ ওয়াইজ",
                TEAL
            )

        tabs.addView(allButton)
        tabs.addView(doctorButton)
        tabs.addView(careButton)

        root.addView(tabs)

        root.addView(
            gap(15)
        )

        val content =
            LinearLayout(this)

        content.orientation =
            LinearLayout.VERTICAL

        root.addView(content)

        showAllSerialContent(
            content,
            selectedDate
        )

        allButton.setOnClickListener {

            showAllSerialContent(
                content,
                selectedDate
            )
        }

        doctorButton.setOnClickListener {

            showDoctorWiseContent(
                content,
                selectedDate
            )
        }

        careButton.setOnClickListener {

            showCareWiseContent(
                content,
                selectedDate
            )
        }

        setContentView(
            refreshScreen(
                root
            ) {

                showTotalSerial()
            }
        )
    }

    // =========================================================
    // TAB BUTTON
    // =========================================================

    private fun tabButton(
        title: String,
        color: Int
    ): TextView {

        val t =
            text(
                title,
                17f,
                WHITE,
                true
            )

        t.background =
            box(
                color,
                15f
            )

        t.setPadding(
            8,
            15,
            8,
            15
        )

        val p =
            LinearLayout.LayoutParams(
                0,
                90,
                1f
            )

        p.setMargins(
            4,
            4,
            4,
            4
        )

        t.layoutParams = p

        return t
    }

    // =========================================================
    // ALL SERIAL
    // =========================================================

    private fun showAllSerialContent(
        content: LinearLayout,
        date: String
    ) {

        content.removeAllViews()

        val records =
            readSerials(date)

        content.addView(
            text(
                "$date • মোট ${records.size} জন",
                20f,
                DARK_BLUE,
                true
            )
        )

        records.forEach {

            addSerialCard(
                content,
                it
            )
        }
    }

    // =========================================================
    // DOCTOR WISE
    // =========================================================

    private fun showDoctorWiseContent(
        content: LinearLayout,
        date: String
    ) {

        content.removeAllViews()

        content.addView(
            text(
                "ডাক্তার নির্বাচন করুন",
                20f,
                DARK_BLUE,
                true
            )
        )

        val doctors =
            readDoctors()

        if (doctors.isEmpty()) {

            content.addView(
                text(
                    "কোনো ডাক্তার যোগ করা হয়নি",
                    17f,
                    GRAY
                )
            )

            return
        }

        val spinner =
            Spinner(this)

        val adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                doctors
            )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spinner.adapter =
            adapter

        content.addView(
            spinner,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                70
            )
        )

        val result =
            LinearLayout(this)

        result.orientation =
            LinearLayout.VERTICAL

        content.addView(result)

        fun load() {

            result.removeAllViews()

            val selected =
                spinner.selectedItem
                    ?.toString()
                    ?: ""

            val list =
                readSerials(date)
                    .filter {
                        it.doctor ==
                                selected
                    }

            list.forEach {

                result.addView(
                    serialSmallCard(
                        it,
                        true
                    )
                )
            }
        }

        spinner.setOnItemSelectedListener(
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
                    load()
                }
            }
        )
    }

    // =========================================================
    // CARE WISE
    // =========================================================

    private fun showCareWiseContent(
        content: LinearLayout,
        date: String
    ) {

        content.removeAllViews()

        content.addView(
            text(
                "Care Of নির্বাচন করুন",
                20f,
                DARK_BLUE,
                true
            )
        )

        val cares =
            readCareOfs()

        if (cares.isEmpty()) {

            content.addView(
                text(
                    "কোনো Care Of যোগ করা হয়নি",
                    17f,
                    GRAY
                )
            )

            return
        }

        val spinner =
            Spinner(this)

        val adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                cares
            )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spinner.adapter =
            adapter

        content.addView(
            spinner,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                70
            )
        )

        val result =
            LinearLayout(this)

        result.orientation =
            LinearLayout.VERTICAL

        content.addView(result)

        spinner.setOnItemSelectedListener(
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

                    result.removeAllViews()

                    val selected =
                        spinner.selectedItem
                            ?.toString()
                            ?: ""

                    readSerials(date)
                        .filter {
                            it.careOf ==
                                    selected
                        }
                        .forEach {

                            result.addView(
                                serialSmallCard(
                                    it,
                                    false
                                )
                            )
                        }
                }
            }
        )
    }

    // =========================================================
    // SERIAL CARD
    // =========================================================

    private fun addSerialCard(
        parent: LinearLayout,
        record: SerialRecord
    ) {

        val card =
            serialCard(
                record
            )

        parent.addView(card)
    }

    private fun serialCard(
        r: SerialRecord
    ): LinearLayout {

        val card =
            LinearLayout(this)

        card.orientation =
            LinearLayout.VERTICAL

        card.setPadding(
            16,
            15,
            16,
            15
        )

        card.background =
            box(
                WHITE,
                18f,
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
                22f,
                BLUE,
                true
            )
        )

        card.addView(
            text(
                "👤 ${r.patient}",
                19f,
                DARK,
                true
            )
        )

        card.addView(
            text(
                "Care Of: ${r.careOf}",
                16f,
                GRAY
            )
        )

        card.addView(
            text(
                "ডাক্তার: ${r.doctor}",
                17f,
                DARK_BLUE,
                true
            )
        )

        card.addView(
            text(
                "ডাক্তার সিরিয়াল: ${r.doctorSerial}",
                15f,
                PURPLE,
                true
            )
        )

        card.addView(
            text(
                "Care Of সিরিয়াল: ${r.careOfSerial}",
                15f,
                TEAL,
                true
            )
        )

        card.addView(
            text(
                "দিয়েছেন: ${r.createdBy} (${r.createdRole})",
                15f,
                TEAL,
                true
            )
        )

        card.addView(
            text(
                "স্ট্যাটাস: ${statusBangla(r.status)}",
                16f,
                statusColor(r.status),
                true
            )
        )

        // =====================================================
        // EDIT / DELETE
        // ONLY CREATOR CAN EDIT / DELETE
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
                action(
                    "✏️  Edit",
                    BLUE
                ) {
                    editSerial(r)
                }

            val delete =
                action(
                    "🗑  Delete",
                    RED
                ) {
                    deleteSerial(r)
                }

            edit.layoutParams =
                LinearLayout.LayoutParams(
                    0,
                    65,
                    1f
                )

            delete.layoutParams =
                LinearLayout.LayoutParams(
                    0,
                    65,
                    1f
                )

            row.addView(edit)
            row.addView(delete)

            card.addView(row)
        }

        // =====================================================
        // COMPLETE / INCOMPLETE
        // ADMIN + OPERATOR ONLY
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

            val completeText =
                if (
                    r.status ==
                    "Completed"
                ) {
                    "↩  অসম্পন্ন করুন"
                } else {
                    "✓  সম্পন্ন করুন"
                }

            card.addView(
                action(
                    completeText,
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
    // SMALL SERIAL CARD
    // =========================================================

    private fun serialSmallCard(
        r: SerialRecord,
        doctorWise: Boolean
    ): LinearLayout {

        val c =
            LinearLayout(this)

        c.orientation =
            LinearLayout.VERTICAL

        c.setPadding(
            15,
            13,
            15,
            13
        )

        c.background =
            box(
                WHITE,
                16f,
                BORDER
            )

        c.addView(
            text(
                if (doctorWise)
                    "ডাক্তার সিরিয়াল #${r.doctorSerial}"
                else
                    "Care Of সিরিয়াল #${r.careOfSerial}",
                20f,
                BLUE,
                true
            )
        )

        c.addView(
            text(
                "মোট সিরিয়াল: #${r.serial}",
                16f,
                GRAY
            )
        )

        c.addView(
            text(
                "রোগী: ${r.patient}",
                17f,
                DARK,
                true
            )
        )

        c.addView(
            text(
                "ডাক্তার: ${r.doctor}",
                15f,
                DARK_BLUE
            )
        )

        c.addView(
            text(
                "Care Of: ${r.careOf}",
                15f,
                TEAL
            )
        )

        c.addView(
            text(
                "স্ট্যাটাস: ${statusBangla(r.status)}",
                15f,
                statusColor(r.status),
                true
            )
        )

        return c
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
    // TOGGLE COMPLETE
    // =========================================================

    private fun toggleComplete(
        r: SerialRecord
    ) {

        val key =
            "serial_${r.id}"

        val raw =
            pref.getString(
                key,
                null
            ) ?: return

        val parts =
            raw.split("|||")
                .toMutableList()

        if (parts.size < 10)
            return

        parts[7] =
            if (
                parts[7] ==
                "Completed"
            )
                "Waiting"
            else
                "Completed"

        pref.edit()
            .putString(
                key,
                parts.joinToString("|||")
            )
            .apply()

        showTotalSerial()
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
                "এই সিরিয়াল আপনি ডিলিট করতে পারবেন না"
            )

            return
        }

        pref.edit()
            .remove(
                "serial_${r.id}"
            )
            .apply()

        toast(
            "সিরিয়াল ডিলিট হয়েছে"
        )

        showTotalSerial()
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
                "এই সিরিয়াল আপনি Edit করতে পারবেন না"
            )

            return
        }

        val root =
            container()

        root.addView(
            text(
                "✏️  সিরিয়াল Edit",
                30f,
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
            input(
                "Care Of"
            )

        care.setText(
            r.careOf
        )

        val doctor =
            input(
                "ডাক্তার"
            )

        doctor.setText(
            r.doctor
        )

        root.addView(patient)
        root.addView(care)
        root.addView(doctor)

        root.addView(
            gap(15)
        )

        root.addView(
            action(
                "💾  পরিবর্তন সংরক্ষণ করুন",
                GREEN
            ) {

                val key =
                    "serial_${r.id}"

                val raw =
                    pref.getString(
                        key,
                        null
                    ) ?: return@action

                val parts =
                    raw.split("|||")
                        .toMutableList()

                if (parts.size >= 10) {

                    parts[4] =
                        patient.text
                            .toString()
                            .trim()

                    parts[5] =
                        care.text
                            .toString()
                            .trim()

                    parts[6] =
                        doctor.text
                            .toString()
                            .trim()

                    pref.edit()
                        .putString(
                            key,
                            parts.joinToString(
                                "|||"
                            )
                        )
                        .apply()

                    toast(
                        "সিরিয়াল আপডেট হয়েছে"
                    )

                    showTotalSerial()
                }
            }
        )

        setContentView(
            refreshScreen(
                root
            ) {}
        )
    }

    // =========================================================
    // DATE SELECT
    // =========================================================

    private fun chooseSerialDate(
        button: TextView,
        root: LinearLayout
    ) {

        val cal =
            java.util.Calendar
                .getInstance()

        android.app.DatePickerDialog(
            this,
            { _, y, m, d ->

                selectedDate =
                    String.format(
                        "%04d-%02d-%02d",
                        y,
                        m + 1,
                        d
                    )

                button.text =
                    "📅   তারিখ: $selectedDate"

                showTotalSerial()

            },
            cal.get(
                java.util.Calendar.YEAR
            ),
            cal.get(
                java.util.Calendar.MONTH
            ),
            cal.get(
                java.util.Calendar.DAY_OF_MONTH
            )
        ).show()
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
            container()

        root.addView(
            text(
                "👨‍⚕️  অ্যাড ডাক্তার",
                31f,
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
            action(
                "➕  ডাক্তার যোগ করুন",
                PURPLE
            ) {

                val name =
                    doctor.text
                        .toString()
                        .trim()

                if (name.isEmpty()) {

                    toast(
                        "ডাক্তারের নাম লিখুন"
                    )

                    return@action
                }

                pref.edit()
                    .putBoolean(
                        "doctor_$name",
                        true
                    )
                    .apply()

                toast(
                    "ডাক্তার যোগ হয়েছে"
                )

                doctor.text.clear()
            }
        )

        setContentView(
            refreshScreen(
                root
            ) {
                showDoctorPage()
            }
        )
    }

    // =========================================================
    // CARE PAGE
    // =========================================================

    private fun showCarePage() {

        val root =
            container()

        root.addView(
            text(
                "👤  অ্যাড Care Of",
                31f,
                DARK_BLUE,
                true
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
            action(
                "➕  Care Of যোগ করুন",
                TEAL
            ) {

                val name =
                    care.text
                        .toString()
                        .trim()

                if (name.isEmpty()) {

                    toast(
                        "Care Of নাম লিখুন"
                    )

                    return@action
                }

                pref.edit()
                    .putBoolean(
                        "care_$name",
                        true
                    )
                    .apply()

                toast(
                    "Care Of যোগ হয়েছে"
                )

                care.text.clear()
            }
        )

        root.addView(
            gap(20)
        )

        root.addView(
            text(
                "বর্তমান Care Of",
                22f,
                DARK_BLUE,
                true
            )
        )

        readCareOfs()
            .forEach {

                root.addView(
                    text(
                        "👤  $it",
                        18f,
                        DARK,
                        true
                    )
                )
            }

        setContentView(
            refreshScreen(
                root
            ) {
                showCarePage()
            }
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
            container()

        root.addView(
            text(
                "👑  Admin Control Panel",
                30f,
                PURPLE,
                true
            )
        )

        val username =
            input(
                "Username"
            )

        val password =
            input(
                "Password",
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
                70
            )
        )

        root.addView(
            action(
                "➕  User / Operator তৈরি করুন",
                PURPLE
            ) {

                val u =
                    username.text
                        .toString()
                        .trim()

                val p =
                    password.text
                        .toString()

                val role =
                    spinner.selectedItem
                        .toString()

                if (u.isEmpty()) {

                    toast(
                        "Username দিন"
                    )

                    return@action
                }

                if (p.length < 4) {

                    toast(
                        "Password কমপক্ষে ৪ অক্ষর"
                    )

                    return@action
                }

                if (
                    pref.contains(
                        "user_$u"
                    )
                ) {

                    toast(
                        "এই Username আগে থেকেই আছে"
                    )

                    return@action
                }

                pref.edit()
                    .putString(
                        "user_$u",
                        u
                    )
                    .putString(
                        "pass_$u",
                        hashPassword(p)
                    )
                    .putString(
                        "role_$u",
                        role
                    )
                    .apply()

                toast(
                    "$role তৈরি হয়েছে"
                )

                showAdminPanel()
            }
        )

        root.addView(
            gap(20)
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

                    root.addView(
                        text(
                            "👤 $u  •  $role",
                            18f,
                            DARK,
                            true
                        )
                    )
                }
            }
        }

        setContentView(
            refreshScreen(
                root
            ) {
                showAdminPanel()
            }
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

            if (
                !key.startsWith(
                    "serial_"
                )
            )
                continue

            val raw =
                pref.getString(
                    key,
                    ""
                ) ?: ""

            val p =
                raw.split("|||")

            if (p.size < 10)
                continue

            if (p[0] != date)
                continue

            list.add(
                SerialRecord(
                    key.removePrefix(
                        "serial_"
                    ),
                    p[0],
                    p[1].toIntOrNull()
                        ?: 0,
                    p[2].toIntOrNull()
                        ?: 0,
                    p[3].toIntOrNull()
                        ?: 0,
                    p[4],
                    p[5],
                    p[6],
                    p[7],
                    p[8],
                    p[9]
                )
            )
        }

        return list.sortedBy {
            it.serial
        }
    }

    // =========================================================
    // DOCTORS
    // =========================================================

    private fun readDoctors():
            List<String> {

        return pref.all.keys
            .filter {
                it.startsWith(
                    "doctor_"
                )
            }
            .map {
                it.removePrefix(
                    "doctor_"
                )
            }
            .sorted()
    }

    // =========================================================
    // CARE OF
    // =========================================================

    private fun readCareOfs():
            List<String> {

        return pref.all.keys
            .filter {
                it.startsWith(
                    "care_"
                )
            }
            .map {
                it.removePrefix(
                    "care_"
                )
            }
            .sorted()
    }

    // =========================================================
    // DASHBOARD COUNTS
    // =========================================================

    private fun countToday():
            Int {

        return readSerials(
            todayKey()
        ).size
    }

    private fun waitingToday():
            Int {

        return readSerials(
            todayKey()
        ).count {
            it.status == "Waiting"
        }
    }

    private fun completedToday():
            Int {

        return readSerials(
            todayKey()
        ).count {
            it.status == "Completed"
        }
    }

    private fun cancelledToday():
            Int {

        return readSerials(
            todayKey()
        ).count {
            it.status == "Cancelled"
        }
    }

    // =========================================================
    // DATE
    // =========================================================

    private fun todayKey():
            String {

        return java.text.SimpleDateFormat(
            "yyyy-MM-dd",
            java.util.Locale
                .getDefault()
        ).format(
            java.util.Date()
        )
    }

    // =========================================================
    // LOGOUT
    // =========================================================

    private fun logout() {

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
                java.security.MessageDigest
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
