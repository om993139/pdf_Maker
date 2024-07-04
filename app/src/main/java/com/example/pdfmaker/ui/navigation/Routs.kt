package com.example.pdfmaker.ui.navigation

import kotlinx.serialization.Serializable

sealed class Routs {

    @Serializable
    object Home
    @Serializable
    object ImageReduceder
    @Serializable
    object About
    @Serializable
    object Setting



}