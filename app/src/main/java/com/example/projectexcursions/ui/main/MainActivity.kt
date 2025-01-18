package com.example.projectexcursions.ui.main

import com.example.projectexcursions.ui.excursionlist.ExListFragment
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
        Log.d("StartMainActivity", "MainActivityStarted")

        viewModel.setStartFragment()
        initCallBack()
        subscribe()
    }

    private fun subscribe() {
        viewModel.menuItem.observe(this) { menuItem ->
            when (menuItem) {
                null -> replaceFragment(ExListFragment())
                "list" -> replaceFragment(ExListFragment())
                "fav" -> replaceFragment(FavFragment())
                "map" -> replaceFragment(MapFragment())
                "profile" -> {
                    lifecycleScope.launch {
                        val isAuth = viewModel.checkAuthStatus()
                        if (isAuth)
                            replaceFragment(ProfileFragment())
                        else {
                            val intent = Intent(this@MainActivity, AuthActivity::class.java)
                            startActivityForResult(intent, AUTH_REQUEST_CODE)
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

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .setReorderingAllowed(true)
            .commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val isAuth = data?.getBooleanExtra(AuthActivity.EXTRA_AUTH_STATUS, false) ?: false
            Log.d("AuthDataNaN?","$data")
            if (isAuth) {
                replaceFragment(ProfileFragment())
            }
        }
    }
}