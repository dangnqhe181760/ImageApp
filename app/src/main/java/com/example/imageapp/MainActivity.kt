package com.example.imageapp

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.imageapp.ui.theme.ImageAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
                    // Call HomePage which reacts to contact changes
                    HomePage()
                }
            }

            // Initialize contacts asynchronously outside the composable

        }
    }
}

@Composable
fun HomePage(){
    val viewModel: MainViewModel = viewModel()
    val observedContacts by viewModel.contactResponse.collectAsState(initial = emptyList())
    viewModel.proceedContact()
    observedContacts?.let { ImageGrid(contacts = it) }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun ImageGrid(contacts: List<Contact>) {
    val context = LocalContext.current
    val selectedContacts = remember { mutableStateListOf<Contact>() }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(contacts.size) { index ->
            val contact = contacts[index]
            val painter = rememberImagePainter(data = contact.pictureUrl)

            // Launched effect to handle image saving
            LaunchedEffect(contact.pictureUrl) {
                Log.d("image", "start to load image")
                val imageLoader = ImageLoader(context)
                val request = ImageRequest.Builder(context)
                    .data(contact.pictureUrl)
                    .build()

                val result = (imageLoader.execute(request) as? SuccessResult)?.drawable
            }

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
                        painter = painterResource(id = R.drawable.baseline_check_circle_outline_24), // Your tick image resource
                        contentDescription = "Selected",
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(24.dp) // Adjust size as needed
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}



@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}