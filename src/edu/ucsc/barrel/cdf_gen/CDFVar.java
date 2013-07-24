/*
CDFVar.java

Description:
   CDFComponent subclass that impements  
   methods for reading and writing CDF variables.

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
import gsfc.nssdc.cdf.CDFException;
import gsfc.nssdc.cdf.CDFConstants;
import gsfc.nssdc.cdf.util.CDFTT2000;
import gsfc.nssdc.cdf.Variable;
import gsfc.nssdc.cdf.Attribute;
import gsfc.nssdc.cdf.Entry;

public class CDFVar extends CDFComponent{
   CDF cdf;
   Variable var;
   String name;
   long var_type;

   public CDFVar(final CDF c, final String n, long t, int r_size, long r_vary){
      cdf = c;
      name = n;
      var_type = t;
      
      try{
         var = Variable.create(
            cdf, name, var_type, 1L, 0L, new long[]{r_size}, 
            r_vary, new long[]{CDFConstants.VARY}
         );
      }catch(CDFException e){
         System.out.println("Could not create variable " + name + ":");
         System.out.println(e.getMessage());
      }
   }
   
   public String getName(){return name;}
   public long getType(){return var_type;}
   
   public void setAttribute(
      final String attr_name, final Object value, long attr_type
   ){
      Attribute attr = getAttribute(attr_name);
      long id;

      try{
         id = var.getID();
         Entry.create(attr, id, attr_type, value);
      }catch(CDFException e){
         System.out.println(
            "Could not create entry '" + value + 
            "' for attribute '" + attr_name + 
            "' in variable '" + name  + "':"
         );
         System.out.println(e.getMessage());
      }
   }

   public void setAttribute(final String attr_name, final Object value){
      long attr_type;

      if(value instanceof Number){
         attr_type = var_type;
      }else{
         attr_type = CDFConstants.CDF_CHAR;
      }

      setAttribute(attr_name, value, attr_type);
   }

   private Attribute getAttribute(final String name){
      Attribute attr;
      long id;
      try{
         //figure out what the id number should be for this attribute
         id = cdf.getAttributeID(name);
         attr = (id != -1L) ? 
            cdf.getAttribute(name) :
            Attribute.create(cdf, name, CDFConstants.VARIABLE_SCOPE);
      }catch(CDFException e){
         attr = null;
         System.out.println("Could not get attribute: ");
         System.out.println(e.getMessage());
      }
      
      return attr;
   }
}
