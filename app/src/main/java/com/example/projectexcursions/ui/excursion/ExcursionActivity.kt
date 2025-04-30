package com.example.projectexcursions.ui.excursion

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.projectexcursions.ApproveExcursionException
import com.example.projectexcursions.R
import com.example.projectexcursions.adapter.PhotoAdapter
import com.example.projectexcursions.adapter.PlacesAdapter
import com.example.projectexcursions.databinding.ActivityExcursionBinding
import com.example.projectexcursions.ui.utilies.CustomMapView
import com.google.android.material.chip.Chip
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.PlacemarkMapObject
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
    private lateinit var mapView: CustomMapView
    private lateinit var placemark: PlacemarkMapObject
    private var isDetailedInfoVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExcursionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mapView = binding.mapview
        mapView.parentScrollView = binding.root
        map = mapView.mapWindow.map

        binding.detailedInfoHeader.setOnClickListener {
            isDetailedInfoVisible = !isDetailedInfoVisible
            binding.detailedInfoContainer.visibility =
                if (isDetailedInfoVisible) View.VISIBLE else View.GONE
            binding.detailedInfoArrow.animate()
                .rotation(if (isDetailedInfoVisible) 180f else 0f)
                .setDuration(200)
                .start()
        }

        initCallback()
        initData()
        subscribe()

    }

    override fun onStart() {
        super.onStart()
        Log.d("onStart", "")
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
        Log.d("onStart", "")
        MapKitFactory.getInstance().onStop()
    }

    @SuppressLint("SetTextI18n")
    private fun initData() {
        val excursionId = intent.getLongExtra(EXTRA_EXCURSION_ID, -1)

        val isModerating = intent.getBooleanExtra(EXTRA_IS_MODERATING, false)
        if (isModerating)
            binding.favoriteButton.visibility = View.GONE
        else {
            binding.commentButton.visibility = View.GONE
            binding.approveButton.visibility = View.GONE
        }

        adapter = PhotoAdapter(this, listOf())
        binding.recyclerViewImages.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
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

        binding.places.layoutManager = LinearLayoutManager(this)
        binding.places.adapter = placesAdapter

        viewModel.loadPlaces(excursionId)
        binding.excursionDescription.movementMethod = ScrollingMovementMethod()
    }

    @SuppressLint("SetTextI18n")
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
                binding.excursionRating.text = excursion.rating.toString()
                binding.topicValue.text = excursion.topic
                binding.cityValue.text = excursion.cityName
                addNewChip(excursion.tags)
                if (excursion.personalRating == null) {
                    binding.myRatingText.alpha = 0.0F
                } else {
                    binding.myRatingText.alpha = 1F
                    binding.myExcursionRating.text = excursion.personalRating.toString()
                    binding.ratingBar.rating = excursion.personalRating
                }
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
                Toast.makeText(this, this.getString(R.string.excursion_eaten), Toast.LENGTH_SHORT)
                    .show()
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
            try {
                for (place in places) {
                    Log.d("PLaceItem", "${place.name}, ${place.id}")
                }
                lifecycleScope.launch {
                    for (place in places) {
                        val point = Point(place.lat, place.lon)
                        viewModel.setPoint(point)
                        delay(500)
                    }
                }
                placesAdapter.updatePlaces(places)
                Log.d("PlaceItemsObserve", "true " + places[places.size - 1].name)
            } catch (indexOutOfBound: IndexOutOfBoundsException) {
                FirebaseCrashlytics.getInstance().recordException(indexOutOfBound)
                Log.d("IndexOutOfBound", "хихи поймали дурачка)")
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                Log.d("Exception", e.message.toString())
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

        viewModel.disapproving.observe(this) { disapproving ->
            if (disapproving) {
                val id = intent.getLongExtra(EXTRA_EXCURSION_ID, -1)
                if (id != -1L) {
                    val disapproveExcursionFrag = DisapproveExcursionFragment.newInstance(id)
                    disapproveExcursionFrag.show(
                        supportFragmentManager,
                        "DisapproveExcursionFragment"
                    )
                }
            } else finish()
            Log.d("disapproving", disapproving.toString())
        }

        viewModel.approve.observe(this) { approved ->
            if (approved) finish()
        }
    }

    @SuppressLint("SetTextI18n")
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
                    Toast.makeText(
                        this@ExcursionActivity,
                        this@ExcursionActivity.getString(R.string.error_favorite),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        binding.ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            if (fromUser) {
                lifecycleScope.launch {
                    val newAverageRating = viewModel.updateRating(rating)
                    Log.d("ratingValue", newAverageRating.toString())
                    binding.excursionRating.text = newAverageRating.toString()
                    binding.myRatingText.alpha = 1F
                    binding.myExcursionRating.text = rating.toString()
                }
            }
        }

        binding.commentButton.setOnClickListener { viewModel.disapprove() }

        binding.approveButton.setOnClickListener {
            it.isClickable = false
            Handler(Looper.getMainLooper()).postDelayed({
                it.isClickable = true
            }, 1000)
            lifecycleScope.launch {
                try {
                    viewModel.excursionApproved()
                    finish()
                } catch (e: ApproveExcursionException) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                    Toast.makeText(this@ExcursionActivity, e.message, Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                    Log.e("ApprovedError", e.message.toString())
                }
            }
        }
    }

    private fun setLocation(point: Point) {
        Log.d("setLocation", "")
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
            FirebaseCrashlytics.getInstance().recordException(eNull)
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

    private fun addNewChip(tags: List<String>) {
        for (tag in tags) {
            try {
                val inflater = LayoutInflater.from(this)
                val newChip =
                    inflater.inflate(
                        R.layout.layout_chip_entry,
                        binding.tagsChipsView,
                        false
                    ) as Chip
                newChip.text = tag
                newChip.setCloseIconVisible(false)
                newChip.isClickable = false
                newChip.setChipBackgroundColorResource(R.color.lighter_blue)
                newChip.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.tagsChipsView.addView(newChip)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error: " + e.message, Toast.LENGTH_SHORT).show()
            }
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
        binding.mapview.visibility = View.GONE
        binding.places.visibility = View.GONE
        binding.approveButton.visibility = View.GONE
        binding.commentButton.visibility = View.GONE
        binding.mainRatingConteiner.visibility = View.GONE
        binding.myRatingContainer.visibility = View.GONE
        binding.ratingDescription.visibility = View.GONE
    }

    private fun hideShimmer() {
        binding.shimmerLayout.stopShimmer()
        binding.shimmerLayout.visibility = View.GONE
        binding.excursionTitle.visibility = View.VISIBLE
        binding.authorContainer.visibility = View.VISIBLE
        binding.excursionDescription.visibility = View.VISIBLE
        binding.favoriteButton.visibility = View.VISIBLE
        binding.recyclerViewImages.visibility = View.VISIBLE
        binding.mapview.visibility = View.VISIBLE
        binding.places.visibility = View.VISIBLE
        binding.ratingDescription.visibility = View.VISIBLE
        val isModerating = intent.getBooleanExtra(EXTRA_IS_MODERATING, false)
        if (isModerating) {
            binding.favoriteButton.visibility = View.GONE
            binding.mainRatingConteiner.visibility = View.GONE
            binding.myRatingContainer.visibility = View.GONE
            binding.ratingDescription.visibility = View.GONE
            binding.commentButton.visibility = View.VISIBLE
            binding.approveButton.visibility = View.VISIBLE
        } else {
            binding.commentButton.visibility = View.GONE
            binding.approveButton.visibility = View.GONE
            binding.favoriteButton.visibility = View.VISIBLE
            binding.mainRatingConteiner.visibility = View.VISIBLE
            binding.myRatingContainer.visibility = View.VISIBLE
            binding.ratingDescription.visibility = View.VISIBLE
        }
    }

    private fun drawRoute(points: List<Point>) {
        if (points.isNotEmpty()) {
            routePolyline = Polyline(points)
            map.mapObjects.addPolyline(routePolyline!!)
        }
    }

    companion object {
        private const val EXTRA_EXCURSION_ID = "EXTRA_EXCURSION_ID"
        private const val EXTRA_IS_MODERATING = "EXTRA_IS_MODERATING"
        internal fun Context.createExcursionActivityIntent(
            excursionId: Long,
            isModerating: Boolean
        ): Intent =
            Intent(this, ExcursionActivity::class.java).apply {
                putExtra(EXTRA_EXCURSION_ID, excursionId)
                putExtra(EXTRA_IS_MODERATING, isModerating)
            }
    }
}