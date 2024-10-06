package com.example.netflixremake.util

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.netflixremake.model.Category
import com.example.netflixremake.model.Movie
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.concurrent.Executors
import javax.net.ssl.HttpsURLConnection

class CategoryTask(private val callback: Callback) {

    private val handle = Handler(Looper.getMainLooper())

    interface Callback{

        fun onPreExecute()
        fun onResult(categories: List<Category>)
        fun onFailure(message: String)
    }

    fun execute(url: String) {

        callback.onPreExecute()
        // nesse momento, estamos utilizando a UI thread
        val executor = Executors.newSingleThreadExecutor()


        executor.execute {
            var urlConnection: HttpsURLConnection? = null
            var stream: InputStream? = null



            try {
                // nesse momento, estamos utililizando a nova thread [processo paralelo]
                val requestURL = URL(url) // abrir uma URL
                urlConnection =
                    requestURL.openConnection() as HttpsURLConnection //abrir conexão
                urlConnection.readTimeout = 2000 // tempo de leitura
                urlConnection.connectTimeout = 2000 // tempo de conexão


                val statusCode = urlConnection.responseCode

                if (statusCode > 400) {
                    throw IOException("Erro na comunicação com o servidor!")

                }

                // Transforma de bytes para string
                stream = urlConnection.inputStream
                val jsonAsString = stream.bufferedReader().use { it.readText() }


                val categories = toCategories(jsonAsString)

                // volta a renderizar na UI Thread
                handle.post {
                    callback.onResult(categories)
                    Log.i("Teste", categories.toString())
                }




            } catch (e: IOException) {
                val message = e.message ?: "erro desconhecido"
                Log.e("Teste", message, e)
                handle.post {
                    callback.onFailure(message)
                }


            } finally {
                urlConnection?.disconnect()
                stream?.close()
            }

        }
    }

    private fun toCategories(jsonAsString: String): List<Category> {
        val categories = mutableListOf<Category>()

        val jsonRoot = JSONObject(jsonAsString)
        val jsonCategories = jsonRoot.getJSONArray("category")

        for (i in 0 until jsonCategories.length()) {
            val jsonCategory = jsonCategories.getJSONObject(i)
            val title = jsonCategory.getString("title")
            val jsonMovies = jsonCategory.getJSONArray("movie")

            val movies = mutableListOf<Movie>()

            for (j in 0 until jsonMovies.length()) {
                val jsonMovie = jsonMovies.getJSONObject(j)
                val id = jsonMovie.getInt("id")
                val coverUrl = jsonMovie.getString("cover_url")

                movies.add(Movie(id, coverUrl))
            }

            categories.add(Category(title, movies))
        }


        return categories

    }
}