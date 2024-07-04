package com.example.pdfmaker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.pdfmaker.ui.screens.home.HomeScreen
import com.example.pdfmaker.ui.theme.PdfMakerTheme
import com.example.pdfmaker.ui.viewmodels.PdfViewModel


class MainActivity : ComponentActivity() {
    private val pdfViewModel by viewModels<PdfViewModel>{
        viewModelFactory {
            addInitializer(PdfViewModel::class){
                PdfViewModel(application)
            }
        }
    }
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            splashScreen.setKeepOnScreenCondition{pdfViewModel.isSplashScreen}
            PdfMakerTheme (pdfViewModel.isDarkMode , false){
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen(pdfViewModel)





                }
            }
        }
    }
}

