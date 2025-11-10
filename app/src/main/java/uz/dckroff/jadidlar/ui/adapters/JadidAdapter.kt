package uz.dckroff.jadidlar.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import uz.dckroff.jadidlar.R
import uz.dckroff.jadidlar.data.models.Jadid
import uz.dckroff.jadidlar.databinding.ItemJadidBinding

class JadidAdapter(
    private val onItemClick: (Jadid) -> Unit
) : ListAdapter<Jadid, JadidAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemJadidBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemJadidBinding,
        private val onItemClick: (Jadid) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(jadid: Jadid) {
            binding.textJadidName.text = jadid.name
            
            Glide.with(binding.root.context)
                .load(jadid.imageUrl)
                .placeholder(R.drawable.sample_jadid)
                .error(R.drawable.sample_jadid)
                .centerCrop()
                .into(binding.imageJadid)
            
            binding.root.setOnClickListener {
                onItemClick(jadid)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Jadid>() {
        override fun areItemsTheSame(oldItem: Jadid, newItem: Jadid): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Jadid, newItem: Jadid): Boolean {
            return oldItem == newItem
        }
    }
}
