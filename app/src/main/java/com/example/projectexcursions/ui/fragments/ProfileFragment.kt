import androidx.fragment.app.Fragment
import com.example.projectexcursions.R


import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.projectexcursions.MyApplication.Companion.getAuthToken
import com.example.projectexcursions.ui.auth.AuthActivity
import com.example.projectexcursions.ui.registration.RegActivity

class ProfileFragment:Fragment(R.layout.fragment_profile) {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        if (isUserAuthenticated()) {


        } else {


            startAuthActivity()
        }

        return view
    }

    private fun isUserAuthenticated(): Boolean {
        return false
    }

    private fun startAuthActivity() {
        val intent = Intent(requireActivity(), AuthActivity::class.java)
        startActivity(intent)
    }
}


