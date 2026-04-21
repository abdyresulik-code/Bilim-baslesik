package com.localstream.app

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.webkit.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val LOCKED_PATH = "/playlist.php"

    private lateinit var webView: WebView
    private lateinit var inputPanel: LinearLayout
    private lateinit var etIp: EditText
    private lateinit var btnGo: Button
    private lateinit var btnSettings: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences("cfg", Context.MODE_PRIVATE)

        webView      = findViewById(R.id.webView)
        inputPanel   = findViewById(R.id.inputPanel)
        etIp         = findViewById(R.id.etIp)
        btnGo        = findViewById(R.id.btnGo)
        btnSettings  = findViewById(R.id.btnSettings)

        webView.settings.apply {
            javaScriptEnabled    = true
            domStorageEnabled    = true
            loadWithOverviewMode = true
            useWideViewPort      = true
            builtInZoomControls  = false
            displayZoomControls  = false
            setSupportZoom(false)
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                val targetUrl   = request.url.toString()
                val allowedIp   = prefs.getString("ip", "") ?: ""
                val allowedBase = "http://$allowedIp$LOCKED_PATH"
                return !targetUrl.startsWith(allowedBase)
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {}
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(msg: ConsoleMessage) = true
        }

        btnGo.setOnClickListener {
            val ip = etIp.text.toString().trim()
            if (ip.isEmpty()) {
                Toast.makeText(this, "IP ýazyň", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            saveAndLoad(ip, prefs)
        }

        btnSettings.setOnClickListener {
            showSettingsDialog(prefs)
        }

        val savedIp = prefs.getString("ip", "") ?: ""
        if (savedIp.isNotEmpty()) {
            saveAndLoad(savedIp, prefs)
        }
    }

    private fun saveAndLoad(ip: String, prefs: android.content.SharedPreferences) {
        prefs.edit().putString("ip", ip).apply()
        webView.loadUrl("http://$ip$LOCKED_PATH")
        inputPanel.visibility  = View.GONE
        btnSettings.visibility = View.VISIBLE
    }

    private fun showSettingsDialog(prefs: android.content.SharedPreferences) {
        val currentIp = prefs.getString("ip", "") ?: ""

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(64, 48, 64, 32)
            setBackgroundColor(Color.parseColor("#1a1a1a"))
        }

        val label = TextView(this).apply {
            text = "Server IP adresini üýtget"
            textSize = 14f
            setTextColor(Color.parseColor("#aaaaaa"))
            setPadding(0, 0, 0, 16)
        }

        val input = EditText(this).apply {
            setText(currentIp)
            setTextColor(Color.WHITE)
            setHintTextColor(Color.parseColor("#555555"))
            hint = "192.168.x.x"
            textSize = 16f
            background = getDrawable(R.drawable.input_bg)
            setPadding(40, 28, 40, 28)
            setSingleLine(true)
        }

        val btnSave = Button(this).apply {
            text = "Sakla we birikdir"
            setTextColor(Color.WHITE)
            textSize = 14f
            background = getDrawable(R.drawable.btn_bg)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 120
            ).also { it.topMargin = 32 }
        }

        layout.addView(label)
        layout.addView(input)
        layout.addView(btnSave)

        val dialog = AlertDialog.Builder(this)
            .setView(layout)
            .create()

        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        }

        btnSave.setOnClickListener {
            val newIp = input.text.toString().trim()
            if (newIp.isEmpty()) {
                Toast.makeText(this, "IP ýazyň", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            dialog.dismiss()
            saveAndLoad(newIp, prefs)
            Toast.makeText(this, "Birikdirildi: $newIp", Toast.LENGTH_SHORT).show()
        }

        dialog.show()

        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.88).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack()
    }
}
