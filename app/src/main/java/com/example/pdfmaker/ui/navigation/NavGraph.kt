package com.example.pdfmaker.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.pdfmaker.ui.screens.image_Size_Reducer.ImageResizer
import com.example.pdfmaker.ui.screens.home.HomeScreen
import com.example.pdfmaker.ui.viewmodels.PdfViewModel


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun NavGraph(
    navController: NavHostController,
    pdfViewModel: PdfViewModel
) {
    NavHost(navController = navController, startDestination = Routs.Home){
        composable<Routs.Home>{
            HomeScreen( pdfViewModel ,navController)
        }
        composable<Routs.ImageReduceder>{
            ImageResizer(navController =navController)
        }
    }
}