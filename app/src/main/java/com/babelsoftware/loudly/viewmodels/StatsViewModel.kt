package com.babelsoftware.loudly.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babelsoftware.loudly.constants.StatPeriod
import com.babelsoftware.loudly.db.MusicDatabase
import com.babelsoftware.innertube.YouTube
import com.babelsoftware.loudly.reportException
import com.babelsoftware.loudly.models.SongWithPlayCount // Yeni modeli import et
import kotlinx.coroutines.flow.map // map operatörünü import et
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StatsViewModel @Inject constructor(
    val database: MusicDatabase,
) : ViewModel() {
    val statPeriod = MutableStateFlow(StatPeriod.`1_WEEK`)

    // DAO'dan tek bir seferde daha uzun bir liste çekelim (örneğin 20 öğe)
    private val allMostPlayedSongs = statPeriod.flatMapLatest { period ->
        // DAO'daki yeni fonksiyonu çağırıyoruz ve limiti artırıyoruz
        database.mostPlayedSongsWithPlayCount(period.toTimeMillis(), limit = 20)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Özet görünüm için kısa liste (ilk 5)
    val summaryMostPlayedSongs = allMostPlayedSongs.map { it.take(5) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Genişletilmiş görünüm için tam liste
    val detailedMostPlayedSongs = allMostPlayedSongs
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Sanatçılar ve albümler için şimdilik aynı mantığı koruyabiliriz.
    val mostPlayedArtists = statPeriod.flatMapLatest { period ->
        database.mostPlayedArtists(period.toTimeMillis()).map { artists ->
            artists.filter { it.artist.isYouTubeArtist }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val mostPlayedAlbums = statPeriod.flatMapLatest { period ->
        database.mostPlayedAlbums(period.toTimeMillis())
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        viewModelScope.launch {
            mostPlayedArtists.collect { artists ->
                artists
                    .map { it.artist }
                    .filter {
                        it.thumbnailUrl == null || Duration.between(it.lastUpdateTime, LocalDateTime.now()) > Duration.ofDays(10)
                    }
                    .forEach { artist ->
                        YouTube.artist(artist.id).onSuccess { artistPage ->
                            database.query {
                                update(artist, artistPage)
                            }
                        }
                    }
            }
        }
        viewModelScope.launch {
            mostPlayedAlbums.collect { albums ->
                albums.filter {
                    it.album.songCount == 0
                }.forEach { album ->
                    YouTube.album(album.id).onSuccess { albumPage ->
                        database.query {
                            update(album.album, albumPage)
                        }
                    }.onFailure {
                        reportException(it)
                        if (it.message?.contains("NOT_FOUND") == true) {
                            database.query {
                                delete(album.album)
                            }
                        }
                    }
                }
            }
        }
    }
}
