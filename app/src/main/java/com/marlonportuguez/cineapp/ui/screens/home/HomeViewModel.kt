package com.marlonportuguez.cineapp.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marlonportuguez.cineapp.data.model.Movie
import com.marlonportuguez.cineapp.data.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val movieRepository: MovieRepository = MovieRepository()
) : ViewModel() {

    private val _movies = MutableStateFlow<List<Movie>>(emptyList())
    val movies: StateFlow<List<Movie>> = _movies.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadMovies()
    }

    fun loadMovies() {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            movieRepository.getAvailableMovies().collect { movieList ->
                _movies.value = movieList
                _isLoading.value = false
                if (movieList.isEmpty() && _error.value == null) {
                    _error.value = "No se encontraron películas disponibles."
                }
            }
        }
    }

    fun resetError() {
        _error.value = null
    }
}