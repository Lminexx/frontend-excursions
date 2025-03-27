package com.example.projectexcursions.ui.main

import com.example.projectexcursions.ui.excursions_list.ExListFragment
import com.example.projectexcursions.ui.favorite_excursions.FavFragment
import com.example.projectexcursions.ui.map.MapFragment
import android.app.Activity
import android.content.Intent
import com.example.projectexcursions.ui.profile.ProfileFragment
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.projectexcursions.R
import com.example.projectexcursions.databinding.ActivityMainBinding
import com.example.projectexcursions.repositories.pointrepo.PointRepository
import com.example.projectexcursions.ui.auth.AuthActivity
import com.example.projectexcursions.ui.not_auth.NotAuthFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var currentFrag: Fragment
    private val AUTH_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d("StartMainActivity", "MainActivityStarted")

        viewModel.setStartFragment()
        initCallBack()
        subscribe()
    }

    override fun onResume() {
        super.onResume()

        Log.d("OnResume", "$currentFrag")
        if (currentFrag == NotAuthFragment())
            replaceFragment(FavFragment())
    }

    private fun subscribe() {
        viewModel.menuItem.observe(this) { menuItem ->
            when (menuItem) {
                null -> replaceFragment(ExListFragment())
                "list" -> replaceFragment(ExListFragment())
                "fav" ->
                    lifecycleScope.launch {
                        val isAuth = viewModel.checkAuthStatus()
                        if (isAuth)
                            replaceFragment(FavFragment())
                        else {
                            replaceFragment(NotAuthFragment().apply {
                                arguments = Bundle().apply {
                                    putString("prev_frag", "fav")
                                    putInt("AUTH_REQUEST_CODE", AUTH_REQUEST_CODE)
                                }
                            })
                        }
                    }
                "map" -> replaceFragment(MapFragment())
                "profile" -> {
                    lifecycleScope.launch {
                        val isAuth = viewModel.checkAuthStatus()
                        if (isAuth)
                            replaceFragment(ProfileFragment())
                        else {
                            replaceFragment(NotAuthFragment().apply {
                                arguments = Bundle().apply {
                                    putString("prev_frag", "profile")
                                    putInt("AUTH_REQUEST_CODE", AUTH_REQUEST_CODE)
                                }
                            })
                        }
                    }
                }
            }
        }

        viewModel.token.observe(this) {token ->
            if (token == null)
                Log.d("CachedToken", "null")
            else
                Log.d("CachedToken", token.token)
        }
    }

    private fun initCallBack() {
        binding.botNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.list -> viewModel.changeMenuItem("list")
                R.id.fav -> viewModel.changeMenuItem("fav")
                R.id.map -> viewModel.changeMenuItem("map")
                R.id.profile -> viewModel.changeMenuItem("profile")
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .setReorderingAllowed(true)
            .commit()
        currentFrag = fragment
    }

    fun updateBottomNavSelectionToList() {
        binding.botNavView.selectedItemId = R.id.list
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d("MainActivity", "onActivityResult called: requestCode=$requestCode, resultCode=$resultCode")

        if (requestCode == AUTH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val isAuth = data?.getBooleanExtra(AuthActivity.EXTRA_AUTH_STATUS, false) ?: false
            val prevFrag = data?.getStringExtra("prev_frag")
            Log.d("MainActivity", "Auth success, prev_frag: $prevFrag")

            if (isAuth) {
                when (prevFrag) {
                    "fav" -> replaceFragment(FavFragment())
                    "profile" -> replaceFragment(ProfileFragment())
                    else -> replaceFragment(ExListFragment())
                }
            } else {
                replaceFragment(ExListFragment())
            }
        }
    }
}