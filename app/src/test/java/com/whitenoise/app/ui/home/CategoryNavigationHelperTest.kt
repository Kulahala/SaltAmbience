package com.whitenoise.app.ui.home

import com.whitenoise.app.core.model.SoundCategory
import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryNavigationHelperTest {

    @Test
    fun getNextCategory_advances_sequentially() {
        assertEquals(SoundCategory.RAIN, CategoryNavigationHelper.getNextCategory(SoundCategory.ALL))
        assertEquals(SoundCategory.NATURE, CategoryNavigationHelper.getNextCategory(SoundCategory.RAIN))
        assertEquals(SoundCategory.LIFE, CategoryNavigationHelper.getNextCategory(SoundCategory.NATURE))
        assertEquals(SoundCategory.NOISE, CategoryNavigationHelper.getNextCategory(SoundCategory.LIFE))
    }

    @Test
    fun getNextCategory_at_last_item_stays_at_last_item() {
        assertEquals(SoundCategory.NOISE, CategoryNavigationHelper.getNextCategory(SoundCategory.NOISE))
    }

    @Test
    fun getPreviousCategory_retreats_sequentially() {
        assertEquals(SoundCategory.LIFE, CategoryNavigationHelper.getPreviousCategory(SoundCategory.NOISE))
        assertEquals(SoundCategory.NATURE, CategoryNavigationHelper.getPreviousCategory(SoundCategory.LIFE))
        assertEquals(SoundCategory.RAIN, CategoryNavigationHelper.getPreviousCategory(SoundCategory.NATURE))
        assertEquals(SoundCategory.ALL, CategoryNavigationHelper.getPreviousCategory(SoundCategory.RAIN))
    }

    @Test
    fun getPreviousCategory_at_first_item_stays_at_first_item() {
        assertEquals(SoundCategory.ALL, CategoryNavigationHelper.getPreviousCategory(SoundCategory.ALL))
    }

    @Test
    fun resolveTargetCategory_swiping_left_beyond_threshold_advances() {
        val target = CategoryNavigationHelper.resolveTargetCategory(
            current = SoundCategory.ALL,
            totalDragOffsetPx = -100f,
            thresholdPx = 80f,
            velocity = 0f
        )
        assertEquals(SoundCategory.RAIN, target)
    }

    @Test
    fun resolveTargetCategory_swiping_right_beyond_threshold_retreats() {
        val target = CategoryNavigationHelper.resolveTargetCategory(
            current = SoundCategory.RAIN,
            totalDragOffsetPx = 100f,
            thresholdPx = 80f,
            velocity = 0f
        )
        assertEquals(SoundCategory.ALL, target)
    }

    @Test
    fun resolveTargetCategory_fast_fling_advances_even_with_low_offset() {
        val target = CategoryNavigationHelper.resolveTargetCategory(
            current = SoundCategory.RAIN,
            totalDragOffsetPx = -20f,
            thresholdPx = 80f,
            velocity = -1200f,
            velocityThreshold = 800f
        )
        assertEquals(SoundCategory.NATURE, target)
    }

    @Test
    fun resolveTargetCategory_fast_fling_retreats_even_with_low_offset() {
        val target = CategoryNavigationHelper.resolveTargetCategory(
            current = SoundCategory.NATURE,
            totalDragOffsetPx = 20f,
            thresholdPx = 80f,
            velocity = 1200f,
            velocityThreshold = 800f
        )
        assertEquals(SoundCategory.RAIN, target)
    }

    @Test
    fun resolveTargetCategory_below_threshold_retains_current() {
        val target = CategoryNavigationHelper.resolveTargetCategory(
            current = SoundCategory.NATURE,
            totalDragOffsetPx = -30f,
            thresholdPx = 80f,
            velocity = -200f,
            velocityThreshold = 800f
        )
        assertEquals(SoundCategory.NATURE, target)
    }
}
