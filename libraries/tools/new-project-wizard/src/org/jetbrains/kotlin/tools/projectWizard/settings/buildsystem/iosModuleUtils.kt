/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.tools.projectWizard.settings.buildsystem

import org.jetbrains.kotlin.tools.projectWizard.core.*
import org.jetbrains.kotlin.tools.projectWizard.ir.buildsystem.gradle.GradleBinaryExpressionIR
import org.jetbrains.kotlin.tools.projectWizard.ir.buildsystem.gradle.GradleByClassTasksCreateIR
import org.jetbrains.kotlin.tools.projectWizard.ir.buildsystem.gradle.GradleConfigureTaskIR
import org.jetbrains.kotlin.tools.projectWizard.ir.buildsystem.gradle.GradleIRListBuilder
import org.jetbrains.kotlin.tools.projectWizard.plugins.printer.GradlePrinter
import org.jetbrains.kotlin.tools.projectWizard.plugins.projectPath
import org.jetbrains.kotlin.tools.projectWizard.plugins.templates.TemplatesPlugin
import org.jetbrains.kotlin.tools.projectWizard.templates.FileTemplate
import org.jetbrains.kotlin.tools.projectWizard.templates.FileTemplateDescriptor
import java.nio.file.Path

fun SettingsWriter.locateDummyFile(iosTargetName: String, kotlinDirectoryName: String, toModulePath: Path): TaskResult<Unit> {
    val dummyFilePath = Defaults.SRC_DIR / "${iosTargetName}Main" / kotlinDirectoryName / "dummyFile.kt"
    return TemplatesPlugin::addFileTemplate.execute(
        FileTemplate(
            FileTemplateDescriptor("ios/dummyFile.kt", dummyFilePath),
            projectPath / toModulePath
        )
    )
}

fun GradleIRListBuilder.packForXcode(iosTargetName: String) =
    +GradleConfigureTaskIR(GradleByClassTasksCreateIR("packForXcode", "Sync")) {
        "group" assign const("build")
        "mode" createValue GradleBinaryExpressionIR(
            raw { +"System.getenv("; +"CONFIGURATION".quotified; +")" },
            "?:",
            const("DEBUG")
        )
        "framework" createValue raw {
            +"kotlin.targets."
            when (dsl) {
                GradlePrinter.GradleDsl.KOTLIN -> +"""getByName<KotlinNativeTarget>("$iosTargetName")"""
                GradlePrinter.GradleDsl.GROOVY -> +iosTargetName
            }
            +".binaries.getFramework(mode)"
        };

        addRaw { +"inputs.property(${"mode".quotified}, mode)" }
        addRaw("dependsOn(framework.linkTask)")
        "targetDir" createValue new("File", raw("buildDir"), const("xcode-frameworks"))
        addRaw("from({ framework.outputDirectory })")
        addRaw("into(targetDir)")
    }

