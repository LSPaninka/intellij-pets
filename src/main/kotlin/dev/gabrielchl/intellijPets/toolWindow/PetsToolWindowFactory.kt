package dev.gabrielchl.intellijPets.toolWindow

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import dev.gabrielchl.intellijPets.settings.PetsSettings
import dev.gabrielchl.intellijPets.utils.Constants
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Point
import java.awt.RenderingHints
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.Timer
import kotlin.math.roundToInt

class PetsToolWindowFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val view = PetsToolWindowContent()
        val content = ContentFactory.getInstance().createContent(view.contentContainerPanel, "", false)
        content.setDisposer(view)
        toolWindow.contentManager.addContent(content)
    }

    private class PetsToolWindowContent : Disposable {
        val contentContainerPanel = JPanel(BorderLayout())
        private val contentPanel = PetsToolWindowContentPanel()

        init {
            contentContainerPanel.add(contentPanel, BorderLayout.CENTER)
            val actionGroup = ActionManager.getInstance().getAction("Pets.Actions") as DefaultActionGroup
            val toolbar = ActionManager.getInstance().createActionToolbar("PetsToolBar", actionGroup, true)
            toolbar.targetComponent = contentContainerPanel
            contentContainerPanel.add(toolbar.component, BorderLayout.NORTH)
        }

        override fun dispose() = contentPanel.dispose()
    }

    internal class PetsToolWindowContentPanel : JPanel() {
        private data class Ball(var x: Double, var y: Double, var velocityY: Double, var framesLeft: Int)

        private val pets = mutableListOf<Pet>()
        private var configuredPets = emptyList<String>()
        private var configuredScale = Constants.DEFAULT_SCALE
        private var pressedPet: Pet? = null
        private var dragged = false
        private var ball: Ball? = null
        private val animationTimer = Timer(TICK_MILLIS) { tick() }

        init {
            isFocusable = true
            synchronizeSettings()
            animationTimer.start()

            addMouseListener(object : MouseAdapter() {
                override fun mousePressed(event: MouseEvent) {
                    pressedPet = petAt(event.point)
                    dragged = false
                    if (SwingUtilities.isLeftMouseButton(event)) pressedPet?.startDragging(event.x)
                }

                override fun mouseReleased(event: MouseEvent) {
                    pressedPet?.stopDragging()
                    pressedPet = null
                }

                override fun mouseClicked(event: MouseEvent) {
                    if (dragged) return
                    val pet = petAt(event.point)
                    when {
                        pet == null && SwingUtilities.isLeftMouseButton(event) -> throwBall(event.point)
                        pet != null && SwingUtilities.isRightMouseButton(event) -> pet.feed()
                        pet != null && event.clickCount >= 2 -> pet.greet()
                        pet != null -> pet.pet()
                    }
                }
            })
            addMouseMotionListener(object : MouseMotionAdapter() {
                override fun mouseDragged(event: MouseEvent) {
                    val pet = pressedPet ?: return
                    dragged = true
                    pet.dragTo(event.x)
                }

                override fun mouseMoved(event: MouseEvent) {
                    pets.lastOrNull()?.followCursor(event.x)
                }
            })
            addMouseListener(object : MouseAdapter() {
                override fun mouseExited(event: MouseEvent) {
                    pets.lastOrNull()?.stopFollowing()
                }
            })
        }

        private fun tick() {
            check(SwingUtilities.isEventDispatchThread()) { "Pet animations must run on the EDT" }
            synchronizeSettings()
            pets.forEach(Pet::tick)
            ball?.let {
                it.velocityY += 0.7
                it.y += it.velocityY
                val floor = (height - 10).toDouble()
                if (it.y > floor) {
                    it.y = floor
                    it.velocityY *= -0.55
                }
                it.framesLeft--
                if (it.framesLeft <= 0) ball = null
            }
            repaint()
        }

        private fun synchronizeSettings() {
            val settings = PetsSettings.instance.state
            val safePets = settings.petList.map(Constants::validPetType)
            val safeScale = Constants.validPetScale(settings.petScale)
            if (safePets == configuredPets && safeScale == configuredScale && pets.isNotEmpty() == safePets.isNotEmpty()) return
            configuredPets = safePets.toList()
            configuredScale = safeScale
            pets.clear()
            configuredPets.forEach { pets += Pet(it, this) }
        }

        private fun petAt(point: Point): Pet? = pets.asReversed().firstOrNull { it.contains(point) }

        private fun throwBall(point: Point) {
            val startY = point.y.coerceIn(8, (height - 10).coerceAtLeast(8))
            ball = Ball(point.x.toDouble(), startY.toDouble(), -5.5, 34)
            pets.minByOrNull { kotlin.math.abs(it.currentX - point.x) }?.chaseBall(point.x)
        }

        override fun paintComponent(graphics: Graphics) {
            super.paintComponent(graphics)
            val g = graphics.create() as Graphics2D
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            pets.forEach { pet ->
                g.drawImage(pet.image, pet.currentX, height - pet.spriteHeight, pet.spriteWidth, pet.spriteHeight, null)
                pet.paintEffects(g)
            }
            ball?.let {
                g.color = Color(72, 137, 214)
                g.fillOval(it.x.roundToInt() - 6, it.y.roundToInt() - 6, 12, 12)
                g.color = Color(224, 239, 255)
                g.drawArc(it.x.roundToInt() - 4, it.y.roundToInt() - 4, 8, 8, 25, 130)
            }
            g.dispose()
        }

        fun dispose() {
            animationTimer.stop()
            pets.clear()
            ball = null
        }

        companion object {
            private const val TICK_MILLIS = 150
        }
    }
}
