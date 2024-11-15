package com.example.projectexcursions.ui.main

import ExListFragment
import FavFragment
import MapFragment
import ProfileFragment
import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import com.example.projectexcursions.R
import com.example.projectexcursions.databinding.ActivityMainBinding



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

        setCurrentFragment(exListFragment)

        initCallBack()
        subscribe()
    }

    private fun subscribe() {}

    private fun initCallBack() {
        binding.botNavView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.list -> setCurrentFragment(exListFragment)
                R.id.fav -> setCurrentFragment(favFragment)
                R.id.map -> setCurrentFragment(mapFragment)
                R.id.profile -> setCurrentFragment(profileFragment)

            }
            true
        }
    }

    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, fragment)
            commit()
        }
}
