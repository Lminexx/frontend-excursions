package com.example.projectexcursions.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.projectexcursions.BuildConfig
import com.example.projectexcursions.R
import com.example.projectexcursions.databinding.FragmentMapBinding
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider

class MapFragment : Fragment(R.layout.fragment_map) {

    private val viewModel: MapViewModel by viewModels()
    private val curPoint: Point = Point(55.751225, 37.62954) //просто для тестов
    private lateinit var placemark: PlacemarkMapObject
    private lateinit var mapView: MapView
    private lateinit var binding: FragmentMapBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("onCreate", "")
        checkPermissions()

        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
        Log.d("YandexMap", "API Key: ${BuildConfig.MAPKIT_API_KEY}")
        MapKitFactory.initialize(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("onCreateView", "")
        binding = FragmentMapBinding.inflate(inflater, container, false)
        mapView = binding.mapview
        viewModel.point(curPoint)
        placemark = mapView.mapWindow.map.mapObjects.addPlacemark()

        subscribe()
        initCallback()

        return binding.root
    }

    private fun initCallback() {
        placemark.addTapListener(placemarkTapListener)
    }

    private fun subscribe() {
        viewModel.curPoint.observe(viewLifecycleOwner) {curPoint ->
            setOnCreateLocation(curPoint)
            setPin(curPoint)
        }
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }


    private val placemarkTapListener = MapObjectTapListener { _, point ->
        Toast.makeText(
            requireContext(),
            "Tapped the point (${point.longitude}, ${point.latitude})",
            Toast.LENGTH_SHORT
        ).show()
        true
    }

    private fun checkPermissions() {
        if (ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
            val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            ActivityCompat.requestPermissions(requireActivity(), permissions, 0)
        }
    }

    private fun setOnCreateLocation(point:Point) {
        try {
            mapView.mapWindow.map.move(
                CameraPosition(
                    point,
                    17.0f,
                    150.0f,
                    30.0f
                )
            )
        } catch (eNull: NullPointerException) {
            Toast.makeText(requireContext(), "Индиана Джонс нашёл неприятный артефакт", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setPin(point: Point) {
        val imageProvider = ImageProvider.fromResource(requireContext(), R.drawable.location_pin_ic)
        placemark.apply {
            geometry = point
            setIcon(imageProvider)
        }
    }
}