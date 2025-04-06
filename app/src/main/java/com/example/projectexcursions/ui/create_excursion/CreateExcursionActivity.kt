package com.example.projectexcursions.ui.create_excursion

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projectexcursions.R
import com.example.projectexcursions.adapter.PhotoAdapter
import com.example.projectexcursions.adapter.PlacesAdapter
import com.example.projectexcursions.adapter.SearchResultsAdapter
import com.example.projectexcursions.databinding.ActivityExcursionCreateBinding
import com.example.projectexcursions.models.PlaceItem
import com.example.projectexcursions.models.SearchResult
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
import kotlin.random.Random

@AndroidEntryPoint
class CreateExcursionActivity : AppCompatActivity() {

    private lateinit var adapter: PhotoAdapter
    private lateinit var binding: ActivityExcursionCreateBinding
    private lateinit var progressBar: ProgressBar
    private lateinit var searchResultsAdapter: SearchResultsAdapter
    private lateinit var placesAdapter: PlacesAdapter
    private lateinit var mapView: MapView
    private lateinit var map: Map
    private lateinit var placemark: PlacemarkMapObject
    private var routePolyline: Polyline? = null
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
        viewModel.getUserPosition()
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
            Log.d("searchAdapter", "true")
            val id = item.id
            val name = item.name
            val point = item.point

