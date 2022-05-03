/*
 *     CuboidRegionClear, A spigot plugin that clears a cuboid region
 *     Copyright (C) 2021  dkim19375
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.dkim19375.cuboidregionclear

import me.dkim19375.cuboidregionclear.command.CuboidRegionCmd
import me.dkim19375.cuboidregionclear.command.CuboidRegionTab
import me.dkim19375.cuboidregionclear.data.MainConfigData
import me.dkim19375.cuboidregionclear.manager.RegionClearManager
import me.dkim19375.dkimbukkitcore.function.logInfo
import me.dkim19375.dkimbukkitcore.javaplugin.CoreJavaPlugin
import me.dkim19375.dkimcore.file.YamlFile
import java.io.File
import kotlin.system.measureTimeMillis

class CuboidRegionClear : CoreJavaPlugin() {
    override val defaultConfig = false
    val mainConfig by lazy { YamlFile(MainConfigData, File(dataFolder, "config.yml")) }
    val manager by lazy { RegionClearManager(this) }

    override fun onEnable() {
        logInfo("Successfully enabled ${description.name} in ${
            measureTimeMillis {
                registerConfig(mainConfig)
                registerCommand("cuboidregionclear", CuboidRegionCmd(this), CuboidRegionTab())
                manager
            }
        }ms!")
    }
}