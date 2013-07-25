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

public class BarrelCDF extends CDFFile{

   public BarrelCDF(final String p, final int l){
      super(p);

      defaultAttributes(l);
      defaultVariables();
   }

   private void defaultAttributes(int lvl){
      //get today's date
      DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
      Calendar cal = Calendar.getInstance();
      String date = dateFormat.format(cal.getTime());
         
      this.attribute("File_naming_convention", "source_datatype_descriptor");
      this.attribute("Data_type", "l" + lvl + ">Level-" + lvl);
      this.attribute("PI_name", "Robyn Millan");
      this.attribute("PI_affiliation","Dartmouth College");
      this.attribute("Mission_group", "RBSP");
      this.attribute("Project","LWS>Living With a Star>BARREL");
      this.attribute("Source_name", "Payload_ID");
      this.attribute("Data_version", CDF_Gen.getSetting("rev"));
      this.attribute("Discipline", "Space Physics>Magnetospheric Science");
      this.attribute("HTTP_LINK","http://barreldata.ucsc.edu");
      this.attribute("LINK_TITLE", "BARREL Data Repository and Tools");
      this.attribute(
         "LINK_TEXT", 
         "Access to all releases of BARREL data and links to the Science " +
         "Operation Center tools."
      );
      this.attribute("Generation_date", String.valueOf(date));
      this.attribute("Generated_by", "BARREL CDF Generator");
      this.attribute(
         "Rules_of_use",  
         "BARREL will make all its scientific data products quickly and " +
         "publicly available but all users are expected to read and follow "+
         "the \"BARREL Mission Data Usage Policy\" which can be found in " +
         "the BARREL data repository or obtained by contacting " + 
         "barrelballoons@gmail.com"
      );
   }

   private void defaultVariables(){
      CDFVar var;
      long min_epoch = 0L;
      long max_epoch = 0L;

      try{
         //calculate min and max epochs
         min_epoch = CDFTT2000.fromUTCparts(2012, 00, 01);
         max_epoch = CDFTT2000.fromUTCparts(2015, 11, 31);
         
      }catch(CDFException e){
         System.out.println(e.getMessage());
      }

      var = new CDFVar(this, "Epoch", CDFConstants.CDF_TIME_TT2000);
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

      var = new CDFVar(this, "FrameGroup", CDFConstants.CDF_INT4);
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

      var = new CDFVar(this, "Q", CDFConstants.CDF_INT4);
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
}
