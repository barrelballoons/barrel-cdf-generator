/*
CDFComponents.java

Description:
   Group of classes describing the various "CDFComponents" (which are either a 
   file or variable at this point).

   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
   This file is part of The BARREL CDF Generator.

   The BARREL CDF Generator is free software: you can redistribute it and/or 
   modify it under the terms of the GNU General Public License as published 
   by the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   The BARREL CDF Generator is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License along with 
   The BARREL CDF Generator.  If not, see <http://www.gnu.org/licenses/>.
   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
*/

package edu.ucsc.barrel.cdf_gen;

import gsfc.nssdc.cdf.CDF;
import gsfc.nssdc.cdf.Attribute;
import gsfc.nssdc.cdf.CDFConstants;
import gsfc.nssdc.cdf.CDFException;

import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

abstract public class CDFComponent{
   //create a list of ISTP compliant limits and fill values
   private static final Map<String, Number> ISTP_CONSTANTS;
   static {
      Map<String, Number> map = new HashMap<String, Number>();
      map.put("FLOAT_FILL", -1e31f);
      map.put("DOUBLE_FILL", -1e31);
      map.put("INT1_FILL",-128);
      map.put("INT2_FILL", -32768);
      map.put("INT4_FILL", -2147483648);
      ISTP_CONSTANTS = Collections.unmodifiableMap(map);
   }
   static public Number getIstpVal(String key){
      return ISTP_CONSTANTS.get(key);
   }
}
