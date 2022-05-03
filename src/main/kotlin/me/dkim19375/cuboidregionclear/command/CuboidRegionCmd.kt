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

package me.dkim19375.cuboidregionclear.command

import me.dkim19375.cuboidregionclear.CuboidRegionClear
import me.dkim19375.cuboidregionclear.util.ErrorMessages
import me.dkim19375.cuboidregionclear.util.Permissions
import me.dkim19375.cuboidregionclear.util.hasPermission
import me.dkim19375.cuboidregionclear.util.sendHelpMessage
import me.dkim19375.cuboidregionclear.util.sendMessage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class CuboidRegionCmd(private val plugin: CuboidRegionClear) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission(Permissions.COMMAND)) {
            sender.sendMessage(ErrorMessages.NO_PERMISSION)
            return true
        }
        if (args.isEmpty()) {
            sender.sendHelpMessage(label)
            return true
        }
        when (args[0]) {
            "help" -> {
                sender.sendHelpMessage(label, args.getOrNull(1)?.toIntOrNull()?.coerceAtLeast(1) ?: 1)
                return true
            }
            "reload" -> {
                plugin.reloadConfig()
                sender.sendMessage(Component.text("Reloaded the config file!").color(NamedTextColor.GREEN))
                return true
            }
            else -> {
                sender.sendMessage(ErrorMessages.INVALID_ARG)
                return true
            }
        }
    }
}