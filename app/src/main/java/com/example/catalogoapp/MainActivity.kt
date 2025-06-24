package com.example.catalogoapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL
import android.widget.EditText
import android.widget.Spinner
import android.widget.Button
import android.widget.ArrayAdapter

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var editTextKeyword: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var buttonApplyFilter: Button
    private var allCategories: List<Category> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerViewCategories)
        progressBar = findViewById(R.id.progressBar)
        editTextKeyword = findViewById(R.id.editTextKeyword)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        buttonApplyFilter = findViewById(R.id.buttonApplyFilter)

        // Configurar RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        categoryAdapter = CategoryAdapter { category ->
            // Navegar a la actividad de productos
            val intent = Intent(this, ProductsActivity::class.java)
            intent.putExtra("category_id", category.id)
            intent.putExtra("category_name", category.name)
            startActivity(intent)
        }
        recyclerView.adapter = categoryAdapter

        // Cargar datos
        loadCategories()

        buttonApplyFilter.setOnClickListener {
            aplicarFiltro()
        }
    }

    private fun loadCategories() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE

        CoroutineScope(Dispatchers.IO).launch {
            val url = "http://10.0.2.2/DS6-2-Catalogo/api/api.php?action=get_all"
            try {
                println("INTENTANDO CONECTAR A LA API")
                val response = URL(url).readText()
                println("RESPUESTA API: $response")
                val json = JSONObject(response)

                if (json.getBoolean("success")) {
                    val categories = json.getJSONArray("categories")
                    val categoriesList = mutableListOf<Category>()

                    for (i in 0 until categories.length()) {
                        val cat = categories.getJSONObject(i)
                        categoriesList.add(
                            Category(
                                id = cat.getInt("id"),
                                name = cat.getString("name"),
                                description = cat.optString("description", ""),
                                image = cat.optString("image", "")
                            )
                        )
                    }

                    runOnUiThread {
                        allCategories = categoriesList
                        // Poblar el Spinner de categorías
                        val categoryNames = mutableListOf<String>("Todas")
                        categoryNames.addAll(categoriesList.map { it.name })
                        val spinnerAdapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, categoryNames)
                        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinnerCategory.adapter = spinnerAdapter
                        categoryAdapter.updateCategories(categoriesList)
                        progressBar.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    }
                } else {
                    println("Error: ${json.getString("error")}")
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Error al cargar datos", Toast.LENGTH_LONG).show()
                        progressBar.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                println("ERROR EN LA CONEXIÓN: " + e.message)
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Error al cargar datos", Toast.LENGTH_LONG).show()
                    progressBar.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun aplicarFiltro() {
        val keyword = editTextKeyword.text.toString().trim().lowercase()
        val selectedCategory = spinnerCategory.selectedItem?.toString() ?: "Todas"

        // Filtrar categorías según palabra clave y categoría seleccionada
        var filteredCategories = allCategories

        if (keyword.isNotEmpty()) {
            filteredCategories = filteredCategories.filter {
                it.name.lowercase().contains(keyword) || it.description.lowercase().contains(keyword)
            }
        }

        if (selectedCategory != "Todas") {
            filteredCategories = filteredCategories.filter { it.name == selectedCategory }
        }

        categoryAdapter.updateCategories(filteredCategories)
    }
}

data class Category(
    val id: Int,
    val name: String,
    val description: String,
    val image: String
)