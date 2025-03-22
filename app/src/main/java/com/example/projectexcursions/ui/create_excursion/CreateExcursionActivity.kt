package com.example.projectexcursions.ui.create_excursion

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projectexcursions.R
import com.example.projectexcursions.adapter.PhotoAdapter
import com.example.projectexcursions.adapter.SearchResultsAdapter
import com.example.projectexcursions.databinding.ActivityExcursionCreateBinding
import com.example.projectexcursions.models.SearchResult
import com.example.projectexcursions.repositories.pointrepo.PointRepositoryImpl
import com.example.projectexcursions.ui.map.poi_map.PoiBottomFragment
import com.example.projectexcursions.ui.utilies.ProgressBar
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.layers.GeoObjectTapListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.GeoObjectSelectionMetadata
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.map.VisibleRegionUtils
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.search.Response
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SearchOptions
import com.yandex.mapkit.search.SearchType
import com.yandex.mapkit.search.Session
import com.yandex.runtime.Error
import com.yandex.runtime.image.ImageProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreateExcursionActivity : AppCompatActivity() {

    private lateinit var adapter: PhotoAdapter
    private lateinit var binding: ActivityExcursionCreateBinding
    private lateinit var progressBar: ProgressBar
    private lateinit var searchResultsAdapter: SearchResultsAdapter
    private lateinit var mapView: MapView
    private lateinit var map: Map
    private lateinit var placemark: PlacemarkMapObject
    private var routePolyline: Polyline? = null
    private val pointRepository = PointRepositoryImpl()
    private val viewModel: CreateExcursionViewModel by viewModels()

    private val REQUEST_CODE_PERMISSION = 1003

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExcursionCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mapView = binding.mapview
        map = mapView.mapWindow.map

        initData()
        initCallback()
        subscribe()
    }

    override fun onStart() {
        super.onStart()
        Log.d("onStart","")
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
        Log.d("onStart","")
        MapKitFactory.getInstance().onStop()
    }

    private fun initData() {
        adapter = PhotoAdapter(this, emptyList())

        searchResultsAdapter = SearchResultsAdapter { item ->
            setLocation(item.point)
            viewModel.hideSearchResults()
        }

        binding.recyclerViewSelectedImages.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.searchResultsRecycler.layoutManager = LinearLayoutManager(this)
        binding.searchResultsRecycler.adapter = searchResultsAdapter
        binding.recyclerViewSelectedImages.setHasFixedSize(true)
        binding.recyclerViewSelectedImages.adapter = adapter
        progressBar = ProgressBar()
    }

    private fun initCallback() {
        binding.buttonCreateExcursion.setOnClickListener {
            it.isClickable = false
            Handler(Looper.getMainLooper()).postDelayed({
                it.isClickable = true
            }, 5000)
            viewModel.clickCreateExcursion()
        }

        binding.excursionDescription.movementMethod = ScrollingMovementMethod()

        binding.buttonSelectImage.setOnClickListener {
            checkPermissionsAndProceed()
        }

        map.addTapListener(geoObjectTapListener())

        binding.searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                search(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                search(newText)
                return true
            }

        })

        binding.searchView.setOnCloseListener {
            try {
                Log.d("SetOnCloseListener", "")
                viewModel.deleteSearchResults()
                false
            } catch (e: Exception) {
                Log.d("CloseSearchException", e.message.toString())
                false
            }
        }

        binding.searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                viewModel.toggleSearchResultsVisibility()
            }
        }
    }

    private fun subscribe() {
        viewModel.wantComeBack.observe(this) { wannaComeBack ->
            if (wannaComeBack) {
                finish()
            }
        }

        viewModel.createExcursion.observe(this) { wannaCreate ->
            if (wannaCreate) {
                val title = binding.excursionTitle.text.toString().trim()
                val description = binding.excursionDescription.text.toString().trim()
                if (viewModel.isExcursionCorrect(this, title, description)) {
                    viewModel.createExcursion(this@CreateExcursionActivity, title, description)
                    progressBar.show(this)
                }
            }
        }

        viewModel.routeLiveData.observe(this) { points ->
            if (!points.isNullOrEmpty()) {
                drawRoute(points)
            }
        }

        viewModel.selectedImages.observe(this) { selectedImages ->
            adapter.updatePhotos(selectedImages)
        }

        viewModel.searchResults.observe(this) { results ->
            searchResultsAdapter.updateData(results)
        }

        viewModel.isSearchResultsVisible.observe(this) { isVisible ->
            binding.searchResultsRecycler.visibility = if (isVisible) View.VISIBLE else View.GONE
        }

        viewModel.nextPoint.observe(this) { point ->
            setLocation(point)
            if (viewModel.prevPoint.value != null) {
                lifecycleScope.launch {
                    viewModel.getRoute()
                }
            }
        }
    }

    private val pickImages = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val clipData = result.data?.clipData
            val imageUris = mutableListOf<Uri>()
            if (clipData != null) {
                for (i in 0 until clipData.itemCount) {
                    imageUris.add(clipData.getItemAt(i).uri)
                }
            } else {
                result.data?.data?.let { imageUris.add(it) }
            }
            if (imageUris.isNotEmpty()) {
                viewModel.addSelectedImages(imageUris)
            }
        }
    }

    private fun checkPermissionsAndProceed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES), REQUEST_CODE_PERMISSION)
            } else {
                openImagePicker()
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_PERMISSION)
            } else {
                openImagePicker()
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        pickImages.launch(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker()
            } else {
                Toast.makeText(this, "Разрешение не предоставлено", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setLocation(point: Point) {
        Log.d("setLocation","")
        try {
            map.move(
                CameraPosition(
                    point,
                    15.0f,
                    150.0f,
                    30.0f
                ),
                Animation(Animation.Type.SMOOTH, 1f),
                null
            )
            setPin(point)
        } catch (eNull: NullPointerException) {
            Toast.makeText(this, "Индиана Джонс нашёл неприятный артефакт", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setPin(point: Point) {
        Log.d("setPin", "")

        val imageProvider = ImageProvider.fromResource(this, R.drawable.ic_location_pin)

        placemark = map.mapObjects.addPlacemark().apply {
            val iconStyle = IconStyle().apply { scale = 0.4f }
            geometry = point
            setIcon(imageProvider, iconStyle)
        }
    }

    private fun search(query: String?) {
        val searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.ONLINE)
        val searchOptions = SearchOptions().apply {
            searchTypes = SearchType.BIZ.value
            resultPageSize = 10
        }

        val searchSessionListener = object : Session.SearchListener {
            override fun onSearchResponse(response: Response) {
                val searchResults = response.collection.children.mapNotNull { result ->
                    val point = result.obj?.geometry?.firstOrNull()?.point
                    val name = result.obj?.name ?: return@mapNotNull null
                    SearchResult(name, point!!)
                }
                viewModel.updateSearchResults(searchResults)
            }

            override fun onSearchError(error: Error) {
                Toast.makeText(this@CreateExcursionActivity, "Ошибка поиска", Toast.LENGTH_SHORT).show()
            }
        }

        if (!query.isNullOrEmpty()) {
            val visibleRegion = VisibleRegionUtils.toPolygon(map.visibleRegion)
            searchManager.submit(query, visibleRegion, searchOptions, searchSessionListener)
        }
    }

    private val geoObjectTapListener = GeoObjectTapListener { event ->
        if (pointRepository.getCachedStart()!=null && pointRepository.getCachedEnd()!=null)
            pointRepository.deleteCachedPoints()
        val geoObject = event.geoObject
        Log.d("GeoObject", "Тап по объекту: ${geoObject.name ?: "Без имени"}")
        val point = geoObject.geometry.firstOrNull()?.point

        if (point != null) {
            map.move(
                CameraPosition(point, 17.0f, 0.0f, 0.0f),
                Animation(Animation.Type.SMOOTH, 1f),
                null
            )
        }

        var address = ""
        val name = geoObject.name ?: "Неизвестное место"
        val description = geoObject.descriptionText ?: "Нет описания"
        val metadata = geoObject.metadataContainer.getItem(GeoObjectSelectionMetadata::class.java)

        if (metadata != null) {
            map.selectGeoObject(metadata)
            Log.d("GeoObject", "Выбран объект с ID: ${metadata.objectId}")
        } else {
            Log.e("GeoObject", "Метаданные умерли(((")
        }

        val searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.ONLINE)
        val searchOptions = SearchOptions().apply {
            searchTypes = SearchType.BIZ.value
            resultPageSize = 1
        }

        val searchSessionListener = object: Session.SearchListener {
            override fun onSearchResponse(response: Response) {
                val result = response.collection.children.firstOrNull()
                address = result?.obj?.name ?: "Тут все взорвали, ниче нет"
                Log.e("onSearchResponse: ", result?.equals(null).toString())
                val latitude = point!!.latitude
                val longitude = point.longitude

                Log.d("point", point.equals(null).toString())
                Log.d("longitude", longitude.toString())
                Log.d("latitude", latitude.toString())

                viewModel.setNextPoint(point)
            }

            override fun onSearchError(p0: Error) {
                Log.e("GeoObject", "Ошибка поиска адреса")
                Toast.makeText(this@CreateExcursionActivity, "Ошибка получения адреса", Toast.LENGTH_SHORT).show()            }
        }

        searchManager.submit(point!!, 1, searchOptions, searchSessionListener)

        Log.d("GeoObject", "Название: $name, Описание: $description")

        return@GeoObjectTapListener true
    }

    private fun drawRoute(points: List<Point>) {
        if (points.isNotEmpty()) {
            routePolyline = Polyline(points)
            map.mapObjects.addPolyline(routePolyline!!)
        }
    }

    private fun clearRoute() {
        routePolyline?.let {
            map.mapObjects.clear()
            routePolyline = null
        }
    }

    private fun geoObjectTapListener(): GeoObjectTapListener {
        return geoObjectTapListener
    }
}