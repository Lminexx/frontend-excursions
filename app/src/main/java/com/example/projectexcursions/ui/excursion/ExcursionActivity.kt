package com.example.projectexcursions.ui.excursion

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.projectexcursions.R
import com.example.projectexcursions.adapter.PhotoAdapter
import com.example.projectexcursions.adapter.PlacesAdapter
import com.example.projectexcursions.databinding.ActivityExcursionBinding
import com.example.projectexcursions.ui.main.MainActivity
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ExcursionActivity : AppCompatActivity() {

    private val viewModel: ExcursionViewModel by viewModels()
    private lateinit var binding: ActivityExcursionBinding
    private lateinit var adapter: PhotoAdapter
    private lateinit var placesAdapter: PlacesAdapter
    private var routePolyline: Polyline? = null
    private lateinit var map: Map
    private lateinit var mapView: MapView
    private lateinit var placemark: PlacemarkMapObject

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExcursionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mapView = binding.mapview
        map = mapView.mapWindow.map

        initCallback()
        initData()
        subscribe()
    }

    override fun onStart() {
        super.onStart()
        Log.d("onStart","")
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
        Log.d("onStart","")
        MapKitFactory.getInstance().onStop()
    }

    private fun initData() {
        val excursionId = intent.getLongExtra(EXTRA_EXCURSION_ID, -1)
        if (excursionId == -1L) {
            Toast.makeText(this, this.getString(R.string.invalid_excursion), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        adapter = PhotoAdapter(this, listOf())
        binding.recyclerViewImages.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewImages.adapter = adapter

        showShimmer()

        viewModel.loadExcursion(excursionId)
        viewModel.loadPhotos(excursionId)

        placesAdapter = PlacesAdapter(
            context = this,
            onItemClick = { placeName ->
                Log.d("PlaceName", "Пока ниче не сделано")
            },
            onDeleteClick = { placeId ->
                Log.d("DeletePlace", "нельзя!")
            },
            isCreating = false,
            places = emptyList()
        )

        binding.places.adapter = placesAdapter

        viewModel.loadPlaces(excursionId)
        binding.excursionDescription.movementMethod = ScrollingMovementMethod()
    }

    private fun subscribe() {
        viewModel.wantComeBack.observe(this) { wannaComeback ->
            if (wannaComeback)
                viewModel.cameBack()
        }

        viewModel.excursion.observe(this) { excursion ->
            if (excursion != null) {
                hideShimmer()
                binding.excursionTitle.text = excursion.title
                binding.excursionAuthor.text = excursion.user.username
                binding.excursionDescription.text = excursion.description
                val url = excursion.user.url
                Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.ic_app_v3)
                    .error(R.drawable.ic_app_v3)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            e?.printStackTrace()
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }
                    })
                    .into(binding.userAvatar)
                if (viewModel.excursion.value!!.favorite)
                    viewModel.fav()
                else
                    viewModel.notFav()
            } else {
                Toast.makeText(this, this.getString(R.string.excursion_eaten), Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.favorite.observe(this) { favorite ->
            if (favorite) {
                binding.favoriteButton.setBackgroundResource(R.drawable.ic_ex_fav_fill)
            } else {
                binding.favoriteButton.setBackgroundResource(R.drawable.ic_ex_fav_hollow)
            }
        }

        viewModel.photos.observe(this) { photos ->
            adapter.updatePhotos(photos)
        }

        viewModel.places.observe(this) { places ->
            placesAdapter.updatePlaces(places)
            lifecycleScope.launch {
                for (place in places) {
                    val point = Point(place.lat, place.lon)
                    viewModel.setPoint(point)
                    delay(75)
                }
            }
        }

        viewModel.routeLiveData.observe(this) { points ->
            if (!points.isNullOrEmpty()) {
                Log.d("RouteData", points.size.toString())
                drawRoute(points)
            }
        }

        viewModel.curPoint.observe(this) { point ->
            if (point != null) {
                Log.d("nextPoint:", "observing")
                setLocation(point)
                if (viewModel.prevPoint.value != null) {
                    lifecycleScope.launch {
                        viewModel.getRoute()
                    }
                }
            }
        }
    }

    private fun initCallback() {
        binding.favoriteButton.setOnClickListener {
            it.isClickable = false
            Handler(Looper.getMainLooper()).postDelayed({
                it.isClickable = true
            }, 1000)
            lifecycleScope.launch {
                if (viewModel.checkAuthStatus()) {
                    viewModel.clickFavorite()
                } else {
                    Toast.makeText(this@ExcursionActivity, this@ExcursionActivity.getString(R.string.error_favorite), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setLocation(point: Point) {
        Log.d("setLocation","")
        try {
            map.move(
                CameraPosition(
                    point,
                    15.0f,
                    150.0f,
                    30.0f
                ),
                Animation(Animation.Type.SMOOTH, 1f),
                null
            )
            setPin(point)
        } catch (eNull: NullPointerException) {
            Toast.makeText(this, "Индиана Джонс нашёл неприятный артефакт", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setPin(point: Point) {
        Log.d("setPin", "")

        val imageProvider = ImageProvider.fromResource(this, R.drawable.ic_location_pin)

        placemark = map.mapObjects.addPlacemark().apply {
            val iconStyle = IconStyle().apply { scale = 0.4f }
            geometry = point
            setIcon(imageProvider, iconStyle)
        }
    }

    private fun showShimmer() {
        binding.shimmerLayout.visibility = View.VISIBLE
        binding.shimmerLayout.startShimmer()
        binding.excursionTitle.visibility = View.GONE
        binding.authorContainer.visibility = View.GONE
        binding.excursionDescription.visibility = View.GONE
        binding.favoriteButton.visibility = View.GONE
        binding.recyclerViewImages.visibility = View.GONE
    }

    private fun hideShimmer() {
        binding.shimmerLayout.stopShimmer()
        binding.shimmerLayout.visibility = View.GONE
        binding.excursionTitle.visibility = View.VISIBLE
        binding.authorContainer.visibility = View.VISIBLE
        binding.excursionDescription.visibility = View.VISIBLE
        binding.favoriteButton.visibility = View.VISIBLE
        binding.recyclerViewImages.visibility = View.VISIBLE
    }

    private fun drawRoute(points: List<Point>) {
        if (points.isNotEmpty()) {
            routePolyline = Polyline(points)
            map.mapObjects.addPolyline(routePolyline!!)
        }
    }

    companion object {
        private const val EXTRA_EXCURSION_ID = "EXTRA_EXCURSION_ID"

        internal fun Context.createExcursionActivityIntent(excursionId: Long): Intent =
            Intent(this, ExcursionActivity::class.java)
                .putExtra(EXTRA_EXCURSION_ID, excursionId)
    }
}