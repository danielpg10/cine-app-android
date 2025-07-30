package com.marlonportuguez.cineapp.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marlonportuguez.cineapp.data.model.MovieListItem
import com.marlonportuguez.cineapp.data.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val movieRepository: MovieRepository = MovieRepository()
) : ViewModel() {

    private val _movies = MutableStateFlow<List<MovieListItem>>(emptyList())
    val movies: StateFlow<List<MovieListItem>> = _movies.asStateFlow()

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
            // Aca estou usando la nueva funcion del repositorio que devuelve la lista de MovieListItem
            movieRepository.getMoviesForList().collect { movieItemList ->
                _movies.value = movieItemList
                _isLoading.value = false
                if (movieItemList.isEmpty() && _error.value == null) {
                    _error.value = "No se encontraron películas disponibles en este momento."
                }
            }
        }
    }

    fun resetError() {
        _error.value = null
    }
}