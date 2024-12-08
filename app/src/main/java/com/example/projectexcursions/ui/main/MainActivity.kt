package com.example.projectexcursions.ui.main

import com.example.projectexcursions.ui.excursionlist.ExListFragment
import FavFragment
import MapFragment
import com.example.projectexcursions.ui.fragments.ProfileFragment
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.projectexcursions.R
import com.example.projectexcursions.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private val exListFragment = ExListFragment()
    private val favFragment = FavFragment()
    private val mapFragment = MapFragment()
    private val profileFragment = ProfileFragment()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.startMainActivity()
        initCallBack()
        subscribe()
    }

    private fun subscribe() {
        viewModel.menuItem.observe(this) {menuItem ->
            when(menuItem) {
                null -> supportFragmentManager.beginTransaction().apply {
                    replace(R.id.fragment_container, exListFragment)
                    commit()
                }
                "list" -> supportFragmentManager.beginTransaction().apply {
                    replace(R.id.fragment_container, exListFragment)
                    commit()
                }
                "fav" -> supportFragmentManager.beginTransaction().apply {
                    replace(R.id.fragment_container, favFragment)
                    commit()
                }
                "map" -> supportFragmentManager.beginTransaction().apply {
                    replace(R.id.fragment_container, mapFragment)
                    commit()
                }
                "profile" -> supportFragmentManager.beginTransaction().apply {
                    replace(R.id.fragment_container, profileFragment)
                    commit()
                }
            }
        }
    }

    private fun initCallBack() {
        binding.botNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.list -> viewModel.clickExList()
                R.id.fav -> viewModel.clickFav()
                R.id.map -> viewModel.clickMap()
                R.id.profile -> viewModel.clickProfile()
            }
            true
        }
    }
}