package com.sevendays.launcher

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
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
        window.decorView.systemUiVisibility = (
            android.view.View.SYSTEM_UI_FLAG_FULLSCREEN or
            android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 8, 18, 8)
            background = try {
                val bitmap = assets.open("launcher_bg.jpg").use { BitmapFactory.decodeStream(it) }
                BitmapDrawable(resources, bitmap).apply { gravity = Gravity.FILL }
            } catch (_: Exception) {
                android.graphics.drawable.GradientDrawable(
                    android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
                    intArrayOf(Color.rgb(13, 16, 17), Color.rgb(58, 42, 34), Color.rgb(12, 14, 15))
                )
            }
        }

        val top = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(Color.argb(135, 10, 10, 10))
        }
        top.addView(navButton("◎  СЕТЕВАЯ ИГРА") {
            showInfo("Сетевая игра", "Сетевой режим пока недоступен.")
        }, LinearLayout.LayoutParams(0, 48, 1f))
        top.addView(navButton("🎮  УПРАВЛЕНИЕ") { showControls() }, LinearLayout.LayoutParams(0, 48, 1f))
        top.addView(navButton("▣  ГРАФИКА") { showGraphics() }, LinearLayout.LayoutParams(0, 48, 1f))
        top.addView(navButton("⚙  НАСТРОЙКИ") { showSettings() }, LinearLayout.LayoutParams(0, 48, 1f))
        top.addView(navButton("⏻  ВЫХОД") { finish() }, LinearLayout.LayoutParams(0, 48, 1f))
        page.addView(top)

        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(4, 8, 4, 8)
        }
        val left = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        left.addView(label("7 DAYS TO DIE", 37f, Color.WHITE).apply {
            typeface = android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.BOLD)
        })
        left.addView(label("THE SURVIVAL HORDE CRAFTING GAME", 11f, Color.rgb(230, 50, 38)))
        left.addView(label("Выживайте. Стройте. Защищайтесь.\nПобеждайте орды зомби!", 17f, Color.WHITE))
        val spacer = TextView(this)
        left.addView(spacer, LinearLayout.LayoutParams(-1, 0, 1f))
        play = makeButton("ИГРАТЬ  ❯", ::onPlayPressed).apply {
            textSize = 27f
            setBackgroundColor(Color.rgb(190, 12, 8))
            isEnabled = false
            alpha = 0.65f
        }
        left.addView(play, LinearLayout.LayoutParams(-1, 66).apply { setMargins(0, 8, 0, 8) })
        left.addView(label("VULKAN / OPENGL ES", 12f, Color.LTGRAY))
        hero.addView(left, LinearLayout.LayoutParams(0, -1, 1.35f))

        val right = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(14, 12, 14, 12)
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.argb(220, 12, 12, 12))
                setStroke(2, Color.rgb(145, 45, 35))
                cornerRadius = 18f
            }
        }
        right.addView(label("СТАТУС", 22f, Color.WHITE))
        status = label("Эмулятор: НЕ ГОТОВ", 14f, Color.rgb(140, 220, 70))
        details = label("Игра: 7 Days to Die\nAPI: Vulkan / OpenGL ES\n\nВыбери папку игры для проверки.", 13f, Color.WHITE)
        right.addView(status)
        right.addView(details, LinearLayout.LayoutParams(-1, 0, 1f))
        right.addView(label("RUNTIME НЕ ПОДКЛЮЧЁН", 11f, Color.rgb(255, 90, 70)))
        hero.addView(right, LinearLayout.LayoutParams(0, -1, 0.78f).apply { setMargins(10, 6, 0, 6) })
        page.addView(hero, LinearLayout.LayoutParams(-1, 0, 1f))

        val cards = LinearLayout(this).apply { gravity = Gravity.CENTER; orientation = LinearLayout.HORIZONTAL }
        cards.addView(cardButton("КАМПАНИЯ", "История и выживание") {
            showInfo("Кампания", "Раздел откроется после интеграции игрового runtime.")
        }, LinearLayout.LayoutParams(0, 82, 1f).apply { setMargins(3, 0, 3, 0) })
        cards.addView(cardButton("СОЗДАНИЕ", "Крафт и строительство") {
            showInfo("Создание", "Игровой интерфейс крафта пока не подключён.")
        }, LinearLayout.LayoutParams(0, 82, 1f).apply { setMargins(3, 0, 3, 0) })
        cards.addView(cardButton("МОДЫ", "Управление модификациями") {
            showInfo("Моды", "Менеджер модов будет добавлен отдельным модулем.")
        }, LinearLayout.LayoutParams(0, 82, 1f).apply { setMargins(3, 0, 3, 0) })
        cards.addView(cardButton("НАСТРОЙКИ", "Параметры лаунчера") {
            showSettings()
        }, LinearLayout.LayoutParams(0, 82, 1f).apply { setMargins(3, 0, 3, 0) })
        page.addView(cards)
        page.addView(label("ВЫБЕРИ ПАПКУ ИГРЫ • ФАЙЛЫ НЕ ИЗМЕНЯЮТСЯ", 10f, Color.LTGRAY).apply {
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(-1, 24))
        setContentView(page)
    }

    private fun cardButton(title: String, subtitle: String, action: () -> Unit) = Button(this).apply {
        text = "$title\n$subtitle"
        textSize = 12f
        setTextColor(Color.WHITE)
        isAllCaps = false
        gravity = Gravity.CENTER
        background = android.graphics.drawable.GradientDrawable().apply {
            setColor(Color.argb(220, 18, 18, 18))
            setStroke(2, Color.rgb(105, 105, 105))
            cornerRadius = 12f
        }
        setOnClickListener { action() }
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
