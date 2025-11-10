package uz.dckroff.jadidlar.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import uz.dckroff.jadidlar.R
import uz.dckroff.jadidlar.data.models.Book
import uz.dckroff.jadidlar.databinding.ItemBookBinding

class BookAdapter(
    private val onItemClick: (Book) -> Unit
) : ListAdapter<Book, BookAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBookBinding.inflate(
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
        private val binding: ItemBookBinding,
        private val onItemClick: (Book) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(book: Book) {
            binding.textBookTitle.text = book.title
            binding.textBookDescription.text = book.description
            
            Glide.with(binding.root.context)
                .load(book.coverImageUrl)
                .placeholder(R.drawable.sample_book)
                .error(R.drawable.sample_book)
                .centerCrop()
                .into(binding.imageBook)
            
            binding.root.setOnClickListener {
                onItemClick(book)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Book>() {
        override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem == newItem
        }
    }
}
