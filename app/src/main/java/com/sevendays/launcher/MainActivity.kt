package com.sevendays.launcher

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.documentfile.provider.DocumentFile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("launcher", MODE_PRIVATE) }
    private lateinit var status: TextView
    private lateinit var details: TextView
    private lateinit var play: Button
    private val pickFolder = 41
    private val bg = Color.rgb(17, 19, 20)
    private val accent = Color.rgb(190, 65, 35)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
        prefs.getString("game_tree", null)?.let { scanGame(Uri.parse(it)) }
        log("Launcher started")
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 22, 28, 22)
            setBackgroundColor(bg)
        }
        root.addView(TextView(this).apply {
            text = "7 DAYS TO DIE"
            textSize = 30f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            letterSpacing = 0.12f
        }, LinearLayout.LayoutParams(-1, 70))
        root.addView(TextView(this).apply {
            text = "ANDROID LAUNCHER  •  PROTOTYPE 0.2.0"
            textSize = 12f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(-1, 32))
        status = TextView(this).apply {
            text = "Папка игры не выбрана"
            textSize = 17f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }
        root.addView(status, LinearLayout.LayoutParams(-1, 54))
        details = TextView(this).apply {
            text = "Выбери папку с установленной PC-версией игры."
            textSize = 14f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
        }
        root.addView(details, LinearLayout.LayoutParams(-1, 0, 1f))
        val choose = makeButton("ВЫБРАТЬ ПАПКУ ИГРЫ")
        choose.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            startActivityForResult(intent, pickFolder)
        }
        root.addView(choose, buttonParams())
        play = makeButton("ИГРАТЬ")
        play.isEnabled = false
        play.alpha = 0.5f
        play.setOnClickListener {
            status.text = "Файлы найдены"
            details.text = "Для запуска PC-версии требуется совместимый Windows runtime (ARM64/Box64/Wine и графический слой). Он пока не подключён."
            log("Play requested; runtime is not integrated")
        }
        root.addView(play, buttonParams())
        val logs = makeButton("ПОКАЗАТЬ ЖУРНАЛ")
        logs.setOnClickListener { details.text = getLogText() }
        root.addView(logs, buttonParams())
        root.addView(TextView(this).apply {
            text = "Сканирование файлов • Runtime подключается отдельным этапом"
            textSize = 11f
            setTextColor(Color.GRAY)
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(-1, 34))
        setContentView(root)
    }

    private fun makeButton(label: String) = Button(this).apply {
        text = label
        textSize = 13f
        setTextColor(Color.WHITE)
        setBackgroundColor(accent)
        isAllCaps = false
    }
    private fun buttonParams() = LinearLayout.LayoutParams(-1, 54).apply { setMargins(0, 6, 0, 6) }

    @Deprecated("Deprecated in Android API")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pickFolder && resultCode == RESULT_OK) {
            val uri = data?.data ?: return
            val flags = data.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            contentResolver.takePersistableUriPermission(uri, flags)
            prefs.edit().putString("game_tree", uri.toString()).apply()
            scanGame(uri)
        }
    }

    private fun scanGame(uri: Uri) {
        try {
            val root = DocumentFile.fromTreeUri(this, uri)
            if (root == null || !root.isDirectory) {
                status.text = "Папка недоступна"
                return
            }
            var exeCount = 0
            var dataCount = 0
            val names = mutableListOf<String>()
            fun walk(dir: DocumentFile, depth: Int) {
                if (depth > 5 || names.size >= 100) return
                dir.listFiles().forEach { file ->
                    if (file.isDirectory) walk(file, depth + 1)
                    else {
                        val name = file.name ?: ""
                        if (name.endsWith(".exe", true)) {
                            exeCount++
                            if (names.size < 20) names.add(name)
                        }
                        if (name.endsWith(".pak", true) || name.endsWith(".assets", true)) dataCount++
                    }
                }
            }
            walk(root, 0)
            val likely = names.any { it.contains("7DaysToDie", true) || it.contains("7Days", true) }
            status.text = if (likely) "Похоже, игра найдена" else "Папка просканирована"
            details.text = "EXE-файлов: $exeCount\nФайлов данных: $dataCount\n\n" +
                (if (names.isEmpty()) "Исполняемые файлы .exe не найдены." else names.joinToString("\n")) +
                "\n\nСканирование не подтверждает совместимость с Android."
            play.isEnabled = exeCount > 0
            play.alpha = if (exeCount > 0) 1f else 0.5f
            log("Scan complete: exe=$exeCount data=$dataCount")
        } catch (e: Exception) {
            status.text = "Ошибка сканирования"
            details.text = e.message ?: "Неизвестная ошибка"
            log("Scan error: " + e.toString())
        }
    }

    private fun log(message: String) {
        try {
            val line = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()) + " " + message + "\n"
            openFileOutput("launcher.log", MODE_APPEND).bufferedWriter().use { it.write(line) }
        } catch (_: Exception) { }
    }
    private fun getLogText(): String = try {
        openFileInput("launcher.log").bufferedReader().use { it.readText().takeLast(5000) }
    } catch (_: Exception) { "Журнал пока пуст." }
}
