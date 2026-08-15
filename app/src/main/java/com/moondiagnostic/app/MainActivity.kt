package com.moondiagnostic.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity() {

    private lateinit var root: LinearLayout

    private val PREF = "MDC_APP"

    private val users = mutableListOf(
        User("admin", "admin123", "Admin"),
        User("operator", "operator123", "Operator"),
        User("user", "user123", "User")
    )

    data class User(
        var username: String,
        var password: String,
        var role: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val savedUser = getSharedPreferences(PREF, MODE_PRIVATE)
            .getString("logged_user", null)

        val savedRole = getSharedPreferences(PREF, MODE_PRIVATE)
            .getString("logged_role", null)

        if (savedUser != null && savedRole != null) {
            showDashboard(savedUser, savedRole)
        } else {
            showLogin()
        }
    }

    // ---------------------------------------------------------
    // COMMON TEXT
    // ---------------------------------------------------------

    private fun makeText(
        value: String,
        size: Float,
        color: Int = Color.DKGRAY,
        bold: Boolean = false
    ): TextView {

        val t = TextView(this)

        t.text = value
        t.textSize = size
        t.setTextColor(color)
        t.gravity = Gravity.CENTER
        t.setPadding(12, 12, 12, 12)

        if (bold) {
            t.setTypeface(null, Typeface.BOLD)
        }

        return t
    }

    private fun makeEditText(
        hint: String,
        password: Boolean = false
    ): EditText {

        val e = EditText(this)

        e.hint = hint
        e.textSize = 16f
        e.setTextColor(Color.DKGRAY)
        e.setHintTextColor(Color.GRAY)
        e.setPadding(18, 10, 18, 10)

        if (password) {
            e.inputType =
                android.text.InputType.TYPE_CLASS_TEXT or
                        android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        e.background = android.graphics.drawable.GradientDrawable().apply {
            setColor(Color.WHITE)
            setStroke(2, Color.rgb(20, 130, 120))
            cornerRadius = 12f
        }

        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            60
        )

        params.setMargins(20, 8, 20, 8)

        e.layoutParams = params

        return e
    }

    private fun makeButton(
        title: String,
        background: Int = Color.rgb(20, 100, 150)
    ): Button {

        val b = Button(this)

        b.text = title
        b.textSize = 16f
        b.setTextColor(Color.WHITE)
        b.setTypeface(null, Typeface.BOLD)
        b.setBackgroundColor(background)

        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            58
        )

        params.setMargins(20, 8, 20, 8)

        b.layoutParams = params

        return b
    }

    // ---------------------------------------------------------
    // BACKGROUND
    // ---------------------------------------------------------

    private fun createRoot(): LinearLayout {

        val layout = LinearLayout(this)

        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER_HORIZONTAL

        layout.setBackgroundColor(
            Color.rgb(238, 247, 255)
        )

        layout.setPadding(20, 20, 20, 20)

        return layout
    }

    // ---------------------------------------------------------
    // LOGIN PAGE
    // ---------------------------------------------------------

    private fun showLogin() {

        root = createRoot()

        val scroll = ScrollView(this)

        val content = LinearLayout(this)

        content.orientation = LinearLayout.VERTICAL
        content.gravity = Gravity.CENTER_HORIZONTAL

        content.setPadding(0, 80, 0, 30)

        // MDC LOGO

        content.addView(
            makeText(
                "MDC",
                52f,
                Color.rgb(20, 75, 135),
                true
            ),
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                90
            )
        )

        content.addView(
            makeText(
                "মুন ডায়াগনস্টিক সেন্টার",
                24f,
                Color.rgb(10, 75, 130),
                true
            )
        )

        content.addView(
            makeText(
                "লগইন করুন",
                24f,
                Color.rgb(20, 70, 120),
                true
            )
        )

        content.addView(
            makeText(
                "অনুমোদিত Username ও Password দিয়ে প্রবেশ করুন",
                14f,
                Color.DKGRAY
            )
        )

        // USERNAME

        val username = makeEditText(
            "Username"
        )

        content.addView(username)

        // PASSWORD

        val password = makeEditText(
            "Password",
            true
        )

        content.addView(password)

        // ROLE

        content.addView(
            makeText(
                "Role নির্বাচন করুন",
                16f,
                Color.DKGRAY,
                true
            )
        )

        val roleSpinner = Spinner(this)

        val roles = arrayOf(
            "Admin",
            "Operator",
            "User"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            roles
        )

        roleSpinner.adapter = adapter

        val spinnerParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            60
        )

        spinnerParams.setMargins(20, 5, 20, 10)

        roleSpinner.layoutParams = spinnerParams

        content.addView(roleSpinner)

        // LOGIN BUTTON

        val loginButton = makeButton(
            "🔐  লগইন করুন",
            Color.rgb(15, 120, 110)
        )

        content.addView(loginButton)

        // LOGIN ACTION

        loginButton.setOnClickListener {

            val enteredUsername =
                username.text.toString().trim()

            val enteredPassword =
                password.text.toString()

            val selectedRole =
                roleSpinner.selectedItem.toString()

            if (enteredUsername.isEmpty()) {

                Toast.makeText(
                    this,
                    "Username লিখুন",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            if (enteredPassword.isEmpty()) {

                Toast.makeText(
                    this,
                    "Password লিখুন",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val foundUser = users.find {

                it.username == enteredUsername &&
                        it.password == enteredPassword &&
                        it.role == selectedRole

            }

            if (foundUser != null) {

                getSharedPreferences(
                    PREF,
                    MODE_PRIVATE
                )
                    .edit()
                    .putString(
                        "logged_user",
                        foundUser.username
                    )
                    .putString(
                        "logged_role",
                        foundUser.role
                    )
                    .apply()

                Toast.makeText(
                    this,
                    "লগইন সফল হয়েছে",
                    Toast.LENGTH_SHORT
                ).show()

                showDashboard(
                    foundUser.username,
                    foundUser.role
                )

            } else {

                Toast.makeText(
                    this,
                    "Username, Password অথবা Role ভুল",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        // FOOTER

        content.addView(
            makeText(
                "\nMoon Diagnostic Center\nসঠিক নির্ণয়, সুস্থ জীবনের প্রত্যয়",
                14f,
                Color.GRAY
            )
        )

        scroll.addView(content)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        setContentView(root)
    }

    // ---------------------------------------------------------
    // DASHBOARD
    // ---------------------------------------------------------

    private fun showDashboard(
        username: String,
        role: String
    ) {

        root = createRoot()

        val scroll = ScrollView(this)

        val content = LinearLayout(this)

        content.orientation = LinearLayout.VERTICAL
        content.gravity = Gravity.CENTER_HORIZONTAL

        // HEADER

        content.addView(
            makeText(
                "MDC",
                44f,
                Color.rgb(20, 70, 130),
                true
            )
        )

        content.addView(
            makeText(
                "স্বাগতম, $username",
                19f,
                Color.DKGRAY,
                true
            )
        )

        content.addView(
            makeText(
                "Role: $role",
                15f,
                Color.rgb(20, 100, 130),
                true
            )
        )

        // LOGOUT

        val logoutButton = makeButton(
            "🚪  লগআউট",
            Color.rgb(190, 60, 60)
        )

        content.addView(logoutButton)

        logoutButton.setOnClickListener {

            getSharedPreferences(
                PREF,
                MODE_PRIVATE
            )
                .edit()
                .clear()
                .apply()

            showLogin()
        }

        // DATE

        val date = SimpleDateFormat(
            "dd-MM-yyyy",
            Locale.getDefault()
        ).format(Date())

        content.addView(
            makeText(
                "📅\nআজকের তারিখ\n$date",
                15f,
                Color.DKGRAY,
                true
            )
        )

        // STATISTICS

        val stats = LinearLayout(this)

        stats.orientation = LinearLayout.HORIZONTAL

        stats.addView(
            makeStat(
                "👥",
                "মোট সিরিয়াল",
                "54 জন"
            )
        )

        stats.addView(
            makeStat(
                "⏳",
                "অপেক্ষমাণ",
                "28 জন"
            )
        )

        content.addView(stats)

        val stats2 = LinearLayout(this)

        stats2.orientation = LinearLayout.HORIZONTAL

        stats2.addView(
            makeStat(
                "✓",
                "সম্পন্ন",
                "26 জন"
            )
        )

        stats2.addView(
            makeStat(
                "❌",
                "বাতিল",
                "0 জন"
            )
        )

        content.addView(stats2)

        // QUICK ACTION

        content.addView(
            makeText(
                "দ্রুত অ্যাকশন",
                22f,
                Color.rgb(20, 70, 120),
                true
            )
        )

        val actions = LinearLayout(this)

        actions.orientation = LinearLayout.VERTICAL

        val totalButton =
            makeButton("📋  টোটাল সিরিয়াল")

        val serialButton =
            makeButton("➕  অ্যাড সিরিয়াল")

        val doctorButton =
            makeButton("👨‍⚕️  অ্যাড ডাক্তার")

        val careButton =
            makeButton("👤  অ্যাড কেয়ার অফ")

        actions.addView(totalButton)
        actions.addView(serialButton)
        actions.addView(doctorButton)
        actions.addView(careButton)

        content.addView(actions)

        totalButton.setOnClickListener {

            Toast.makeText(
                this,
                "টোটাল সিরিয়াল নির্বাচন করা হয়েছে",
                Toast.LENGTH_SHORT
            ).show()
        }

        serialButton.setOnClickListener {

            Toast.makeText(
                this,
                "অ্যাড সিরিয়াল নির্বাচন করা হয়েছে",
                Toast.LENGTH_SHORT
            ).show()
        }

        doctorButton.setOnClickListener {

            if (role == "Admin") {

                Toast.makeText(
                    this,
                    "অ্যাড ডাক্তার - Admin Access",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                Toast.makeText(
                    this,
                    "এই কাজটি শুধুমাত্র Admin করতে পারবেন",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        careButton.setOnClickListener {

            if (role == "Admin") {

                Toast.makeText(
                    this,
                    "অ্যাড কেয়ার অফ - Admin Access",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                Toast.makeText(
                    this,
                    "এই কাজটি শুধুমাত্র Admin করতে পারবেন",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        // DOCTOR

        content.addView(
            makeText(
                "ডাক্তার ওয়াইজ সিরিয়াল",
                20f,
                Color.rgb(20, 70, 120),
                true
            )
        )

        content.addView(
            makeText(
                "ডাক্তার নির্বাচন করে তার সিরিয়ালগুলো দেখা যাবে",
                14f
            )
        )

        // CARE OF

        content.addView(
            makeText(
                "কেয়ার ওয়াইজ সিরিয়াল",
                20f,
                Color.rgb(20, 70, 120),
                true
            )
        )

        content.addView(
            makeText(
                "কেয়ার অফ নির্বাচন করে সংশ্লিষ্ট সিরিয়ালগুলো দেখা যাবে",
                14f
            )
        )

        // ADMIN PANEL

        if (role == "Admin") {

            content.addView(
                makeText(
                    "👑 Admin Control Panel",
                    21f,
                    Color.rgb(20, 70, 120),
                    true
                )
            )

            val userManageButton =
                makeButton(
                    "👥  User / Operator Management",
                    Color.rgb(90, 80, 150)
                )

            content.addView(userManageButton)

            userManageButton.setOnClickListener {

                showUserManagement()
            }
        }

        content.addView(
            makeText(
                "\nমুন ডায়াগনস্টিক সেন্টার\nআপনার বিশ্বস্ত স্বাস্থ্যসেবা কেন্দ্র",
                14f,
                Color.GRAY
            )
        )

        scroll.addView(content)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        setContentView(root)
    }

    // ---------------------------------------------------------
    // STAT CARD
    // ---------------------------------------------------------

    private fun makeStat(
        icon: String,
        title: String,
        value: String
    ): TextView {

        val t = makeText(
            "$icon\n$title\n$value",
            14f,
            Color.DKGRAY,
            true
        )

        val params = LinearLayout.LayoutParams(
            0,
            130,
            1f
        )

        params.setMargins(8, 8, 8, 8)

        t.layoutParams = params

        t.setBackgroundColor(Color.WHITE)

        return t
    }

    // ---------------------------------------------------------
    // ADMIN USER MANAGEMENT
    // ---------------------------------------------------------

    private fun showUserManagement() {

        root = createRoot()

        val scroll = ScrollView(this)

        val content = LinearLayout(this)

        content.orientation = LinearLayout.VERTICAL

        content.gravity = Gravity.CENTER_HORIZONTAL

        content.addView(
            makeText(
                "👑 Admin Control Panel",
                24f,
                Color.rgb(20, 70, 120),
                true
            )
        )

        content.addView(
            makeText(
                "User এবং Operator তৈরি / মুছে ফেলুন",
                15f,
                Color.DKGRAY
            )
        )

        val username =
            makeEditText("নতুন Username")

        val password =
            makeEditText("নতুন Password", true)

        content.addView(username)
        content.addView(password)

        val roleSpinner = Spinner(this)

        val roleList = arrayOf(
            "Operator",
            "User"
        )

        roleSpinner.adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                roleList
            )

        content.addView(roleSpinner)

        val addButton =
            makeButton(
                "➕  নতুন User তৈরি করুন",
                Color.rgb(15, 120, 110)
            )

        content.addView(addButton)

        addButton.setOnClickListener {

            val newUsername =
                username.text.toString().trim()

            val newPassword =
                password.text.toString()

            val newRole =
                roleSpinner.selectedItem.toString()

            if (newUsername.isEmpty() ||
                newPassword.isEmpty()
            ) {

                Toast.makeText(
                    this,
                    "Username ও Password লিখুন",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            if (newUsername == "admin") {

                Toast.makeText(
                    this,
                    "এই Username ব্যবহার করা যাবে না",
                    Toast.LENGTH_LONG
                ).show()

                return@setOnClickListener
            }

            val exists = users.any {
                it.username == newUsername
            }

            if (exists) {

                Toast.makeText(
                    this,
                    "এই Username ইতিমধ্যে আছে",
                    Toast.LENGTH_LONG
                ).show()

            } else {

                users.add(
                    User(
                        newUsername,
                        newPassword,
                        newRole
                    )
                )

                username.text.clear()
                password.text.clear()

                Toast.makeText(
                    this,
                    "নতুন $newRole তৈরি হয়েছে",
                    Toast.LENGTH_SHORT
                ).show()

                showUserManagement()
            }
        }

        content.addView(
            makeText(
                "বর্তমান User / Operator",
                20f,
                Color.rgb(20, 70, 120),
                true
            )
        )

        for (u in users) {

            if (u.role != "Admin") {

                val row =
                    LinearLayout(this)

                row.orientation =
                    LinearLayout.HORIZONTAL

                row.gravity =
                    Gravity.CENTER_VERTICAL

                row.setPadding(
                    10,
                    5,
                    10,
                    5
                )

                val info =
                    makeText(
                        "${u.username}   (${u.role})",
                        16f,
                        Color.DKGRAY,
                        true
                    )

                val delete =
                    Button(this)

                delete.text = "মুছুন"

                delete.setTextColor(
                    Color.WHITE
                )

                delete.setBackgroundColor(
                    Color.rgb(190, 60, 60)
                )

                val infoParams =
                    LinearLayout.LayoutParams(
                        0,
                        60,
                        1f
                    )

                row.addView(
                    info,
                    infoParams
                )

                row.addView(
                    delete,
                    LinearLayout.LayoutParams(
                        100,
                        55
                    )
                )

                delete.setOnClickListener {

                    users.remove(u)

                    Toast.makeText(
                        this,
                        "${u.username} মুছে ফেলা হয়েছে",
                        Toast.LENGTH_SHORT
                    ).show()

                    showUserManagement()
                }

                content.addView(row)
            }
        }

        val back =
            makeButton(
                "← Dashboard-এ ফিরে যান",
                Color.rgb(80, 80, 80)
            )

        content.addView(back)

        back.setOnClickListener {

            val savedUser =
                getSharedPreferences(
                    PREF,
                    MODE_PRIVATE
                ).getString(
                    "logged_user",
                    "admin"
                )

            val savedRole =
                getSharedPreferences(
                    PREF,
                    MODE_PRIVATE
                ).getString(
                    "logged_role",
                    "Admin"
                )

            showDashboard(
                savedUser ?: "admin",
                savedRole ?: "Admin"
            )
        }

        scroll.addView(content)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        setContentView(root)
    }
}
