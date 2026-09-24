package com.whitenoise.app

import com.whitenoise.app.core.audio.AudioMixerEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class AudioMixerEngineLoadControlTest {

    @Test
    fun testCreateLowLatencyLoadControlReturnsIndependentInstances() {
        val control1 = AudioMixerEngine.createLowLatencyLoadControl()
        val control2 = AudioMixerEngine.createLowLatencyLoadControl()

        assertNotNull("First LoadControl instance must not be null", control1)
        assertNotNull("Second LoadControl instance must not be null", control2)
        assertNotSame(
            "Each ExoPlayer must receive an independent DefaultLoadControl instance to avoid single-thread affinity assertions",
            control1,
            control2
        )
    }

    @Test
    fun testConcurrentFactoryCallsProduceDistinctInstances() {
        // Simulate 15 sound tracks creating their load controls concurrently across background threads
        val threadCount = 15
        val instances = ConcurrentHashMap.newKeySet<Int>()
        val latch = CountDownLatch(threadCount)

        val threads = (1..threadCount).map {
            Thread {
                try {
                    val control = AudioMixerEngine.createLowLatencyLoadControl()
                    instances.add(System.identityHashCode(control))
                } finally {
                    latch.countDown()
                }
            }
        }

        threads.forEach { it.start() }
        val finished = latch.await(5, TimeUnit.SECONDS)

        assertTrue("All concurrent threads must finish within timeout", finished)
        assertEquals(
            "All 15 LoadControl instances created across threads must have distinct identity hashes",
            threadCount,
            instances.size
        )
    }

    @Test
    fun testLowLatencyBufferDurationsPreservedViaReflection() {
        val loadControl = AudioMixerEngine.createLowLatencyLoadControl()

        val clazz = loadControl.javaClass

        // Helper to find field in class or superclass
        fun getFieldVal(fieldName: String): Any? {
            var current: Class<*>? = clazz
            while (current != null) {
                try {
                    val field = current.getDeclaredField(fieldName)
                    field.isAccessible = true
                    return field.get(loadControl)
                } catch (_: NoSuchFieldException) {
                    current = current.superclass
                }
            }
            return null
        }

        val minBufferUs = getFieldVal("minBufferUs") as? Long
        val maxBufferUs = getFieldVal("maxBufferUs") as? Long
        val bufferForPlaybackUs = getFieldVal("bufferForPlaybackUs") as? Long
        val bufferForPlaybackAfterRebufferUs = getFieldVal("bufferForPlaybackAfterRebufferUs") as? Long
        val prioritizeTimeOverSize = getFieldVal("prioritizeTimeOverSizeThresholds") as? Boolean

        // Strictly assert fields exist and match exact Media3 DefaultLoadControl configuration
        assertNotNull("minBufferUs field must exist on DefaultLoadControl", minBufferUs)
        assertEquals("minBufferUs must be 1,000,000 us (1000ms)", 1_000_000L, minBufferUs)

        assertNotNull("maxBufferUs field must exist on DefaultLoadControl", maxBufferUs)
        assertEquals("maxBufferUs must be 2,000,000 us (2000ms)", 2_000_000L, maxBufferUs)

        assertNotNull("bufferForPlaybackUs field must exist on DefaultLoadControl", bufferForPlaybackUs)
        assertEquals("bufferForPlaybackUs must be 50,000 us (50ms)", 50_000L, bufferForPlaybackUs)

        assertNotNull("bufferForPlaybackAfterRebufferUs field must exist on DefaultLoadControl", bufferForPlaybackAfterRebufferUs)
        assertEquals("bufferForPlaybackAfterRebufferUs must be 100,000 us (100ms)", 100_000L, bufferForPlaybackAfterRebufferUs)

        assertNotNull("prioritizeTimeOverSizeThresholds field must exist on DefaultLoadControl", prioritizeTimeOverSize)
        assertEquals("prioritizeTimeOverSizeThresholds must be true", true, prioritizeTimeOverSize)
    }

    @Test
    fun testSharedLoadControlFailsAcrossThreadsWhileIndependentInstancesSucceed() {
        // 1. Reproduce the bug: A shared DefaultLoadControl throws IllegalStateException when onPrepared is called across threads
        val sharedControl = AudioMixerEngine.createLowLatencyLoadControl()
        val thread1 = Thread {
            sharedControl.onPrepared(androidx.media3.exoplayer.analytics.PlayerId("rain"))
        }
        thread1.start()
        thread1.join()

        var crossThreadException: Throwable? = null
        val thread2 = Thread {
            try {
                sharedControl.onPrepared(androidx.media3.exoplayer.analytics.PlayerId("storm"))
            } catch (t: Throwable) {
                crossThreadException = t
            }
        }
        thread2.start()
        thread2.join()

        assertNotNull("Calling onPrepared across threads on shared LoadControl MUST throw", crossThreadException)
        assertTrue(
            "Shared LoadControl must throw IllegalStateException due to single-thread affinity check",
            crossThreadException is IllegalStateException
        )
        assertTrue(
            "Exception message must mention sharing same LoadControl",
            crossThreadException?.message?.contains("Players that share the same LoadControl must share the same playback thread") == true
        )

        // 2. Verify the fix: Independent DefaultLoadControl instances from factory method succeed concurrently across 15 threads
        val threadCount = 15
        val exceptions = java.util.concurrent.CopyOnWriteArrayList<Throwable>()
        val threads = (1..threadCount).map { i ->
            Thread {
                try {
                    val independentControl = AudioMixerEngine.createLowLatencyLoadControl()
                    independentControl.onPrepared(androidx.media3.exoplayer.analytics.PlayerId("track_$i"))
                } catch (t: Throwable) {
                    exceptions.add(t)
                }
            }
        }
        threads.forEach { it.start() }
        threads.forEach { it.join() }

        assertTrue(
            "Independent LoadControls must not throw any exception when onPrepared is called across distinct threads: $exceptions",
            exceptions.isEmpty()
        )
    }
}
