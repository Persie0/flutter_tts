package com.eyedeadevelopment.fluttertts

import android.speech.tts.TextToSpeech
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.*

class FlutterTtsTeardownTest {
    @Test
    fun cleanupRunsOnWorkerInsteadOfBlockingCaller() {
        val engine = mock(TextToSpeech::class.java)
        val enteredShutdown = CountDownLatch(1)
        val unblockShutdown = CountDownLatch(1)
        doAnswer {
            enteredShutdown.countDown()
            unblockShutdown.await(2, TimeUnit.SECONDS)
            null
        }.`when`(engine).shutdown()

        try {
            shutdownTtsInBackground(engine)
            assertTrue("shutdown was never reached", enteredShutdown.await(2, TimeUnit.SECONDS))
        } finally {
            unblockShutdown.countDown()
        }
        verify(engine).stop()
        verify(engine).shutdown()
    }

    @Test
    fun nullEngineIsSafeDuringEarlyDetach() {
        shutdownTtsInBackground(null)
    }
}
