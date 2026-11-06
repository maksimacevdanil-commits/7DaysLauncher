package com.sevendays.launcher

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
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
    private val bg = Color.rgb(14, 16, 17)
    private val panel = Color.rgb(29, 32, 33)
    private val accent = Color.rgb(177, 57, 34)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
        prefs.getString("game_tree", null)?.let { scanGame(Uri.parse(it)) }
        log("Launcher started on " + Build.MODEL)
    }

    private fun buildUi() {
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 12, 22, 12)
            setBackgroundColor(bg)
        }
        val header = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        header.addView(label("7 DAYS TO DIE", 26f, Color.WHITE), LinearLayout.LayoutParams(0, 58, 1f))
        header.addView(label("ANDROID EDITION  •  0.3.0", 11f, Color.LTGRAY))
        page.addView(header)

        val nav = LinearLayout(this).apply { gravity = Gravity.CENTER; setBackgroundColor(panel) }
        nav.addView(navButton("СЕТЬ") { showInfo("Сетевая игра", "Сетевой режим пока не реализован в лаунчере.") })
        nav.addView(navButton("УПРАВЛЕНИЕ") { showControls() })
        nav.addView(navButton("ГРАФИКА") { showGraphics() })
        nav.addView(navButton("НАСТРОЙКИ") { showSettings() })
        page.addView(nav, LinearLayout.LayoutParams(-1, 48))

        page.addView(label("SURVIVE THE NIGHT", 19f, Color.rgb(205, 105, 72)).apply {
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(-1, 48))

        val statusPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(14, 10, 14, 10)
            setBackgroundColor(panel)
        }
        status = label("СТАТУС: ПАПКА ИГРЫ НЕ ВЫБРАНА", 15f, Color.WHITE)
        details = label("Выбери каталог установленной PC-версии игры. Лаунчер проверит структуру файлов.", 13f, Color.LTGRAY)
        statusPanel.addView(status)
        statusPanel.addView(details, LinearLayout.LayoutParams(-1, 0, 1f))
        page.addView(statusPanel, LinearLayout.LayoutParams(-1, 0, 1f).apply { setMargins(0, 8, 0, 8) })

        val actions = LinearLayout(this).apply { gravity = Gravity.CENTER }
        actions.addView(makeButton("ВЫБРАТЬ ПАПКУ") { chooseFolder() }, LinearLayout.LayoutParams(0, 52, 1f).apply { setMargins(3, 0, 3, 0) })
        play = makeButton("ИГРАТЬ") { onPlayPressed() }
        play.isEnabled = false
        play.alpha = 0.45f
        actions.addView(play, LinearLayout.LayoutParams(0, 52, 1f).apply { setMargins(3, 0, 3, 0) })
        actions.addView(makeButton("ЖУРНАЛ") { showLog() }, LinearLayout.LayoutParams(0, 52, 1f).apply { setMargins(3, 0, 3, 0) })
        page.addView(actions)

        val bottom = LinearLayout(this).apply { gravity = Gravity.CENTER }
        bottom.addView(navButton("КАМПАНИЯ") { showInfo("Кампания", "Этот раздел станет доступен после интеграции игрового runtime.") })
        bottom.addView(navButton("КРАФТ") { showInfo("Крафт", "Игровые меню пока не подключены.") })
        bottom.addView(navButton("МОДЫ") { showInfo("Моды", "Менеджер модификаций появится в следующей версии.") })
        page.addView(bottom, LinearLayout.LayoutParams(-1, 42))
        page.addView(label("PC GAME RUNTIME: НЕ ПОДКЛЮЧЁН  •  ФАЙЛЫ НЕ ИЗМЕНЯЮТСЯ", 10f, Color.GRAY).apply {
            gravity = Gravity.CENTER
        })
        val scroll = ScrollView(this)
        scroll.addView(page)
        setContentView(scroll)
    }

    private fun label(value: String, size: Float, color: Int) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
        gravity = Gravity.CENTER_VERTICAL
        setPadding(8, 5, 8, 5)
    }

    private fun makeButton(value: String, action: () -> Unit) = Button(this).apply {
        text = value
        textSize = 12f
        setTextColor(Color.WHITE)
        setBackgroundColor(accent)
        isAllCaps = false
        setOnClickListener { action() }
    }

    private fun navButton(value: String, action: () -> Unit) = Button(this).apply {
        text = value
        textSize = 10f
        setTextColor(Color.WHITE)
        isAllCaps = false
        setBackgroundColor(panel)
        setOnClickListener { action() }
    }

    private fun chooseFolder() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        startActivityForResult(intent, pickFolder)
    }

    private fun onPlayPressed() {
        showInfo("Runtime не готов", "Файлы игры обнаружены, но Windows EXE нельзя запустить напрямую в Android. Нужен совместимый ARM64 runtime с Wine/Box64 и графическим переводчиком. Этот модуль ещё не интегрирован.")
        log("Play requested while runtime unavailable")
    }

    private fun showControls() {
        val current = prefs.getString("control_profile", "Сенсорное управление (планируется)") ?: "По умолчанию"
        AlertDialog.Builder(this).setTitle("Управление")
            .setMessage("Профиль: $current\n\nСхема сенсорных кнопок будет добавлена после подключения runtime. Сейчас игра не запущена.")
            .setPositiveButton("Понятно", null).show()
    }

    private fun showGraphics() {
        val options = arrayOf("Совместимость", "Сбалансированная", "Качество")
        val current = prefs.getInt("graphics", 1).coerceIn(0, 2)
        AlertDialog.Builder(this).setTitle("Графический профиль")
            .setSingleChoiceItems(options, current) { dialog, which ->
                prefs.edit().putInt("graphics", which).apply()
                log("Graphics profile selected: " + options[which])
                dialog.dismiss()
                showInfo("Профиль сохранён", "Выбран профиль: " + options[which] + ". Он будет применён после подключения графического runtime.")
            }.setNegativeButton("Отмена", null).show()
    }

    private fun showSettings() {
        val info = "Устройство: ${Build.MANUFACTURER} ${Build.MODEL}\nAndroid: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})\nАрхитектура: ${Build.SUPPORTED_ABIS.joinToString()}\nВерсия лаунчера: 0.3.0"
        AlertDialog.Builder(this).setTitle("Диагностика устройства")
            .setMessage(info).setPositiveButton("ОК", null).show()
    }

    private fun showInfo(title: String, message: String) {
        AlertDialog.Builder(this).setTitle(title).setMessage(message).setPositiveButton("ОК", null).show()
    }

    private fun showLog() = showInfo("Журнал лаунчера", getLogText())

    @Deprecated("Deprecated in Android API")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pickFolder && resultCode == RESULT_OK) {
            val uri = data?.data ?: return
            val flags = data.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            try {
                contentResolver.takePersistableUriPermission(uri, flags)
                prefs.edit().putString("game_tree", uri.toString()).apply()
                scanGame(uri)
            } catch (e: Exception) {
                status.text = "НЕ УДАЛОСЬ ОТКРЫТЬ ПАПКУ"
                details.text = e.message ?: "Ошибка доступа"
                log("Permission error: " + e.toString())
            }
        }
    }

    private fun scanGame(uri: Uri) {
        try {
            val root = DocumentFile.fromTreeUri(this, uri)
            if (root == null || !root.isDirectory) {
                status.text = "ПАПКА НЕДОСТУПНА"
                return
            }
            var exeCount = 0
            var dataCount = 0
            var visited = 0
            val names = mutableListOf<String>()
            fun walk(dir: DocumentFile, depth: Int) {
                if (depth > 5 || visited >= 4000) return
                dir.listFiles().forEach { file ->
                    if (visited++ >= 4000) return
                    if (file.isDirectory) walk(file, depth + 1)
                    else {
                        val name = file.name ?: ""
                        if (name.endsWith(".exe", true)) {
                            exeCount++
                            if (names.size < 30) names.add(name)
                        }
                        if (name.endsWith(".pak", true) || name.endsWith(".assets", true)) dataCount++
                    }
                }
            }
            walk(root, 0)
            val likely = names.any { it.contains("7DaysToDie", true) || it.contains("7Days", true) }
            status.text = if (likely) "ВОЗМОЖНО, ИГРА НАЙДЕНА" else "СКАНИРОВАНИЕ ЗАВЕРШЕНО"
            details.text = "Каталог: ${root.name ?: "выбран"}\nEXE-файлов: $exeCount\nФайлов данных: $dataCount\nПроверено элементов: $visited\n\n" +
                (if (names.isEmpty()) "EXE-файлы не найдены." else names.joinToString("\n")) +
                "\n\nПроверка структуры не гарантирует запуск на Android."
            play.isEnabled = exeCount > 0
            play.alpha = if (exeCount > 0) 1f else 0.45f
            log("Scan complete: exe=$exeCount data=$dataCount visited=$visited")
        } catch (e: Exception) {
            status.text = "ОШИБКА СКАНИРОВАНИЯ"
            details.text = e.message ?: "Неизвестная ошибка"
            play.isEnabled = false
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
