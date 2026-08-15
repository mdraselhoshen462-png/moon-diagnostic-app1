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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.LinkedHashMap

class MainActivity : Activity() {

    private val BG = Color.rgb(239, 248, 255)
    private val BLUE = Color.rgb(25, 82, 140)
    private val DARK = Color.rgb(45, 45, 45)
    private val WHITE = Color.WHITE
    private val GREEN = Color.rgb(20, 130, 115)
    private val RED = Color.rgb(190, 55, 55)

    private val users = LinkedHashMap<String, UserAccount>()

    private var currentUsername = ""
    private var currentRole = ""

    data class UserAccount(
        var password: String,
        var role: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Default Admin
        users["admin"] = UserAccount("admin123", "Admin")

        showLogin()
    }

    // --------------------------------------------------
    // LOGIN
    // --------------------------------------------------

    private fun showLogin() {

        val root = ScrollView(this)

        root.setBackgroundColor(BG)

        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        box.gravity = Gravity.CENTER_HORIZONTAL
        box.setPadding(25, 80, 25, 40)

        box.addView(
            label(
                "MDC",
                48f,
                BLUE,
                true
            )
        )

        box.addView(
            label(
                "মুন ডায়াগনস্টিক সেন্টার",
                25f,
                BLUE,
                true
            )
        )

        box.addView(
            label(
                "লগইন করুন",
                23f,
                DARK,
                true
            )
        )

        addSpace(box, 25)

        val username = EditText(this)
        username.hint = "Username"
        username.textSize = 17f
        username.setSingleLine(true)
        username.setPadding(20, 15, 20, 15)

        box.addView(
            username,
            matchParams(60)
        )

        addSpace(box, 12)

        val password = EditText(this)
        password.hint = "Password"
        password.textSize = 17f
        password.inputType =
            android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD

        password.setSingleLine(true)
        password.setPadding(20, 15, 20, 15)

        box.addView(
            password,
            matchParams(60)
        )

        addSpace(box, 18)

        val login = actionButton(
            "🔐  লগইন",
            BLUE
        )

        box.addView(
            login,
            matchParams(58)
        )

        login.setOnClickListener {

            val u = username.text.toString().trim()
            val p = password.text.toString()

            val account = users[u]

            if (account != null && account.password == p) {

                currentUsername = u
                currentRole = account.role

                Toast.makeText(
                    this,
                    "লগইন সফল হয়েছে",
                    Toast.LENGTH_SHORT
                ).show()

                showDashboard()

            } else {

                Toast.makeText(
                    this,
                    "Username অথবা Password ভুল",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        addSpace(box, 35)

        box.addView(
            label(
                "Moon Diagnostic Center",
                15f,
                Color.GRAY,
                false
            )
        )

        box.addView(
            label(
                "সঠিক নির্ণয়, সুস্থ জীবনের প্রত্যয়",
                14f,
                Color.GRAY,
                false
            )
        )

        root.addView(box)

        setContentView(root)
    }

    // --------------------------------------------------
    // DASHBOARD
    // --------------------------------------------------

    private fun showDashboard() {

        val scroll = ScrollView(this)
        scroll.setBackgroundColor(BG)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(18, 30, 18, 40)

        root.addView(
            label(
                "MDC",
                42f,
                BLUE,
                true
            )
        )

        root.addView(
            label(
                "স্বাগতম, $currentUsername",
                20f,
                DARK,
                true
            )
        )

        root.addView(
            label(
                "Role: $currentRole",
                16f,
                GREEN,
                true
            )
        )

        addSpace(root, 15)

        val logout = actionButton(
            "🚪  Logout",
            RED
        )

        root.addView(
            logout,
            matchParams(52)
        )

        logout.setOnClickListener {

            currentUsername = ""
            currentRole = ""

            showLogin()
        }

        addSpace(root, 18)

        val date = SimpleDateFormat(
            "dd-MM-yyyy",
            Locale.getDefault()
        ).format(Date())

        root.addView(
            label(
                "📅\nআজকের তারিখ\n$date",
                16f,
                DARK,
                true
            )
        )

        addSpace(root, 10)

        val stats = LinearLayout(this)
        stats.orientation = LinearLayout.HORIZONTAL

        stats.addView(
            statCard("👥", "মোট সিরিয়াল", "54 জন"),
            weightParams()
        )

        stats.addView(
            statCard("⏳", "অপেক্ষমাণ", "28 জন"),
            weightParams()
        )

        root.addView(stats)

        val stats2 = LinearLayout(this)
        stats2.orientation = LinearLayout.HORIZONTAL

        stats2.addView(
            statCard("✓", "সম্পন্ন", "26 জন"),
            weightParams()
        )

        stats2.addView(
            statCard("❌", "বাতিল", "0 জন"),
            weightParams()
        )

        root.addView(stats2)

        addSpace(root, 15)

        root.addView(
            label(
                "দ্রুত অ্যাকশন",
                24f,
                BLUE,
                true
            )
        )

        addAction(root, "📋  টোটাল সিরিয়াল") {
            Toast.makeText(
                this,
                "টোটাল সিরিয়াল",
                Toast.LENGTH_SHORT
            ).show()
        }

        addAction(root, "➕  অ্যাড সিরিয়াল") {
            Toast.makeText(
                this,
                "অ্যাড সিরিয়াল",
                Toast.LENGTH_SHORT
            ).show()
        }

        addAction(root, "👨‍⚕️  অ্যাড ডাক্তার") {
            Toast.makeText(
                this,
                "অ্যাড ডাক্তার",
                Toast.LENGTH_SHORT
            ).show()
        }

        addAction(root, "👤  অ্যাড কেয়ার অফ") {
            Toast.makeText(
                this,
                "অ্যাড কেয়ার অফ",
                Toast.LENGTH_SHORT
            ).show()
        }

        addSpace(root, 15)

        root.addView(
            label(
                "ডাক্তার ওয়াইজ সিরিয়াল",
                22f,
                BLUE,
                true
            )
        )

        root.addView(
            label(
                "ডাক্তার নির্বাচন করে তার সিরিয়ালগুলো দেখা যাবে",
                15f,
                DARK,
                false
            )
        )

        addSpace(root, 15)

        root.addView(
            label(
                "কেয়ার ওয়াইজ সিরিয়াল",
                22f,
                BLUE,
                true
            )
        )

        root.addView(
            label(
                "কেয়ার অফ নির্বাচন করে সংশ্লিষ্ট সিরিয়ালগুলো দেখা যাবে",
                15f,
                DARK,
                false
            )
        )

        // Admin only
        if (currentRole == "Admin") {

            addSpace(root, 20)

            root.addView(
                label(
                    "👑 Admin Control Panel",
                    23f,
                    BLUE,
                    true
                )
            )

            root.addView(
                label(
                    "User এবং Operator পরিচালনা করুন",
                    15f,
                    DARK,
                    false
                )
            )

            addSpace(root, 12)

            val adminButton = actionButton(
                "⚙️  Admin Control Panel",
                BLUE
            )

            root.addView(
                adminButton,
                matchParams(58)
            )

            adminButton.setOnClickListener {
                showAdminPanel()
            }
        }

        addSpace(root, 30)

        root.addView(
            label(
                "মুন ডায়াগনস্টিক সেন্টার",
                15f,
                Color.GRAY,
                true
            )
        )

        root.addView(
            label(
                "আপনার বিশ্বস্ত স্বাস্থ্যসেবা কেন্দ্র",
                14f,
                Color.GRAY,
                false
            )
        )

        scroll.addView(root)

        setContentView(scroll)
    }

    // --------------------------------------------------
    // ADMIN PANEL
    // --------------------------------------------------

    private fun showAdminPanel() {

        if (currentRole != "Admin") {
            Toast.makeText(
                this,
                "শুধুমাত্র Admin এই পেজ ব্যবহার করতে পারবেন",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val scroll = ScrollView(this)
        scroll.setBackgroundColor(BG)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(22, 35, 22, 40)

        root.addView(
            label(
                "👑 Admin Control Panel",
                25f,
                BLUE,
                true
            )
        )

        root.addView(
            label(
                "User এবং Operator পরিচালনা করুন",
                16f,
                DARK,
                false
            )
        )

        addSpace(root, 20)

        val username = EditText(this)
        username.hint = "নতুন Username"
        username.textSize = 17f
        username.setSingleLine(true)
        username.setPadding(18, 12, 18, 12)

        root.addView(
            username,
            matchParams(58)
        )

        addSpace(root, 10)

        val password = EditText(this)
        password.hint = "নতুন Password"
        password.textSize = 17f
        password.setSingleLine(true)
        password.setPadding(18, 12, 18, 12)

        root.addView(
            password,
            matchParams(58)
        )

        addSpace(root, 12)

        val roleSpinner = Spinner(this)

        val roles = arrayOf(
            "Operator",
            "User"
        )

        roleSpinner.adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                roles
            )

        root.addView(
            roleSpinner,
            matchParams(55)
        )

        addSpace(root, 12)

        val create = actionButton(
            "➕  নতুন User / Operator তৈরি করুন",
            GREEN
        )

        root.addView(
            create,
            matchParams(58)
        )

        create.setOnClickListener {

            val u = username.text.toString().trim()
            val p = password.text.toString()
            val role = roleSpinner.selectedItem.toString()

            if (u.isEmpty() || p.isEmpty()) {

                Toast.makeText(
                    this,
                    "Username এবং Password দিন",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            if (users.containsKey(u)) {

                Toast.makeText(
                    this,
                    "এই Username আগে থেকেই আছে",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            users[u] = UserAccount(
                p,
                role
            )

            username.text.clear()
            password.text.clear()

            Toast.makeText(
                this,
                "$role সফলভাবে তৈরি হয়েছে",
                Toast.LENGTH_SHORT
            ).show()

            showAdminPanel()
        }

        addSpace(root, 25)

        root.addView(
            label(
                "বর্তমান User / Operator",
                22f,
                BLUE,
                true
            )
        )

        users.forEach { (name, account) ->

            if (name != "admin") {

                val card = LinearLayout(this)
                card.orientation = LinearLayout.VERTICAL
                card.setPadding(18, 15, 18, 15)

                card.background = rounded(
                    WHITE,
                    14
                )

                val info = label(
                    "👤  $name\nRole: ${account.role}",
                    17f,
                    DARK,
                    true
                )

                card.addView(info)

                addSpace(card, 8)

                val delete = actionButton(
                    "🗑️  Delete",
                    RED
                )

                card.addView(
                    delete,
                    matchParams(48)
                )

                delete.setOnClickListener {

                    users.remove(name)

                    Toast.makeText(
                        this,
                        "$name মুছে ফেলা হয়েছে",
                        Toast.LENGTH_SHORT
                    ).show()

                    showAdminPanel()
                }

                val params =
                    LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )

                params.setMargins(0, 10, 0, 10)

                root.addView(card, params)
            }
        }

        addSpace(root, 20)

        val back = actionButton(
            "← Dashboard-এ ফিরে যান",
            BLUE
        )

        root.addView(
            back,
            matchParams(55)
        )

        back.setOnClickListener {
            showDashboard()
        }

        scroll.addView(root)

        setContentView(scroll)
    }

    // --------------------------------------------------
    // UI HELPERS
    // --------------------------------------------------

    private fun label(
        value: String,
        size: Float,
        color: Int,
        bold: Boolean
    ): TextView {

        val t = TextView(this)

        t.text = value
        t.textSize = size
        t.setTextColor(color)
        t.gravity = Gravity.CENTER
        t.setPadding(10, 8, 10, 8)

        if (bold) {
            t.setTypeface(
                null,
                Typeface.BOLD
            )
        }

        return t
    }

    private fun actionButton(
        title: String,
        color: Int
    ): TextView {

        val b = TextView(this)

        b.text = title
        b.textSize = 16f
        b.setTextColor(Color.WHITE)
        b.gravity = Gravity.CENTER
        b.setTypeface(null, Typeface.BOLD)
        b.setPadding(15, 10, 15, 10)

        b.background = rounded(
            color,
            12
        )

        return b
    }

    private fun statCard(
        icon: String,
        title: String,
        value: String
    ): LinearLayout {

        val card = LinearLayout(this)

        card.orientation =
            LinearLayout.VERTICAL

        card.gravity = Gravity.CENTER

        card.setPadding(8, 12, 8, 12)

        card.background = rounded(
            WHITE,
            12
        )

        card.addView(
            label(
                icon,
                25f,
                DARK,
                false
            )
        )

        card.addView(
            label(
                title,
                16f,
                DARK,
                true
            )
        )

        card.addView(
            label(
                value,
                17f,
                DARK,
                true
            )
        )

        val p =
            LinearLayout.LayoutParams(
                0,
                125,
                1f
            )

        p.setMargins(5, 5, 5, 5)

        card.layoutParams = p

        return card
    }

    private fun addAction(
        root: LinearLayout,
        title: String,
        click: () -> Unit
    ) {

        val b = actionButton(
            title,
            BLUE
        )

        root.addView(
            b,
            matchParams(58)
        )

        b.setOnClickListener {
            click()
        }

        addSpace(root, 8)
    }

    private fun rounded(
        color: Int,
        radius: Int
    ): GradientDrawable {

        val d = GradientDrawable()

        d.setColor(color)
        d.cornerRadius =
            radius.toFloat()

        d.setStroke(
            1,
            Color.rgb(220, 230, 240)
        )

        return d
    }

    private fun matchParams(
        height: Int
    ): LinearLayout.LayoutParams {

        return LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            height
        )
    }

    private fun weightParams():
            LinearLayout.LayoutParams {

        return LinearLayout.LayoutParams(
            0,
            125,
            1f
        )
    }

    private fun addSpace(
        root: LinearLayout,
        height: Int
    ) {

        val space = Space(this)

        root.addView(
            space,
            LinearLayout.LayoutParams(
                1,
                height
            )
        )
    }
}
