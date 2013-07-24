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
      private CDFFile cdf;
      private String path;
      private int lvl;
      private int lastRec;

   public BarrelCDF(final String p, final int l){
      CDFVar var;
      long id;

      lvl = l;
      path = p;

      try{
         //calculate min and max epochs
         long min_epoch = CDFTT2000.fromUTCparts(2012, 00, 01);
         long max_epoch = CDFTT2000.fromUTCparts(2015, 11, 31);
         

         //get today's date
         DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
         Calendar cal = Calendar.getInstance();
         String date = dateFormat.format(cal.getTime());
         
         //create or open a cdf and fill it with default global variables
         cdf = new CDFFile(path);

         cdf.setAttribute(
            "File_naming_convention", "source_datatype_descriptor"
         );
         cdf.setAttribute("Data_type", "l" + lvl + ">Level-" + lvl);
         cdf.setAttribute("PI_name", "Robyn Millan");
         cdf.setAttribute("PI_affiliation","Dartmouth College");
         cdf.setAttribute("Mission_group", "RBSP");
         cdf.setAttribute("Project","LWS>Living With a Star>BARREL");
         cdf.setAttribute("Source_name", "Payload_ID");
         cdf.setAttribute("Data_version", CDF_Gen.getSetting("rev"));
         cdf.setAttribute(
            "Discipline", "Space Physics>Magnetospheric Science"
         );
         cdf.setAttribute("HTTP_LINK","http://barreldata.ucsc.edu");
         cdf.setAttribute("LINK_TITLE", "BARREL Data Repository");
         cdf.setAttribute("Generation_date", String.valueOf(date));
         cdf.setAttribute("Generated_by", "BARREL CDF Generator");
         cdf.setAttribute(
            "Rules_of_use",  
            "BARREL will make all its scientific data products quickly and " +
            "publicly available but all users are expected to read and follow "+
            "the \"BARREL Mission Data Usage Policy\" which can be found in " +
            "the BARREL data repository or obtained by contacting " + 
            "barrelballoons@gmail.com"
         );
         
         //set all default varialbes
         var = new CDFVar(cdf.getCDF(),"Epoch", CDF_TIME_TT2000, 1, VARY);
         var.setAttribute("FIELDNAM", "Epoch");
         var.setAttribute("CATDESC", "Default time");
         var.setAttribute("VAR_TYPE", "support_data");
         var.setAttribute("UNITS", "ns");
         var.setAttribute("SCALETYPE", "linear");
         var.setAttribute("VALIDMIN", min_epoch, CDF_TIME_TT2000);
         var.setAttribute("VALIDMAX", max_epoch, CDF_TIME_TT2000);
         var.setAttribute("FILLVAL", Long.MIN_VALUE, CDF_TIME_TT2000);
         var.setAttribute("LABLAXIS", "Epoch");
         var.setAttribute("MONOTON", "INCREASE");
         var.setAttribute("TIME_BASE", "J2000");
         var.setAttribute("TIME_SCALE", "Terrestrial Time");
         var.setAttribute("REFERENCE_POSITION", "Rotating Earch Geoid");

         var = new CDFVar(cdf.getCDF(),"FrameGroup", CDF_INT4, 1, VARY);
         var.setAttribute("FIELDNAM", "Frame Number");
         var.setAttribute("CATDESC", "DPU Frame Counter");
         var.setAttribute("VAR_TYPE", "data");
         var.setAttribute("DEPEND_0", "Epoch");
         var.setAttribute("FORMAT", "%u");
         var.setAttribute("DISPLAY_TYPE", "time_series");
         var.setAttribute("VALIDMIN", 0);
         var.setAttribute("VALIDMAX", 2147483647);
         var.setAttribute("FILLVAL", var.getIstpVal("INT4_FILL"));
         var.setAttribute("LABLAXIS", "Frame");

         var = new CDFVar(cdf.getCDF(),"Q", CDF_INT4, 1, VARY);
         var.setAttribute("FIELDNAM", "Data Quality");
         var.setAttribute(
            "CATDESC", "32bit flag used to indicate data quality"
         );
         var.setAttribute("VAR_TYPE", "data");
         var.setAttribute("DEPEND_0", "Epoch");
         var.setAttribute("FORMAT", "%u");
         var.setAttribute("SCALETYPE", "linear");
         var.setAttribute("DISPLAY_TYPE", "time_series");
         var.setAttribute("VALIDMIN", 0);
         var.setAttribute("VALIDMAX", 2147483647);
         var.setAttribute("FILLVAL", var.getIstpVal("INT4_FILL"));
         var.setAttribute("LABLAXIS", "Q");

      }catch(CDFException e){
         System.out.println(e.getMessage());
      }
   }

   public CDF getCDF(){return cdf.getCDF();}
   public String getPath(){return path;}

   public void setAttribute(
      final String key, final Object val, final long scope, 
      final long id, final long type
   )throws CDFException{
      Attribute attr;

      //either create or get the attirbute
      try{
         attr = Attribute.create(
            cdf.getCDF(), String.valueOf(key), scope 
         );
      }catch(CDFException e){
         if(e.getCurrentStatus() == ATTR_EXISTS){
            attr = cdf.getCDF().getAttribute(key);
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
      Variable var = cdf.getCDF().getVariable(name);
      long start = var.getNumWrittenRecords();
      long size = data.length;
      var.putHyperData(
         start, size, 1, 
         new long[] {0}, new long[] {1}, new long[] {1},
         data
      );
   }
   public void writeData(String name, int[] data) throws CDFException{
      Variable var = cdf.getCDF().getVariable(name);
      long start = var.getNumWrittenRecords();
      long size = data.length;
      var.putHyperData(
         start, size, 1, 
         new long[] {0}, new long[] {1}, new long[] {1},
         data
      );
   }
   public void writeData(String name, long[] data) throws CDFException{
      Variable var = this.cdf.getCDF().getVariable(name);
      long start = var.getNumWrittenRecords();
      long size = data.length;
      var.putHyperData(
         start, size, 1, 
         new long[] {0}, new long[] {1}, new long[] {1},
         data
      );
   }
   public void writeData(String name, float[] data) throws CDFException{
      Variable var = this.cdf.getCDF().getVariable(name);
      long start = var.getNumWrittenRecords();
      long size = data.length;
      var.putHyperData(
         start, size, 1, 
         new long[] {0}, new long[] {1}, new long[] {1},
         data
      );
   }
   public void writeData(String name, double[] data) throws CDFException{
      Variable var = this.cdf.getCDF().getVariable(name);
      long start = var.getNumWrittenRecords();
      long size = data.length;
      var.putHyperData(
         start, size, 1, 
         new long[] {0}, new long[] {1}, new long[] {1},
         data
      );
   }
   public void writeData(String name, String[] data) throws CDFException{
      Variable var = this.cdf.getCDF().getVariable(name);
      long start = var.getNumWrittenRecords();
      long size = data.length;
      var.putHyperData(
         start, size, 1, 
         new long[] {0}, new long[] {1}, new long[] {1},
         data
      );
   }
   public void writeData(String name, int[][] data) throws CDFException{
      Variable var = this.cdf.getCDF().getVariable(name);
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
      Variable var = this.cdf.getCDF().getVariable(name);
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
      Variable var = this.cdf.getCDF().getVariable(name);
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
      Variable var = this.cdf.getCDF().getVariable(name);
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
         cdf.getCDF().close();
      }catch(CDFException e){
         System.out.println(e.getMessage());
      }
   }
}
