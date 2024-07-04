package com.example.pdfmaker.ui.screens.image_Size_Reducer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import coil.compose.rememberAsyncImagePainter


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun ImageResizer(viewModel: MainViewModel = viewModel(), navController: NavHostController) {
    val context = LocalContext.current
    val imageUri by viewModel.imageUri.collectAsState()
    val compressionLevel by viewModel.compressionLevel.collectAsState()
    val status by viewModel.status.collectAsState()


    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                viewModel.setImageUri(it)
                viewModel.loadBitmap(context)
            }
        }
    )

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            imagePickerLauncher.launch("image/*")
        } else {
            Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    var isButtonClicked by remember { mutableStateOf(false) }
    val buttonScale by animateFloatAsState(if (isButtonClicked) 1.2f else 1.0f)


    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(text = "Image Resizer")
            },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "back")
                    }
                }

            )
        }

    ) { innerPadding ->


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = {
                when {
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        imagePickerLauncher.launch("image/*")
                    }

                    else -> {
                        requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    }
                }
            }) {
                Text("Select Image", fontStyle = FontStyle.Italic)
            }

            Spacer(modifier = Modifier.height(16.dp))




            imageUri?.let {
                Image(
                    painter = rememberAsyncImagePainter(model = it),
                    contentDescription = null,
                    modifier = Modifier.size(300.dp)
                )

            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Compression Level: ${compressionLevel.toInt()}%")
            Slider(
                value = compressionLevel,
                onValueChange = { viewModel.setCompressionLevel(it) },
                valueRange = 0f..100f,
                steps = 100
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    isButtonClicked = true
                    viewModel.compressImage(context)
                    isButtonClicked = false
                },
                modifier = Modifier
                    .scale(buttonScale)
                    .padding(20.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
            ) {
                Text("Compress Image", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = status)
        }
    }
}


class MainViewModel : ViewModel() {
    private val _imageUri = MutableStateFlow<Uri?>(null)
    val imageUri: StateFlow<Uri?> = _imageUri.asStateFlow()

    private val _compressionLevel = MutableStateFlow(50f)
    val compressionLevel: StateFlow<Float> = _compressionLevel.asStateFlow()

    private val _status = MutableStateFlow("")
    val status: StateFlow<String> = _status.asStateFlow()


    fun setImageUri(uri: Uri) {
        _imageUri.value = uri
    }

    fun loadBitmap(context: Context) {
        _imageUri.value?.let { uri ->
            // Use Coil to load bitmap
            // Not needed to store bitmap as ViewModel state
        }
    }

    fun setCompressionLevel(level: Float) {
        _compressionLevel.value = level
    }

    // fun compressImage(context: Context) {
    //val resolver = context.contentResolver
    // _imageUri.value?.let { uri ->
    // Use Coil to load bitmap
    // val file = File(context.externalCacheDir, "compressed_image.jpg")
    //  val outputStream = FileOutputStream(file)
    // Perform compression as needed
    //   _status.value = "Image compressed and saved to ${file.absolutePath}"
    // }
    //}

    fun compressImage(context: Context) {
        val resolver = context.contentResolver
        _imageUri.value?.let { uri ->
            val inputStream = resolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            // Generate unique file name
            val timeStamp =
                SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
            val fileName = "compressed_image_$timeStamp.jpg"

            // Save compressed image to external storage directory
            val fileDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val file = File(fileDir, fileName)

            try {
                val outputStream = FileOutputStream(file)
                bitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    _compressionLevel.value.toInt(),
                    outputStream
                )
                outputStream.flush()
                outputStream.close()
                _status.value = "Image compressed and saved to ${file.absolutePath}"
                Toast.makeText(context, "Image compressed and saved", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
                _status.value = "Failed to save image: ${e.message}"
                Toast.makeText(context, "Failed to compress image", Toast.LENGTH_SHORT).show()
            }
        }
            ?: run {
                Toast.makeText(context, "No image to compress", Toast.LENGTH_SHORT).show()
            }
    }

}


