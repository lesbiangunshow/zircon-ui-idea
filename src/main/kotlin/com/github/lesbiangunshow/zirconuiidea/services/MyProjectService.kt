package com.github.lesbiangunshow.zirconuiidea.services

import com.github.lesbiangunshow.zirconuiidea.MyBundle
import com.intellij.openapi.project.Project

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
