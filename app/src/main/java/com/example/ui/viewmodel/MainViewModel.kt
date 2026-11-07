package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.UserProfile
import com.example.data.repository.AuthRepository
import com.example.data.repository.NewsRepository
import com.example.data.repository.PerformanceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val authRepo: AuthRepository,
    private val newsRepo: NewsRepository,
    private val performanceRepo: PerformanceRepository
) : ViewModel() {

    val currentUser = authRepo.currentUser
    val userProfile: StateFlow<UserProfile?> = authRepo.userProfile

    init {
        viewModelScope.launch {
            authRepo.attemptAutoSignIn()
            newsRepo.seedInitialArticlesIfEmpty()
            performanceRepo.seedInitialPerformanceIfEmpty()
        }
    }

    fun updateTargetExam(exam: String) {
        viewModelScope.launch {
            authRepo.updateTargetExam(exam)
        }
    }

    fun updateDailyGoal(goal: Long) {
        viewModelScope.launch {
            authRepo.updateDailyGoal(goal)
        }
    }

    fun signOut(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepo.signOut(onComplete)
        }
    }
}
