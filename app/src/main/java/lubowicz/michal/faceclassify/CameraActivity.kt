package lubowicz.michal.faceclassify

import android.Manifest
import android.content.ContentValues
import androidx.camera.core.CameraControl

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import lubowicz.michal.faceclassify.databinding.ActivityCameraBinding
import lubowicz.michal.faceclassify.databinding.ActivityMainBinding
import org.tensorflow.lite.support.image.TensorImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URI
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


typealias LumaListener = (luma: Double) -> Unit

class CameraActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityCameraBinding

    private var imageCapture: ImageCapture? = null
    private var imagePath: Uri? = null

    private var currentCamera = CameraSelector.DEFAULT_BACK_CAMERA
    private lateinit var capturedImageView: ImageView
    private var cameraControl: CameraControl? = null
    private var flashEnabled = false

    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        viewBinding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        capturedImageView = findViewById(R.id.capturedImageView)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        if (allPermissionsGranted()) {
            Log.d(TAG, "Permissions granted, starting camera")
            startCamera()
        } else {
            Log.d(TAG, "Requesting permissions")
            requestPermissions()
        }

        viewBinding.takePictureButton.setOnClickListener { takePhoto() }
        viewBinding.switchCameraButton.setOnClickListener { switchCamera() }
        viewBinding.usePictureButton.setOnClickListener { usePicture() }
        viewBinding.takeNewPictureButton.setOnClickListener { takeNewPicture() }

        cameraExecutor = Executors.newSingleThreadExecutor()

        val screenWidth = resources.displayMetrics.widthPixels
        val previewWidth = (screenWidth * 1.0).toInt()
        val previewHeight = (screenWidth * 1.0 * 4.0 / 3.0).toInt()

        val layoutParams = viewBinding.viewFinder.layoutParams
        layoutParams.width = previewWidth
        layoutParams.height = previewHeight
        viewBinding.viewFinder.layoutParams = layoutParams


        val layoutImageParams = viewBinding.capturedImageView.layoutParams
        layoutImageParams.width = previewWidth
        layoutImageParams.height = previewHeight
        viewBinding.capturedImageView.layoutParams = layoutImageParams


        viewBinding.toggleFlashButton.setOnClickListener {
            toggleFlash()
            viewBinding.toggleFlashButton.text = if (flashEnabled) "Wyłącz lampę" else "Włącz lampę"
        }

    }

    private fun toggleFlash() {
        flashEnabled = !flashEnabled
    }
    private fun takeNewPicture() {
        capturedImageView.visibility = View.INVISIBLE
        viewBinding.usePictureButton.visibility = View.INVISIBLE
        viewBinding.takeNewPictureButton.visibility = View.INVISIBLE
        viewBinding.takePictureButton.visibility = View.VISIBLE
        viewBinding.switchCameraButton.visibility = View.VISIBLE
        viewBinding.toggleFlashButton.visibility = View.VISIBLE
        startCamera()
    }

    private fun usePicture() {
        val intent = Intent(this, MainActivity::class.java)

        intent.putExtra("imagePath", imagePath)

        startActivity(intent)
        finish()
    }




    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && it.value == false)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(baseContext,
                    "Permission request denied",
                    Toast.LENGTH_SHORT).show()
            } else {
                startCamera()
            }
        }

    private fun takePhoto() {
        Log.d(TAG, "takePhoto()")
        if(flashEnabled) cameraControl?.enableTorch(true)
        waitMs(10)
        val imageCapture = imageCapture ?: return

        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun
                        onImageSaved(output: ImageCapture.OutputFileResults){
                    imagePath = output.savedUri

                    imagePath?.let { displayCapturedImage(it) }
                    stopCameraPreview()
                }

            }

        )
        waitMs(30)
        if(flashEnabled) cameraControl?.enableTorch(false)
    }

    private fun waitMs(milliseconds: Long) {
        try {
            Thread.sleep(milliseconds)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
    private fun switchCamera() {
        currentCamera = if (currentCamera == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }

        startCamera()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)

                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .build()
            try {
                cameraProvider.unbindAll()

                val camera = cameraProvider.bindToLifecycle(
                    this, currentCamera, preview, imageCapture)
                cameraControl = camera.cameraControl

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }


    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }
    private fun stopCameraPreview() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            cameraProvider.unbindAll()
        }, ContextCompat.getMainExecutor(this))

        viewBinding.usePictureButton.visibility = View.VISIBLE
        viewBinding.takeNewPictureButton.visibility = View.VISIBLE
        viewBinding.takePictureButton.visibility = View.INVISIBLE
        viewBinding.switchCameraButton.visibility = View.INVISIBLE
        viewBinding.toggleFlashButton.visibility = View.INVISIBLE


    }

    private fun displayCapturedImage(imagePath: Uri) {
        capturedImageView.visibility = View.VISIBLE

        val file: File? = Logic().uriToFile(imagePath, contentResolver, cacheDir)
        file?.let {
            val rotatedBitmap = Logic().rotateBitmapIfNeeded(BitmapFactory.decodeFile(it.absolutePath), it)
            capturedImageView.setImageBitmap(rotatedBitmap)
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}