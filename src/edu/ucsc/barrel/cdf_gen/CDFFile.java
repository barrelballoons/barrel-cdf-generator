/*
CDFFile.java

Description:
   CDFComponent subclass that impements  
   methods for reading and writing CDF files

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
import gsfc.nssdc.cdf.Attribute;
import gsfc.nssdc.cdf.Entry;

import java.io.File;

public class CDFFile extends CDFComponent{
   CDF cdf;

   public CDFFile(final String path){
      //create a new CDF or open an existing one. 
      try{
         if(!(new File(path)).exists()){cdf = CDF.create(path);}
         if(cdf == null){cdf = CDF.open(path);}
      }catch(CDFException e){
         System.out.println(e.getMessage());
      }
   }

   public void setAttribute(final String name, final String value, long id){
      Attribute attr = getAttribute(name);

      try{
         Entry.create(attr, id, CDFConstants.CDF_CHAR, value);
      }catch(CDFException e){
         System.out.println(
            "Could not create entry '" + value + 
            "' for attribute '" + name + "':"
         );
         System.out.println(e.getMessage());
      }
   }
   public void setAttribute(final String name, final String value){
      Attribute attr = getAttribute(name);
      long entry = (attr == null) ? 0 : attr.getNumEntries();
      setAttribute(name, value, entry);
   }

   public void close(){
      try{cdf.close();}
      catch(CDFException e){
         System.out.println("Could not close CDF file:");
         System.out.println(e.getMessage());
      }
   }

   public CDF getCDF(){return cdf;}

   private Attribute getAttribute(final String name){
      Attribute attr;
      long id;
      try{
         //figure out what the id number should be for this attribute
         id = cdf.getAttributeID(name);
         attr = (id != -1L) ? 
            cdf.getAttribute(name) :
            Attribute.create(cdf, name, CDFConstants.GLOBAL_SCOPE);
      }catch(CDFException e){
         attr = null;
         System.out.println("Could not get attribute: ");
         System.out.println(e.getMessage());
      }
      
      return attr;
   }

}
