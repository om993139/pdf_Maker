package com.example.pdfmaker.ui.screens.home.components

import android.content.ClipData
import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.pdfmaker.R
import com.example.pdfmaker.ui.viewmodels.PdfViewModel
import com.example.pdfmaker.utils.deleteFile
import com.example.pdfmaker.utils.getFileUri
import com.example.pdfmaker.utils.renameFile
import com.example.pdfmaker.utils.showToast
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import java.util.Date

@Composable
fun RenameDeleteDialog(pdfViewModel: PdfViewModel) {

    var newNameText by remember(pdfViewModel.currentPdfEntity) {
        mutableStateOf(pdfViewModel.currentPdfEntity?.name ?: "")
    }

    val context = LocalContext.current

    if (pdfViewModel.showRenameDialog) {

        Dialog(onDismissRequest = {
            pdfViewModel.showRenameDialog = false
        }) {

            Surface(
                shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        stringResource(R.string.rename_pdf),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(value = newNameText,
                        onValueChange = { newText -> newNameText = newText },
                        label = { Text(stringResource(R.string.pdf_name)) })

                    Spacer(modifier = Modifier.height(8.dp))

                    Row {


                        IconButton(onClick = {

                            pdfViewModel.currentPdfEntity?.let {
                                pdfViewModel.showRenameDialog = false
                               val getFileUri = getFileUri(context , it.name)
                               val shareIntent = Intent(Intent.ACTION_SEND)
                                shareIntent.type = "application/pdf"
                                shareIntent.clipData = ClipData.newRawUri("" , getFileUri)
                                shareIntent.putExtra(Intent.EXTRA_STREAM, getFileUri)
                                shareIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                context.startActivity(Intent.createChooser(shareIntent , "Share"))
                            }

                        }) {
                            Icon(
                                painterResource(id = R.drawable.ic_share),
                                contentDescription = null,

                            )
                        }





                        IconButton(onClick = {

                            pdfViewModel.currentPdfEntity?.let {
                                pdfViewModel.showRenameDialog = false
                                if (deleteFile(context, it.name)) {
                                    pdfViewModel.deletePdf(it)
                                } else {

                                    context.showToast("Something went wrong")
                                }
                            }

                        }) {
                            Icon(
                                painterResource(id = R.drawable.icons8_delete2),
                                contentDescription = null,
                                tint = Color.Red

                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = {

                            pdfViewModel.showRenameDialog = false
                        }) { Text(stringResource(R.string.cancel)) }


                        Spacer(Modifier.width(8.dp))
                        Button(onClick = {
                            pdfViewModel.currentPdfEntity?.let { pdf ->
                                if (!pdf.name.equals(newNameText, true)) {
                                    pdfViewModel.showRenameDialog = false
                                    renameFile(
                                        context,
                                        pdf.name,
                                        newNameText
                                    )
                                    val updatePdf = pdf.copy(
                                        name = newNameText, lastModifiedTime = Date()
                                    )
                                    pdfViewModel.updatePdf(updatePdf)
                                } else {
                                    pdfViewModel.showRenameDialog = false
                                }
                            }
                        }) { Text(stringResource(R.string.update)) }
                    }
                }

            }

        }
    }
}