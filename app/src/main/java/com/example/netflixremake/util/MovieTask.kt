package com.example.netflixremake.util

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.netflixremake.model.Category
import com.example.netflixremake.model.Movie
import com.example.netflixremake.model.MovieDetail
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.concurrent.Executors
import javax.net.ssl.HttpsURLConnection

class MovieTask(private val callback: Callback) {

    private val handle = Handler(Looper.getMainLooper())

    interface Callback{

        fun onPreExecute()
        fun onResult(movieDetail: MovieDetail)
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

                if (statusCode == 400) {
                    stream = urlConnection.errorStream

                } else if (statusCode > 400) {
                    throw IOException("Erro na comunicação com o servidor!")

                }

                // Transforma de bytes para string
                stream = urlConnection.inputStream
                val jsonAsString = stream.bufferedReader().use { it.readText() }


                val movieDetail = toMovieDetail(jsonAsString)

                // volta a renderizar na UI Thread
                handle.post {
                    callback.onResult(movieDetail)
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

  private fun toMovieDetail(jsonAsString: String): MovieDetail {
      val json = JSONObject(jsonAsString)

      val id = json.getInt("id")
      val title = json.getString("title")
      val desc = json.getString("desc")
      val cast = json.getString("cast")
      val coverUrl = json.getString("cover_url")
      val jsonMovies = json.getJSONArray("movie")

      val similars = mutableListOf<Movie>()

      for (i in 0 until jsonMovies.length()) {
          val jsonMovie = jsonMovies.getJSONObject(i)

          val similarId = jsonMovie.getInt("id")
          val similarCoverUrl = jsonMovie.getString("cover_url")

          val m = Movie(similarId, similarCoverUrl)
          similars.add(m)
      }
      val movie = Movie(id, coverUrl, title, desc, cast)

      return MovieDetail(movie, similars)
  }
}