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
import java.util.Map;
import java.util.HashMap;

public class CDFFile implements CDFComponent{
   private static long type = CDFConstants.CDF_;
   private CDF cdf;
   private String name;
   private String path;
   private Map<String, CDFVar> vars;

   public CDFFile(final String p){
      this.path = p;

      String[] path_parts = p.split("/");
      this.name = path_parts[path_parts.length - 1];

      //create a new CDF or open an existing one. 
      try{
         if(!(new File(p)).exists()){this.cdf = CDF.create(path);}
         if(this.cdf == null){this.cdf = CDF.open(path);}
      }catch(CDFException e){
         System.out.println("Could not create/open CDF file '" + path + "':");
         System.out.println(e.getMessage());
      }

      //create a map in which to store teh CDFVariables
      vars = new HashMap<String, CDFVar>();
   }
   
   public CDF getCDF(){return this.cdf;}
   public long getID(){return this.cdf.getID();}
   public long getType(){return this.type;} 
   public String getName(){return this.name;}
   public String getPath(){return this.path;}

   //forwarding functions for creating and selecting attributes in this CDF file
   public CDFAttribute attribute(
      final String name, final String value, long type
   ){
      CDFAttribute attr = new CDFAttribute(this, name, value, type);

      return attr;
   }
   public CDFAttribute attribute(final String name, final String value){
      CDFAttribute attr = new CDFAttribute(this, name, value);

      return attr;
   }
   public CDFAttribute attribute(final String name){
      CDFAttribute attr = new CDFAttribute(this, name);

      return attr;
   }
   
   //functions for over writing current attribute values in this CDF file
   public void editAttribute(final String name, final String value, long entry){
      CDFAttribute attr = new CDFAttribute(this, name);
      attr.setValue(value, entry);
   }
   public void editAttribute(final String name, final String value){
      //assume the last entry for this attribute
      long entry = attribute(name).getNumEntries() - 1L;
      editAttribute(name, value, entry);
   }
   
   //accessor for the vars HashMap
   public CDFVar getVar(String name){
      return vars.get(name);
   }
   public void addVar(String name, CDFVar var){
      vars.put(name, var);
   }

   //functions for writing data to its variables
   public void addData(String name, short[] data){
      CDFVar var = vars.get(name);
      var.writeData(name, data);
   }
   public void addData(String name, int[] data){
      CDFVar var = vars.get(name);
      var.writeData(name, data);
   }
   public void addData(String name, long[] data){
      CDFVar var = vars.get(name);
      var.writeData(name, data);
   }
   public void addData(String name, float[] data){
      CDFVar var = vars.get(name);
      var.writeData(name, data);
   }
   public void addData(String name, double[] data){
      CDFVar var = vars.get(name);
      var.writeData(name, data);
   }
   public void addData(String name, String[] data){
      CDFVar var = vars.get(name);
      var.writeData(name, data);
   }
   public void addData(String name, short[][] data){
      CDFVar var = vars.get(name);
      var.writeData(name, data);
   }
   public void addData(String name, int[][] data){
      CDFVar var = vars.get(name);
      var.writeData(name, data);
   }
   public void addData(String name, long[][] data){
      CDFVar var = vars.get(name);
      var.writeData(name, data);
   }
   public void addData(String name, float[][] data){
      CDFVar var = vars.get(name);
      var.writeData(name, data);
   }
   public void addData(String name, double[][] data){
      CDFVar var = vars.get(name);
      var.writeData(name, data);
   }
   public void addData(String name, String[][] data){
      CDFVar var = vars.get(name);
      var.writeData(name, data);
   }

   //close the CDF file. This must be done before the program exits
   public void close(){
      try{cdf.close();}
      catch(CDFException e){
         System.out.println("Could not close CDF file '" + this.name + "':");
         System.out.println(e.getMessage());
      }
   }
}
