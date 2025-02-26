package com.example.projectexcursions.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.projectexcursions.R
import com.example.projectexcursions.databinding.FragmentMapBinding
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.GeoObjectTapListener
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CameraUpdateReason
import com.yandex.mapkit.map.GeoObjectSelectionMetadata
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectTapListener
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

class MapFragment : Fragment(R.layout.fragment_map) {

    private val REQUEST_CODE_PERMISSION = 1004

    private val viewModel: MapViewModel by viewModels()
    private lateinit var placemark: PlacemarkMapObject
    private lateinit var mapView: MapView
    private lateinit var binding: FragmentMapBinding
    private lateinit var map: Map

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

        subscribe()
        initCallback()

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        Log.d("onResume", "")
        viewModel.startLocationTracker()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initCallback() {
        Log.d("initCallback","")
        map.addTapListener(geoObjectTapListener())
        map.mapObjects.addTapListener(mapTapListener())//реализацию этой штуки надо будет поменять, это для тестов
    }

    private fun subscribe() {
        Log.d("subscribe","")
        viewModel.curPoint.observe(viewLifecycleOwner) {curPoint ->
            Log.d("subscribe","curPoint: ${curPoint.latitude}, ${curPoint.longitude}")
            setLocation(curPoint)
        }
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
                viewModel.startLocationTracker()
            } else {
                Toast.makeText(requireContext(), "Разрешение не предоставлено", Toast.LENGTH_SHORT).show()
            }
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
                viewModel.startLocationTracker()
            }, 1000)
        } else {
            Log.d("Permissions Denied", "no(((")
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                REQUEST_CODE_PERMISSION)
        }
    }

    private fun setLocation(point:Point) {
        Log.d("setLocation","")
        try {
            map.move(
                CameraPosition(
                    point,
                    17.0f,
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

    private fun setPin(point: Point, name: String? = null) {
        Log.d("setPin", "")

        val imageProvider = ImageProvider.fromResource(requireContext(), R.drawable.ic_location_pin)

        placemark = map.mapObjects.addPlacemark().apply {
            val iconStyle = IconStyle().apply { scale = 0.4f }
            geometry = point
            setIcon(imageProvider, iconStyle)
            addTapListener { _, tappedPoint ->
                Toast.makeText(
                    requireContext(),
                    "Tapped the point $name: (${tappedPoint.latitude}, ${tappedPoint.longitude})",
                    Toast.LENGTH_SHORT
                ).show()
                true
            }
        }
    }

    private fun geoObjectTapListener(): GeoObjectTapListener {
        return geoObjectTapListener
    }

    private fun mapTapListener(): MapObjectTapListener {
        return mapTapListener
    }

    private fun search() {
        val searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.ONLINE)
        val searchOptions = SearchOptions().apply {
            searchTypes = SearchType.BIZ.value or SearchType.GEO.value
            resultPageSize = 20
        }

        val searchSessionListener = object : Session.SearchListener {
            override fun onSearchResponse(response: Response) {
                Log.d("Search", "Найдено объектов: ${response.collection.children.size}")
                map.mapObjects.clear()

                response.collection.children.forEach { searchResult ->
                    val point = searchResult.obj?.geometry?.firstOrNull()?.point
                    val name = searchResult.obj?.name ?: "Без имени"

                    if (point != null) {
                        setPin(point, name)
                    }
                }
            }

            override fun onSearchError(error: Error) {
                Toast.makeText(requireContext(), "Ошибка поиска: $error", Toast.LENGTH_SHORT).show()
            }
        }

        val visibleRegion = VisibleRegionUtils.toPolygon(map.visibleRegion)
        searchManager.submit("кафе", visibleRegion, searchOptions, searchSessionListener)
    }

    private val geoObjectTapListener = GeoObjectTapListener { event ->
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

        val name = geoObject.name ?: "Неизвестное место"
        val description = geoObject.descriptionText ?: "Нет описания"
        val metadata = geoObject.metadataContainer.getItem(GeoObjectSelectionMetadata::class.java)

        Toast.makeText(requireContext(),
            "Название: $name\nОписание: $description\n" +
                    "Координаты: ${point?.latitude ?: "null"}, ${point?.longitude ?: "null"}", Toast.LENGTH_SHORT).show()
        Log.d("GeoObject", "Название: $name, Описание: $description")

        Log.d("GeoObject", "Название: $name, Описание: $description")

        if (metadata != null) {
            map.selectGeoObject(metadata)
            Log.d("GeoObject", "Выбран объект с ID: ${metadata.objectId}")
        } else {
            Log.e("GeoObject", "Метаданные умерли(((")
        }
        return@GeoObjectTapListener true
    }

    private val mapTapListener = MapObjectTapListener { _, point ->
        Log.d("MapTap", "Кликнули по карте в точке: ${point.latitude}, ${point.longitude}")
        Toast.makeText(requireContext(), "Клик в ${point.latitude}, ${point.longitude}", Toast.LENGTH_SHORT).show()
        true
    }
}