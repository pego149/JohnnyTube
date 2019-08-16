package sk.pego149.johnnytube

import android.app.Activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Spinner
import android.widget.ArrayAdapter

import kotlinx.android.synthetic.main.activity_settings.*

//Aktivita ktora sa stara o nastavenia aplikacie
class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val staticSpinner = findViewById<View>(R.id.spinner) as Spinner
        val staticAdapter = ArrayAdapter.createFromResource(this, R.array.languages, android.R.layout.simple_spinner_item)
        staticAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        staticSpinner.adapter = staticAdapter
        val intent = intent
        spinner.setSelection(intent.extras?.getInt("lang") as Int)
        button_save.setOnClickListener {
            intent.putExtra("lang", spinner.selectedItemPosition)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
        button_cancel.setOnClickListener{
            finish()
        }
    }
}
