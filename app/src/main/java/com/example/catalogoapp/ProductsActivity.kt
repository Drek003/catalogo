package com.example.catalogoapp

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL

class ProductsActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var categoryTitle: TextView
    private lateinit var backButton: ImageButton
    private lateinit var productAdapter: ProductAdapter

    private var categoryId: Int = 0
    private var categoryName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_products)

        // Obtener datos de la categoría
        categoryId = intent.getIntExtra("category_id", 0)
        categoryName = intent.getStringExtra("category_name") ?: "Productos"

        recyclerView = findViewById(R.id.recyclerViewProducts)
        progressBar = findViewById(R.id.progressBar)
        categoryTitle = findViewById(R.id.textViewCategoryTitle)
        backButton = findViewById(R.id.buttonBack)

        // Configurar UI
        categoryTitle.text = categoryName
        backButton.setOnClickListener { finish() }

        // Configurar RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        productAdapter = ProductAdapter()
        recyclerView.adapter = productAdapter

        // Cargar productos
        loadProducts()
    }

    private fun loadProducts() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE

        CoroutineScope(Dispatchers.IO).launch {
            val url = "http://10.0.2.2/DS6-2-Catalogo/api/api.php?action=get_all"
            try {
                val response = URL(url).readText()
                val json = JSONObject(response)

                if (json.getBoolean("success")) {
                    val products = json.getJSONArray("products")
                    val productsList = mutableListOf<Product>()

                    for (i in 0 until products.length()) {
                        val prod = products.getJSONObject(i)
                        val productCategoryId = prod.optInt("category_id", 0)
                        
                        // Solo agregar productos de la categoría seleccionada
                        if (productCategoryId == categoryId) {
                            productsList.add(
                                Product(
                                    id = prod.getInt("id"),
                                    name = prod.getString("name"),
                                    description = prod.optString("description", ""),
                                    price = prod.getDouble("price"),
                                    image = prod.optString("image", ""),
                                    categoryId = productCategoryId
                                )
                            )
                        }
                    }

                    runOnUiThread {
                        productAdapter.updateProducts(productsList)
                        progressBar.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    }
                } else {
                    println("Error: ${json.getString("error")}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
            }
        }
    }
}

data class Product(
    val id: Int,
    val name: String,
    val description: String,
    val price: Double,
    val image: String,
    val categoryId: Int
) 