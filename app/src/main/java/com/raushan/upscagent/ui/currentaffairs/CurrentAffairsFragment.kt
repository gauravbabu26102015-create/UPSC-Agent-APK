package com.raushan.upscagent.ui.currentaffairs

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.raushan.upscagent.R
import com.raushan.upscagent.data.model.CurrentAffair
import com.raushan.upscagent.data.repository.AppRepository
import com.raushan.upscagent.utils.CurrentAffairsFetcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CurrentAffairsFragment : Fragment() {

    private lateinit var repository: AppRepository
    private lateinit var adapter: CurrentAffairsAdapter
    private var currentCategory = "All"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_current_affairs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = AppRepository(requireContext())

        adapter = CurrentAffairsAdapter(
            onItemClick = { affair -> openAffair(affair) },
            onBookmark = { affair -> toggleBookmark(affair) }
        )

        view.findViewById<RecyclerView>(R.id.rv_current_affairs).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@CurrentAffairsFragment.adapter
        }

        // Category chips
        val chipGroup = view.findViewById<ChipGroup>(R.id.chip_group_categories)
        val categories = listOf("All", "National", "International", "Economy", "Science & Tech", "Environment", "Polity", "Bookmarked")
        categories.forEach { category ->
            val chip = Chip(requireContext()).apply {
                text = category
                isCheckable = true
                isChecked = category == "All"
                setOnClickListener { filterByCategory(category) }
            }
            chipGroup.addView(chip)
        }

        // Swipe to refresh
        view.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_affairs).setOnRefreshListener {
            refreshAffairs()
        }

        // Load data
        observeAffairs()
    }

    private fun observeAffairs() {
        viewLifecycleOwner.lifecycleScope.launch {
            repository.getAllAffairs().collectLatest { affairs ->
                adapter.submitList(affairs)
                view?.findViewById<TextView>(R.id.tv_empty_affairs)?.visibility =
                    if (affairs.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun filterByCategory(category: String) {
        currentCategory = category
        viewLifecycleOwner.lifecycleScope.launch {
            val flow = when (category) {
                "All" -> repository.getAllAffairs()
                "Bookmarked" -> repository.getBookmarkedAffairs()
                else -> repository.getAffairsByCategory(category)
            }
            flow.collectLatest { affairs ->
                adapter.submitList(affairs)
            }
        }
    }

    private fun refreshAffairs() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val affairs = withContext(Dispatchers.IO) {
                    CurrentAffairsFetcher.fetchFromMultipleSources()
                }
                if (affairs.isNotEmpty()) {
                    repository.insertAffairs(affairs)
                    Toast.makeText(requireContext(), "✅ ${affairs.size} articles updated!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "No new articles found", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to fetch: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                view?.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_affairs)?.isRefreshing = false
            }
        }
    }

    private fun openAffair(affair: CurrentAffair) {
        viewLifecycleOwner.lifecycleScope.launch {
            repository.markAsRead(affair.id)
        }
        // Open in browser
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(affair.sourceUrl)))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Cannot open link", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleBookmark(affair: CurrentAffair) {
        viewLifecycleOwner.lifecycleScope.launch {
            repository.toggleBookmark(affair.id, !affair.isBookmarked)
        }
    }
}

// ==================== CURRENT AFFAIRS ADAPTER ====================

class CurrentAffairsAdapter(
    private val onItemClick: (CurrentAffair) -> Unit,
    private val onBookmark: (CurrentAffair) -> Unit
) : RecyclerView.Adapter<CurrentAffairsAdapter.ViewHolder>() {

    private var affairs = listOf<CurrentAffair>()

    fun submitList(list: List<CurrentAffair>) {
        affairs = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_current_affair, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(affairs[position])
    override fun getItemCount() = affairs.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(affair: CurrentAffair) {
            itemView.findViewById<TextView>(R.id.tv_affair_title).text = affair.title
            itemView.findViewById<TextView>(R.id.tv_affair_summary).text = affair.summary
            itemView.findViewById<TextView>(R.id.tv_affair_source).text = "${affair.source} • ${affair.publishedDate}"
            itemView.findViewById<TextView>(R.id.tv_affair_category).text = affair.category

            val btnBookmark = itemView.findViewById<ImageButton>(R.id.btn_bookmark)
            btnBookmark.setImageResource(
                if (affair.isBookmarked) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark_outline
            )
            btnBookmark.setOnClickListener { onBookmark(affair) }

            // Dim if read
            itemView.alpha = if (affair.isRead) 0.7f else 1.0f
            itemView.setOnClickListener { onItemClick(affair) }
        }
    }
}
