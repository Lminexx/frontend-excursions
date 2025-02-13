import com.bumptech.glide.Glide
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.projectexcursions.R
import com.example.projectexcursions.databinding.ItemFullscreenPhotoBinding

class FullScreenPhotoAdapter(private val photos: List<Uri>) :
    RecyclerView.Adapter<FullScreenPhotoAdapter.PhotoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding = ItemFullscreenPhotoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(photos[position])
    }

    override fun getItemCount(): Int = photos.size

    inner class PhotoViewHolder(private val binding: ItemFullscreenPhotoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(uri: Uri) {
            Glide.with(binding.root.context)
                .load(uri)
                .transition(DrawableTransitionOptions.withCrossFade())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.color.lighter_blue)
                .into(binding.imageViewFull)
        }
    }
}
