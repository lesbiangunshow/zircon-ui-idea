package com.github.lesbiangunshow.zirconplugin.services

import com.github.lesbiangunshow.zirconplugin.MyBundle
import com.intellij.openapi.project.Project

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
