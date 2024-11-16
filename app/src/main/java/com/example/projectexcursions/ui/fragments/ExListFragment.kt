import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectexcursions.R
import com.example.projectexcursions.adapters.ExcursionAdapter
import com.example.projectexcursions.models.Excursion

class ExListFragment : Fragment(R.layout.fragment_excursions_list) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val excursions = listOf(
            Excursion("Excursion 1", "Description for excursion 1"),
            Excursion("Excursion 2", "Description for excursion 2"),
            Excursion("Excursion 3", "Description for excursion 3")
        )

        val adapter = ExcursionAdapter(excursions) { excursion ->
            println("Clicked on: ${excursion.title}")
        }

        recyclerView.adapter = adapter
    }
}
