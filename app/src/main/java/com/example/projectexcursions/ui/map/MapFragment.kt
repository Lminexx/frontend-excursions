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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.projectexcursions.BuildConfig
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("onCreate", "")

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("onCreateView", "")
        binding = FragmentMapBinding.inflate(inflater, container, false)
        mapView = binding.mapview
        placemark = mapView.mapWindow.map.mapObjects.addPlacemark()

        checkPermissions()
        subscribe()
        initCallback()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        checkPermissions()
    }

    private fun initCallback() {
        Log.d("initCallback","")
        val placemarkTapListener = MapObjectTapListener { _, point ->
            Toast.makeText(
                requireContext(),
                "Tapped the point (${point.longitude}, ${point.latitude})",
                Toast.LENGTH_SHORT
            ).show()
            true
        }
        placemark.addTapListener(placemarkTapListener)
    }

    private fun subscribe() {
        Log.d("subscribe","")
        viewModel.curPoint.observe(viewLifecycleOwner) {curPoint ->
            Log.d("subscribe","curPoint: ${curPoint.latitude}, ${curPoint.longitude}")
            setOnCreateLocation(curPoint)
            setPin(curPoint)
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("onStart","")
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
    }

    override fun onStop() {
        mapView.onStop()
        Log.d("onStart","")
        MapKitFactory.getInstance().onStop()
        super.onStop()
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
            viewModel.startLocationTracker()
        } else {
            Log.d("Permissions Denied", "no(((")
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                REQUEST_CODE_PERMISSION)
        }
    }

    private fun setOnCreateLocation(point:Point) {
        Log.d("setOnCreateLocation","")
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
        } catch (eNull: NullPointerException) {
            Toast.makeText(requireContext(), "Индиана Джонс нашёл неприятный артефакт", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setPin(point: Point) {
        Log.d("setPin","")
        val imageProvider = ImageProvider.fromResource(requireContext(), R.drawable.ic_location_pin)
        placemark.apply {
            val iconStyle = IconStyle().apply { scale = 0.4f }
            geometry = point
            setIcon(imageProvider, iconStyle)
            setText(
                "",
                TextStyle()
            )
        }
    }
    /*
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("onViewCreated","")
        viewModel.startLocationTracker()
    }*/
}