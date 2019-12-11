/* 
 * Copyright (C) 2018 arx iT
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

if (!String.prototype.repeat) {
  String.prototype.repeat = function (count) {
    "use strict";
    if (this === null)
      throw new TypeError("ne peut convertir " + this + " en objet");
    var str = "" + this;
    count = +count;
    if (count !== count)
      count = 0;
    if (count < 0)
      throw new RangeError("le nombre de répétitions doit être positif");
    if (count === Infinity)
      throw new RangeError("le nombre de répétitions doit être inférieur à l'infini");
    count = Math.floor(count);
    if (str.length === 0 || count === 0)
      return "";
    // En vérifiant que la longueur résultant est un entier sur 31-bit
    // cela permet d'optimiser l'opération.
    // La plupart des navigateurs (août 2014) ne peuvent gérer des
    // chaînes de 1 << 28 caractères ou plus. Ainsi :
    if (str.length * count >= 1 << 28)
      throw new RangeError("le nombre de répétitions ne doit pas dépasser la taille de chaîne maximale");
    var rpt = "";
    for (var i = 0; i < count; i++) {
      rpt += str;
    }
    return rpt;
  }
}
