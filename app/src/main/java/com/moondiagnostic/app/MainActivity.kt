package com.moondiagnostic.app

import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private fun text(
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
        if (bold) t.setTypeface(null, Typeface.BOLD)

        t.setPadding(12, 12, 12, 12)

        return t
    }

    private fun button(
        icon: String,
        title: String,
        color: Int
    ): TextView {
        val b = text("$icon\n$title", 15f, Color.DKGRAY, true)

        b.setBackgroundColor(Color.WHITE)

        val params = LinearLayout.LayoutParams(
            0,
            150,
            1f
        )

        params.setMargins(8, 8, 8, 8)
        b.layoutParams = params

        b.setOnClickListener {
            android.widget.Toast.makeText(
                this,
                "$title নির্বাচন করা হয়েছে",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }

        return b
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(Color.rgb(238, 247, 255))
        root.setPadding(18, 20, 18, 10)

        // Header
        val header = LinearLayout(this)
        header.orientation = LinearLayout.VERTICAL
        header.gravity = Gravity.CENTER

        header.addView(
            text(
                "MDC",
                42f,
                Color.rgb(20, 70, 130),
                true
            )
        )

        header.addView(
            text(
                "মুন ডায়াগনস্টিক সেন্টার",
                25f,
                Color.rgb(10, 80, 150),
                true
            )
        )

        header.addView(
            text(
                "সঠিক নির্ণয়, সুস্থ জীবনের প্রত্যয়",
                15f,
                Color.rgb(30, 80, 130),
                false
            )
        )

        root.addView(
            header,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                180
            )
        )

        // Admin
        root.addView(
            text(
                "স্বাগতম, Admin",
                17f,
                Color.DKGRAY,
                true
            )
        )

        // Statistics
        val stats = LinearLayout(this)
        stats.orientation = LinearLayout.HORIZONTAL

        stats.addView(
            text(
                "📅\nআজকের তারিখ\n১০ মে, ২০২৪",
                14f,
                Color.DKGRAY,
                true
            ),
            LinearLayout.LayoutParams(0, 150, 1f)
        )

        stats.addView(
            text(
                "👥\nমোট সিরিয়াল\n54 জন",
                14f,
                Color.DKGRAY,
                true
            ),
            LinearLayout.LayoutParams(0, 150, 1f)
        )

        root.addView(stats)

        val stats2 = LinearLayout(this)
        stats2.orientation = LinearLayout.HORIZONTAL

        stats2.addView(
            text(
                "⏳\nঅপেক্ষমাণ\n28 জন",
                14f,
                Color.DKGRAY,
                true
            ),
            LinearLayout.LayoutParams(0, 150, 1f)
        )

        stats2.addView(
            text(
                "✓\nসম্পন্ন সিরিয়াল\n26 জন",
                14f,
                Color.DKGRAY,
                true
            ),
            LinearLayout.LayoutParams(0, 150, 1f)
        )

        root.addView(stats2)

        // Quick Actions title
        root.addView(
            text(
                "দ্রুত অ্যাকশন",
                20f,
                Color.rgb(20, 70, 120),
                true
            )
        )

        // Four buttons
        val actions = LinearLayout(this)
        actions.orientation = LinearLayout.HORIZONTAL

        actions.addView(
            button("📋", "টোটাল সিরিয়াল", Color.BLUE)
        )

        actions.addView(
            button("➕", "অ্যাড সিরিয়াল", Color.GREEN)
        )

        actions.addView(
            button("👨‍⚕️", "অ্যাড ডাক্তার", Color.ORANGE)
        )

        actions.addView(
            button("👤", "অ্যাড কেয়ার অফ", Color.MAGENTA)
        )

        root.addView(actions)

        // Footer
        root.addView(
            text(
                "\nমুন ডায়াগনস্টিক সেন্টার\nআপনার বিশ্বস্ত স্বাস্থ্যসেবা কেন্দ্র",
                14f,
                Color.GRAY,
                false
            )
        )

        setContentView(root)
    }
}
