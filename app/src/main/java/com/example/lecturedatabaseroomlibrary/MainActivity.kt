package com.example.lecturedatabaseroomlibrary

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.lecturedatabaseroomlibrary.databinding.ActivityMainBinding
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var lectionAdapter: LectionAdapter
    private lateinit var lectionDao: LectionDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val addLectionButton = binding.addLectionButton

        addLectionButton.setOnClickListener {
            showAddLectionDialog()
        }
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Здесь можно выполнить операции очистки базы данных, если необходимо
            }
        }


        // Инициализируйте DAO в фоновом потоке с помощью корутины
        CoroutineScope(Dispatchers.IO).launch {
            val db = Room.databaseBuilder(applicationContext, MainDb::class.java, "AppDatabase.db")
                .addMigrations(MIGRATION_1_2) // Добавляем миграции
                .build()
            lectionDao = db.getLectionDao()

            // Инициализируйте адаптер с пустым списком
            lectionAdapter = LectionAdapter(emptyList()) { lection ->
                openLectionEditor(lection)
            }
            withContext(Dispatchers.Main) {
                binding.recyclerView.adapter = lectionAdapter
                binding.recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
            }

            // Получите список лекций из базы данных в фоновом потоке
            GetLectionsTask().execute()
        }
    }

    private fun showAddLectionDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater

        // Создаем представление диалогового окна из макета
        val dialogView = inflater.inflate(R.layout.dialog_edit_lection, null)

        // Получаем ссылки на элементы представления диалогового окна
        val titleEditText = dialogView.findViewById<EditText>(R.id.titleEditText)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.descriptionEditText)

        builder.setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val title = titleEditText.text.toString()
                val description = descriptionEditText.text.toString()

                // Добавляем новую лекцию в базу данных в фоновом потоке
                AddLectionTask(title, description).execute()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private inner class AddLectionTask(
        private val title: String,
        private val description: String
    ) : AsyncTask<Void, Void, Unit>() {

        override fun doInBackground(vararg params: Void?): Unit {
            val newLection = T_Lection(null, title, description)

            // Используем withContext вместо runBlocking
            CoroutineScope(Dispatchers.IO).launch {
                withContext(Dispatchers.IO) {
                    lectionDao.insert(newLection)
                }
            }
        }

        override fun onPostExecute(result: Unit?) {
            GetLectionsTask().execute()
        }
    }
    private inner class GetLectionsTask : AsyncTask<Void, Void, Deferred<List<T_Lection>>>() {
        override fun doInBackground(vararg params: Void?): Deferred<List<T_Lection>> {
            return CoroutineScope(Dispatchers.IO).async {
                lectionDao.getAllLections()
            }
        }

        override fun onPostExecute(result: Deferred<List<T_Lection>>?) {
            super.onPostExecute(result)
            CoroutineScope(Dispatchers.Main).launch {
                val lections = result?.awaitResult() ?: emptyList()
                lectionAdapter.updateLections(lections)
            }
        }
    }

    private suspend fun <T> Deferred<T>.awaitResult(): T? {
        return this.await()
    }


    private fun openLectionEditor(lection: T_Lection) {
        // ... код открытия редактора лекции
    }
}


/*class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var lectionAdapter: LectionAdapter
    private lateinit var lectionDao: LectionDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val db = MainDb.getDb(this) // вызываем создание бд при открытии приложения - единственное, что нужно скопировать отсюда

        // !Следующий код не копировать - он для теста!



        }


}
*/
