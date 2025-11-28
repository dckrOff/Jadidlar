package uz.dckroff.jadidlar.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import uz.dckroff.jadidlar.data.models.Test
import uz.dckroff.jadidlar.databinding.ItemQuizBinding

class QuizAdapter(
    private val onStartClick: (Test) -> Unit
) : ListAdapter<Test, QuizAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemQuizBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onStartClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemQuizBinding,
        private val onStartClick: (Test) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(test: Test) {
            binding.tvQuizName.text = test.title
            binding.tvQuizDescription.text = test.description
            binding.tvQuizCount.text = "${test.questionCount} savol"
            binding.tvTimeLimit.text = "${test.timeLimit} daqiqa"
            
            binding.btnStart.setOnClickListener {
                onStartClick(test)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Test>() {
        override fun areItemsTheSame(oldItem: Test, newItem: Test): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Test, newItem: Test): Boolean {
            return oldItem == newItem
        }
    }
}
