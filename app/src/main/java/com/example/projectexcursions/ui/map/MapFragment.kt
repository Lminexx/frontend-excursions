package com.example.projectexcursions.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.projectexcursions.R
import com.example.projectexcursions.databinding.FragmentMapBinding
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.mapview.MapView

class MapFragment:Fragment(R.layout.fragment_map) {

    private val viewModel: MapViewModel by viewModels()
    private lateinit var mapView: MapView
    private lateinit var binding: FragmentMapBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapKitFactory.initialize(requireContext())
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapBinding.inflate(inflater, container, false)
        mapView = binding.mapview

        initCallback()
        subscribe()

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        MapKitFactory.getInstance().onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()

        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    private fun initCallback() {}

    private fun subscribe() {}
}