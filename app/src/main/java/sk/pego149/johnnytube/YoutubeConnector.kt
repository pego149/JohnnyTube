package sk.pego149.johnnytube

import android.util.Log
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.model.SearchResult

import java.io.IOException
import java.util.ArrayList

class YoutubeConnector{
    //Youtube objekt pre pracu api s Youtube Data API
    private val youtube: YouTube

    //list videi ktory sa vrati pomocou vyhladavania na youtube
    private var query: YouTube.Search.List? = null

    init {
        //Youtube.Builder vracia instanciu noveho buildra
        //Parametre:
        //transport - HTTP transport
        //jsonFactory - JSON factory
        //httpRequestInitializer - HTTP request initializator alebo null pre nic
        youtube = YouTube.Builder(NetHttpTransport(), JacksonFactory.getDefaultInstance(),
            HttpRequestInitializer { request ->
                request.headers.set("X-Android-Package", PACKAGENAME)
                request.headers.set("X-Android-Cert", SHA1)
            }).setApplicationName("JohnnyTube").build()

        try {

            // Definuje API request pre ziskavanie vysledkov hladania
            query = youtube.search().list("id,snippet")

            //nastavenie API kluca
            query!!.key = KEY

            // Vysledky ktore su iba videa
            query!!.type = "video"

            //nastavenie poli ktore chceme ziskat:
            //-kind of video
            //-video ID
            //-nazov videa
            //-popis video
            //-url nahladu k videu
            query!!.fields = "items(id/kind,id/videoId,snippet/title,snippet/description,snippet/thumbnails/high/url,snippet/channelTitle)"

        } catch (e: IOException) {

            Log.d("YC", "Could not initialize: $e")
        }

    }

    fun search(keywords: String, relatedToId: String): List<VideoItem>? {

        if (keywords != "") {
            query!!.q = keywords
        }
        if (relatedToId != "") {
            query!!.relatedToVideoId = relatedToId
        }

        query!!.maxResults = MAXRESULTS

        return try {

            val response = query!!.execute()
            val results = response.items
            var items: List<VideoItem> = ArrayList()
            if (results != null) {
                items = setItemsList(results.iterator())
            }

            items

        } catch (e: IOException) {
            Log.d("YC", "Could not search: $e")
            null
        }

    }

    companion object {
        const val KEY = "AIzaSyDUEZRDVEZobaKU3EfJ-HARTLP0j3wT7hs"
        const val PACKAGENAME = "sk.pego149.johnnytube"
        const val SHA1 = "0C:86:46:3C:AD:CF:96:66:2A:03:73:91:28:E6:0A:3D:60:49:43:A9"
        private const val MAXRESULTS: Long = 25

        //metoda ktora naplna arraylist vysledkami
        private fun setItemsList(iteratorSearchResults: Iterator<SearchResult>): List<VideoItem> {
            val tempSetItems = ArrayList<VideoItem>()
            while (iteratorSearchResults.hasNext()) {
                val singleVideo = iteratorSearchResults.next()
                val rId = singleVideo.id
                if (rId.kind == "youtube#video") {
                    val item = VideoItem()
                    val thumbnail = singleVideo.snippet.thumbnails.high
                    item.channel = singleVideo.snippet.channelTitle
                    item.id = singleVideo.id.videoId
                    item.title = singleVideo.snippet.title
                    item.description = singleVideo.snippet.description
                    item.thumbnailURL = thumbnail.url

                    //pridanie jedneho videa do docasneho arraylistu
                    tempSetItems.add(item)

                    //ladenie
                    println(" Channel:" + singleVideo.snippet.channelTitle)
                    println(" Video Id" + rId.videoId)
                    println(" Title: " + singleVideo.snippet.title)
                    println(" Thumbnail: " + thumbnail.url)
                    println(" Description: " + singleVideo.snippet.description)
                    println("\n-------------------------------------------------------------\n")
                }
            }
            return tempSetItems
        }
    }
}