            val placeItem = createPlaceItem(id, name, point)
            viewModel.setPoint(point)
            viewModel.addPlace(placeItem)
            viewModel.hideSearchResults()
        }

        placesAdapter = PlacesAdapter(
            context = this,
            onItemClick = { placeName ->
                Log.d("PlaceName", "Пока ниче не сделано")
            },
            onDeleteClick = { placeId ->
                viewModel.deletePlace(placeId)
            },
            places = emptyList()
        )

        binding.recyclerViewSelectedImages.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.searchResultsRecycler.layoutManager = LinearLayoutManager(this)
        binding.places.layoutManager = LinearLayoutManager(this)
        binding.searchResultsRecycler.adapter = searchResultsAdapter
        binding.places.adapter = placesAdapter
        binding.recyclerViewSelectedImages.setHasFixedSize(true)
        binding.recyclerViewSelectedImages.adapter = adapter
        progressBar = ProgressBar()
    }

    @SuppressLint("ClickableViewAccessibility")
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
                val places = viewModel.placeItems.value ?: emptyList()
                if (viewModel.isExcursionCorrect(this, title, description, places)) {
                    viewModel.createExcursion(this@CreateExcursionActivity, title, description)
                    //finish()
                }
            }
        }

        viewModel.routeLiveData.observe(this) { points ->
            if (!points.isNullOrEmpty()) {
                Log.d("RouteData", points.size.toString())
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

        viewModel.curPoint.observe(this) { point ->
            if (point != null) {
                Log.d("nextPoint:", "observing")
                setLocation(point)
                if (viewModel.prevPoint.value != null) {
                    lifecycleScope.launch {
                        viewModel.getRoute()
                    }
                }
            }
        }

        viewModel.userPos.observe(this) { point ->
            setUserLocation(point)
        }

        viewModel.placeItems.observe(this) { placeItems ->

            try {
                for (placeItem in placeItems) {
                    Log.d("PLaceItem", "${placeItem.name}, ${placeItem.id}")
                }
                placesAdapter.updatePlaces(placeItems)
                Log.d("PlaceItemsObserve", "true " + placeItems[placeItems.size - 1].name)
            } catch (indexOutOfBound: IndexOutOfBoundsException) {
                clearRoute()
                Log.d("IndexOutOfBound", "хихи поймали дурачка)")
            } catch (e: Exception) {
                clearRoute()
                Log.d("Exception", e.message.toString())
            }
        }

        viewModel.deletingPLaceId.observe(this) { placeId ->
            Log.d("DeletingPlace", "true")
            clearRoute()
            lifecycleScope.launch {
                val places = viewModel.placeItems.value
                for (place in places!!) {
                    if (placeId != place.id) {
                        viewModel.setPoint(place.point)
                    }
                }
            }
        }
    }

    private val pickImages =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
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
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES), REQUEST_CODE_PERMISSION
                )
            } else {
                openImagePicker()
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_PERMISSION
                )
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

    private fun setUserLocation(point: Point) {
        Log.d("setUserLocation","")
        try {
            map.move(
                CameraPosition(
                    point,
                    13.0f,
                    150.0f,
                    30.0f
                ),
                Animation(Animation.Type.SMOOTH, 1f),
                null
            )
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
            val searchManager =
                SearchFactory.getInstance().createSearchManager(SearchManagerType.ONLINE)
            val searchOptions = SearchOptions().apply {
                searchTypes = SearchType.BIZ.value
                resultPageSize = 10
            }

            val searchSessionListener = object : Session.SearchListener {
                override fun onSearchResponse(response: Response) {
                        val searchResults = response.collection.children.mapNotNull { result ->
                            val point = result.obj?.geometry?.firstOrNull()?.point
                            Log.d("point", point?.equals(null).toString())
                            val name = result.obj?.name ?: return@mapNotNull null
                            Log.d("name", name.equals(null).toString())
                            val id = viewModel.getId(10)
                            Log.d("id", id.equals(null).toString())
                            SearchResult(id, name, point!!)
                        }
                    for (searchResult in searchResults) {
                        Log.d("SearchResult", "${searchResult.id}, ${searchResult.name}, ${searchResult.point}")
                    }
                    viewModel.updateSearchResults(searchResults)
                }

                override fun onSearchError(error: Error) {
                    Toast.makeText(
                        this@CreateExcursionActivity,
                        "Ошибка поиска",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            if (!query.isNullOrEmpty()) {
                val visibleRegion = VisibleRegionUtils.toPolygon(map.visibleRegion)
                searchManager.submit(query, visibleRegion, searchOptions, searchSessionListener)
            }
    }

    private val geoObjectTapListener = GeoObjectTapListener { event ->
        try {
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
            val metadata =
                geoObject.metadataContainer.getItem(GeoObjectSelectionMetadata::class.java)
            val id = metadata.objectId

            val placeItem = createPlaceItem(id, name, point)

            if (metadata != null) {
                map.selectGeoObject(metadata)
                Log.d("GeoObject", "Выбран объект с ID: ${metadata.objectId}")
            } else {
                Log.e("GeoObject", "Метаданные умерли(((")
            }

            val searchManager =
                SearchFactory.getInstance().createSearchManager(SearchManagerType.ONLINE)
            val searchOptions = SearchOptions().apply {
                searchTypes = SearchType.BIZ.value
                resultPageSize = 1
            }

            val searchSessionListener = object : Session.SearchListener {
                override fun onSearchResponse(response: Response) {
                    val result = response.collection.children.firstOrNull()
                    address = result?.obj?.name ?: "Тут все взорвали, ниче нет"
                    Log.e("onSearchResponse: ", result?.equals(null).toString())
                    val latitude = point?.latitude
                    val longitude = point?.longitude

                    Log.d("point", point?.equals(null).toString())
                    Log.d("longitude", longitude.toString())
                    Log.d("latitude", latitude.toString())

                    viewModel.setPoint(point!!)
                    viewModel.addPlace(placeItem)
                }

                override fun onSearchError(p0: Error) {
                    Log.e("GeoObject", "Ошибка поиска адреса")
                    Toast.makeText(
                        this@CreateExcursionActivity,
                        "Ошибка получения адреса",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            searchManager.submit(point!!, 1, searchOptions, searchSessionListener)

            Log.d("GeoObject", "Название: $name, Описание: $description")

            return@GeoObjectTapListener true
        } catch (e: Exception) {
            Log.e("GeoObjectException", "${e.message}")
            return@GeoObjectTapListener false
        }
    }

    private fun drawRoute(points: List<Point>) {
        if (points.isNotEmpty()) {
            routePolyline = Polyline(points)
            map.mapObjects.addPolyline(routePolyline!!)
        }
    }

    private fun clearRoute() {
        Log.d("clearRoute","")
        Log.d("RoutePolyline", routePolyline?.equals(null).toString())
        Log.d("PlaceItemsSize", "${viewModel.placeItems.value?.size}")
        if (routePolyline != null) {
            Log.d("RoutePolyline", routePolyline?.equals(null).toString())
            Log.d("PlaceItemsSize", "${viewModel.placeItems.value?.size}")
            map.mapObjects.clear()
            routePolyline = null
        } else if (viewModel.placeItems.value?.size == 1) {
            Log.d("RoutePolyline", routePolyline?.equals(null).toString())
            Log.d("PlaceItemsSize", "${viewModel.placeItems.value?.size}")
            map.mapObjects.clear()
            routePolyline = null
            viewModel.clearRouteData()
        } else {
            Log.d("RoutePolyline", routePolyline?.equals(null).toString())
            Log.d("PlaceItemsSize", "${viewModel.placeItems.value?.size}")
            map.mapObjects.clear()
            viewModel.resetPoints()
        }
    }

    private fun geoObjectTapListener(): GeoObjectTapListener {
        return geoObjectTapListener
    }

    private fun createPlaceItem(id: String, name: String, point: Point?): PlaceItem {
        var photos: List<String> = emptyList()
        lifecycleScope.launch {
            photos = viewModel.getPhotos(point!!)
        }

        val photoUris: MutableList<Uri> = mutableListOf()

        for (photo in photos) {
            photoUris.add(photo.toUri())
        }

        return PlaceItem(id, name, point!!, photoUris)
    }
}
//TODO сделать получение списка фото места
