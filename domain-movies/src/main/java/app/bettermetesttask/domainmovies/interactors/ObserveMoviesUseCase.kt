package app.bettermetesttask.domainmovies.interactors

import app.bettermetesttask.domaincore.utils.Result
import app.bettermetesttask.domaincore.utils.onError
import app.bettermetesttask.domaincore.utils.onSuccess
import app.bettermetesttask.domainmovies.entries.Movie
import app.bettermetesttask.domainmovies.repository.MoviesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class ObserveMoviesUseCase @Inject constructor(
    private val repository: MoviesRepository
) {

    suspend operator fun invoke(onError: (String) -> Unit): Flow<Result<List<Movie>>> {
        return combine(
            repository.getLocalMovies(),
            repository.observeLikedMovieIds()
        ) { localMovies, likedMoviesIds ->
            val movies = localMovies.map {
                if (likedMoviesIds.contains(it.id)) {
                    it.copy(liked = true)
                } else {
                    it
                }
            }
            Result.Success(movies)
        }.onStart {
            repository.getMovies().onSuccess {
                repository.addLocalMovies(it)
            }.onError {
                onError(it.message.toString())
            }
        }
    }


}

