import androidx.fragment.app.Fragment
import com.example.projectexcursions.R
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.projectexcursions.token_bd.TokenRepository
import com.example.projectexcursions.ui.auth.AuthActivity
import com.example.projectexcursions.ui.auth.AuthViewModel
import com.example.projectexcursions.ui.auth.AuthViewModelFactory

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var authViewModel: AuthViewModel
    private lateinit var tokenRepository: TokenRepository // Объявляем переменную

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Получаем контекст приложения и передаем его в TokenRepository
        tokenRepository = TokenRepository(requireContext())

        // Инициализация ViewModel
        val factory = AuthViewModelFactory(tokenRepository)
        authViewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)

        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Проверка аутентификации
        authViewModel.checkAndDecodeToken().observe(viewLifecycleOwner, Observer { claims ->
            if (claims != null) {
                // Пользователь аутентифицирован, можете выполнить действия для отображения профиля
                
            } else {
                // Пользователь не аутентифицирован, переходим на экран аутентификации
                startAuthActivity()
            }
        })

        return view
    }

    private fun startAuthActivity() {
        val intent = Intent(requireActivity(), AuthActivity::class.java)
        startActivity(intent)
    }
}