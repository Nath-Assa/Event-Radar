package com.github.se.eventradar.viewmodel.qrCode

import android.graphics.ImageFormat
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer

class QrCodeAnalyser: ImageAnalysis.Analyzer {

    // list of supported Image Formats
    private val supportedImageFormats =
        listOf(ImageFormat.YUV_420_888, ImageFormat.YUV_422_888, ImageFormat.YUV_444_888)

//    var decodedString: String? = null
      var onDecoded: ((String?) -> Unit)? = null //

    override fun analyze(image: ImageProxy) {

        // only want to scan if it is a QR Code
        if (image.format in supportedImageFormats) {
            val bytes = image.planes.first().buffer.toByteArray()

            // parameters to scan
            val source =
                PlanarYUVLuminanceSource(
                    bytes, image.width, image.height, 0, 0, image.width, image.height, false
                )

            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            try {

                // result is the info encoded in to QR Code (String of userId in our case)
                val result =
                    MultiFormatReader()
                        .apply {
                            setHints(
                                mapOf(DecodeHintType.POSSIBLE_FORMATS to arrayListOf(BarcodeFormat.QR_CODE))
                            )
                        }
                        .decode(binaryBitmap)
                //if onDecoded is null (has not been initialised b Viewmodel) will simply return Null

                onDecoded?.invoke(result.toString())
//                initialiseString(result.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            } finally { // close image once scanning process done
                image.close()
            }
        }
    }

    // method to return all bytes in a ByteArray
    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()
        return ByteArray(remaining()).also { get(it) }
    }


//    private fun initialiseString (result: String?) {
//        decodedString = result
//    }
}
