package app.bettermetesttask.datamovies.repository.stores

import app.bettermetesttask.domaincore.utils.FakeResponse
import app.bettermetesttask.domainmovies.entries.Movie
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.random.Random

class MoviesRestStore @Inject constructor() {

    private val statusCodes = listOf(200, 201, 202, 304, 400)

    suspend fun getMovies(): FakeResponse<List<Movie>> {
        val statusCode = statusCodes.random()
        if (statusCode >= 400) {
            throw IllegalStateException("Did not manage to retrieve movies from remote server")
        }
        delay(Random.nextLong(500, 3_000))
        return FakeResponse(
            code = statusCode,
            data = MoviesFactory.createMoviesList()
        )
    }
}

