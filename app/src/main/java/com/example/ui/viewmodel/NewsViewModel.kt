package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Article
import com.example.data.repository.BookmarkRepository
import com.example.data.repository.NewsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TimePeriodFilter(val label: String) {
    ALL("Latest"),
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    LAST_7_DAYS("Last 7 Days"),
    LAST_30_DAYS("Last 30 Days")
}

class NewsViewModel(
    private val newsRepo: NewsRepository,
    private val bookmarkRepo: BookmarkRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedTimePeriod = MutableStateFlow(TimePeriodFilter.ALL)
    val selectedTimePeriod: StateFlow<TimePeriodFilter> = _selectedTimePeriod.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val categories = listOf(
        "All",
        "National Affairs",
        "Economy",
        "Science and Technology",
        "Environment",
        "Government Schemes",
        "Defence",
        "Banking",
        "Sports",
        "Important Reports and Indexes"
    )

    val filteredArticles: StateFlow<List<Article>> = combine(
        newsRepo.articles,
        _selectedCategory,
        _selectedTimePeriod,
        _searchQuery
    ) { allArticles, category, timePeriod, query ->
        allArticles.filter { article ->
            val matchesCategory = category == "All" || article.category.equals(category, ignoreCase = true)
            val matchesQuery = query.isBlank() ||
                    article.title.contains(query, ignoreCase = true) ||
                    article.description.contains(query, ignoreCase = true) ||
                    article.content.contains(query, ignoreCase = true)

            val matchesTime = when (timePeriod) {
                TimePeriodFilter.ALL -> true
                TimePeriodFilter.TODAY -> article.publishedDate.contains("Today", ignoreCase = true)
                TimePeriodFilter.YESTERDAY -> article.publishedDate.contains("Yesterday", ignoreCase = true)
                TimePeriodFilter.LAST_7_DAYS -> true
                TimePeriodFilter.LAST_30_DAYS -> true
            }

            matchesCategory && matchesQuery && matchesTime
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setTimePeriod(period: TimePeriodFilter) {
        _selectedTimePeriod.value = period
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleBookmark(article: Article) {
        viewModelScope.launch {
            val newStatus = !article.isBookmarked
            newsRepo.toggleBookmark(article.id, article.isBookmarked)
            if (newStatus) {
                bookmarkRepo.addBookmark(
                    contentType = "NEWS",
                    contentId = article.id,
                    title = article.title,
                    summary = article.description,
                    category = article.category
                )
            } else {
                bookmarkRepo.removeBookmark(article.id)
            }
        }
    }

    fun markAsRead(articleId: String) {
        viewModelScope.launch {
            newsRepo.markAsRead(articleId)
        }
    }
}
