package app.bettermetesttask.movies.sections

import app.bettermetesttask.domainmovies.entries.Movie

sealed class MoviesState(open val error: String? = null) {

    data class Initial(override val error: String? = null) : MoviesState()

    data class Loading(override val error: String? = null) : MoviesState()

    data class MoviesLoaded(
        val movies: List<Movie> = emptyList(),
        override val error: String? = null
    ) : MoviesState()

    data class MovieOpened(
        val movie: Movie,
        override val error: String? = null
    ) : MoviesState()
}