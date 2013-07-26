/*
Pps.java

Description:
   Creates Magnetometer CDF files.

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

public class Pps{
   private BarrelCDF cdf;
   private CDFVar var;
   private String path;
   private int date, lvl;

   public Pps(final String p, final int d, final int l){
      cdf = new BarrelCDF(p, l);
      
      this.path = p;
      this.date = d;
      this.lvl = l;

      addPpsGlobalAtts();
      addPpsVars();
   }

   private void addPpsGlobalAtts(){
      //Set global attributes specific to this type of CDF
      this.cdf.attribute(
         "Logical_source_description", "Pulse Per Second"
      );
      this.cdf.attribute(
         "TEXT", 
         "Number of milliseconds into the frame when " + 
         "the GPS pulse per second arrived."
      );
      this.cdf.attribute("Instrument_type", "GPS");
      this.cdf.attribute("Descriptor", "GPS");
      this.cdf.attribute("Time_resolution", "1Hz");
      this.cdf.attribute("Logical_source", "payload_id_l" + lvl  + "_gps");
      this.cdf.attribute(
         "Logical_file_id",
         "payload_id_l" + lvl  + "_gps_20" + date  + 
         "_V" + CDF_Gen.getSetting("rev")
      );
   }

   private void addPpsVars(){
      var = new CDFVar(cdf, "GPS_PPS", CDFConstants.CDF_INT2);
      var.attribute("FIELDNAM", "Pulse Per Second");
      var.attribute(
         "CATDESC", "Milliseconds before GPS pulse arrived." 
      );
      var.attribute("VAR_TYPE", "data");
      var.attribute("DEPEND_0", "Epoch");
      var.attribute("FORMAT", "%i");
      var.attribute("UNITS", "ms");
      var.attribute("SCALETYP", "linear");
      var.attribute("DISPLAY_TYPE", "time_series");
      var.attribute("VALIDMIN", 0);
      var.attribute("VALIDMAX", 1000);
      var.attribute("FILLVAL", Constants.INT2_FILL);
      var.attribute("LABLAXIS", "PPS");

      var = new CDFVar(cdf, "Version", CDFConstants.CDF_INT2);
      var.attribute("FIELDNAM", "Software Version");
      var.attribute("CATDESC", "Software Version.");
      var.attribute("VAR_TYPE", "data");
      var.attribute("DEPEND_0", "Epoch");
      var.attribute("FORMAT", "%i");
      var.attribute("SCALETYP", "linear");
      var.attribute("DISPLAY_TYPE", "time_series");
      var.attribute("VALIDMIN", 0);
      var.attribute("VALIDMAX", 32);
      var.attribute("FILLVAL", Constants.INT2_FILL);
      var.attribute("LABLAXIS", "Version");

      var = new CDFVar(cdf, "Payload_ID", CDFConstants.CDF_INT2);
      var.attribute("FIELDNAM", "Payload ID");
      var.attribute(
         "CATDESC", "ID transmitted by the payload's DPU."
      );
      var.attribute("VAR_TYPE", "data");
      var.attribute("DEPEND_0", "Epoch");
      var.attribute("FORMAT", "%i");
      var.attribute("SCALETYP", "linear");
      var.attribute("DISPLAY_TYPE", "time_series");
      var.attribute("VALIDMIN", 0);
      var.attribute("VALIDMAX", 64);
      var.attribute("FILLVAL", Constants.INT2_FILL);
      var.attribute("LABLAXIS", "ID");
   }

   public void close(){
      this.cdf.close();
   }
}
