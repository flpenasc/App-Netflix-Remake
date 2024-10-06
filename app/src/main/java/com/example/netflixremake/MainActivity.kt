package com.example.netflixremake

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.netflixremake.model.Category
import com.example.netflixremake.model.Movie
import com.example.netflixremake.util.CategoryTask


class MainActivity : AppCompatActivity(), CategoryTask.Callback {

    private lateinit var adapter: CategoryAdapter
    private lateinit var progress: ProgressBar
    private val categories = mutableListOf<Category>()

    // arquitetura mvc (model view controller)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        progress = findViewById(R.id.progress_main)


        // na vertical a lista (MainAdapter) de categorias
        // e dentro de cada item (categoria) teremos uma lista
        // (MovieAdapter) de filmes (ImageView)
        adapter = CategoryAdapter(categories) {id ->
            val intent = Intent(this@MainActivity, MovieActivity::class.java)
            intent.putExtra("id", id)
            startActivity(intent)
        }
        val rv: RecyclerView = findViewById(R.id.rv_main)
        rv.adapter = adapter
        rv.layoutManager = LinearLayoutManager(this)


        CategoryTask(this).execute("https://atway.tiagoaguiar.co/fenix/netflixapp/home?apiKey=882911fe-7ce9-40a1-86b9-820f00cbfc5c")

    }

    override fun onPreExecute() {
        progress.visibility = View.VISIBLE
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResult(categories: List<Category>) {
        this.categories.clear()
        this.categories.addAll(categories)
        adapter.notifyDataSetChanged()
        progress.visibility = View.GONE


    }

    override fun onFailure(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        progress.visibility = View.GONE
    }


}