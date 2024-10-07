package com.example.imageapp

import android.app.Activity
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.imageapp.ui.theme.ImageAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.IOException
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Scaffold(
                modifier = Modifier.semantics {
                    testTagsAsResourceId = true
                },
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onBackground,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
            ) { padding ->
                Row(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .navigationBarsPadding()
                        .consumeWindowInsets(padding)
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.only(
                                WindowInsetsSides.Horizontal,
                            ),
                        ),
                ) {
                    // Call TabScreen() which reacts to contact changes
                    TabScreen()
                }
            }

            // Initialize contacts asynchronously outside the composable

        }
    }
}

@Composable
fun HomePage() {
    val viewModel: MainViewModel = viewModel()
    val observedContacts by viewModel.contactResponse.collectAsState(initial = emptyList())
    viewModel.proceedContact()
    observedContacts?.let { ImageGrid(contacts = it) }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun SavedImage() {
    val context = LocalContext.current
    val images = remember { mutableStateListOf<Uri>() }
    val selectedImages = remember { mutableStateListOf<Uri>() }
    val coroutineScope = rememberCoroutineScope()

    // Load saved images from gallery
    LaunchedEffect(Unit) {
        loadImagesFromGallery(context, images)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (selectedImages.isNotEmpty()) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        selectedImages.forEach { imageUri ->
                            deleteImageFromGallery(context, imageUri)
                            images.remove(imageUri)
                        }
                        selectedImages.clear()  // Clear selection after deletion
                    }
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Delete Selected Images")
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),  // 3 columns grid
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(images.size) { index ->
                val imageUri = images[index]
                val painter = rememberImagePainter(data = imageUri)

                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .aspectRatio(1f) // Ensures the images are square
                        .fillMaxWidth() // Fill the width of the grid cell
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    if (selectedImages.contains(imageUri)) {
                                        selectedImages.remove(imageUri)
                                    } else {
                                        selectedImages.add(imageUri)
                                    }
                                }
                            )
                        }
                ) {
                    Image(
                        painter = painter,
                        contentDescription = null,
                        contentScale = ContentScale.Crop, // Crop to fit within bounds
                        modifier = Modifier.fillMaxSize() // Fill the grid cell
                    )

                    // Show a checkmark on selected images
                    if (selectedImages.contains(imageUri)) {
                        Image(
                            painter = painterResource(id = R.drawable.baseline_check_circle_outline_24),
                            contentDescription = "Selected",
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(24.dp)
                                .padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}

// Function to delete an image from the gallery
fun deleteImageFromGallery(context: Context, imageUri: Uri) {
    try {
        context.contentResolver.delete(imageUri, null, null)
        Log.d("image", "Deleted image: $imageUri")
    } catch (e: Exception) {
        Log.e("image", "Failed to delete image: $imageUri", e)
    }
}

// Load images from the custom gallery folder
fun loadImagesFromGallery(context: Context, images: SnapshotStateList<Uri>) {
    val projection = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DISPLAY_NAME
    )

    val query = context.contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection,
        "${MediaStore.Images.Media.RELATIVE_PATH} = ?",
        arrayOf("${Environment.DIRECTORY_PICTURES}/MyAppImages/"),
        "${MediaStore.Images.Media.DATE_ADDED} DESC"
    )

    query?.use { cursor ->
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        while (cursor.moveToNext()) {
            val id = cursor.getLong(idColumn)
            val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            images.add(uri)
        }
    }
}

@Composable
fun TabScreen() {
    var tabIndex by remember { mutableStateOf(0) }

    val tabs = listOf("Online Image", "Saved Image")

    Column(modifier = Modifier.fillMaxWidth()) {
        TabRow(selectedTabIndex = tabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(text = { Text(title) },
                    selected = tabIndex == index,
                    onClick = { tabIndex = index },
                    icon = {
                        when (index) {
                            0 -> Image(
                                painter = painterResource(id = R.drawable.baseline_language_24),
                                contentDescription = "Selected",
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(4.dp)
                            )
                            1 -> Image(
                                painter = painterResource(id = R.drawable.baseline_download_for_offline_24),
                                contentDescription = "Selected",
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(4.dp)
                            )
                        }
                    }
                )
            }
        }
        when (tabIndex) {
            0 -> HomePage()
            1 -> SavedImage()
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun ImageGrid(contacts: List<Contact>) {
    val context = LocalContext.current
    val selectedContacts = remember { mutableStateListOf<Contact>() }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                coroutineScope.launch {
                    selectedContacts.forEach { contact ->
                        withContext(Dispatchers.IO) {
                            Log.d("image", "start to load image")
                            val bitmap = loadBitmapFromUrl(contact.pictureUrl)
                            if (bitmap != null) {
                                saveImageToGallery(context, bitmap, contact.id.toString())
                            }
                        }
                    }
                }
            }
        ) {
            Text(text = "Save Image")
        }

        Spacer(modifier = Modifier.width(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(contacts.size) { index ->
                val contact = contacts[index]
                val painter = rememberImagePainter(data = contact.pictureUrl)

                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .aspectRatio(1f) // Ensure a square aspect ratio
                        .fillMaxWidth() // Auto-fit to the available width
                ) {
                    Image(
                        painter = painter,
                        contentDescription = null,
                        contentScale = ContentScale.Crop, // Crop to fit within bounds
                        modifier = Modifier
                            .fillMaxSize() // Fill the entire box
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = {
                                        Log.d("image", "Image tapped")
                                    },
                                    onLongPress = {
                                        if (selectedContacts.contains(contact)) {
                                            selectedContacts.remove(contact)
                                        } else {
                                            selectedContacts.add(contact)
                                        }
                                        Log.d("image", "Image long-pressed")
                                    }
                                )
                            }
                    )

                    // Show tick image if the contact is selected
                    if (selectedContacts.contains(contact)) {
                        Image(
                            painter = painterResource(id = R.drawable.baseline_check_circle_outline_24),
                            contentDescription = "Selected",
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(24.dp)
                                .padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}

suspend fun loadBitmapFromUrl(url: String): Bitmap? = withContext(Dispatchers.IO) {
    try {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.doInput = true
        connection.connect()
        val input = connection.inputStream
        BitmapFactory.decodeStream(input)
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

fun saveImageToGallery(context: Context, bitmap: Bitmap, imageName: String) {
    val resolver = context.contentResolver

    // Set the content values for the image
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "$imageName.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/MyAppImages")
        put(MediaStore.Images.Media.IS_PENDING, 1)
    }

    // Insert the image into the MediaStore
    val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

    if (imageUri != null) {
        resolver.openOutputStream(imageUri)?.use { outputStream ->
            // Save the bitmap to the output stream
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        }

        // After saving, mark the image as complete
        contentValues.clear()
        contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(imageUri, contentValues, null, null)
    } else {
        Log.e("saveImage", "Failed to save image")
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}