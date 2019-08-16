package sk.pego149.johnnytube

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayer.OnInitializedListener
import com.google.android.youtube.player.YouTubePlayer.Provider
import com.google.android.youtube.player.YouTubePlayerView
import android.content.res.Configuration
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.WindowManager

//Aktivita ktora prehrava videa pomocou YouTubePlayerView
//Pomocou OnInitializedListener ziskame stav ci prehavac bol nacitany uspesne alebo neuspesne
class PlayerActivity : YouTubeBaseActivity(), OnInitializedListener {

    private var playerView: YouTubePlayerView? = null
    private var youPlayer: YouTubePlayer? = null
    private var searchResults: List<VideoItem>? = null
    private var handler: Handler? = null

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        handler = Handler()
        setContentView(R.layout.activity_player)

        //ziskanie youtube player view
        playerView = findViewById(R.id.player_view)

        //inicializacia method of YouTubePlayerView pomocou api kluca
        playerView!!.initialize(getString(R.string.developer_key), this)

        val vTitle = findViewById<View>(R.id.player_title) as TextView
        val vDesc = findViewById<View>(R.id.player_description) as TextView
        val vChannelName = findViewById<View>(R.id.player_channel_name) as TextView

        //nastavovanie atributov View z UI pomocou intentov
        vTitle.text = intent.getStringExtra("VIDEO_TITLE")
        vDesc.text = intent.getStringExtra("VIDEO_DESC")
        vChannelName.text = intent.getStringExtra("VIDEO_CHANNEL")
        searchOnYoutube(intent.getStringExtra("VIDEO_ID"))
    }

    //metoda ktora sa zavola ak inicializacia YouTubePlayerView zlyha
    override fun onInitializationFailure(
        provider: Provider,
        result: YouTubeInitializationResult
    ) {
        Toast.makeText(this, "Fail", Toast.LENGTH_LONG).show()
    }

    //metoda ktora sa zavola ak inicializacia YouTubePlayerView je uspesna
    override fun onInitializationSuccess(
        provider: Provider, player: YouTubePlayer,
        restored: Boolean
    )
    {
        youPlayer = player
        if (!restored) {
            //cueVideo zoberie video ID ako parameter a inicializuje prehravac s videom,
            //a pripravi prehravac na prehranie videa
            //pokial sa nezavola play() tak prehravac nestahuje ziadne udaje streamu
            player.cueVideo(intent.getStringExtra("VIDEO_ID"))
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // Kontroluje konfiguraciu obrazovky
        when {
            newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE -> window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            newConfig.orientation == Configuration.ORIENTATION_PORTRAIT -> window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }

    //aktivita ktora vyhladava videa pomocou youtube connector
    private fun searchOnYoutube(keywords: String) {
        object : Thread() {
            override fun run() {
                val yc = YoutubeConnector()
                searchResults = yc.search("", keywords)
                handler?.post {
                    fillYoutubeVideos()
                }
            }
        }.start()
    }

    //metoda, ktora vytvara adapter a nastavuje ho do recyclerView
    private fun fillYoutubeVideos() {
        var youtubeAdapter: YoutubeAdapter? = null
        val mRecyclerView: RecyclerView = findViewById(R.id.item_recyclerview_player)
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = LinearLayoutManager(this)
        when (searchResults) {
            null -> Toast.makeText(this, getString(R.string.conn_err), Toast.LENGTH_SHORT).show()
            else -> youtubeAdapter = YoutubeAdapter(applicationContext, searchResults!!)
        }
        //nastavi adapter do recycler view
        mRecyclerView.adapter = youtubeAdapter
        youtubeAdapter?.notifyDataSetChanged()
    }
}