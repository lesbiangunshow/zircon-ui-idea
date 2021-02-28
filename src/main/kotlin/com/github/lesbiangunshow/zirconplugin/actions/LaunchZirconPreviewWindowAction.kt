package com.github.lesbiangunshow.zirconplugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.wm.ToolWindowManager

class LaunchZirconPreviewWindowAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        e.project?.let {
            ToolWindowManager.getInstance(it).getToolWindow("Zircon Preview")?.show()
        }

    }
}