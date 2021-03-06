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

package me.dkim19375.cuboidregionclear.util

import java.time.DayOfWeek

fun DayOfWeek.getCalendarValue(): Int = when (this) {
    DayOfWeek.SUNDAY -> 1
    DayOfWeek.MONDAY -> 2
    DayOfWeek.TUESDAY -> 3
    DayOfWeek.WEDNESDAY -> 4
    DayOfWeek.THURSDAY -> 5
    DayOfWeek.FRIDAY -> 6
    DayOfWeek.SATURDAY -> 7
}