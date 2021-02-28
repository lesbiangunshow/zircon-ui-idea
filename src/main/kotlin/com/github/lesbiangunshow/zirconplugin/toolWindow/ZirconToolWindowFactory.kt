package com.github.lesbiangunshow.zirconplugin.toolWindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class ZirconToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val newWindow = ZirconToolWindow()
        newWindow.createZirconCanvas()
        val contentFactory = ContentFactory.SERVICE.getInstance()

        val content = contentFactory.createContent(newWindow.content, "Zircon Preview", false)
        toolWindow.contentManager.addContent(content)
    }
}