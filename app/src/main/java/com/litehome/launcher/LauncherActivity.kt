package com.litehome.launcher

import android.app.Activity
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LauncherActivity : Activity() {
    private val bg = Color.rgb(16, 17, 20)
    private val panel = Color.rgb(29, 30, 35)
    private val white = Color.rgb(246, 246, 248)
    private val muted = Color.rgb(164, 166, 175)
    private lateinit var root: FrameLayout
    private lateinit var homeView: LinearLayout
    private lateinit var drawerView: LinearLayout
    private lateinit var grid: GridLayout
    private lateinit var favoritesGrid: GridLayout
    private lateinit var search: EditText
    private lateinit var clock: TextView
    private lateinit var date: TextView
    private var drawerOpen = false
    private var downX = 0f
    private var downY = 0f
    private var apps: List<ResolveInfo> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        apps = loadApps()
        buildUi()
        updateTime()
    }

    override fun onResume() {
        super.onResume()
        if (::root.isInitialized) {
            apps = loadApps()
            refreshHome()
            if (drawerOpen) renderApps(search.text?.toString().orEmpty())
            updateTime()
        }
    }

    private fun loadApps(): List<ResolveInfo> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return packageManager.queryIntentActivities(intent, 0)
            .filter { it.activityInfo.packageName != packageName }
            .distinctBy { "${it.activityInfo.packageName}/${it.activityInfo.name}" }
            .sortedBy { it.loadLabel(packageManager).toString().lowercase(Locale.getDefault()) }
    }

    private fun buildUi() {
        root = FrameLayout(this).apply { setBackgroundColor(bg) }
        homeView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(34), dp(24), dp(24))
        }
        val topSpace = View(this)
        homeView.addView(topSpace, LinearLayout.LayoutParams(1, 0, 1f))
        clock = TextView(this).apply {
            textSize = 58f; setTextColor(white); typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
            gravity = Gravity.CENTER; includeFontPadding = false
        }
        homeView.addView(clock, LinearLayout.LayoutParams(-1, -2))
        date = TextView(this).apply {
            textSize = 14f; setTextColor(muted); gravity = Gravity.CENTER; setPadding(0, dp(8), 0, 0)
        }
        homeView.addView(date, LinearLayout.LayoutParams(-1, -2))
        val spacer = View(this)
        homeView.addView(spacer, LinearLayout.LayoutParams(1, 0, 1f))
        val favoritesLabel = TextView(this).apply {
            text = "FAVORITES"; textSize = 10f; letterSpacing = 0.16f; setTextColor(muted)
            gravity = Gravity.CENTER; setPadding(0, 0, 0, dp(16))
        }
        homeView.addView(favoritesLabel)
        favoritesGrid = GridLayout(this).apply { columnCount = 4; alignmentMode = GridLayout.ALIGN_BOUNDS }
        homeView.addView(favoritesGrid, LinearLayout.LayoutParams(-1, -2))
        val hint = TextView(this).apply {
            text = "↑  Swipe up for all apps"; textSize = 12f; setTextColor(muted)
            gravity = Gravity.CENTER; setPadding(0, dp(28), 0, dp(6))
        }
        homeView.addView(hint)
        root.addView(homeView, FrameLayout.LayoutParams(-1, -1))

        drawerView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(8))
            setBackgroundColor(bg)
            visibility = View.GONE
        }
        val drawerTitle = TextView(this).apply {
            text = "Apps"; textSize = 28f; setTextColor(white)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            setPadding(dp(4), dp(8), 0, dp(16))
        }
        drawerView.addView(drawerTitle)
        search = EditText(this).apply {
            hint = "Search apps"; textSize = 15f; setTextColor(white); setHintTextColor(muted)
            singleLine = true; setPadding(dp(16), 0, dp(16), 0)
            background = rounded(panel, dp(18))
        }
        drawerView.addView(search, LinearLayout.LayoutParams(-1, dp(48)))
        val scroll = ScrollView(this).apply { isFillViewport = false; clipToPadding = false }
        grid = GridLayout(this).apply { columnCount = 4; useDefaultMargins = false }
        scroll.addView(grid, ScrollView.LayoutParams(-1, -2))
        val scrollLp = LinearLayout.LayoutParams(-1, 0, 1f).apply { topMargin = dp(12) }
        drawerView.addView(scroll, scrollLp)
        root.addView(drawerView, FrameLayout.LayoutParams(-1, -1))
        setContentView(root)
        search.addTextChangedListener(SimpleTextWatcher { renderApps(it) })
        refreshHome()
        renderApps("")
    }

    private fun refreshHome() {
        if (!::homeView.isInitialized) return
        val favorites = favoritesGrid
        favorites.removeAllViews()
        val preferred = listOf("com.android.chrome", "com.google.android.apps.maps", "com.google.android.youtube", "com.android.camera2", "com.google.android.dialer", "com.google.android.apps.messaging", "com.oplus.camera")
        val selected = preferred.mapNotNull { pkg -> apps.firstOrNull { it.activityInfo.packageName == pkg } }.distinctBy { it.activityInfo.packageName }.toMutableList()
        for (app in apps) {
            if (selected.size >= 4) break
            if (selected.none { it.activityInfo.packageName == app.activityInfo.packageName }) selected.add(app)
        }
        selected.take(4).forEach { app -> favorites.addView(appTile(app, true), GridLayout.LayoutParams().apply {
            width = 0; height = dp(92); columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
        }) }
    }

    private fun renderApps(query: String) {
        if (!::grid.isInitialized) return
        grid.removeAllViews()
        val filtered = apps.filter { it.loadLabel(packageManager).toString().contains(query.trim(), true) }
        filtered.forEach { app -> grid.addView(appTile(app, false), GridLayout.LayoutParams().apply {
            width = 0; height = dp(96); columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
        }) }
    }

    private fun appTile(info: ResolveInfo, favorite: Boolean): View {
        val tile = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            setPadding(dp(2), dp(8), dp(2), dp(4)); isClickable = true; isFocusable = true
            setOnClickListener { launch(info) }
        }
        val icon = ImageView(this).apply {
            setImageDrawable(info.loadIcon(packageManager)); scaleType = ImageView.ScaleType.FIT_CENTER
            contentDescription = info.loadLabel(packageManager).toString()
        }
        tile.addView(icon, LinearLayout.LayoutParams(dp(if (favorite) 42 else 38), dp(if (favorite) 42 else 38)))
        val label = TextView(this).apply {
            text = info.loadLabel(packageManager); textSize = if (favorite) 10.5f else 10f
            setTextColor(if (favorite) white else muted); gravity = Gravity.CENTER
            maxLines = 2; ellipsize = android.text.TextUtils.TruncateAt.END
            setPadding(0, dp(7), 0, 0)
        }
        tile.addView(label, LinearLayout.LayoutParams(-1, -2))
        return tile
    }

    private fun launch(info: ResolveInfo) {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            .setClassName(info.activityInfo.packageName, info.activityInfo.name)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try { startActivity(intent) } catch (_: Exception) { }
    }

    private fun updateTime() {
        val now = Date()
        if (::clock.isInitialized) {
            clock.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
            date.text = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(now)
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> { downX = ev.x; downY = ev.y }
            MotionEvent.ACTION_UP -> {
                val dy = ev.y - downY
                val dx = ev.x - downX
                if (kotlin.math.abs(dy) > dp(72) && kotlin.math.abs(dy) > kotlin.math.abs(dx) * 1.25f) {
                    if (dy < 0 && !drawerOpen) { showDrawer(); return true }
                    if (dy > 0 && drawerOpen && downY < dp(100)) { hideDrawer(); return true }
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun showDrawer() {
        drawerOpen = true; drawerView.visibility = View.VISIBLE
        search.setText("")
        search.clearFocus()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    private fun hideDrawer() {
        drawerOpen = false; drawerView.visibility = View.GONE
        search.clearFocus()
    }

    @Deprecated("Deprecated in Android API")
    override fun onBackPressed() {
        if (drawerOpen) hideDrawer() else moveTaskToBack(true)
    }

    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color); cornerRadius = radius.toFloat()
    }
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density + 0.5f).toInt()
}
