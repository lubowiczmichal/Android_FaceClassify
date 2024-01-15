package lubowicz.michal.faceclassify

import android.R
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCapture
import lubowicz.michal.faceclassify.databinding.ActivityMainBinding
import java.io.File


class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding

    private var imageCapture: ImageCapture? = null
    private var imagePath: Uri? = null
    private lateinit var capturedImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        val checkboxValues = booleanArrayOf(false, false, false, false)
        imagePath = intent.getParcelableExtra("imagePath")
        capturedImageView = findViewById(viewBinding.capturedImageView.id)
        var imageBitmap: Bitmap? = null
        if (imagePath != null) {
            viewBinding.startLayer.visibility = View.INVISIBLE
            viewBinding.standardLayer.visibility = View.VISIBLE
            viewBinding.classify.visibility = View.VISIBLE
        }

        //hide test mode
        if (true) {
            viewBinding.startLayer.visibility = View.INVISIBLE
            viewBinding.standardLayer.visibility = View.VISIBLE
        }

        viewBinding.imageCaptureButton.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }
        viewBinding.classify.setOnClickListener {
            imagePath?.let { it1 ->
                val result = Logic().classify(imageBitmap!!, contentResolver, cacheDir, applicationContext, viewBinding.spinner.selectedItem.toString(), checkboxValues.map { it ?: false }.toTypedArray())
                viewBinding.result.text = handleResult(result)
            }
        }

        viewBinding.checkBoxGender.setOnCheckedChangeListener { _, isChecked ->
            checkboxValues[0] = isChecked
        }

        viewBinding.checkBoxAge.setOnCheckedChangeListener { _, isChecked ->
            checkboxValues[1] = isChecked
        }

        viewBinding.checkBoxEmo.setOnCheckedChangeListener { _, isChecked ->
            checkboxValues[2] = isChecked
        }

        viewBinding.checkBoxEth.setOnCheckedChangeListener { _, isChecked ->
            checkboxValues[3] = isChecked
        }

        val options = ArrayList<String>()
        options.add("wydajny")
        options.add("optymalny")
        options.add("dokładny")

        val arrayAdapter = ArrayAdapter(this, R.layout.simple_spinner_item, options)
        arrayAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        viewBinding.spinner.adapter = arrayAdapter
        viewBinding.testSpinner.adapter = arrayAdapter


        viewBinding.normalMode.setOnClickListener {
            viewBinding.startLayer.visibility = View.INVISIBLE
            viewBinding.standardLayer.visibility = View.VISIBLE
        }

        viewBinding.testMode.setOnClickListener {
            viewBinding.startLayer.visibility = View.INVISIBLE
            viewBinding.testLayer.visibility = View.VISIBLE
        }

        viewBinding.startTest.setOnClickListener{
            val result = Logic().classifyTestMode(applicationContext)
            viewBinding.testResult.text = result
        }
        viewBinding.info.setOnClickListener{
            showCustomDialog()
        }

        imagePath?.let {
            capturedImageView.visibility = View.VISIBLE
            val file: File? = Logic().uriToFile(it, contentResolver, cacheDir)
            file?.let {
                imageBitmap = Logic().rotateBitmapIfNeeded(BitmapFactory.decodeFile(it.absolutePath), it)
                capturedImageView.setImageBitmap(imageBitmap)

            }
        }

    }
    private fun showCustomDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Instrukcja")

        val textView = TextView(this)
        textView.text = "Do poprawnego działania aplikacji wymangany jest dostęp do kamery telefonu, możesz jej udzielić w ustawieniach aplikacji.\nTwoje zdjęcia są przetwarzane lokalnie z wykorzystaniem zasobów telefonu nie są nigdzie udostępnianie, a aplikacja nie korzysta z Internetu.\nAplikacja umożliwia klasyfikacje zdjęć twarzy pod kątem płci płci, wieku, emocji oraz pochodzenia etnicznego.\nDostępne są trzy tryby klasyfikacji z wykorzystaniem głębokich sieci neuronowych:\n- wydajny (model sieci MobileNetV3Small),\n- optymalny (model sieci EfficientNetV2B3),\n- dokładny (model sieci EfficientNetB6).\nW celu poprawnej klasyfikacji należy zrobić zdjęcie tak, aby twarz zajmowała jak największą powierzchnie zdjęcia."
        val paddingInDp = 16
        val scale = resources.displayMetrics.density
        val paddingInPixels = (paddingInDp * scale + 0.5f).toInt()
        textView.setPadding(paddingInPixels, paddingInPixels, paddingInPixels, paddingInPixels)

        builder.setView(textView)

        builder.setPositiveButton("OK") { dialog, which ->
        }
        val dialog = builder.create()
        dialog.show()
    }
    fun handleResult(map: Map<String,String>): String {
        var result = ""
        if (map["Gender"] != null) {
            result += "Płeć: " + map["Gender"] + "\n"
        }
        if (map["Age"] != null) {
            result += "Grupa wiekowa: " + map["Age"] + "\n"
        }
        if (map["Emo"] != null) {
            result += "Emocje: " + map["Emo"] + "\n"
        }
        if (map["Eth"] != null) {
            result += "Pochodzenie etniczne: " + map["Eth"] + "\n"
        }
        return result
    }


    override fun onDestroy() {
        super.onDestroy()
    }

}