package dev.gabrielchl.intellijPets.toolWindow

import dev.gabrielchl.intellijPets.settings.PetsSettings
import dev.gabrielchl.intellijPets.utils.Constants
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Image
import java.awt.Point
import java.awt.RenderingHints
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.util.EnumMap
import java.util.Locale
import javax.imageio.ImageIO
import javax.swing.JPanel
import kotlin.math.abs
import kotlin.math.max
import kotlin.random.Random

/** One animated pet. All methods are called from Swing's event dispatch thread. */
class Pet(val variant: String, private val container: JPanel) {
    private enum class State { ATTACK, IDLE, JUMP, LIEDOWN, RUN, SIT, WALK }
    private enum class Effect { HEARTS, TREAT, SPARKLES }
    private data class ActiveEffect(val type: Effect, var frame: Int = 0)

    private var frameIndex = 0
    private var state = State.SIT
    var currentX = 0
        private set
    private var targetX = currentX
    private var facingRight = true
    private var dragging = false
    private var dragOffset = 0
    private var chasingBall = false
    private val effects = mutableListOf<ActiveEffect>()
    private val sprites = EnumMap<State, List<BufferedImage>>(State::class.java)

    val spriteWidth: Int
    val spriteHeight: Int
    private val speed: Int
    var image: Image
        private set

    init {
        val spriteSize = Constants.PET_TO_SPRITE_SIZE.getValue(variant)
        val scale = Constants.validPetScale(PetsSettings.instance.state.petScale)
        spriteWidth = (spriteSize * 1.6 * scale).toInt().coerceAtLeast(1)
        spriteHeight = (spriteSize * 1.6 * scale).toInt().coerceAtLeast(1)
        speed = (7.0 / 40 * spriteSize * scale).toInt().coerceAtLeast(1)
        State.entries.forEach { sprites[it] = loadFrames(it, spriteSize) }
        image = scaledFrame()
    }

    private fun loadFrames(state: State, spriteSize: Int): List<BufferedImage> {
        val path = "/spritesheets/$variant/${state.name.lowercase(Locale.ROOT)}.png"
        val resource = javaClass.getResource(path) ?: return listOf(fallbackFrame(spriteSize))
        val sheet = runCatching { resource.openStream().use(ImageIO::read) }.getOrNull()
            ?: return listOf(fallbackFrame(spriteSize))
        val count = (sheet.width / spriteSize).coerceAtLeast(1)
        return (0 until count).map { index ->
            sheet.getSubimage(index * spriteSize, 0, minOf(spriteSize, sheet.width - index * spriteSize), minOf(spriteSize, sheet.height))
        }
    }

