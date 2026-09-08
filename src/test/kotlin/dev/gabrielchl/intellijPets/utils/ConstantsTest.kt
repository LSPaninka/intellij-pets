package dev.gabrielchl.intellijPets.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class ConstantsTest {
    @Test
    fun `known pet type is preserved`() {
        assertEquals("bunny", Constants.validPetType("bunny"))
    }

    @Test
    fun `unknown pet type falls back safely`() {
        assertEquals(Constants.DEFAULT_PET, Constants.validPetType("../../unexpected"))
    }

    @Test
    fun `pet scale is finite and constrained`() {
        assertEquals(0.2, Constants.validPetScale(-10.0))
        assertEquals(3.0, Constants.validPetScale(50.0))
        assertEquals(Constants.DEFAULT_SCALE, Constants.validPetScale(Double.NaN))
        assertEquals(Constants.DEFAULT_SCALE, Constants.validPetScale(Double.POSITIVE_INFINITY))
    }
}
