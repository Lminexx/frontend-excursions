package com.example.projectexcursions.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.auth0.android.jwt.JWT
import com.example.projectexcursions.R
import com.example.projectexcursions.token_bd.TokenDao
import com.example.projectexcursions.ui.auth.AuthActivity
import com.example.projectexcursions.ui.create_excursion.CreateExcursionActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    @Inject lateinit var tokenDao: TokenDao

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkToken()


        view.findViewById<Button>(R.id.button_create_excursion).setOnClickListener {
            navigateToCreateExcursionActivity()
        }
    }

    private fun checkToken() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val tokenEntity = tokenDao.getToken() // Получение токена из БД.

                if (tokenEntity == null) {
                    // Если токен отсутствует в базе данных, переходим к авторизации.
                    withContext(Dispatchers.Main) {
                        navigateToAuthActivity()
                    }
                } else if (isValidToken(tokenEntity.token)) {
                    // Если токен валиден, извлекаем имя пользователя.
                    val userName = extractUserNameFromToken(tokenEntity.token)
                    withContext(Dispatchers.Main) {
                        showUserName(userName)
                    }
                } else {
                    // Если токен невалиден, запускаем AuthActivity для авторизации.
                    withContext(Dispatchers.Main) {
                        navigateToAuthActivity()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Ошибка при проверке токена", Toast.LENGTH_SHORT).show()
                    navigateToAuthActivity()
                }
            }
        }
    }

    private fun navigateToAuthActivity() {
        val intent = Intent(requireContext(), AuthActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    // Новая функция для перехода в CreateExcursionActivity.
    private fun navigateToCreateExcursionActivity() {
        val intent = Intent(requireContext(), CreateExcursionActivity::class.java)
        startActivity(intent)
    }

    private fun isValidToken(token: String): Boolean {
        return try {
            val jwt = JWT(token)
            !jwt.isExpired(10) // Проверка на истечение срока действия за 10 секунд от текущего времени.
        } catch (e: Exception) {
            false // Если возникло исключение при декодировании, считаем токен невалидным.
        }
    }

    private fun extractUserNameFromToken(token: String): String? {
        return try {
            val jwt = JWT(token)
            jwt.getClaim("user_name").asString() // Извлечение имени пользователя из claim "user_name".
        } catch (e: Exception) {
            null // В случае ошибки возвращаем null.
        }
    }

    private fun showUserName(userName: String?) {
        userName?.let {
            Toast.makeText(requireContext(), "Привет, $it", Toast.LENGTH_SHORT).show()
        } ?: run {
            Toast.makeText(requireContext(), "Имя пользователя не найдено", Toast.LENGTH_SHORT).show()
        }
    }
}
