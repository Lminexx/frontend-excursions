import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectexcursions.R
import com.example.projectexcursions.adapters.ExcursionAdapter
import com.example.projectexcursions.models.Excursion
import com.example.projectexcursions.models.ExcursionResponse
import com.example.projectexcursions.net.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ExListFragment : Fragment(R.layout.fragment_excursions_list) {

    private lateinit var adapter: ExcursionAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        var excursions = listOf(
            Excursion(1, "GDF", "SDFGSD"),
            Excursion(2, "GDssF", "SDdfdsFGSD"),
            Excursion(3, "GDF", "SDFGSD")
        )

        adapter = ExcursionAdapter(excursions) {excursion ->
            Toast.makeText(requireContext(), excursion.title + ": \n"
                    + excursion.description, Toast.LENGTH_SHORT).show()
        }

        /*val adapter = ExcursionAdapter(emptyList()) { excursion ->
            Toast.makeText(requireContext(), excursion.title + ": \n" + excursion.description, Toast.LENGTH_SHORT).show()
        }*/

        recyclerView.adapter = adapter

        /*fetchExcursions()*/
    }
    private fun fetchExcursions() {
        val call = ApiClient.instance.getExcursions(page = 0, size = 10)
        call.enqueue(object : Callback<ExcursionResponse> {
            override fun onResponse(
                call: Call<ExcursionResponse>,
                response: Response<ExcursionResponse>
            ) {
                val excursions = response.body()?.content
                if (excursions != null) {
                    adapter.updateData(excursions)
                }
            }

            override fun onFailure(p0: Call<ExcursionResponse>, p1: Throwable) {
                Toast.makeText(requireContext(), "TODO: \"JOPA\"", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
