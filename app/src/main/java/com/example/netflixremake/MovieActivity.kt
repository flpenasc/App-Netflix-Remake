package com.example.netflixremake

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.netflixremake.model.Movie
import com.example.netflixremake.model.MovieDetail
import com.example.netflixremake.util.MovieTask
import com.squareup.picasso.Picasso

class MovieActivity : AppCompatActivity(), MovieTask.Callback {

    private lateinit var txtTitle: TextView
    private lateinit var txtDes: TextView
    private lateinit var txtCast: TextView
    private lateinit var rv: RecyclerView
    private lateinit var adapter: MovieAdapter
    private lateinit var progress: ProgressBar
    private var movies = mutableListOf<Movie>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie)

        txtTitle = findViewById(R.id.movie_txt_title)
        txtDes = findViewById(R.id.movie_txt_desc)
       txtCast= findViewById(R.id.movie_txt_cast)
        rv = findViewById(R.id.movie_rv_similar)
        progress = findViewById(R.id.movie_progress)

        val id = intent?.getIntExtra("id", 0) ?: throw IllegalStateException("ID não encontrado")
        val url = "https://atway.tiagoaguiar.co/fenix/netflixapp/movie/$id?apiKey=882911fe-7ce9-40a1-86b9-820f00cbfc5c"
        MovieTask(this).execute(url)



        
        adapter = MovieAdapter(movies, R.layout.movie_item_similar)
        rv.layoutManager = GridLayoutManager(this, 3)
        rv.adapter =adapter

        val toolbar: Toolbar = findViewById(R.id.movie_toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)




    }

    override fun onPreExecute() {
        progress.visibility = View.VISIBLE

    }

    override fun onFailure(message: String) {
        progress.visibility = View.GONE

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResult(movieDetail: MovieDetail) {
        progress.visibility = View.GONE

        txtTitle.text = movieDetail.movie.title
        txtDes.text = movieDetail.movie.desc
        txtCast.text = movieDetail.movie.cast

        movies.clear()
        movies.addAll(movieDetail.similars)
        adapter.notifyDataSetChanged()


        val layerDrawable: LayerDrawable = ContextCompat.getDrawable(this, R.drawable.shadows) as LayerDrawable
        val coverImg: ImageView = findViewById(R.id.movie_img)
        coverImg.setImageDrawable(layerDrawable)


        Picasso.get().load(movieDetail.movie.coverUrl).into(coverImg)


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}