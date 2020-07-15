/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.tools.projectWizard.moduleConfigurators

import org.jetbrains.annotations.NonNls
import org.jetbrains.kotlin.tools.projectWizard.KotlinNewProjectWizardBundle
import org.jetbrains.kotlin.tools.projectWizard.ir.buildsystem.KotlinBuildSystemPluginIR
import org.jetbrains.kotlin.tools.projectWizard.plugins.kotlin.ModulesToIrConversionData
import org.jetbrains.kotlin.tools.projectWizard.settings.buildsystem.Module
import org.jetbrains.kotlin.tools.projectWizard.settings.buildsystem.ModuleKind

object MppModuleConfigurator : ModuleConfigurator {
    override val moduleKind = ModuleKind.multiplatform

    @NonNls
    override val suggestedModuleName = "shared"

    @NonNls
    override val id = "multiplatform"
    override val text = KotlinNewProjectWizardBundle.message("module.configurator.mpp")
    override val canContainSubModules = true

    override fun createKotlinPluginIR(configurationData: ModulesToIrConversionData, module: Module): KotlinBuildSystemPluginIR? =
        KotlinBuildSystemPluginIR(
            KotlinBuildSystemPluginIR.Type.multiplatform,
            version = configurationData.kotlinVersion
        )
}
