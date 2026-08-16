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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.database.*
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
    // FIREBASE
    // =========================================================

    private lateinit var db: DatabaseReference

    // =========================================================
    // SESSION
    // =========================================================

    private var currentUsername = ""
    private var currentRole = ""

    private var currentPage = "LOGIN"

    private val handler = Handler(Looper.getMainLooper())

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

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        db = FirebaseDatabase
            .getInstance()
            .reference

        val prefs = getSharedPreferences(
            "MDC_SESSION",
            MODE_PRIVATE
        )

        currentUsername =
            prefs.getString(
                "username",
                ""
            ) ?: ""

        currentRole =
            prefs.getString(
                "role",
                ""
            ) ?: ""

        if (currentUsername.isNotEmpty()) {

            showDashboard()

        } else {

            showLogin()
        }
    }

    // =========================================================
    // COMMON TEXT
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
    // SCREEN WITH PULL REFRESH
    // =========================================================

    private fun screen(
        view: View
    ): SwipeRefreshLayout {

        val refresh =
            SwipeRefreshLayout(this)

        refresh.setColorSchemeColors(
            BLUE,
            TEAL,
            GREEN,
            PURPLE
        )

        val scroll =
            ScrollView(this)

        scroll.isFillViewport = true

        scroll.addView(view)

        refresh.addView(scroll)

        refresh.setOnRefreshListener {

            refreshCurrentPage()

            refresh.isRefreshing = false
        }

        return refresh
    }

    // =========================================================
    // REFRESH CURRENT PAGE
    // =========================================================

    private fun refreshCurrentPage() {

        when (currentPage) {

            "DASHBOARD" ->
                showDashboard()

            "TOTAL" ->
                showTotalSerial()

            "DOCTOR" ->
                showDoctorWise()

            "CARE" ->
                showCareWise()

            "ADD_SERIAL" ->
                showAddSerial()

            "DOCTOR_MANAGER" ->
                showDoctorManager()

            "CARE_MANAGER" ->
                showCareManager()

            "ADMIN" ->
                showAdminPanel()
        }
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
            15,
            8,
            15
        )

        box.background =
            rounded(
                WHITE,
                20f,
                BORDER
            )

        box.elevation = 5f

        box.addView(
            text(
                icon,
                42f,
                color,
                true
            )
        )

        box.addView(
            text(
                title,
                18f,
                DARK_BLUE,
                true
            )
        )

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
                18f,
                WHITE,
                true
            )

        b.background =
            rounded(
                color,
                15f
            )

        b.elevation = 4f

        b.layoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                72
            ).apply {

                setMargins(
                    7,
                    7,
                    7,
                    7
                )
            }

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

        e.layoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                72
            ).apply {

                setMargins(
                    7,
                    7,
                    7,
                    7
                )
            }

        return e
    }

    // =========================================================
    // LOGIN
    // =========================================================

    private fun showLogin() {

        currentPage = "LOGIN"

        val root =
            rootLayout()

        root.addView(
            space(45)
        )

        root.addView(
            text(
                "🏥",
                65f,
                BLUE,
                true
            )
        )

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
                "সঠিক নির্ণয়, সুস্থ জীবনের প্রত্যয়",
                16f,
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
            25,
            16,
            30
        )

        card.background =
            rounded(
                WHITE,
                22f,
                BORDER
            )

        card.elevation = 8f

        root.addView(
            card
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
            space(15)
        )

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

        root.addView(
            space(20)
        )

        root.addView(
            text(
                "Admin অনুমোদন ছাড়া User / Operator অ্যাপ ব্যবহার করতে পারবে না",
                15f,
                GRAY,
                true
            )
        )

        root.addView(
            space(15)
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

        db.child("users")
            .child(safeKey(username))
            .get()
            .addOnSuccessListener {

                if (!it.exists()) {

                    toast(
                        "Username অথবা Password ভুল"
                    )

                    return@addOnSuccessListener
                }

                val savedPassword =
                    it.child("password")
                        .getValue(String::class.java)
                        ?: ""

                val role =
                    it.child("role")
                        .getValue(String::class.java)
                        ?: ""

                val active =
                    it.child("active")
                        .getValue(Boolean::class.java)
                        ?: false

                if (
                    !active
                ) {

                    toast(
                        "এই User বর্তমানে অনুমোদিত নয়"
                    )

                    return@addOnSuccessListener
                }

                if (
                    savedPassword !=
                    hashPassword(password)
                ) {

                    toast(
                        "Username অথবা Password ভুল"
                    )

                    return@addOnSuccessListener
                }

                currentUsername =
                    username

                currentRole =
                    role

                getSharedPreferences(
                    "MDC_SESSION",
                    MODE_PRIVATE
                )
                    .edit()
                    .putString(
                        "username",
                        username
                    )
                    .putString(
                        "role",
                        role
                    )
                    .apply()

                toast(
                    "সফলভাবে Login হয়েছে"
                )

                showDashboard()
            }
            .addOnFailureListener {

                toast(
                    "Database connection সমস্যা"
                )
            }
    }

    // =========================================================
    // DASHBOARD
    // =========================================================

    private fun showDashboard() {

        currentPage =
            "DASHBOARD"

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
                17f,
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
            space(15)
        )

        root.addView(
            text(
                "⚡  দ্রুত অ্যাকশন",
                27f,
                DARK_BLUE,
                true
            )
        )

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

            },
            gridParams()
        )

        row1.addView(
            bigButton(
                "➕",
                "অ্যাড সিরিয়াল",
                GREEN
            ) {

                showAddSerial()

            },
            gridParams()
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

                    showDoctorManager()

                } else {

                    toast(
                        "শুধুমাত্র Admin ডাক্তার Add করতে পারবেন"
                    )
                }

            },
            gridParams()
        )

        row2.addView(
            bigButton(
                "👤",
                "অ্যাড কেয়ার অফ",
                TEAL
            ) {

                showCareManager()

            },
            gridParams()
        )

        root.addView(row2)

        root.addView(
            space(20)
        )

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

            root.addView(
                space(10)
            )
        }

        root.addView(
            text(
                "🔄 উপর থেকে নিচে Pull করে Refresh করুন",
                15f,
                TEAL,
                true
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
            screen(root)
        )
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

        currentPage =
            "ADD_SERIAL"

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

        root.addView(
            space(12)
        )

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

        root.addView(card)

        card.addView(
            text(
                "📅  সিরিয়ালের তারিখ",
                19f,
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
                18f,
                DARK_BLUE,
                true
            )

        dateButton.background =
            rounded(
                WHITE,
                15f,
                BLUE
            )

        card.addView(
            dateButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                70
            )
        )

        dateButton.setOnClickListener {

            DatePickerDialog(
                this,
                { _, y, m, d ->

                    selectedDate.set(
                        y,
                        m,
                        d
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

        val care =
            input(
                "Care Of নাম লিখুন"
            )

        card.addView(care)

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
                ) {}

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    if (position > 0) {

                        care.setText(
                            careSpinner
                                .selectedItem
                                .toString()
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

        val doctor =
            input(
                "ডাক্তারের নাম লিখুন"
            )

        card.addView(doctor)

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
                ) {}

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    if (position > 0) {

                        doctor.setText(
                            doctorSpinner
                                .selectedItem
                                .toString()
                        )
                    }
                }
            }

        card.addView(
            space(10)
        )

        card.addView(
            actionButton(
                "✅   সিরিয়াল তৈরি করুন",
                GREEN
            ) {

                val p =
                    patient.text
                        .toString()
                        .trim()

                val c =
                    care.text
                        .toString()
                        .trim()

                val d =
                    doctor.text
                        .toString()
                        .trim()

                if (p.isEmpty()) {

                    toast(
                        "রোগীর নাম লিখুন"
                    )

                    return@actionButton
                }

                if (c.isEmpty()) {

                    toast(
                        "Care Of নির্বাচন বা লিখুন"
                    )

                    return@actionButton
                }

                if (d.isEmpty()) {

                    toast(
                        "ডাক্তার নির্বাচন বা লিখুন"
                    )

                    return@actionButton
                }

                saveSerial(
                    selectedDate,
                    p,
                    c,
                    d
                )
            }
        )
    }

    // =========================================================
    // SAVE SERIAL - FIREBASE TRANSACTION
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

        val counter =
            db.child("counters")
                .child(dateKey)

        counter.child("total")
            .runTransaction(
                object :
                    Transaction.Handler {

                    override fun doTransaction(
                        currentData: MutableData
                    ): Transaction.Result {

                        val current =
                            currentData
                                .getValue(Int::class.java)
                                ?: 0

                        currentData.value =
                            current + 1

                        return Transaction.success(
                            currentData
                        )
                    }

                    override fun onComplete(
                        error: DatabaseError?,
                        committed: Boolean,
                        snapshot: DataSnapshot?
                    ) {

                        if (
                            error != null ||
                            !committed
                        ) {

                            toast(
                                "Serial তৈরি করা যায়নি"
                            )

                            return
                        }

                        val totalNumber =
                            snapshot
                                ?.getValue(
                                    Int::class.java
                                )
                                ?: 1

                        getNextDoctorNumber(
                            dateKey,
                            doctor
                        ) { doctorNumber ->

                            getNextCareNumber(
                                dateKey,
                                care
                            ) { careNumber ->

                                val id =
                                    db.child(
                                        "serials"
                                    ).push().key
                                        ?: return@getNextCareNumber

                                val record =
                                    hashMapOf(
                                        "id" to id,
                                        "date" to dateKey,
                                        "totalNumber" to totalNumber,
                                        "doctorNumber" to doctorNumber,
                                        "careNumber" to careNumber,
                                        "patient" to patient,
                                        "careOf" to care,
                                        "doctor" to doctor,
                                        "status" to "Waiting",
                                        "createdBy" to currentUsername,
                                        "createdRole" to currentRole,
                                        "createdTime" to currentTime()
                                    )

                                db.child(
                                    "serials"
                                )
                                    .child(id)
                                    .setValue(record)
                                    .addOnSuccessListener {

                                        toast(
                                            "Serial #$totalNumber তৈরি হয়েছে"
                                        )

                                        showTotalSerial()
                                    }
                                    .addOnFailureListener {

                                        toast(
                                            "Serial Save হয়নি"
                                        )
                                    }
                            }
                        }
                    }
                }
            )
    }

    // =========================================================
    // DOCTOR NUMBER
    // =========================================================

    private fun getNextDoctorNumber(
        date: String,
        doctor: String,
        callback: (Int) -> Unit
    ) {

        val key =
            safeKey(doctor)

        db.child("counters")
            .child(date)
            .child("doctors")
            .child(key)
            .runTransaction(
                object :
                    Transaction.Handler {

                    override fun doTransaction(
                        data: MutableData
                    ): Transaction.Result {

                        val current =
                            data.getValue(
                                Int::class.java
                            ) ?: 0

                        data.value =
                            current + 1

                        return Transaction.success(
                            data
                        )
                    }

                    override fun onComplete(
                        error: DatabaseError?,
                        committed: Boolean,
                        snapshot: DataSnapshot?
                    ) {

                        val number =
                            snapshot?.getValue(
                                Int::class.java
                            ) ?: 1

                        callback(number)
                    }
                }
            )
    }

    // =========================================================
    // CARE NUMBER
    // =========================================================

    private fun getNextCareNumber(
        date: String,
        care: String,
        callback: (Int) -> Unit
    ) {

        val key =
            safeKey(care)

        db.child("counters")
            .child(date)
            .child("cares")
            .child(key)
            .runTransaction(
                object :
                    Transaction.Handler {

                    override fun doTransaction(
                        data: MutableData
                    ): Transaction.Result {

                        val current =
                            data.getValue(
                                Int::class.java
                            ) ?: 0

                        data.value =
                            current + 1

                        return Transaction.success(
                            data
                        )
                    }

                    override fun onComplete(
                        error: DatabaseError?,
                        committed: Boolean,
                        snapshot: DataSnapshot?
                    ) {

                        val number =
                            snapshot?.getValue(
                                Int::class.java
                            ) ?: 1

                        callback(number)
                    }
                }
            )
    }

    // =========================================================
    // TOTAL SERIAL
    // =========================================================

    private fun showTotalSerial() {

        currentPage =
            "TOTAL"

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

        root.addView(
            text(
                "তারিখ অনুযায়ী সকল সিরিয়াল",
                16f,
                GRAY
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
                    10,
                    7,
                    10
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
                ).format(
                    date.time
                )

            readSerials(
                key
            ) { records ->

                list.removeAllViews()

                list.addView(
                    text(
                        "📅 ${formatDateForUser(date)}  •  মোট ${records.size} জন",
                        19f,
                        TEAL,
                        true
                    )
                )

                if (records.isEmpty()) {

                    list.addView(
                        text(
                            "এই তারিখে কোনো সিরিয়াল নেই",
                            18f,
                            GRAY
                        )
                    )

                    return@readSerials
                }

                // =================================================
                // TOTAL LIST
                // =================================================

                list.addView(
                    text(
                        "📋 সকল সিরিয়াল",
                        24f,
                        BLUE,
                        true
                    )
                )

                records.forEach { r ->

                    list.addView(
                        serialCard(r)
                    )
                }

                // =================================================
                // NESTED DOCTOR WISE
                // =================================================

                list.addView(
                    space(15)
                )

                list.addView(
                    text(
                        "👨‍⚕️ Doctor-wise Serial",
                        25f,
                        PURPLE,
                        true
                    )
                )

                records
                    .groupBy { it.doctor }
                    .forEach { (doctor, doctorRecords) ->

                        list.addView(
                            text(
                                "👨‍⚕️ $doctor",
                                21f,
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

                // =================================================
                // NESTED CARE WISE
                // =================================================

                list.addView(
                    space(15)
                )

                list.addView(
                    text(
                        "👤 Care Of-wise Serial",
                        25f,
                        TEAL,
                        true
                    )
                )

                records
                    .groupBy { it.careOf }
                    .forEach { (care, careRecords) ->

                        list.addView(
                            text(
                                "👤 $care",
                                21f,
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
                { _, y, m, d ->

                    date.set(
                        y,
                        m,
                        d
                    )

                    dateButton.text =
                        formatDateForUser(
                            date
                        )

                    load()
                },
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH),
                date.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        load()

        setContentView(
            screen(root)
        )
    }

    // =========================================================
    // READ SERIALS - REAL TIME
    // =========================================================

    private fun readSerials(
        date: String,
        callback: (List<SerialRecord>) -> Unit
    ) {

        db.child("serials")
            .orderByChild("date")
            .equalTo(date)
            .get()
            .addOnSuccessListener { snapshot ->

                val list =
                    mutableListOf<SerialRecord>()

                for (
                    child in snapshot.children
                ) {

                    val r =
                        childToRecord(child)

                    if (r != null) {

                        list.add(r)
                    }
                }

                callback(
                    list.sortedBy {
                        it.totalNumber
                    }
                )
            }
            .addOnFailureListener {

                callback(
                    emptyList()
                )
            }
    }

    // =========================================================
    // REAL-TIME LISTENER
    // =========================================================

    private var realtimeListener:
        ValueEventListener? = null

    private fun attachRealtime(
        date: String
    ) {

        removeRealtime()

        val reference =
            db.child("serials")
                .orderByChild("date")
                .equalTo(date)

        realtimeListener =
            object : ValueEventListener {

                override fun onDataChange(
                    snapshot: DataSnapshot
                ) {

                    if (
                        currentPage == "TOTAL" ||
                        currentPage == "DOCTOR" ||
                        currentPage == "CARE" ||
                        currentPage == "DASHBOARD"
                    ) {

                        refreshCurrentPage()
                    }
                }

                override fun onCancelled(
                    error: DatabaseError
                ) {}
            }

        reference.addValueEventListener(
            realtimeListener!!
        )
    }

    private fun removeRealtime() {

        realtimeListener?.let {

            db.child("serials")
                .removeEventListener(it)
        }

        realtimeListener = null
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

                "Completed" ->
                    GREEN

                "Cancelled" ->
                    RED

                else ->
                    ORANGE
            }

        card.addView(
            text(
                when (r.status) {

                    "Completed" ->
                        "✅ সম্পন্ন"

                    "Cancelled" ->
                        "❌ বাতিল"

                    else ->
                        "⏳ অপেক্ষমাণ"
                },
                18f,
                statusColor,
                true
            )
        )

        // =====================================================
        // PERMISSION
        // =====================================================

        val isOwner =
            r.createdBy.equals(
                currentUsername,
                true
            )

        val isAdmin =
            currentRole.equals(
                "Admin",
                true
            )

        val completedByOperator =
            r.status == "Completed" &&
            r.createdRole.equals(
                "Operator",
                true
            )

        /*
         * Operator completed করলে User edit/delete করতে পারবে না।
         */

        val canEditDelete =
            isAdmin ||
            (
                isOwner &&
                !completedByOperator
            )

        if (canEditDelete) {

            val row =
                LinearLayout(this)

            row.orientation =
                LinearLayout.HORIZONTAL

            row.addView(
                smallButton(
                    "✏️ Edit",
                    BLUE
                ) {

                    showEditSerial(r)

                },
                smallParams()
            )

            row.addView(
                smallButton(
                    "🗑️ Delete",
                    RED
                ) {

                    deleteSerial(r)

                },
                smallParams()
            )

            card.addView(row)
        }

        // =====================================================
        // COMPLETE / UNCOMPLETE
        // =====================================================

        if (
            isAdmin ||
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
                        "✅ সম্পন্ন করুন",
                        GREEN
                    ) {

                        updateStatus(
                            r,
                            "Completed"
                        )

                    },
                    smallParams()
                )

            } else {

                row.addView(
                    smallButton(
                        "↩ অপেক্ষমাণ করুন",
                        ORANGE
                    ) {

                        updateStatus(
                            r,
                            "Waiting"
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

        b.setOnClickListener {
            onClick()
        }

        return b
    }

    private fun smallParams():
        LinearLayout.LayoutParams {

        return LinearLayout.LayoutParams(
            0,
            55,
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
    // UPDATE STATUS
    // =========================================================

    private fun updateStatus(
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
                "আপনার permission নেই"
            )

            return
        }

        db.child("serials")
            .child(record.id)
            .child("status")
            .setValue(status)
            .addOnSuccessListener {

                toast(
                    if (
                        status == "Completed"
                    )
                        "Serial সম্পন্ন হয়েছে"
                    else
                        "Serial অপেক্ষমাণ হয়েছে"
                )

                refreshCurrentPage()
            }
    }

    // =========================================================
    // EDIT SERIAL
    // =========================================================

    private fun showEditSerial(
        record: SerialRecord
    ) {

        val isAdmin =
            currentRole.equals(
                "Admin",
                true
            )

        val isOwner =
            record.createdBy.equals(
                currentUsername,
                true
            )

        val locked =
            record.status == "Completed" &&
            record.createdRole.equals(
                "Operator",
                true
            )

        if (
            !isAdmin &&
            (!isOwner || locked)
        ) {

            toast(
                "এই Serial Edit করার permission নেই"
            )

            return
        }

        currentPage =
            "EDIT"

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
            actionButton(
                "💾   Save Changes",
                GREEN
            ) {

                updateSerial(
                    record,
                    patient.text
                        .toString()
                        .trim(),
                    care.text
                        .toString()
                        .trim(),
                    doctor.text
                        .toString()
                        .trim()
                )
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

        val isAdmin =
            currentRole.equals(
                "Admin",
                true
            )

        val isOwner =
            record.createdBy.equals(
                currentUsername,
                true
            )

        val locked =
            record.status == "Completed" &&
            record.createdRole.equals(
                "Operator",
                true
            )

        if (
            !isAdmin &&
            (!isOwner || locked)
        ) {

            toast(
                "এই Serial Edit করা যাবে না"
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

        val updates =
            hashMapOf<String, Any>(
                "patient" to patient,
                "careOf" to care,
                "doctor" to doctor
            )

        db.child("serials")
            .child(record.id)
            .updateChildren(updates)
            .addOnSuccessListener {

                toast(
                    "Serial আপডেট হয়েছে"
                )

                showTotalSerial()
            }
    }

    // =========================================================
    // DELETE SERIAL
    // =========================================================

    private fun deleteSerial(
        record: SerialRecord
    ) {

        val isAdmin =
            currentRole.equals(
                "Admin",
                true
            )

        val isOwner =
            record.createdBy.equals(
                currentUsername,
                true
            )

        val locked =
            record.status == "Completed" &&
            record.createdRole.equals(
                "Operator",
                true
            )

        if (
            !isAdmin &&
            (!isOwner || locked)
        ) {

            toast(
                "এই Serial Delete করা যাবে না"
            )

            return
        }

        db.child("serials")
            .child(record.id)
            .removeValue()
            .addOnSuccessListener {

                toast(
                    "Serial Delete হয়েছে"
                )

                showTotalSerial()
            }
    }

    // =========================================================
    // DOCTOR WISE
    // =========================================================

    private fun showDoctorWise() {

        currentPage =
            "DOCTOR"

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

        val button =
            text(
                formatDateForUser(date),
                20f,
                DARK_BLUE,
                true
            )

        button.background =
            rounded(
                WHITE,
                16f,
                PURPLE
            )

        root.addView(
            button,
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
                ).format(
                    date.time
                )

            readSerials(key) { records ->

                records
                    .groupBy {
                        it.doctor
                    }
                    .forEach {
                        doctor,
                        doctorRecords ->

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
                            .forEach {

                                list.addView(
                                    serialCard(it)
                                )
                            }
                    }
            }
        }

        button.setOnClickListener {

            DatePickerDialog(
                this,
                { _, y, m, d ->

                    date.set(
                        y,
                        m,
                        d
                    )

                    button.text =
                        formatDateForUser(date)

                    load()
                },
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH),
                date.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        load()

        setContentView(
            screen(root)
        )
    }

    // =========================================================
    // CARE WISE
    // =========================================================

    private fun showCareWise() {

        currentPage =
            "CARE"

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

        val button =
            text(
                formatDateForUser(date),
                20f,
                DARK_BLUE,
                true
            )

        button.background =
            rounded(
                WHITE,
                16f,
                TEAL
            )

        root.addView(
            button,
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
                ).format(
                    date.time
                )

            readSerials(key) { records ->

                records
                    .groupBy {
                        it.careOf
                    }
                    .forEach {
                        care,
                        careRecords ->

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
                            .forEach {

                                list.addView(
                                    serialCard(it)
                                )
                            }
                    }
            }
        }

        button.setOnClickListener {

            DatePickerDialog(
                this,
                { _, y, m, d ->

                    date.set(
                        y,
                        m,
                        d
                    )

                    button.text =
                        formatDateForUser(date)

                    load()
                },
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH),
                date.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        load()

        setContentView(
            screen(root)
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
                    name.text
                        .toString()
                        .trim()
                )
            }
        )

        val list =
            LinearLayout(this)

        list.orientation =
            LinearLayout.VERTICAL

        root.addView(
            space(15)
        )

        root.addView(
            text(
                "বর্তমান ডাক্তার",
                23f,
                DARK_BLUE,
                true
            )
        )

        root.addView(list)

        db.child("doctors")
            .get()
            .addOnSuccessListener {

                list.removeAllViews()

                for (
                    child in it.children
                ) {

                    val value =
                        child.getValue(
                            String::class.java
                        ) ?: continue

                    list.addView(
                        managerRow(
                            value,
                            PURPLE
                        ) {

                            deleteDoctor(value)
                        }
                    )
                }
            }

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

        if (name.isEmpty()) {

            toast(
                "ডাক্তারের নাম লিখুন"
            )

            return
        }

        db.child("doctors")
            .child(safeKey(name))
            .setValue(name)
            .addOnSuccessListener {

                toast(
                    "ডাক্তার Add হয়েছে"
                )

                showDoctorManager()
            }
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

            return
        }

        db.child("doctors")
            .child(safeKey(name))
            .removeValue()
            .addOnSuccessListener {

                toast(
                    "ডাক্তার Delete হয়েছে"
                )

                showDoctorManager()
            }
    }

    // =========================================================
    // CARE MANAGER
    // =========================================================

    private fun showCareManager() {

        currentPage =
            "CARE_MANAGER"

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

        val name =
            input(
                "Care Of নাম"
            )

        root.addView(name)

        root.addView(
            actionButton(
                "➕   Care Of Add করুন",
                TEAL
            ) {

                addCare(
                    name.text
                        .toString()
                        .trim()
                )
            }
        )

        root.addView(
            space(15)
        )

        val list =
            LinearLayout(this)

        list.orientation =
            LinearLayout.VERTICAL

        root.addView(
            text(
                "বর্তমান Care Of",
                23f,
                DARK_BLUE,
                true
            )
        )

        root.addView(list)

        db.child("cares")
            .get()
            .addOnSuccessListener {

                list.removeAllViews()

                for (
                    child in it.children
                ) {

                    val value =
                        child.getValue(
                            String::class.java
                        ) ?: continue

                    list.addView(
                        managerRow(
                            value,
                            TEAL
                        ) {

                            deleteCare(value)
                        }
                    )
                }
            }

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

        db.child("cares")
            .child(safeKey(name))
            .setValue(name)
            .addOnSuccessListener {

                toast(
                    "Care Of Add হয়েছে"
                )

                showCareManager()
            }
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
                "শুধুমাত্র Admin Delete করতে পারবেন"
            )

            return
        }

        db.child("cares")
            .child(safeKey(name))
            .removeValue()
            .addOnSuccessListener {

                toast(
                    "Care Of Delete হয়েছে"
                )

                showCareManager()
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

        currentPage =
            "ADMIN"

        val root =
            rootLayout()

        root.addView(
            text(
                "👑",
                55f,
                PURPLE,
                true
            )
        )

        root.addView(
            text(
                "Admin Control Panel",
                30f,
                DARK_BLUE,
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

        val roleSpinner =
            Spinner(this)

        setupSpinner(
            roleSpinner,
            listOf(
                "Operator",
                "User"
            ),
            "Role নির্বাচন করুন"
        )

        root.addView(
            roleSpinner,
            spinnerParams()
        )

        root.addView(
            actionButton(
                "➕   User / Operator তৈরি করুন",
                PURPLE
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

        root.addView(
            space(20)
        )

        root.addView(
            text(
                "বর্তমান User / Operator",
                23f,
                DARK_BLUE,
                true
            )
        )

        val list =
            LinearLayout(this)

        list.orientation =
            LinearLayout.VERTICAL

        root.addView(list)

        db.child("users")
            .get()
            .addOnSuccessListener {

                list.removeAllViews()

                for (
                    child in it.children
                ) {

                    val user =
                        child.child("username")
                            .getValue(
                                String::class.java
                            ) ?: continue

                    val role =
                        child.child("role")
                            .getValue(
                                String::class.java
                            ) ?: ""

                    if (
                        user.equals(
                            "admin",
                            true
                        )
                    ) continue

                    list.addView(
                        managerRow(
                            "$user\nRole: $role",
                            PURPLE
                        ) {

                            deleteUser(user)
                        }
                    )
                }
            }

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

        if (password.length < 4) {

            toast(
                "Password কমপক্ষে ৪ অক্ষরের হতে হবে"
            )

            return
        }

        if (
            username.contains(
                "."
            ) ||
            username.contains(
                "#"
            ) ||
            username.contains(
                "$"
            ) ||
            username.contains(
                "["
            ) ||
            username.contains(
                "]"
            )
        ) {

            toast(
                "Username-এ . # $ [ ] ব্যবহার করা যাবে না"
            )

            return
        }

        val data =
            hashMapOf<String, Any>(
                "username" to username,
                "password" to hashPassword(password),
                "role" to role,
                "active" to true
            )

        db.child("users")
            .child(safeKey(username))
            .setValue(data)
            .addOnSuccessListener {

                toast(
                    "$role তৈরি হয়েছে"
                )

                showAdminPanel()
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
                "Admin Delete করা যাবে না"
            )

            return
        }

        db.child("users")
            .child(safeKey(username))
            .removeValue()
            .addOnSuccessListener {

                toast(
                    "$username Delete হয়েছে"
                )

                showAdminPanel()
            }
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
            14,
            10,
            10,
            10
        )

        row.background =
            rounded(
                WHITE,
                15f,
                BORDER
            )

        row.layoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                75
            ).apply {

                setMargins(
                    5,
                    4,
                    5,
                    4
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
                    65,
                    55
                )
            )
        }

        return row
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

        spinner.adapter =
            adapter
    }

    private fun spinnerParams():
        LinearLayout.LayoutParams {

        return LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            68
        ).apply {

            setMargins(
                7,
                7,
                7,
                12
            )
        }
    }

    // =========================================================
    // DOCTOR LIST
    // =========================================================

    private fun getDoctorList():
        List<String> {

        return emptyList()
    }

    // =========================================================
    // CARE LIST
    // =========================================================

    private fun getCareList():
        List<String> {

        return emptyList()
    }

    // =========================================================
    // DATE
    // =========================================================

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
    // HASH
    // =========================================================

    private fun hashPassword(
        password: String
    ): String {

        val bytes =
            MessageDigest
                .getInstance(
                    "SHA-256"
                )
                .digest(
                    password.toByteArray()
                )

        return bytes.joinToString("") {

            "%02x".format(it)
        }
    }

    // =========================================================
    // SAFE FIREBASE KEY
    // =========================================================

    private fun safeKey(
        value: String
    ): String {

        return value
            .replace(
                ".",
                "_dot_"
            )
            .replace(
                "#",
                "_hash_"
            )
            .replace(
                "$",
                "_dollar_"
            )
            .replace(
                "[",
                "_open_"
            )
            .replace(
                "]",
                "_close_"
            )
            .replace(
                "/",
                "_slash_"
            )
    }

    // =========================================================
    // DATA TO RECORD
    // =========================================================

    private fun childToRecord(
        child: DataSnapshot
    ): SerialRecord? {

        return try {

            SerialRecord(

                id =
                    child.child("id")
                        .getValue(
                            String::class.java
                        )
                        ?: child.key
                        ?: "",

                date =
                    child.child("date")
                        .getValue(
                            String::class.java
                        )
                        ?: "",

                totalNumber =
                    child.child("totalNumber")
                        .getValue(
                            Int::class.java
                        )
                        ?: 0,

                doctorNumber =
                    child.child("doctorNumber")
                        .getValue(
                            Int::class.java
                        )
                        ?: 0,

                careNumber =
                    child.child("careNumber")
                        .getValue(
                            Int::class.java
                        )
                        ?: 0,

                patient =
                    child.child("patient")
                        .getValue(
                            String::class.java
                        )
                        ?: "",

                careOf =
                    child.child("careOf")
                        .getValue(
                            String::class.java
                        )
                        ?: "",

                doctor =
                    child.child("doctor")
                        .getValue(
                            String::class.java
                        )
                        ?: "",

                status =
                    child.child("status")
                        .getValue(
                            String::class.java
                        )
                        ?: "Waiting",

                createdBy =
                    child.child("createdBy")
                        .getValue(
                            String::class.java
                        )
                        ?: "",

                createdRole =
                    child.child("createdRole")
                        .getValue(
                            String::class.java
                        )
                        ?: "",

                createdTime =
                    child.child("createdTime")
                        .getValue(
                            String::class.java
                        )
                        ?: ""
            )

        } catch (
            e: Exception
        ) {

            null
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

        currentUsername = ""
        currentRole = ""

        getSharedPreferences(
            "MDC_SESSION",
            MODE_PRIVATE
        )
            .edit()
            .clear()
            .apply()

        toast(
            "Logout হয়েছে"
        )

        showLogin()
    }

    // =========================================================
    // ANDROID BACK BUTTON
    // =========================================================

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

    // =========================================================
    // LIFECYCLE
    // =========================================================

    override fun onDestroy() {

        removeRealtime()

        handler.removeCallbacksAndMessages(
            null
        )

        super.onDestroy()
    }
}
