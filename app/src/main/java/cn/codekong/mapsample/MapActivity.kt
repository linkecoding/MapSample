package cn.codekong.mapsample

import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import cn.codekong.mapsample.databinding.ActivityMapBinding
import com.arcgismaps.ArcGISEnvironment
import com.arcgismaps.LicenseKey
import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.Basemap
import com.arcgismaps.mapping.layers.ArcGISVectorTiledLayer
import com.arcgismaps.mapping.layers.vectortiles.VectorTileCache
import com.arcgismaps.mapping.view.MapView
import java.io.File
import java.io.FileOutputStream
import kotlin.concurrent.thread

class MapActivity : AppCompatActivity() {
    private val activityMapBinding: ActivityMapBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_map)
    }

    private val mapView: MapView by lazy {
        activityMapBinding.mapView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ArcGISEnvironment.setLicense(LicenseKey.create("runtimelite,1000,rud1441455565,none,2K0RJAY3FPGZH2RGJ223")!!)
        lifecycle.addObserver(mapView)

        checkAndCopyVtpkFile {
            getExternalFilesDir(Environment.MEDIA_UNKNOWN)?.let { extFileDir ->
                val vectorTileCachePath = extFileDir.absoluteFile.toString() + "/china.vtpk"
                if (!File(vectorTileCachePath).exists()) {
                    Toast.makeText(this@MapActivity, "请将vtpk文件放置在SD卡根目录下", Toast.LENGTH_SHORT)
                        .show()
                    return@checkAndCopyVtpkFile
                }
                val cache = VectorTileCache(vectorTileCachePath)

                // Use the tile cache to create an ArcGISVectorTiledLayer.
                val tiledLayer = ArcGISVectorTiledLayer(cache)

                // Display the vector tiled layer as a basemap.
                mapView.map = ArcGISMap(Basemap(tiledLayer))
                mapView.isAttributionBarVisible = false
            }

        }
    }

    fun checkAndCopyVtpkFile(copyFinished: () -> Unit) {
        thread {
            val appFileDir = getExternalFilesDir(Environment.MEDIA_UNKNOWN)
            appFileDir?.let {
                val vectorTileCachePath = it.absolutePath + "/china.vtpk"
                if (!File(vectorTileCachePath).exists()) {
                    assets.open("china.vtpk").use { input ->
                        FileOutputStream(vectorTileCachePath).use { output ->
                            input.copyTo(output)
                            copyFinished()
                        }
                    }
                } else {
                    copyFinished()
                }
            }
        }
    }
}