package com.github.lesbiangunshow.zirconplugin.toolWindow

import com.github.lesbiangunshow.zirconplugin.utils.BasicCanvasRenderer
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.ui.layout.LCFlags
import com.intellij.ui.layout.cellPanel
import com.intellij.ui.layout.panel
import org.hexworks.zircon.api.application.AppConfig
import org.hexworks.zircon.api.builder.graphics.BoxBuilder
import org.hexworks.zircon.internal.grid.ThreadSafeTileGrid
import javax.swing.JPanel

class ZirconToolWindow {

    val zirconPanel: JPanel = cellPanel()

    fun createZirconCanvas() {
        val actionManager = ActionManager.getInstance()
        val actionGroup = DefaultActionGroup("ACTION_GROUP", false)
        actionGroup.add(actionManager.getAction("RefreshZirconPreviewAction"))
        val actionToolbar = actionManager.createActionToolbar("ACTION_TOOLBAR", actionGroup, true)
        actionToolbar.setTargetComponent(content)

        val box = BoxBuilder().build()
        val appConfig = AppConfig.defaultConfiguration()
        val tileGrid = ThreadSafeTileGrid(
            initialTileset = appConfig.defaultTileset,
            initialSize = box.size
        )
        val renderer = BasicCanvasRenderer(
            zirconPanel,
            tileGrid
        )
        renderer.create()
        renderer.render()
    }

    val content = panel(LCFlags.fill ) {
            zirconPanel
        }
}