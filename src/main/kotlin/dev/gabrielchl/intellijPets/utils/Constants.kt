package dev.gabrielchl.intellijPets.utils

object Constants {
    val PET_TYPES = listOf(
        "axolotl",
        "bunny",
        "cat-1",
        "cat-2",
        "cat-3",
        "cat-4",
        "cat-5",
        "chicken",
        "dog-1",
        "dog-2",
        "dog-3",
        "dog-4",
        "dog-5",
        "hedgehog"
    )

    val PET_TO_SPRITE_SIZE: Map<String, Int> = java.util.Map.ofEntries(
        java.util.Map.entry("axolotl", 32),
        java.util.Map.entry("bunny", 40),
        java.util.Map.entry("cat-1", 40),
        java.util.Map.entry("cat-2", 40),
        java.util.Map.entry("cat-3", 40),
        java.util.Map.entry("cat-4", 40),
        java.util.Map.entry("cat-5", 40),
        java.util.Map.entry("chicken", 40),
        java.util.Map.entry("dog-1", 32),
        java.util.Map.entry("dog-2", 32),
        java.util.Map.entry("dog-3", 32),
        java.util.Map.entry("dog-4", 32),
        java.util.Map.entry("dog-5", 32),
        java.util.Map.entry("hedgehog", 32)
    )

    const val DEFAULT_PET = "cat-1"
    const val DEFAULT_SCALE = 1.0
    val PET_SCALE_RANGE = 0.2..3.0

    fun validPetType(value: String): String = value.takeIf(PET_TYPES::contains) ?: DEFAULT_PET

    fun validPetScale(value: Double): Double =
        value.takeIf { it.isFinite() }?.coerceIn(PET_SCALE_RANGE) ?: DEFAULT_SCALE
}
