package com.example.catalogoapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.*

class ProductAdapter : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    private var products = listOf<Product>()

    fun updateProducts(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageViewProduct)
        private val nameTextView: TextView = itemView.findViewById(R.id.textViewProductName)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.textViewProductDescription)
        private val priceTextView: TextView = itemView.findViewById(R.id.textViewProductPrice)

        fun bind(product: Product) {
            nameTextView.text = product.name
            descriptionTextView.text = product.description
            
            // Formatear precio
            val formatter = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
            priceTextView.text = formatter.format(product.price)
            
            // Aquí podrías cargar la imagen usando Glide o Picasso
            // Por ahora usamos la imagen por defecto
        }
    }
} 