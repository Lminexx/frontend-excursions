import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectexcursions.R
import com.example.projectexcursions.adapters.ExcursionAdapter
import com.example.projectexcursions.models.Excursion
import com.example.projectexcursions.ui.main.MainActivity

class ExListFragment : Fragment(R.layout.fragment_excursions_list) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val excursions = listOf(
            Excursion("Batman in Chicago", "Places for detonators"),
            Excursion("Chill", "Для души..."),
            Excursion("Я не придумал", "Я не придумал")
        )

        val adapter = ExcursionAdapter(excursions) { excursion ->
            Toast.makeText(requireContext(), "Открывается " + excursion.title + ": \n" + excursion.description, Toast.LENGTH_SHORT).show()
        }

        recyclerView.adapter = adapter
    }
}
