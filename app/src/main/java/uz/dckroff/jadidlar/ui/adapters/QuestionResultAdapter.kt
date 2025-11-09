package uz.dckroff.jadidlar.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import uz.dckroff.jadidlar.R
import uz.dckroff.jadidlar.databinding.ItemQuestionResultBinding

data class QuestionResult(
    val questionNumber: Int,
    val questionText: String,
    val correctAnswer: String,
    val userAnswer: String,
    val isCorrect: Boolean,
    val explanation: String = ""
)

class QuestionResultAdapter : ListAdapter<QuestionResult, QuestionResultAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemQuestionResultBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemQuestionResultBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(result: QuestionResult) {
            binding.questionNumberTextView.text = "${result.questionNumber}-savol"
            binding.questionTextView.text = result.questionText
            binding.correctAnswerTextView.text = result.correctAnswer
            binding.yourAnswerTextView.text = result.userAnswer
            
            if (result.explanation.isNotEmpty()) {
                binding.explanationLabelTextView.visibility = View.VISIBLE
                binding.explanationTextView.visibility = View.VISIBLE
                binding.explanationTextView.text = result.explanation
            } else {
                binding.explanationLabelTextView.visibility = View.GONE
                binding.explanationTextView.visibility = View.GONE
            }
            
            val context = binding.root.context
            if (result.isCorrect) {
                binding.resultIconImageView.setImageResource(R.drawable.ic_correct)
                binding.resultIconImageView.setColorFilter(
                    ContextCompat.getColor(context, R.color.text_color_easy)
                )
                binding.yourAnswerTextView.setTextColor(
                    ContextCompat.getColor(context, R.color.text_color_easy)
                )
            } else {
                binding.resultIconImageView.setImageResource(R.drawable.ic_error)
                binding.resultIconImageView.setColorFilter(
                    ContextCompat.getColor(context, R.color.text_color_hard)
                )
                binding.yourAnswerTextView.setTextColor(
                    ContextCompat.getColor(context, R.color.text_color_hard)
                )
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<QuestionResult>() {
        override fun areItemsTheSame(oldItem: QuestionResult, newItem: QuestionResult): Boolean {
            return oldItem.questionNumber == newItem.questionNumber
        }

        override fun areContentsTheSame(oldItem: QuestionResult, newItem: QuestionResult): Boolean {
            return oldItem == newItem
        }
    }
}
