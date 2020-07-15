/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.tools.projectWizard.moduleConfigurators


import org.jetbrains.kotlin.tools.projectWizard.core.Reader
import org.jetbrains.kotlin.tools.projectWizard.ir.buildsystem.BuildSystemIR
import org.jetbrains.kotlin.tools.projectWizard.plugins.kotlin.ModulesToIrConversionData
import org.jetbrains.kotlin.tools.projectWizard.settings.buildsystem.Module
import org.jetbrains.kotlin.tools.projectWizard.settings.javaPackage

const val MODULE_WITH_MANIFEST = "androidMain"

fun settingsForTemplateRendering(
    configurationData: ModulesToIrConversionData,
    module: Module, ): Map<String, String?> {
    val javaPackage = module.javaPackage(configurationData.pomIr)
    return mapOf("package" to javaPackage.asCodePackage())
}

fun addDependencies(reader: Reader, configurationData: ModulesToIrConversionData, module: Module): List<BuildSystemIR> =
   emptyList()