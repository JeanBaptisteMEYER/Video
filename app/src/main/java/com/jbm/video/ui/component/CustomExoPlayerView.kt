package com.jbm.video.ui.component

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView

class CustomExoPlayerView @JvmOverloads constructor(
    context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : PlayerView(context!!, attrs, defStyleAttr) {

    init {
        setOnTouchListener(object : OnTouchListener {
            val gestureDetector =
                GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                    override fun onDoubleTapEvent(e: MotionEvent): Boolean {
                        player?.let {
                            when (shouldForward(e.x)) {
                                true -> {
                                    if (it.playbackState != Player.STATE_ENDED) {
                                        it.seekForward()
                                    }
                                }

                                false -> {
                                    it.seekBack()
                                }

                                else -> {}
                            }
                        }
                        return true
                    }

                    override fun onScroll(
                        e1: MotionEvent?,
                        e2: MotionEvent,
                        distanceX: Float,
                        distanceY: Float
                    ): Boolean {
                        return when {
                            distanceY > 10 -> {
                                enterFullScreen()
                                true
                            }

                            distanceY < -10 -> {
                                exitFullScreen()
                                true
                            }

                            else -> super.onScroll(e1, e2, distanceX, distanceY)
                        }
                    }

                    override fun onSingleTapUp(e: MotionEvent): Boolean {
                        return true
                    }

                    @OptIn(UnstableApi::class)
                    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                        if (isControllerFullyVisible) {
                            hideController()
                        } else {
                            showController()
                        }
                        return true
                    }
                })

            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                return gestureDetector.onTouchEvent(event);
            }
        })
    }

    private fun enterFullScreen() {
        (context as? Activity)?.window?.decorView?.systemUiVisibility =
            (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        (context as? Activity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        val params = layoutParams
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        params.height = ViewGroup.LayoutParams.MATCH_PARENT
        layoutParams = params
    }

    private fun exitFullScreen() {
        (context as? Activity)?.window?.decorView?.systemUiVisibility =
            View.SYSTEM_UI_FLAG_VISIBLE
        (context as? Activity)?.requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        val params = layoutParams
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        params.height =
            (200 * context.applicationContext.resources.displayMetrics.density).toInt()
        layoutParams = params
    }

    private fun shouldForward(posX: Float): Boolean? {
        return when {
            posX < this.width * 0.35 -> false
            posX > this.width * 0.65 -> true
            else -> null
        }
    }
}
