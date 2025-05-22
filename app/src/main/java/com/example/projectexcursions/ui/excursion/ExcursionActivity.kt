package com.example.projectexcursions.ui.excursion

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.projectexcursions.utilies.ApproveExcursionException
import com.example.projectexcursions.R
import com.example.projectexcursions.adapter.PhotoAdapter
import com.example.projectexcursions.adapter.PlacesAdapter
import com.example.projectexcursions.databinding.ActivityExcursionBinding
import com.example.projectexcursions.ui.create_excursion.CreateExcursionActivity
import com.example.projectexcursions.models.PlaceItem
import com.example.projectexcursions.repositories.tokenrepo.TokenRepository
import com.example.projectexcursions.utilies.CustomMapView
import com.google.android.material.chip.Chip
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.tbuonomo.viewpagerdotsindicator.SpringDotsIndicator
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.runtime.image.ImageProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class ExcursionActivity : AppCompatActivity() {

    @Inject
    lateinit var tokenRepo: TokenRepository
    private lateinit var adapter: PhotoAdapter
    private lateinit var placesAdapter: PlacesAdapter
    private lateinit var binding: ActivityExcursionBinding
    private lateinit var map: Map
    private lateinit var viewPager: ViewPager2
    private lateinit var mapView: CustomMapView
    private lateinit var indicator: SpringDotsIndicator
    private lateinit var pinsLayer: MapObjectCollection
    private lateinit var routeLayer: MapObjectCollection
    private var isAuth = false
    private var isMine = false
    private var isModerating = false
    private var excursionId: Long = 0L
    private var isDetailedInfoVisible = false
    private val viewModel: ExcursionViewModel by viewModels()
    private val placemarksMap = mutableMapOf<String, PlacemarkMapObject>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExcursionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mapView = binding.mapview
        mapView.parentScrollView = binding.root
        map = mapView.mapWindow.map
        val mapRoot = map.mapObjects
        pinsLayer = mapRoot.addCollection()
        routeLayer = mapRoot.addCollection()

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
        excursionId = intent.getLongExtra(EXTRA_EXCURSION_ID, -1)

        isAuth = tokenRepo.getCachedToken() != null
        isModerating = isAuth && intent.getBooleanExtra(EXTRA_IS_MODERATING, false)

        adapter = PhotoAdapter(this, listOf())
        viewPager = binding.viewPagerImages
        viewPager.adapter = adapter

        indicator = binding.dotsIndicator
        indicator.setViewPager2(viewPager)

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
            onApproveClick = { placeId, newName ->
                Log.d("Надо доработать UI)", "")
            },
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
            if (wannaComeback) {
                viewModel.cameBack()
                finish()
            }
        }

        viewModel.excursion.observe(this) { excursion ->
            if (excursion != null) {
                hideShimmer()
                binding.excursionTitle.text = excursion.title
                binding.excursionAuthor.text = excursion.user.username
                binding.excursionDescription.text = excursion.description
                binding.excursionRating.text = excursion.rating.toString()
                binding.topicValue.text = translateTopic(excursion.topic)
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
                AlertDialog.Builder(this)
                    .setTitle("Экскурсия съедена")
                    .setMessage("Произошло проблема при загрузке")
                    .setPositiveButton("Попробовать снова") { dialog, _ ->
                        viewModel.loadExcursion(intent.getLongExtra(EXTRA_EXCURSION_ID, -1))
                        dialog.dismiss()
                    }
                    .setNegativeButton("Пока:(") { _, _ ->
                        finish()
                    }
                    .setCancelable(false)
                    .show()
            }
            viewModel.isMine()
        }

        viewModel.favorite.observe(this) { favorite ->
            val animRes = if (favorite) {
                R.drawable.anim_fav_fill
            } else {
                R.drawable.anim_favorite_unfill
            }

            val drawable = AppCompatResources.getDrawable(this, animRes)
            binding.favoriteButton.setImageDrawable(drawable)

            (drawable as? AnimatedVectorDrawable)?.start()
        }

        viewModel.photos.observe(this) { photos ->
            adapter.updatePhotos(photos)
        }

        viewModel.places.observe(this) { places ->
            try {
                lifecycleScope.launch {
                    for (place in places)
                        setLocation(place)
                    if (places.size > 1) viewModel.getRoute()
                }
                placesAdapter.updatePlaces(places)
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                Log.d("Exception", e.message.toString())
            }
        }

        viewModel.route.observe(this) { points ->
            try {
                if (!points.isNullOrEmpty()) {
                    Log.d("RouteData", points.size.toString())
                    drawRoute(points)
                } else {
                    routeLayer.clear()
                }
            } catch (e: NullPointerException) {
                FirebaseCrashlytics.getInstance().recordException(e)
            }
        }

        viewModel.approve.observe(this) { approved ->
            if (approved) finish()
        }

        viewModel.isMine.observe(this) { mine ->
            isMine = mine
        }

        viewModel.rating.observe(this) { rating ->
            binding.excursionRating.text = rating.toString()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initCallback() {
        binding.menuButton.setOnClickListener { view ->
            showMenu(view)
        }

        binding.btnSendBack.setOnClickListener {
            lifecycleScope.launch {
                viewModel.excursionPended(excursionId)
            }
        }

        binding.btnReject.setOnClickListener {
            lifecycleScope.launch {
                viewModel.excursionRejected(excursionId)
            }
        }

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
            lifecycleScope.launch {
                if (fromUser) {
                    viewModel.updateRating(rating)
                    binding.myExcursionRating.text = rating.toString()
                }
            }
        }

        binding.btnApprove.setOnClickListener {
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

        binding.detailedInfoHeader.setOnClickListener {
            isDetailedInfoVisible = !isDetailedInfoVisible
            binding.detailedInfoContainer.visibility =
                if (isDetailedInfoVisible) View.VISIBLE else View.GONE
            binding.detailedInfoArrow.animate()
                .rotation(if (isDetailedInfoVisible) 180f else 0f)
                .setDuration(200)
                .start()
        }
    }

    private fun setLocation(place: PlaceItem) {
        Log.d("setLocation", "")
        try {
            val point = Point(place.lat, place.lon)
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
            setPin(place)
        } catch (eNull: NullPointerException) {
            FirebaseCrashlytics.getInstance().recordException(eNull)
        }
    }

    private fun setPin(place: PlaceItem) {
        Log.d("setPin", "")
        val point = Point(place.lat, place.lon)

        if (placemarksMap.containsKey(place.id)) {
            Log.d("setPin", "Метка с ID ${place.id} уже существует")
            return
        }

        val imageProvider = ImageProvider.fromResource(this, R.drawable.ic_location_pin)

        val placemark = pinsLayer.addPlacemark().apply {
            val iconStyle = IconStyle().apply { scale = 0.4f }
            geometry = point
            setIcon(imageProvider, iconStyle)
        }
        Log.d("setPin", "Добавлен пин с ID: ${place.id}")

        placemarksMap[place.id] = placemark
    }

    private fun addNewChip(tags: List<String>) {
        binding.tagsChipsView.removeAllViews()
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

    private fun showMenu(view: View) {
        PopupMenu(this, view).apply {
            menuInflater.inflate(R.menu.excursion_menu, menu)
            try {
                val fieldMPopup = javaClass.getDeclaredField("mPopup")
                fieldMPopup.isAccessible = true
                val mPop = fieldMPopup.get(this)
                mPop.javaClass
                    .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                    .invoke(mPop, true)
            } catch (e: Exception) {
                Log.e("MenuError", e.message.toString())
                FirebaseCrashlytics.getInstance().recordException(e)
            }
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.deleteButton -> {
                        android.app.AlertDialog.Builder(this@ExcursionActivity)
                            .setTitle("Вы хотите удалить экскурсию?")
                            .setMessage("Она будет удалена безвозвратно")
                            .setPositiveButton("Да") { dialog, _ ->
                                lifecycleScope.launch {
                                    viewModel.deleteExcursion()
                                    setResult(RESULT_OK)
                                }
                                dialog.dismiss()
                            }
                            .setNegativeButton("Нет") { dialog, _ -> dialog.dismiss() }
                            .show()
                        true
                    }
                    R.id.editButton -> {
                        val intent = Intent(this@ExcursionActivity, CreateExcursionActivity::class.java).apply {
                            putExtra("id", viewModel.excursion.value?.id ?: -1)
                            putExtra("title", binding.excursionTitle.toString())
                            putExtra("description", binding.excursionDescription.text.toString())
                            putExtra("topic", binding.topicValue.text.toString())
                            putExtra("city", binding.cityValue.text.toString())
                            var count = 0
                            viewModel.excursion.value?.tags?.forEach { tag ->
                                count++
                                putExtra("tag$count", tag)
                            }
                            putExtra("tag_count", count)
                            count = 0
                            viewModel.photos.value?.forEach { photo ->
                                count++
                                putExtra("photo$count", photo.toString())
                            }
                            putExtra("photo_count", count)
                        }
                        editLauncher.launch(intent)
                        true
                    }
                    else -> false
                }
            }
            show()
        }
    }

    private fun translateTopic(topic: String): String {
        return when (topic) {
            "UNDEFINED" -> "Другая"
            "WALKING" -> "Пешая"
            "TRIP" -> "Путешествие"
            "ACADEMIC" -> "Позновательная"
            else -> "UNDEFINED"
        }
    }

    private fun showShimmer() {
        binding.shimmerLayout.visibility = View.VISIBLE
        binding.shimmerLayout.startShimmer()
        binding.excursionTitle.visibility = View.GONE
        binding.authorContainer.visibility = View.GONE
        binding.excursionDescription.visibility = View.GONE
        binding.favoriteButton.visibility = View.GONE
        viewPager.visibility = View.GONE
        indicator.visibility = View.GONE
        binding.mapview.visibility = View.GONE
        binding.places.visibility = View.GONE
        binding.moderatingBtnsContainer.visibility = View.GONE
        binding.ratingContainer.visibility = View.GONE
        binding.detailedInfoHeader.visibility = View.GONE
        binding.menuButton.visibility = View.GONE
    }

    private fun hideShimmer() {
        binding.shimmerLayout.stopShimmer()
        binding.shimmerLayout.visibility = View.GONE
        binding.excursionTitle.visibility = View.VISIBLE
        binding.authorContainer.visibility = View.VISIBLE
        binding.excursionDescription.visibility = View.VISIBLE
        binding.favoriteButton.visibility = View.VISIBLE
        binding.mapview.visibility = View.VISIBLE
        binding.places.visibility = View.VISIBLE
        binding.detailedInfoHeader.visibility = View.VISIBLE
        binding.ratingContainer.visibility = View.VISIBLE
        viewPager.visibility = View.VISIBLE
        indicator.visibility = View.VISIBLE

        val showMenuBtn = isMine
        Log.d("DBG", "menuButton should be visible? $showMenuBtn")
        binding.menuButton.visibility =
            if (showMenuBtn) View.VISIBLE else View.GONE

        binding.moderatingBtnsContainer.visibility =
            if (isModerating && !isMine) View.VISIBLE else View.GONE

        binding.favoriteButton.visibility = if (isAuth) View.VISIBLE else View.GONE

        binding.ratingBar.isEnabled = isAuth && !isMine && !isModerating
    }

    private fun drawRoute(points: List<Point>) {
        routeLayer.clear()

        if (points.isNotEmpty()) {
            val polyline = Polyline(points)
            routeLayer.addPolyline(polyline)
        }
    }

    private val editLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            recreate()
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