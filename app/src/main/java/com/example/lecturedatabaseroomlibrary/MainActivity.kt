package com.example.lecturedatabaseroomlibrary

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
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



        val vInfo = findViewById<ImageButton>(R.id.info_but)
        vInfo.setOnClickListener {
            val i= Intent(this, InfoActivity::class.java)
            startActivity(i)
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
            .setPositiveButton("Добавить") { _, _ ->
                val title = titleEditText.text.toString()
                val description = descriptionEditText.text.toString()

                // Добавляем новую лекцию в базу данных в фоновом потоке
                AddLectionTask(title, description).execute()
            }
            .setNegativeButton("Отмена", null)
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



    private fun openLectionEditor(lection: T_Lection?) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater

        // Создаем представление диалогового окна из макета
        val dialogView = inflater.inflate(R.layout.dialog_edit_lection, null)

        // Получаем ссылки на элементы представления диалогового окна
        val titleEditText = dialogView.findViewById<EditText>(R.id.titleEditText)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.descriptionEditText)

        // Если lection не null, заполняем поля данными из lection
        if (lection != null) {
            titleEditText.setText(lection.title)
            descriptionEditText.setText(lection.description)
        }

        // Создаем кнопку удаления и делаем ее видимой только для существующей лекции
        val deleteButton = Button(this)
        deleteButton.text = "Удалить"
        deleteButton.visibility = if (lection != null) View.VISIBLE else View.GONE
        deleteButton.setOnClickListener {
            showDeleteConfirmationDialog(lection)
        }

        // Добавляем кнопку удаления в диалоговое окно
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.addView(dialogView)
        layout.addView(deleteButton)

        builder.setView(layout)
            .setPositiveButton("Сохранить") { _, _ ->
                val updatedTitle = titleEditText.text.toString()
                val updatedDescription = descriptionEditText.text.toString()

                // Если lection не null, обновляем существующую lection
                if (lection != null) {
                    UpdateLectionTask(lection.lectionId, updatedTitle, updatedDescription).execute()
                } else {
                    // Иначе, добавляем новую lection
                    AddLectionTask(updatedTitle, updatedDescription).execute()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showDeleteConfirmationDialog(lection: T_Lection?) {
        if (lection != null) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Удалить запись")
                .setMessage("Вы действительно хотите удалить эту запись?")
                .setPositiveButton("Да") { _, _ ->
                    deleteLection(lection)
                }
                .setNegativeButton("Нет", null)
            val dialog = builder.create()
            dialog.show()
        }
    }

    private fun deleteLection(lection: T_Lection) {
        lifecycleScope.launch(Dispatchers.IO) {
            lection.lectionId?.let { lectionId ->
                lectionDao.delete(lection)
            }
            withContext(Dispatchers.Main) {
                GetLectionsTask().execute()
            }
        }
    }





    private inner class UpdateLectionTask(
        private val lectionId: Int?,
        private val title: String,
        private val description: String
    ) : AsyncTask<Void, Void, Unit>() {

        override fun doInBackground(vararg params: Void?): Unit {
            val updatedLection = T_Lection(lectionId, title, description)

            CoroutineScope(Dispatchers.IO).launch {
                withContext(Dispatchers.IO) {
                    lectionDao.update(updatedLection)
                }
            }
        }

        override fun onPostExecute(result: Unit?) {
            GetLectionsTask().execute()
        }
    }

    private fun deleteLection(lectionId: Int?) {
        if (lectionId != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val lectionToDelete = lectionDao.getLectionById(lectionId)
                if (lectionToDelete != null) {
                    lectionDao.delete(lectionToDelete)
                }
                withContext(Dispatchers.Main) {
                    // Обновить представление или выполнить другие операции на основном потоке
                }
            }
        }

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
