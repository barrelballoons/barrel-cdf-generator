/*
BarrelCDF.java

Description:
   Superclass for creating individual CDF files.

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

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;
import java.util.Arrays;

public class BarrelCDF implements CDFConstants{
      private CDF cdf;
      private String path;
      private int lvl;
      private int lastRec;

   public BarrelCDF(final String p, final int l){
      path = p;
      lvl = l;
      
      try{
         //create a new CDF or open an existing one. 
         if(!(new File(path)).exists()){cdf = CDF.create(path);}
         if(cdf == null){cdf = CDF.open(path);}

         addGlobalAtts();
         addVars();
      }catch(CDFException e){
         System.out.println(e.getMessage());
      }
   }

   private void addGlobalAtts() throws CDFException{
      //get today's date
      DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
      Calendar cal = Calendar.getInstance();
      String date = dateFormat.format(cal.getTime());
      
      //fill global_attrs HashMap with attribute info used by all CDFs
      setAttribute("File_naming_convention", "source_datatype_descriptor");
      setAttribute("Data_type", "l" + lvl + ">Level-" + lvl);
      setAttribute("PI_name", "Robyn Millan");
      setAttribute("PI_affiliation","Dartmouth College");
      setAttribute("Mission_group", "RBSP");
      setAttribute("Project","LWS>Living With a Star>BARREL");
      setAttribute("Source_name", "Payload_ID");
      setAttribute("Data_version", CDF_Gen.getSetting("rev"));
      setAttribute("Discipline", "Space Physics>Magnetospheric Science");
      setAttribute("HTTP_LINK","http://barreldata.ucsc.edu");
      setAttribute("LINK_TITLE", "BARREL Data Repository");
      setAttribute("Generation_date", String.valueOf(date));
      setAttribute("Generated_by", "BARREL CDF Generator");
      setAttribute(
         "Rules_of_use",  
         "BARREL will make all its scientific data products quickly and " +
         "publicly available but all users are expected to read and follow " +
         "the \"BARREL Mission Data Usage Policy\" which can be found in " +
         "the BARREL data repository or obtained by contacting " + 
         "barrelballoons@gmail.com"
      );
   }

   public void addVars() throws CDFException{
      //create variables used by all CDFs
      Variable var;
      long id;

      //calculate min and max epochs
      long min_epoch = CDFTT2000.fromUTCparts(2012, 00, 01);
      long max_epoch = CDFTT2000.fromUTCparts(2015, 11, 31);
      

      var = 
         Variable.create(
            cdf, "Epoch", CDF_TIME_TT2000, 1L, 0L, new  long[] {1}, 
            VARY, new long[] {NOVARY}
      ); 
      id = var.getID();
      setAttribute("FIELDNAM", "Epoch", VARIABLE_SCOPE, id);
      setAttribute("CATDESC", "Default time", VARIABLE_SCOPE, id);
      setAttribute("VAR_TYPE", "support_data", VARIABLE_SCOPE, id);
      setAttribute("UNITS", "ns", VARIABLE_SCOPE, id);
      setAttribute("SCALETYPE", "linear", VARIABLE_SCOPE, id);
      setAttribute("VALIDMIN", min_epoch, VARIABLE_SCOPE, id, CDF_TIME_TT2000);
      setAttribute("VALIDMAX", max_epoch, VARIABLE_SCOPE, id, CDF_TIME_TT2000);
      setAttribute(
         "FILLVAL", Long.MIN_VALUE, VARIABLE_SCOPE, id, CDF_TIME_TT2000
      );
      setAttribute("LABLAXIS", "Epoch", VARIABLE_SCOPE, id);
      setAttribute("MONOTON", "INCREASE", VARIABLE_SCOPE, id);
      setAttribute("TIME_BASE", "J2000", VARIABLE_SCOPE, id);
      setAttribute("TIME_SCALE", "Terrestrial Time", VARIABLE_SCOPE, id);
      setAttribute(
         "REFERENCE_POSITION", "Rotating Earch Geoid", VARIABLE_SCOPE, id
      );

      var = 
         Variable.create(
            cdf, "FrameGroup", CDF_INT4, 1L, 0L, new  long[] {1}, 
            VARY, new long[] {NOVARY}
      );
      id = var.getID();
      setAttribute("FIELDNAM", "Frame Number", VARIABLE_SCOPE, id);
      setAttribute("CATDESC", "DPU Frame Counter", VARIABLE_SCOPE, id);
      setAttribute("VAR_TYPE", "data", VARIABLE_SCOPE, id);
      setAttribute("DEPEND_0", "Epoch", VARIABLE_SCOPE, id);
      setAttribute("FORMAT", "%u", VARIABLE_SCOPE, id);
      setAttribute("DISPLAY_TYPE", "time_series", VARIABLE_SCOPE, id);
      setAttribute("VALIDMIN", 0, VARIABLE_SCOPE, id, CDF_INT4);
      setAttribute("VALIDMAX", 2147483647, VARIABLE_SCOPE, id, CDF_INT4);
      setAttribute(
         "FILLVAL", Constants.INT4_FILL, VARIABLE_SCOPE, id, CDF_INT4
      );
      setAttribute("LABLAXIS", "Frame", VARIABLE_SCOPE, id);

      var = 
         Variable.create(
            cdf, "Q", CDF_INT4, 1L, 0L, new  long[] {1}, 
            VARY, new long[] {NOVARY}
      );
      id = var.getID();
      setAttribute("FIELDNAM", "Data Quality", VARIABLE_SCOPE, id);
      setAttribute(
         "CATDESC", "32bit flag used to indicate data quality", 
         VARIABLE_SCOPE, id
      );
      setAttribute("VAR_TYPE", "data", VARIABLE_SCOPE, id);
      setAttribute("DEPEND_0", "Epoch", VARIABLE_SCOPE, id);
      setAttribute("FORMAT", "%u", VARIABLE_SCOPE, id);
      setAttribute("SCALETYPE", "linear", VARIABLE_SCOPE, id);
      setAttribute("DISPLAY_TYPE", "time_series", VARIABLE_SCOPE, id);
      setAttribute("VALIDMIN", 0, VARIABLE_SCOPE, id, CDF_INT4);
      setAttribute("VALIDMAX", 2147483647, VARIABLE_SCOPE, id, CDF_INT4);
      setAttribute(
         "FILLVAL", Constants.INT4_FILL, VARIABLE_SCOPE, id, CDF_INT4
      );
      setAttribute("LABLAXIS", "Q", VARIABLE_SCOPE, id);
   }

   public CDF getCDF(){return cdf;}
   public String getPath(){return path;}

   public void setAttribute(
      final String key, final Object val, final long scope, 
      final long id, final long type
   )throws CDFException{
      Attribute attr;

      //either create or get the attirbute
      try{
         attr = Attribute.create(
            cdf, String.valueOf(key), scope 
         );
      }catch(CDFException e){
         if(e.getCurrentStatus() == ATTR_EXISTS){
            attr = cdf.getAttribute(key);
         }else{
            System.out.println("Error getting CDF attriubute: " + key);
            System.out.println("Error code = " + e.getCurrentStatus());
            return;
         }
      }
      
      Entry.create(attr, id, type, val);
   }
   public void setAttribute(
      final String key, final Object val, final long scope, final long id
   )throws CDFException{
      long type;

      //figure out what type of variable to store this as
      if(val instanceof String){type = CDF_CHAR;}
      else{
         double test_val = Double.valueOf(val.toString());
         if(test_val == (long)test_val){
            if(test_val < Integer.MAX_VALUE && test_val > Integer.MIN_VALUE){
               type = CDF_INT4;
            }
            else{type = CDF_INT8;}
         }else{
            type = CDF_DOUBLE;
         }
      }

      setAttribute(key, val, scope, id, type);
   }

   public void setAttribute(
      final String key, final Object val
   )throws CDFException{
      //attributes without id or scope are assumed to be single instance globals
      setAttribute(key, val, GLOBAL_SCOPE, 0);
   }

   public void writeData(String name, short[] data) throws CDFException{
      Variable var = this.cdf.getVariable(name);
      long start = var.getNumWrittenRecords();
      long size = data.length;
      var.putHyperData(
         start, size, 1, 
         new long[] {0}, new long[] {1}, new long[] {1},
         data
      );
   }
   public void writeData(String name, int[] data) throws CDFException{
      Variable var = this.cdf.getVariable(name);
      long start = var.getNumWrittenRecords();
      long size = data.length;
      var.putHyperData(
         start, size, 1, 
         new long[] {0}, new long[] {1}, new long[] {1},
         data
      );
   }
   public void writeData(String name, long[] data) throws CDFException{
      Variable var = this.cdf.getVariable(name);
      long start = var.getNumWrittenRecords();
      long size = data.length;
      var.putHyperData(
         start, size, 1, 
         new long[] {0}, new long[] {1}, new long[] {1},
         data
      );
   }
   public void writeData(String name, float[] data) throws CDFException{
      Variable var = this.cdf.getVariable(name);
      long start = var.getNumWrittenRecords();
      long size = data.length;
      var.putHyperData(
         start, size, 1, 
         new long[] {0}, new long[] {1}, new long[] {1},
         data
      );
   }
   public void writeData(String name, double[] data) throws CDFException{
      Variable var = this.cdf.getVariable(name);
      long start = var.getNumWrittenRecords();
      long size = data.length;
      var.putHyperData(
         start, size, 1, 
         new long[] {0}, new long[] {1}, new long[] {1},
         data
      );
   }
   public void writeData(String name, String[] data) throws CDFException{
      Variable var = this.cdf.getVariable(name);
      long start = var.getNumWrittenRecords();
      long size = data.length;
      var.putHyperData(
         start, size, 1, 
         new long[] {0}, new long[] {1}, new long[] {1},
         data
      );
   }
   public void writeData(String name, int[][] data) throws CDFException{
      Variable var = this.cdf.getVariable(name);
      long start = var.getNumWrittenRecords();
      long size = data.length;
      long[] dimCnts = {data[0].length, 1};
      var.putHyperData(
         start, size, 1, 
         new long[] {0}, dimCnts, new long[] {1},
         data
      );
   }
   public void writeData(String name, long[][] data) throws CDFException{
      Variable var = this.cdf.getVariable(name);
      long start = var.getNumWrittenRecords();
      long size = data.length;
      long[] dimCnts = {data[0].length, 1};
      var.putHyperData(
         start, size, 1, 
         new long[] {0}, dimCnts, new long[] {1},
         data
      );
   }
   public void writeData(String name, float[][] data) throws CDFException{
      Variable var = this.cdf.getVariable(name);
      long start = var.getNumWrittenRecords();
      long size = data.length;
      long[] dimCnts = {data[0].length, 1};
      var.putHyperData(
         start, size, 1, 
         new long[] {0}, dimCnts, new long[] {1},
         data
      );
   }
   public void writeData(String name, double[][] data) throws CDFException{
      Variable var = this.cdf.getVariable(name);
      long start = var.getNumWrittenRecords();
      long size = data.length;
      long[] dimCnts = {data[0].length, 1};
      var.putHyperData(
         start, size, 1, 
         new long[] {0}, dimCnts, new long[] {1},
         data
      );
   }

   public void close(){
      try{
         cdf.close();
      }catch(CDFException e){
         System.out.println(e.getMessage());
      }
   }
}
