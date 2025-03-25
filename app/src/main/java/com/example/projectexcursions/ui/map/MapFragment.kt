package com.example.projectexcursions.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projectexcursions.R
import com.example.projectexcursions.adapter.SearchResultsAdapter
import com.example.projectexcursions.databinding.FragmentMapBinding
import com.example.projectexcursions.models.SearchResult
import com.example.projectexcursions.repositories.pointrepo.PointRepositoryImpl
import com.example.projectexcursions.ui.map.poi_map.PoiBottomFragment
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

@AndroidEntryPoint

class MapFragment: Fragment(R.layout.fragment_map) {

    private val REQUEST_CODE_PERMISSION = 1004

    private val viewModel: MapViewModel by activityViewModels()
    private lateinit var placemark: PlacemarkMapObject
    private lateinit var mapView: MapView
    private lateinit var binding: FragmentMapBinding
    private lateinit var searchResultsAdapter: SearchResultsAdapter
    private lateinit var map: Map
    private var routePolyline: Polyline? = null
    private val pointRepository = PointRepositoryImpl()
    private lateinit var userLocationPlacemark: PlacemarkMapObject

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.initialize(requireContext())
        checkPermissions()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("onCreateView", "")
        binding = FragmentMapBinding.inflate(inflater, container, false)
        mapView = binding.mapview
        map = mapView.mapWindow.map

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
        initCallback()
        subscribe()
    }


    override fun onResume() {
        super.onResume()

        Log.d("onResume", "")
        viewModel.getUserLocation()
    }

    override fun onPause() {
        super.onPause()

        viewModel.deleteUserPos()
        viewModel.endRoute()
        pointRepository.setFirst()
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

        searchResultsAdapter = SearchResultsAdapter { item ->
            setLocation(item.point)
            viewModel.hideSearchResults()
        }

        binding.searchResultsRecycler.layoutManager = LinearLayoutManager(context)
        binding.searchResultsRecycler.adapter = searchResultsAdapter
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initCallback() {
        Log.d("initCallback","")
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
                if (pointRepository.hasRoute()) {
                    val route = pointRepository.getRoute()
                    Log.d("route", "${route != null}")
                    if (route != null) {
                        map.mapObjects.clear()
                        map.mapObjects.addPolyline(Polyline(route))
                    }
                } else {
                    val curPoint = viewModel.getUserPos()!!
                    setLocation(curPoint)
                }
                false
            } catch (e: Exception) {
                Log.d("CloseSearchException", e.message.toString())
                viewModel.getUserLocation()
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
        Log.d("subscribe","")
        viewModel.curPoint.observe(viewLifecycleOwner) { curPoint ->
            Log.d("curPoint", "${curPoint!=null}")
            if (curPoint != null && pointRepository.hasRoute()) {
                setUserLocationPin(curPoint)
            } else if (!pointRepository.hasRoute() && curPoint != null) {
                setLocation(curPoint)
            } else {
                map.mapObjects.clear()
            }
        }

        viewModel.userLocationOnRoute.observe(viewLifecycleOwner) { location ->
            if (location != null && pointRepository.hasRoute()) {
                updateUserLocationPin(location)
            }
        }

        viewModel.routeEnded.observe(viewLifecycleOwner) { isRouteFinished ->
            if (isRouteFinished && pointRepository.isFirstRoute()) {
                showRouteCompletedDialog()
                clearRoute()
                viewModel.getUserLocation()
            }
        }

        viewModel.routeLiveData.observe(viewLifecycleOwner) { points ->
            if (!points.isNullOrEmpty()) {
                drawRoute(points)
            }
        }

        viewModel.searchResults.observe(viewLifecycleOwner) { results ->
            searchResultsAdapter.updateData(results)
        }

        viewModel.isSearchResultsVisible.observe(viewLifecycleOwner) { isVisible ->
            binding.searchResultsRecycler.visibility = if (isVisible) View.VISIBLE else View.GONE
        }
    }

    private fun checkPermissions() {
        Log.d("checkPermissions","ACCESS_FINE_LOCATION = ${Manifest.permission.ACCESS_FINE_LOCATION}, \n" +
                "ACCESS_COARSE_LOCATION = ${Manifest.permission.ACCESS_COARSE_LOCATION}")
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            Log.d("Permissions Granted", "YEES")
            Handler(Looper.getMainLooper()).postDelayed({
                viewModel.getUserLocation()
            }, 750)
        } else {
            Log.d("Permissions Denied", "no(((")
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                REQUEST_CODE_PERMISSION)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d("onRequestPermissionsResult", "")
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                || grantResults.getOrNull(1) == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permissions", "Granted")
                viewModel.getUserLocation()
            } else {
                Toast.makeText(requireContext(), "Разрешение не предоставлено", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(requireContext(), "Индиана Джонс нашёл неприятный артефакт", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setPin(point: Point) {
        Log.d("setPin", "")

        val imageProvider = ImageProvider.fromResource(requireContext(), R.drawable.ic_location_pin)

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
                    val id = viewModel.getId(10)
                    SearchResult(id, name, point!!)
                }
                viewModel.updateSearchResults(searchResults)
            }

            override fun onSearchError(error: Error) {
                Toast.makeText(requireContext(), "Ошибка поиска", Toast.LENGTH_SHORT).show()
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

                val latitude = point!!.latitude
                val longitude = point.longitude

                Log.d("point", point.equals(null).toString())
                Log.d("longitude", longitude.toString())
                Log.d("latitude", latitude.toString())

                viewModel.setEndPoint(point)

                val bottomSheetFragment = PoiBottomFragment.newInstance(name, address, description)
                bottomSheetFragment.show(parentFragmentManager, "PoiBottomFragment")
            }

            override fun onSearchError(p0: Error) {
                Log.e("GeoObject", "Ошибка поиска адреса")
                Toast.makeText(requireContext(), "Ошибка получения адреса", Toast.LENGTH_SHORT).show()            }
        }

        searchManager.submit(point!!, 1, searchOptions, searchSessionListener)

        Log.d("GeoObject", "Название: $name, Описание: $description")

        return@GeoObjectTapListener true
    }

    private fun showRouteCompletedDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Маршрут завершён!")
            .setMessage("Чиназес бро!")
            .setPositiveButton("ОК") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun drawRoute(points: List<Point>) {
        clearRoute()
        pointRepository.setIsntFirst()
        if (points.isNotEmpty()) {
            viewModel.endPoint.value?.let { setLocation(it) }
            routePolyline = Polyline(points)
            map.mapObjects.addPolyline(routePolyline!!)
            viewModel.startLocationTracker()
        }
    }

    private fun clearRoute() {
        routePolyline?.let {
            map.mapObjects.clear()
            routePolyline = null
        }
    }

    private fun setUserLocationPin(point: Point) {
        Log.d("setUserLocationPin","")
        val imageProvider = ImageProvider.fromResource(requireContext(), R.drawable.ic_location_pin)
        userLocationPlacemark = map.mapObjects.addPlacemark().apply {
            val iconStyle = IconStyle().apply { scale = 0.4f }
            geometry = point
            setIcon(imageProvider, iconStyle)
        }
    }

    private fun updateUserLocationPin(point: Point) {
        if (::userLocationPlacemark.isInitialized) {
            userLocationPlacemark.geometry = point
        } else {
            val imageProvider = ImageProvider.fromResource(requireContext(), R.drawable.ic_location_pin)
            userLocationPlacemark = map.mapObjects.addPlacemark().apply {
                val iconStyle = IconStyle().apply { scale = 0.4f }
                geometry = point
                setIcon(imageProvider, iconStyle)
            }
        }
    }

    private fun geoObjectTapListener(): GeoObjectTapListener {
        return geoObjectTapListener
    }
}
/* TODO надо прописать CameraListener, чтобы обновлялся поиск */