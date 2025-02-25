package com.example.projectexcursions.ui.map

import android.Manifest
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
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.map.TextStyle
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider

class MapFragment : Fragment(R.layout.fragment_map) {

    private val REQUEST_CODE_PERMISSION = 1004

    private val viewModel: MapViewModel by viewModels()
    private lateinit var placemark: PlacemarkMapObject
    private lateinit var mapView: MapView
    private lateinit var binding: FragmentMapBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("onCreateView", "")
        binding = FragmentMapBinding.inflate(inflater, container, false)
        mapView = binding.mapview

        checkPermissions()
        subscribe()
        initCallback()

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        Log.d("onResume", "")
        viewModel.startLocationTracker()
    }

    private fun initCallback() {
        Log.d("initCallback","")
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
            mapView.mapWindow.map.move(
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

    private fun setPin(point: Point) {
        Log.d("setPin", "")
        val imageProvider = ImageProvider.fromResource(requireContext(), R.drawable.ic_location_pin)

        if (this::placemark.isInitialized) {
            mapView.mapWindow.map.mapObjects.remove(placemark)
        }

        placemark = mapView.mapWindow.map.mapObjects.addPlacemark().apply {
            val iconStyle = IconStyle().apply { scale = 0.4f }
            geometry = point
            setIcon(imageProvider, iconStyle)
            addTapListener { _, tappedPoint ->
                Log.d(
                    "PlacemarkTap",
                    "Tapped point: ${tappedPoint.latitude}, ${tappedPoint.longitude}"
                )
                Toast.makeText(
                    requireContext(),
                    "Tapped the point (${tappedPoint.latitude}, ${tappedPoint.longitude})",
                    Toast.LENGTH_SHORT
                ).show()
                true
            }
        }
    }
}