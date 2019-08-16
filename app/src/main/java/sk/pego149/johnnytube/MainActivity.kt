package sk.pego149.johnnytube

import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.*
import java.util.*
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {
    private var sharedPref: SharedPreferences? = null
    private var language: Int = 0

    //YoutubeAdapter trieda ktora sa stara ako adapter o naplnanie RecyclerView pomocou CardView (video_item.xml)
    private var youtubeAdapter: YoutubeAdapter? = null

    //RecyclerView sa stara o list itemov ktory je prave viditelny na obrazovke
    private var mRecyclerView: RecyclerView? = null

    //ProgressDialog indikuje nacitavanie dat z iternetu
    private var mProgressDialog: ProgressDialog? = null

    //Handler sluzi na spustenie vlakna, ktore vyplni zoznam po stiahnutí dát z internetu a naplnenie obrázkov, názvu a popisu
    private var handler: Handler? = null

    //list typu VideoItem ktory obsahuje vysledky, kazdy item v liste obsahuje id, title, description a thumbnail url
    private var searchResults: List<VideoItem>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = applicationContext.getSharedPreferences("settings", Context.MODE_PRIVATE)
        language = nacitaj("lang")
        changeLang(language)
        setContentView(R.layout.activity_main)
        mRecyclerView = findViewById(R.id.item_recyclerview)
        mProgressDialog = ProgressDialog(this)
        handler = Handler()
        mProgressDialog?.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        mRecyclerView?.setHasFixedSize(true)
        mRecyclerView?.layoutManager = LinearLayoutManager(this)
        searchOnYoutube("")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)

        val searchItem = menu?.findItem(R.id.search)
        val searchView = searchItem?.actionView as SearchView
        searchView.queryHint = getString(R.string.search_help)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
            override fun onQueryTextSubmit(query: String): Boolean {
                //nastavenie progress spravy
                mProgressDialog?.setMessage(getString(R.string.search_progress) + searchView.query.toString())
                mProgressDialog?.show()

                //volanie vyhladavacej metody so vstupom ktory uzivatel zadal
                searchOnYoutube(searchView.query.toString())

                //ziskavanie instancie nejakej vstupnej metody ktoru uzivatel pouziva
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                //skrytie klavesnice
                imm.hideSoftInputFromWindow(
                    currentFocus!!.windowToken,
                    InputMethodManager.RESULT_UNCHANGED_SHOWN
                )
                searchView.setQuery("", false)
                searchView.isIconified = true
                return false
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_info -> showInfo()
            R.id.action_exit -> {
                finish()
                exitProcess(0)
            }
            R.id.action_settings -> {
                val intent2 = Intent(this, SettingsActivity::class.java)
                intent2.putExtra("lang", language)
                startActivityForResult(intent2, 123)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 123) {
            if (resultCode == Activity.RESULT_OK) {
                language = data?.extras?.getInt("lang") as Int
                uloz("lang",language)
                changeLang(language)
            }
        }
    }

    //aktivita ktora vyhladava videa pomocou youtube connector
    fun searchOnYoutube(keywords: String) {
        object : Thread() {
            override fun run() {
                val yc = YoutubeConnector()
                searchResults = yc.search(keywords, "")
                handler?.post {
                    fillYoutubeVideos()
                    mProgressDialog?.dismiss()
                }
            }
        }.start()
    }

    //metoda, ktora vytvara adapter a nastavuje ho do recyclerView
    private fun fillYoutubeVideos() {
        if (searchResults == null) {
            Toast.makeText(this, getString(R.string.conn_err), Toast.LENGTH_SHORT).show()
        }else{
            youtubeAdapter = YoutubeAdapter(applicationContext, searchResults!!)
        }
        //nastavi adapter do recycler view
        mRecyclerView?.adapter = youtubeAdapter

        youtubeAdapter?.notifyDataSetChanged()
    }

    //zobrazenie informacii o aplikacii
    private fun showInfo() {
        val myDialog = Dialog(this)
        myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        myDialog.setContentView(R.layout.info_dialog_activity)
        myDialog.show()
    }

    //ulozenie konfiguracie aplikacie
    private fun uloz(key: String, value: Int) {
        val editor: SharedPreferences.Editor? = sharedPref?.edit()
        editor?.putInt(key, value)
        editor?.apply()
    }

    //nacitanie konfiguracie
    private fun nacitaj(key: String): Int {
        return sharedPref?.getInt(key, 0) as Int
    }

    //zmena jazyka
    private fun changeLang(lang: Int){
        val languageToLoad = if (lang == 0)
            "sk"
        else
            "en"
        val locale = Locale(languageToLoad)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
    }

    override fun onRestart() {
        super.onRestart()
        val languageToLoad = if (language == 0)
            "sk"
        else
            "en"
        if (Locale(languageToLoad) == Locale.getDefault()) {
            changeLang(language)
            recreate()
        }
    }
}
