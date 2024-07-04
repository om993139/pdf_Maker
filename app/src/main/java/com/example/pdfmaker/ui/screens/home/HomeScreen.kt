package com.example.pdfmaker.ui.screens.home


import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pdfmaker.R
import com.example.pdfmaker.data.models.PdfEntity
import com.example.pdfmaker.ui.navigation.Routs
import com.example.pdfmaker.ui.screens.common.ErrorScreen
import com.example.pdfmaker.ui.screens.common.LoadingDialog
import com.example.pdfmaker.ui.screens.home.components.PdfLayout
import com.example.pdfmaker.ui.screens.home.components.RenameDeleteDialog
import com.example.pdfmaker.utils.copyPdfFileToAppDirectory
import com.example.pdfmaker.utils.showToast
import com.example.pdfmaker.ui.viewmodels.PdfViewModel
import com.example.pdfmaker.utils.Resource
import com.example.pdfmaker.utils.getFileSize
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID


@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun HomeScreen(pdfViewModel: PdfViewModel, navController: NavHostController) {

    LoadingDialog(pdfViewModel = pdfViewModel)
    RenameDeleteDialog(pdfViewModel = pdfViewModel)
    val activity = LocalContext.current as Activity
    val context = LocalContext.current
    val pdfState by pdfViewModel.pdfStateFlow.collectAsState()
    val message = pdfViewModel.message

    val scope = rememberCoroutineScope()
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())





    LaunchedEffect(Unit) {
        message.collect {
            when (it) {
                is Resource.Success -> {
                    context.showToast(it.data)
                }

                is Resource.Error -> {
                    context.showToast(it.message)
                }

                Resource.Idle -> {}
                Resource.Loading -> {}
            }
        }
    }


    val scannerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartIntentSenderForResult()
        )
        { result ->

            if (result.resultCode == Activity.RESULT_OK) {
                val scanningResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
                scanningResult?.pdf?.let { pdf ->
                    Log.d("pdfName", pdf.uri.lastPathSegment.toString())
                    val date = Date()
                    val fileName = SimpleDateFormat(
                        "dd-MMM-yyyy  HH:mm:ss",
                        Locale.getDefault()
                    ).format(date) + ".pdf"

                    copyPdfFileToAppDirectory(
                        context,
                        pdf.uri, fileName
                    )


                    val pdfEntity = PdfEntity(
                        UUID.randomUUID().toString(),
                        fileName,
                        getFileSize(context, fileName),
                        date
                    )
                    pdfViewModel.insertPdf(
                        pdfEntity
                    )
                }
            }
        }


    val scanner = remember {
        GmsDocumentScanning.getClient(
            GmsDocumentScannerOptions.Builder()
                .setGalleryImportAllowed(true)
                .setResultFormats(
                    GmsDocumentScannerOptions.RESULT_FORMAT_JPEG,
                    GmsDocumentScannerOptions.RESULT_FORMAT_PDF
                )
                .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL).build()
        )

    }


    androidx.compose.material3.ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(300.dp)
                        .padding(20.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_brand1),
                        contentDescription = "App Logo",
                        modifier = Modifier
                            .size(100.dp)
                            .align(Alignment.CenterHorizontally)


                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    //HorizontalDivider()
                    NavigationDrawerItem(
                        label = { Text("Home") },
                        selected = true,
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Home"
                            )
                        },
                        onClick = {
                            navController.navigate(Routs.Home)
                            coroutineScope.launch {
                                drawerState.close()
                            }
                        }
                    )

                    NavigationDrawerItem(
                        label = { Text("Reduced Image Size") },
                        selected = false,
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Reduced Image Size"
                            )
                        },
                        onClick = {
                            navController.navigate(Routs.ImageReduceder)
                            coroutineScope.launch {
                                drawerState.close()
                            }
                        }
                    )
                    //HorizontalDivider()

                    NavigationDrawerItem(
                        label = { Text("About") },
                        selected = false,
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = "Versoin 1.0"
                            )
                        },
                        onClick = {
                            coroutineScope.launch {
                                drawerState.close()
                            }
                            Toast.makeText(context, "About clicked", Toast.LENGTH_SHORT).show()

                        }
                    )
                    //HorizontalDivider()
                    NavigationDrawerItem(
                        label = { Text("Settings") },
                        selected = false,
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "Settings"
                            )
                        },
                        onClick = {
                            coroutineScope.launch {
                                drawerState.close()
                            }
                            Toast.makeText(context, "Setting clicked", Toast.LENGTH_SHORT).show()

                        }
                    )

                }
            }
        }
    ) {


        Scaffold(
            topBar = {
                TopAppBar(title = {
                    Text(text = "Pdf Maker")


                },

                    navigationIcon = {

                        IconButton(onClick = {

                            scope.launch {
                                drawerState.open()
                            }

                        }) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "menu")
                        }
                    },
                    scrollBehavior = scrollBehavior,


                    actions = {
                        Switch(checked = pdfViewModel.isDarkMode, onCheckedChange = {

                            pdfViewModel.isDarkMode = it
                        })


                    })
            },


            floatingActionButton = {
                ExtendedFloatingActionButton(onClick = {
                    scanner.getStartScanIntent(activity).addOnSuccessListener {

                        scannerLauncher.launch(
                            IntentSenderRequest.Builder(it).build()
                        )


                    }.addOnFailureListener {
                        it.printStackTrace()
                        context.showToast(it.message.toString())
                    }


                }, text = {
                    Text(text = stringResource(R.string.scan))
                }, icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.cameraicon),
                        contentDescription = "camera"
                    )
                })
            }


        ) { paddingValue ->


            pdfState.DisplayResult(onLoading = {

                //if circular progress bar use in screen


                // Box(
                //     modifier = Modifier.fillMaxSize(),
                //    contentAlignment = Alignment.Center
                // ) {
                //  CircularProgressIndicator()
                // }

            }, onSuccess = { pdfList ->

                if (pdfList.isEmpty()) {
                    ErrorScreen(message = "No pdf")
                } else {

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValue)
                    ) {
                        items(items = pdfList, key = { pdfEntity ->
                            pdfEntity.id
                        }) { pdfEntity ->
                            PdfLayout(pdfEntity = pdfEntity, pdfViewModel = pdfViewModel)

                        }
                    }
                }
            }, onError = {

                ErrorScreen(message = it)
            })


        }

    }
}







