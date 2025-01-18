import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.projectexcursions.R
import com.example.projectexcursions.adapter.FavAdapter
import com.example.projectexcursions.databinding.FragmentFavBinding
import com.example.projectexcursions.ui.favorite_excursions.FavViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

//@AndroidEntryPoint
class FavFragment : Fragment(R.layout.fragment_fav) {

//    private lateinit var binding: FragmentFavBinding
//    @Inject
//    lateinit var adapter: FavAdapter
//    private val viewModel: FavViewModel by viewModels()
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        binding = FragmentFavBinding.inflate(inflater, container, false)
//        return binding.root
//    }
}