    private fun fallbackFrame(size: Int): BufferedImage = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB).also { frame ->
        val graphics = frame.createGraphics()
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics.color = Color(122, 174, 106)
        graphics.fillOval(size / 8, size / 4, size * 3 / 4, size * 5 / 8)
        graphics.color = Color(42, 52, 40)
        graphics.fillOval(size * 3 / 10, size * 2 / 5, max(2, size / 12), max(2, size / 12))
        graphics.fillOval(size * 6 / 10, size * 2 / 5, max(2, size / 12), max(2, size / 12))
        graphics.dispose()
    }

    fun contains(point: Point): Boolean =
        point.x in currentX..(currentX + spriteWidth) && point.y in (container.height - spriteHeight)..container.height

    fun pet() {
        state = State.IDLE
        frameIndex = 0
        effects += ActiveEffect(Effect.HEARTS)
    }

    fun greet() {
        state = State.JUMP
        frameIndex = 0
        effects += ActiveEffect(Effect.SPARKLES)
    }

    fun feed() {
        state = State.ATTACK
        frameIndex = 0
        effects += ActiveEffect(Effect.TREAT)
    }

    fun startDragging(mouseX: Int) {
        dragging = true
        dragOffset = mouseX - currentX
        state = State.RUN
        frameIndex = 0
    }

    fun dragTo(mouseX: Int) {
        if (!dragging) return
        val previous = currentX
        currentX = clampX(mouseX - dragOffset)
        targetX = currentX
        if (currentX != previous) facingRight = currentX > previous
    }

    fun stopDragging() {
        if (!dragging) return
        dragging = false
        state = State.SIT
        frameIndex = 0
    }

    fun chaseBall(x: Int) {
        chasingBall = true
        targetX = clampX(x - spriteWidth / 2)
        state = State.RUN
        frameIndex = 0
    }

    fun followCursor(x: Int) {
        if (dragging || chasingBall || abs(x - (currentX + spriteWidth / 2)) <= 30) return
        targetX = clampX(x - spriteWidth / 2)
    }

    fun stopFollowing() {
        if (!dragging && !chasingBall && state == State.SIT) targetX = currentX
    }

    fun tick() {
        effects.forEach { it.frame++ }
        effects.removeAll { it.frame > EFFECT_DURATION }

        if (!dragging) updateMovement()
        val frames = sprites.getValue(state)
        frameIndex++
        if (frameIndex >= frames.size) {
            frameIndex = 0
            completeAnimationCycle()
        }
        image = scaledFrame()
    }

    private fun updateMovement() {
        if (currentX == targetX) {
            if (state == State.WALK || state == State.RUN) {
                state = if (chasingBall) State.JUMP else State.SIT
                chasingBall = false
                frameIndex = 0
            }
            return
        }
        if (state != State.RUN) state = State.WALK
        val delta = (targetX - currentX).coerceIn(-speed, speed)
        currentX += delta
        facingRight = delta >= 0
    }

    private fun completeAnimationCycle() {
        when (state) {
            State.JUMP, State.ATTACK -> state = State.SIT
            State.SIT -> when (Random.nextInt(10)) {
                0 -> state = State.IDLE
                1 -> state = State.LIEDOWN
                2 -> wander()
            }
            State.IDLE -> when (Random.nextInt(10)) {
                0 -> state = State.SIT
                1 -> wander()
            }
            State.LIEDOWN -> if (Random.nextInt(15) == 0) state = State.SIT
            else -> Unit
        }
    }

    private fun wander() {
        val maximum = (container.width - spriteWidth).coerceAtLeast(0)
        targetX = if (maximum == 0) 0 else Random.nextInt(maximum + 1)
    }

    private fun clampX(x: Int): Int = x.coerceIn(0, (container.width - spriteWidth).coerceAtLeast(0))

    private fun scaledFrame(): Image {
        val source = sprites.getValue(state)[frameIndex.coerceIn(sprites.getValue(state).indices)]
        if (facingRight) return source.getScaledInstance(spriteWidth, spriteHeight, Image.SCALE_FAST)
        val transform = AffineTransform.getScaleInstance(-1.0, 1.0).apply { translate(-source.width.toDouble(), 0.0) }
        val flipped = AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR).filter(source, null)
        return flipped.getScaledInstance(spriteWidth, spriteHeight, Image.SCALE_FAST)
    }

    fun paintEffects(graphics: Graphics2D) {
        effects.forEach { effect ->
            val progress = effect.frame.toDouble() / EFFECT_DURATION
            when (effect.type) {
                Effect.HEARTS -> paintHeart(graphics, currentX + spriteWidth / 2, container.height - spriteHeight - (progress * 28).toInt())
                Effect.TREAT -> paintTreat(graphics, currentX + spriteWidth / 2, container.height - spriteHeight + (progress * spriteHeight / 2).toInt())
                Effect.SPARKLES -> paintSparkles(graphics, currentX + spriteWidth / 2, container.height - spriteHeight / 2, effect.frame)
            }
        }
    }

    private fun paintHeart(g: Graphics2D, x: Int, y: Int) {
        g.color = Color(236, 79, 118)
        g.fillOval(x - 9, y, 10, 10)
        g.fillOval(x, y, 10, 10)
        val xs = intArrayOf(x - 9, x + 10, x)
        val ys = intArrayOf(y + 5, y + 5, y + 18)
        g.fillPolygon(xs, ys, 3)
    }

    private fun paintTreat(g: Graphics2D, x: Int, y: Int) {
        g.color = Color(184, 116, 63)
        g.fillRoundRect(x - 9, y, 18, 9, 6, 6)
        g.color = Color(117, 72, 40)
        g.drawRoundRect(x - 9, y, 18, 9, 6, 6)
    }

    private fun paintSparkles(g: Graphics2D, x: Int, y: Int, frame: Int) {
        g.color = Color(255, 193, 54)
        val radius = 15 + frame % 12
        repeat(4) { index ->
            val dx = if (index % 2 == 0) radius else 0
            val dy = if (index % 2 == 1) radius else 0
            g.fillOval(x + if (index < 2) dx else -dx - 4, y + if (index < 2) dy else -dy - 4, 5, 5)
        }
    }

    companion object {
        private const val EFFECT_DURATION = 14
    }
}
