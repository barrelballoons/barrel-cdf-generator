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

import gsfc.nssdc.cdf.CDFConstants;
import gsfc.nssdc.cdf.util.CDFTT2000;
import gsfc.nssdc.cdf.CDFException;

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

public class BarrelCDF{
      private CDFFile cdf;
      private String path;
      private int lvl;
      private int lastRec;

   public BarrelCDF(final String p, final int l){
      CDFVar var;
      long min_epoch;
      long max_epoch;

      this.lvl = l;
      this.path = p;

      //get today's date
      DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
      Calendar cal = Calendar.getInstance();
      String date = dateFormat.format(cal.getTime());
         
      try{
         //calculate min and max epochs
         min_epoch = CDFTT2000.fromUTCparts(2012, 00, 01);
         max_epoch = CDFTT2000.fromUTCparts(2015, 11, 31);
         
      }catch(CDFException e){
         System.out.println(e.getMessage());
      }

      //create or open a cdf file
      cdf = new CDFFile(p);

      cdf.attribute("File_naming_convention", "source_datatype_descriptor");
      cdf.attribute("Data_type", "l" + lvl + ">Level-" + lvl);
      cdf.attribute("PI_name", "Robyn Millan");
      cdf.attribute("PI_affiliation","Dartmouth College");
      cdf.attribute("Mission_group", "RBSP");
      cdf.attribute("Project","LWS>Living With a Star>BARREL");
      cdf.attribute("Source_name", "Payload_ID");
      cdf.attribute("Data_version", CDF_Gen.getSetting("rev"));
      cdf.attribute("Discipline", "Space Physics>Magnetospheric Science");
      cdf.attribute("HTTP_LINK","http://barreldata.ucsc.edu");
      cdf.attribute("LINK_TITLE", "BARREL Data Repository");
      cdf.attribute("Generation_date", String.valueOf(date));
      cdf.attribute("Generated_by", "BARREL CDF Generator");
      cdf.attribute(
         "Rules_of_use",  
         "BARREL will make all its scientific data products quickly and " +
         "publicly available but all users are expected to read and follow "+
         "the \"BARREL Mission Data Usage Policy\" which can be found in " +
         "the BARREL data repository or obtained by contacting " + 
         "barrelballoons@gmail.com"
      );
      
      //set all default varialbes
      var = new CDFVar(cdf, "Epoch", CDFConstants.CDF_TIME_TT2000);
      var.attribute("FIELDNAM", "Epoch");
      var.attribute("CATDESC", "Default time");
      var.attribute("VAR_TYPE", "support_data");
      var.attribute("UNITS", "ns");
      var.attribute("SCALETYPE", "linear");
      var.attribute("VALIDMIN", min_epoch);
      var.attribute("VALIDMAX", max_epoch);
      var.attribute("FILLVAL", Long.MIN_VALUE);
      var.attribute("LABLAXIS", "Epoch");
      var.attribute("MONOTON", "INCREASE");
      var.attribute("TIME_BASE", "J2000");
      var.attribute("TIME_SCALE", "Terrestrial Time");
      var.attribute("REFERENCE_POSITION", "Rotating Earch Geoid");

      var = new CDFVar(cdf, "FrameGroup", CDFConstants.CDF_INT4);
      var.attribute("FIELDNAM", "Frame Number");
      var.attribute("CATDESC", "DPU Frame Counter");
      var.attribute("VAR_TYPE", "data");
      var.attribute("DEPEND_0", "Epoch");
      var.attribute("FORMAT", "%u");
      var.attribute("DISPLAY_TYPE", "time_series");
      var.attribute("VALIDMIN", 0);
      var.attribute("VALIDMAX", 2147483647);
      var.attribute("FILLVAL", var.getIstpVal("INT4_FILL"));
      var.attribute("LABLAXIS", "Frame");

      var = new CDFVar(cdf.getCDF(),"Q", CDFConstants.CDF_INT4);
      var.attribute("FIELDNAM", "Data Quality");
      var.attribute("CATDESC", "32bit flag used to indicate data quality");
      var.attribute("VAR_TYPE", "data");
      var.attribute("DEPEND_0", "Epoch");
      var.attribute("FORMAT", "%u");
      var.attribute("SCALETYPE", "linear");
      var.attribute("DISPLAY_TYPE", "time_series");
      var.attribute("VALIDMIN", 0);
      var.attribute("VALIDMAX", 2147483647);
      var.attribute("FILLVAL", var.getIstpVal("INT4_FILL"));
      var.attribute("LABLAXIS", "Q");
   }

   public String getPath(){return path;}

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
