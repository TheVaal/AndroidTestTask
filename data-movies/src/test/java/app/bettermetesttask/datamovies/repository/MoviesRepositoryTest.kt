package app.bettermetesttask.datamovies.repository

import app.bettermetesttask.datamovies.database.entities.MovieEntity
import app.bettermetesttask.datamovies.repository.stores.MoviesFactory
import app.bettermetesttask.datamovies.repository.stores.MoviesLocalStore
import app.bettermetesttask.datamovies.repository.stores.MoviesMapper
import app.bettermetesttask.datamovies.repository.stores.MoviesRestStore
import app.bettermetesttask.domaincore.utils.FakeResponse
import app.bettermetesttask.domaincore.utils.onSuccess
import app.bettermetesttask.domainmovies.entries.Movie
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
@ExtendWith(MockitoExtension::class)
internal class MoviesRepositoryTest {

    // Mocks
    private val localStore: MoviesLocalStore = mock()
    private val mapper: MoviesMapper = MoviesMapper()

    // SUT
    private lateinit var repository: MoviesRepositoryImpl

    @BeforeEach
    fun setUp() {
        repository = MoviesRepositoryImpl(localStore, mapper)
    }

    @Test
    fun `getMovies returns successful result from restStore`() = runTest {
        // Given
        val restStoreMock = mock<MoviesRestStore>()
        val fakeMovies = MoviesFactory.createMoviesList()

        whenever(restStoreMock.getMovies()).thenReturn(
            FakeResponse(
                code = 200,
                data = fakeMovies
            )
        )

        // Replace restStore with mocked one (т.к. он private val)
        val field = repository.javaClass.getDeclaredField("restStore")
        field.isAccessible = true
        field.set(repository, restStoreMock)

        // When
        val result = repository.getMovies()

        // Then
        result.onSuccess {
            assertEquals(fakeMovies, it)
        }
    }

    @Test
    fun `getMovie maps local movie to domain model`() = runTest {
        // Given
        val movieId = 2
        val localMovie = MovieEntity(
            id = movieId,
            title = "Movie #$movieId",
            description = "Some movie description #$movieId",
            posterPath = "https://www.themoviedb.org/t/p/w440_and_h660_face/lWlsZIsrGVWHtBeoOeLxIKDd9uy.jpg"
        )
        val mappedMovie = Movie(
            id = movieId,
            title = "Movie #$movieId",
            description = "Some movie description #$movieId",
            posterPath = "https://www.themoviedb.org/t/p/w440_and_h660_face/lWlsZIsrGVWHtBeoOeLxIKDd9uy.jpg"
        )

        whenever(localStore.getMovie(movieId)).thenReturn(localMovie)

        // When
        val result = repository.getMovie(movieId)

        // Then
        result.onSuccess {
            assertEquals(mappedMovie, it)
        }

        // Verify interactions
        verify(localStore).getMovie(movieId)

    }

    @Test
    fun `observeLikedMovieIds returns flow from localStore`() = runTest {
        // Given
        val likedIds = listOf(1, 2, 3)
        val flow = flowOf(likedIds)

        whenever(localStore.observeLikedMoviesIds()).thenReturn(flow)

        // When
        val result = repository.observeLikedMovieIds().first()

        // Then
        assertEquals(likedIds, result)
        verify(localStore).observeLikedMoviesIds()
    }

    @Test
    fun `addMovieToFavorites calls localStore likeMovie`() = runTest {
        // Given
        val movieId = 123

        // When
        repository.addMovieToFavorites(movieId)

        // Then
        verify(localStore).likeMovie(movieId)
    }

    @Test
    fun `removeMovieFromFavorites calls localStore dislikeMovie`() = runTest {
        // Given
        val movieId = 456

        // When
        repository.removeMovieFromFavorites(movieId)

        // Then
        verify(localStore).dislikeMovie(movieId)
    }
}

