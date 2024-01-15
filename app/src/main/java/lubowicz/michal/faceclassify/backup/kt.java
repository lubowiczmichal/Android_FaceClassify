//package lubowicz.michal.faceclassify
//
//import android.Manifest
//import android.app.Activity
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.graphics.Bitmap
//import android.os.Build
//import android.os.Bundle
//import android.os.PersistableBundle
//import android.provider.MediaStore
//import android.util.Log
//import android.view.View
//import android.widget.Button
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.annotation.RequiresApi
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.ViewModelProvider
//import com.google.android.gms.tflite.client.TfLiteInitializationOptions
//import org.tensorflow.lite.support.image.TensorImage
//
//class MainActivity : AppCompatActivity() {
//
//private val cameraRequestCode = 123
//private lateinit var imageView: ImageView
//
//@RequiresApi(Build.VERSION_CODES.M)
//    override fun onCreate(savedInstanceState: Bundle?) {
//            super.onCreate(savedInstanceState)
//            setContentView(R.layout.activity_main)
//            var options = TfLiteInitializationOptions.builder()
//            .setEnableGpuDelegateSupport(true)
//            .build()
//            imageView = findViewById(R.id.imageView)
//            val openCameraButton: Button = findViewById(R.id.openCameraButton)
//            val runClassify: Button = findViewById(R.id.runClassify)
//
//            val resultText: TextView = findViewById(R.id.resultText)
//            openCameraButton.setOnClickListener {
//            if (checkCameraPermission()) {
//            dispatchTakePictureIntent()
//            } else {
//            requestPermissions(arrayOf(Manifest.permission.CAMERA), cameraRequestCode)
//            }
//            }
//
//            runClassify.setOnClickListener {
//            //imageView.value?.let { it1 -> classify(it1, resultText) }
//            }
//
//            }
//
//private val activityResultLauncher =
//        registerForActivityResult(
//        ActivityResultContracts.RequestMultiplePermissions())
//        { permissions ->
//        // Handle Permission granted/rejected
//        var permissionGranted = true
//        permissions.entries.forEach {
//        if (it.key in REQUIRED_PERMISSIONS && it.value == false)
//        permissionGranted = false
//        }
//        if (!permissionGranted) {
//        Toast.makeText(baseContext,
//        "Permission request denied",
//        Toast.LENGTH_SHORT).show()
//        } else {
//        startCamera()
//        }
//        }
//
//
//private fun checkCameraPermission(): Boolean {
//        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
//        }
//
//private fun dispatchTakePictureIntent() {
//        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
//        takePictureIntent.resolveActivity(packageManager)?.also {
//        startActivityForResult(takePictureIntent, cameraRequestCode)
//        }
//        }
//        }
//
//
//        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == cameraRequestCode && resultCode == RESULT_OK) {
//        val imageBitmap = data?.extras?.get("data") as Bitmap
//        imageView.setImageBitmap(imageBitmap)
//        imageView.visibility = View.VISIBLE
//
//        }
//        }
//
//        fun classify(bitmap: Bitmap, resultText: TextView) {
//        val model = MobileNetEthnicityCorrect.newInstance(applicationContext)
//
//        val image = TensorImage.fromBitmap(bitmap)
//
//        val startTime = System.currentTimeMillis()
//        val outputs = model.process(image)
//        val endTime = System.currentTimeMillis()
//        val elapsedTime = endTime - startTime
//
//        val probability = outputs.probabilityAsCategoryList
//        val maxProbabilityIndex = probability.indexOfFirst { it.score == probability.maxByOrNull { it.score }?.score }
//
//        val label = probability[maxProbabilityIndex].label
//
//        resultText.text = "Wyniki klasyfikacji:\n" +
//        "Klasyfikacja płci: mężczyzna\n" +
//        "Klasyfikacja wieku: grupa 25-34\n" +
//        "Klasyfikacja pochodzenia etnicznego rasa biała\n" +
//        "Klasyfikacja emocji radość\n" +
//        "W czasie 632 ms"
//        model.close()
//        }
//        }
//
//// Klasa ViewModel
//class MainViewModel : ViewModel() {
//        val capturedImage = MutableLiveData<Bitmap>()
//
//        fun setCapturedImage(bitmap: Bitmap) {
//        capturedImage.value = bitmap
//        }
//        }
