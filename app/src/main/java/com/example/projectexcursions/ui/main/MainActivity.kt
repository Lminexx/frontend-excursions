package com.example.projectexcursions.ui.main

import com.example.projectexcursions.ui.excursionlist.ExListFragment
import FavFragment
import MapFragment
import android.app.Activity
import android.content.Context
import android.content.Intent
import com.example.projectexcursions.ui.profile.ProfileFragment
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.projectexcursions.R
import com.example.projectexcursions.databinding.ActivityMainBinding
import com.example.projectexcursions.ui.auth.AuthActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private val AUTH_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initCallBack()
        subscribe()
    }

    private fun subscribe() {
        viewModel.menuItem.observe(this) { menuItem ->
            when (menuItem) {
                "list" -> switchFragment(ExListFragment())
                "fav" -> switchFragment(FavFragment())
                "map" -> switchFragment(MapFragment())
                "profile" -> {
                    lifecycleScope.launch {
                        viewModel.checkAuthStatus()

                        viewModel.isAuth.observe(this@MainActivity) { isAuth ->
                            if (isAuth)
                                switchFragment(ProfileFragment())
                            else {
                                val intent = Intent(this@MainActivity, AuthActivity::class.java)
                                startActivityForResult(intent, AUTH_REQUEST_CODE)
                            }
                        }
                    }
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

    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            switchFragment(ProfileFragment())
        }
    }
}