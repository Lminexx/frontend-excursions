package com.example.projectexcursions.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.projectexcursions.databases.daos.TokenDao
import com.example.projectexcursions.repositories.tokenrepo.TokenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    repository: TokenRepository,
    tokenDao: TokenDao
): ViewModel() {

    private val _isAuth = MutableLiveData<Boolean>()

    val isAuth: LiveData<Boolean> get() = _isAuth

    fun openNotAuthProfile() {
        _isAuth.value = false
    }
}