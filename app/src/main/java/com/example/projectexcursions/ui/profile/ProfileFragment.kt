package com.example.projectexcursions.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.projectexcursions.R
import com.example.projectexcursions.utilies.UsernameNotFoundException
import com.example.projectexcursions.databinding.FragmentProfileBinding
import com.example.projectexcursions.ui.create_excursion.CreateExcursionActivity
import com.example.projectexcursions.ui.created_excursions_list.CreatedExListActivity
import com.example.projectexcursions.ui.moderating_excursions_list.ModeratingExListActivity
import com.example.projectexcursions.ui.main.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    @Inject
    lateinit var firebaseAuth: FirebaseAuth
    @Inject
    lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var binding: FragmentProfileBinding
    private val viewModel: ProfileViewModel by viewModels()
    private var isModerator = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isModerator = arguments?.getBoolean(IS_MODERATOR) ?: false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initData()
        initCallback()
        subscribe()
    }

    private fun initData() {
        if (isModerator)
            binding.moderatingExcursions.visibility = View.VISIBLE
        else
            binding.moderatingExcursions.visibility = View.GONE

        val decodedToken = viewModel.getDecodeToken()
        val url = decodedToken?.get("url")?.asString()
        if (url != null) {
            Glide.with(requireContext())
                .load(url)
                .placeholder(R.drawable.ic_app_v3)
                .error(R.drawable.ic_app_v3)
                .into(binding.userPhoto)
        } else {
            binding.userPhoto.setBackgroundResource(R.drawable.ic_app_v3)
        }
    }

    private fun initCallback() {
        binding.buttCreateExcursion.setOnClickListener { viewModel.clickCreateExcursion() }
        binding.buttLogOut.setOnClickListener { viewModel.clickComeBack() }
        binding.createdExcursionsList.setOnClickListener { viewModel.createdExcsList() }
        binding.moderatingExcursions.setOnClickListener { viewModel.moderateExcursions() }
    }

    private fun subscribe() {
        viewModel.wantCreate.observe(viewLifecycleOwner) { wannaCreate ->
            if (wannaCreate) {
                startActivity(Intent(requireContext(), CreateExcursionActivity::class.java))
                viewModel.isCreating()
            }
        }

        viewModel.username.observe(viewLifecycleOwner) { username ->
            binding.userNickname.text =
                username ?: throw UsernameNotFoundException("Usera net v tokene")
        }

        viewModel.logout.observe(viewLifecycleOwner) { wannaLogOut ->
            if (wannaLogOut) {
                Log.d("WantLogOut", "true")
                if (firebaseAuth.currentUser != null) {
                    firebaseAuth.signOut()
                    googleSignInClient.signOut()
                    viewModel.logout()
                    (requireActivity() as? MainActivity)?.updateBottomNavSelectionToList()
                } else {
                    viewModel.logout()
                    (requireActivity() as? MainActivity)?.updateBottomNavSelectionToList()
                }
            }
        }

        viewModel.goToCreatedExcs.observe(viewLifecycleOwner) { wannaCreated ->
            if (wannaCreated) {
                startActivity(Intent(requireContext(), CreatedExListActivity::class.java))
            }
        }

        viewModel.moderateExcursions.observe(viewLifecycleOwner) { wannaModerate ->
            if (wannaModerate) {
                startActivity(Intent(requireContext(), ModeratingExListActivity::class.java))
            }
        }
    }

    companion object {
        private const val IS_MODERATOR = "is_moderator"

        fun newInstance(isModerator: Boolean): ProfileFragment {
            val fragment = ProfileFragment()
            fragment.arguments = Bundle().apply {
                putBoolean(IS_MODERATOR, isModerator)
            }
            return fragment
        }
    }
}