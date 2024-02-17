package ua.pasinfosc

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.annotation.OptIn
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.Listener
import androidx.media3.common.Player.PLAY_WHEN_READY_CHANGE_REASON_END_OF_MEDIA_ITEM
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ua.pasinfosc.MainActivity.Companion.context
import ua.pasinfosc.domain.entities.Stop
import ua.pasinfosc.domain.usecases.GetBusIdUseCase
import ua.pasinfosc.domain.usecases.GetFilesAdUseCase
import ua.pasinfosc.domain.usecases.GetMarqueeSpeedUseCase
import ua.pasinfosc.domain.usecases.GetTimeForAdUseCase
import ua.pasinfosc.utils.CustomEventLogger
import ua.pasinfosc.utils.pasinfoscLog

class MainViewModel(application: Application) : AndroidViewModel(application), KoinComponent {

    private val getAdVideos: GetFilesAdUseCase by inject()
    private val getBusIdUseCase: GetBusIdUseCase by inject()
    private val getTimeForAdUseCase: GetTimeForAdUseCase by inject()
    private val getMarqueeSpeedUseCase: GetMarqueeSpeedUseCase by inject()

    private val _routeId = MutableStateFlow<String?>(null)
    private val _stops = MutableStateFlow<List<Stop>>(emptyList())
    private val _finalStop = MutableStateFlow<String?>(null)

    val marqueeSpeed = try {
        getMarqueeSpeedUseCase()
    } catch (_: Exception) {
        30
    }
    val routeId: StateFlow<String?> get() = _routeId
    val stops: StateFlow<List<Stop>> get() = _stops
    val finalStop: StateFlow<String?> get() = _finalStop

    var playSoundListener: ((String) -> Unit)? = null
    private var lastPlayedSound: String? = null

    private val _adShown = MutableStateFlow(false)
    val adShown: StateFlow<Boolean> get() = _adShown

    private lateinit var adList: List<Uri>

    private val playerListener = object : Listener {
        @OptIn(androidx.media3.common.util.UnstableApi::class)
        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
        }
    }
    var adPlayer: ExoPlayer = ExoPlayer.Builder(application).build().apply {
//        playWhenReady = true
        addListener(playerListener)
        addAnalyticsListener(CustomEventLogger())
    }

    @OptIn(UnstableApi::class)
    private fun reinitiatePlayer() {
        adPlayer.release()
        adPlayer = ExoPlayer.Builder(getApplication()).build().apply {
            repeatMode = Player.REPEAT_MODE_ALL
            pauseAtEndOfMediaItems = true
            addListener(playerListener)
            addListener(object : Listener {
                @OptIn(UnstableApi::class)
                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    pasinfoscLog("PLAYER ERROR: $error")
                    pasinfoscLog("resetting player")
                    _adShown.value = false
                    newAdWaitingJob?.cancel()
                    viewModelScope.launch {
                        reinitiatePlayer()
                        adList = emptyList()
                        delayNewAd()
                    }
                }

                override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                    super.onPlayWhenReadyChanged(playWhenReady, reason)
                    if (reason == PLAY_WHEN_READY_CHANGE_REASON_END_OF_MEDIA_ITEM) {
                        _adShown.value = false
                        newAdWaitingJob?.cancel()
                        newAdWaitingJob = viewModelScope.launch {
                            pasinfoscLog("ad item finished, delay the next one")
                            if (delayNewAd()) {
                                if (isActive && adList.isNotEmpty()) {
                                    pasinfoscLog("playing the next ad")
                                    adPlayer.play()
                                    _adShown.value = true
                                }
                            }
                        }
                    }
                }
            })
            addAnalyticsListener(CustomEventLogger())
        }
    }

    private var newAdWaitingJob: Job? = null

    private var alreadyInitialized = false

    @OptIn(androidx.media3.common.util.UnstableApi::class)
    fun init(context: Context) {
        if (alreadyInitialized) return
        alreadyInitialized = true

        _routeId.value = ""
        viewModelScope.launch {
            adList = getAdVideos().filter { it.checkIfVideo(context) }

            BusApi.init(
                context,
                getBusIdUseCase()
            ) { finalStopName, routeId, stops, endless, sound ->
                _finalStop.value = finalStopName
                _routeId.value = routeId
                _stops.value = stops.run {
                    if (endless) stops + stops.subList(
                        fromIndex = 0,
                        toIndex = stops.indexOfFirst {
                            it.state == Stop.State.CURRENT || it.state == Stop.State.NEXT
                        }
                    ) else stops
                }

                if (sound != lastPlayedSound && sound.isNotEmpty()) {
                    pasinfoscLog("pause ad to display stop info")
                    newAdWaitingJob?.cancel()
                    newAdWaitingJob = null
                    adPlayer.pause()
                    _adShown.value = false

                    newAdWaitingJob = viewModelScope.launch {
                        if (delayNewAd()) {
                            pasinfoscLog("resume playing ad after stop info")
                            _adShown.value = true
                            delay(50)
                            adPlayer.play()
                        }
                    }

                    lastPlayedSound = sound
                    playSoundListener?.invoke(sound)
                }
            }

            pasinfoscLog("fist init player")
            reinitiatePlayer()
            newAdWaitingJob = viewModelScope.launch {
//                if (delayNewAd()) {
                if (isActive && adList.isNotEmpty()) {
                    adList.forEach {
                        adPlayer.addMediaItem(MediaItem.fromUri(it))
                    }
                    adPlayer.prepare()

                    if (delayNewAd()) {
                        pasinfoscLog("playing initial ad")
                        adPlayer.play()
                        _adShown.value = true
                    }
                }
//                }
            }
        }
    }

    private fun Uri.checkIfVideo(context: Context): Boolean {
        return if (ContentResolver.SCHEME_CONTENT == scheme) {
            context.contentResolver.getType(this)
        } else {
            MimeTypeMap
                .getSingleton()
                .getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(toString()))
        }?.startsWith("video") == true
    }

    private suspend fun delayNewAd(): Boolean {
        delay(getTimeForAdUseCase())

        val newAdList = getAdVideos().filter { it.checkIfVideo(context!!) }
        pasinfoscLog("new ad list: $newAdList")
        return if (newAdList != adList) {
            adList = newAdList
            pasinfoscLog("detected ad list change, updating player")

            _adShown.value = false
            adPlayer.pause()
            adPlayer.clearMediaItems()
            newAdList.forEach {
                adPlayer.addMediaItem(MediaItem.fromUri(it))
            }
            adPlayer.prepare()
            delay(getTimeForAdUseCase())
            adPlayer.play()
            _adShown.value = true

            false
        } else {
            true
        }
    }
}