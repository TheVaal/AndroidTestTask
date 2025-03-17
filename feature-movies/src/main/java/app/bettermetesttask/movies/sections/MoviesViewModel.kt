package app.bettermetesttask.movies.sections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.bettermetesttask.domaincore.utils.Result
import app.bettermetesttask.domaincore.utils.onError
import app.bettermetesttask.domaincore.utils.onSuccess
import app.bettermetesttask.domainmovies.entries.Movie
import app.bettermetesttask.domainmovies.interactors.AddMovieToFavoritesUseCase
import app.bettermetesttask.domainmovies.interactors.ObserveMovieUseCase
import app.bettermetesttask.domainmovies.interactors.ObserveMoviesUseCase
import app.bettermetesttask.domainmovies.interactors.RemoveMovieFromFavoritesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

const val ERROR_DURATION = 3000L

class MoviesViewModel @Inject constructor(
    private val observeMoviesUseCase: ObserveMoviesUseCase,
    private val observeMovieUseCase: ObserveMovieUseCase,
    private val likeMovieUseCase: AddMovieToFavoritesUseCase,
    private val dislikeMovieUseCase: RemoveMovieFromFavoritesUseCase,
    private val adapter: MoviesAdapter
) : ViewModel() {

    private val _uiState: MutableStateFlow<MoviesState> =
        MutableStateFlow(MoviesState.Initial())

    val uiState: StateFlow<MoviesState>
        get() = _uiState.asStateFlow()

    private var state: MoviesState
        get() = _uiState.value
        set(newState) {
            _uiState.update { newState }
        }
    private var _job: Job = Job()

    fun loadMovies() {
        _job = viewModelScope.launch(Dispatchers.IO) {
            observeMoviesUseCase(
                onError = {
                    showError(it)
                }
            ).collect { result ->
                if (result is Result.Success) {
                    state = MoviesState.MoviesLoaded(
                        result.data,
                        state.error
                    )
                    adapter.submitList(result.data)
                }
            }
        }
    }

    fun likeMovie(movie: Movie) {

        viewModelScope.launch(Dispatchers.IO) {
            if (!movie.liked) {
                likeMovieUseCase(movie.id)
            } else {
                dislikeMovieUseCase(movie.id)
            }
        }
    }

    private fun showError(error: String) {
        viewModelScope.launch {
            state = when (state) {
                is MoviesState.MoviesLoaded -> {
                    MoviesState.MoviesLoaded(
                        movies = (state as MoviesState.MoviesLoaded).movies,
                        error = error
                    )

                }

                else -> {
                    MoviesState.MoviesLoaded(
                        movies = emptyList(),
                        error = error
                    )
                }
            }
            delay(ERROR_DURATION)
            hideError()
        }

    }

    private fun hideError() {
        viewModelScope.launch {
            state = when (state) {
                is MoviesState.MoviesLoaded -> {
                    (state as MoviesState.MoviesLoaded).copy(
                        error = null
                    )
                }

                else -> {
                    MoviesState.MoviesLoaded(
                        movies = emptyList(),
                        error = null
                    )
                }
            }
        }

    }

    fun openMovieDetails(movie: Movie) {
        _job.cancel()
        _job = viewModelScope.launch {
            observeMovieUseCase(movie.id).collect { result ->
                result.onSuccess {
                    state = MoviesState.MovieOpened(
                        error = state.error,
                        movie = it
                    )
                }.onError {
                    showError(it.message.toString())
                }
            }
        }

    }

    fun hideMovie() {
        state = MoviesState.Loading()
        _job.cancel()
        loadMovies()
    }
}