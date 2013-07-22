/*
Ephm.java

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

public class Ephm extends BarrelCDF{
   private CDF cdf;
   private Variable var;
   private long id;
   private String path;
   private int date, lvl;

   public Ephm(final String p, final int d, final int l){
      super(p, l);
      path = p;
      date = d;
      lvl = l;

      try{
         cdf = super.getCDF();
      
         addEphmGlobalAtts();
         addEphmVars();
      }catch(CDFException e){
         System.out.println(e.getMessage());
      }
   }

   private void addEphmGlobalAtts() throws CDFException{
      //Set global attributes specific to this type of CDF
      setAttribute(
         "Logical_source_description", "Coordinates"
      );
      setAttribute(
         "TEXT", 
         "Geographic and magnetic corrdinates." 
      );
      setAttribute("Instrument_type", "GPS");
      setAttribute("Descriptor", "ephm>EPHeMeris");
      setAttribute("Time_resolution", "4s");
      setAttribute("Logical_source", "payload_id_l" + lvl  + "_ephm");
      setAttribute(
         "Logical_file_id",
         "payload_id_l" + lvl  + "_ephm_20" + date  +
         "_V" + CDF_Gen.getSetting("rev")
      );
   }

   private void addEphmVars() throws CDFException{
      var = 
         Variable.create(
            cdf, "GPS_Lat", CDF_FLOAT, 1L, 0L, new  long[] {1}, 
            VARY, new long[] {NOVARY}
         );   
      id = var.getID();
      setAttribute("FIELDNAM", "GPS_Lat", VARIABLE_SCOPE, id);
      setAttribute(
         "CATDESC", "GPS Latitude returned every four seconds.", 
         VARIABLE_SCOPE, id
      );
      setAttribute(
         "VAR_NOTES", 
         "Converted from raw int value by multiplying by scaling factor " +
         "8.38190317154 * 10^-8", 
         VARIABLE_SCOPE, id
      );
      setAttribute("VAR_TYPE", "data", VARIABLE_SCOPE, id);
      setAttribute("DEPEND_0", "Epoch", VARIABLE_SCOPE, id);
      setAttribute("FORMAT", "%f", VARIABLE_SCOPE, id);
      setAttribute("UNITS", "deg.", VARIABLE_SCOPE, id);
      setAttribute("SCALETYP", "linear", VARIABLE_SCOPE, id);
      setAttribute("DISPLAY_TYPE", "time_series", VARIABLE_SCOPE, id);
      setAttribute("VALIDMIN", -180f, VARIABLE_SCOPE, id, CDF_FLOAT);
      setAttribute("VALIDMAX", 180f, VARIABLE_SCOPE, id, CDF_FLOAT);
      setAttribute(
         "FILLVAL", Constants.FLOAT_FILL, VARIABLE_SCOPE, id, CDF_FLOAT
      );
      setAttribute("LABLAXIS", "Lat", VARIABLE_SCOPE, id);

      var = 
         Variable.create(
            cdf, "GPS_Lon", CDF_FLOAT, 1L, 0L, new  long[] {1}, 
            VARY, new long[] {NOVARY}
         );   
      id = var.getID();
      setAttribute("FIELDNAM", "GPS_Lon", VARIABLE_SCOPE, id);
      setAttribute(
         "CATDESC", "GPS Longitude returned every four seconds.", 
         VARIABLE_SCOPE, id
      );
      setAttribute(
         "VAR_NOTES", 
         "Converted from raw int value by multiplying by scaling factor " +
         "8.38190317154 * 10^-8", 
         VARIABLE_SCOPE, id
      );
      setAttribute("VAR_TYPE", "data", VARIABLE_SCOPE, id);
      setAttribute("DEPEND_0", "Epoch", VARIABLE_SCOPE, id);
      setAttribute("FORMAT", "%f", VARIABLE_SCOPE, id);
      setAttribute("UNITS", "deg.", VARIABLE_SCOPE, id);
      setAttribute("SCALETYP", "linear", VARIABLE_SCOPE, id);
      setAttribute("DISPLAY_TYPE", "time_series", VARIABLE_SCOPE, id);
      setAttribute("VALIDMIN", -180f, VARIABLE_SCOPE, id, CDF_FLOAT);
      setAttribute("VALIDMAX", 180f, VARIABLE_SCOPE, id, CDF_FLOAT);
      setAttribute(
         "FILLVAL", Constants.FLOAT_FILL, VARIABLE_SCOPE, id, CDF_FLOAT
      );
      setAttribute("LABLAXIS", "Lon", VARIABLE_SCOPE, id);

      var = 
         Variable.create(
            cdf, "GPS_Alt", CDF_FLOAT, 1L, 0L, new  long[] {1}, 
            VARY, new long[] {NOVARY}
         );   
      id = var.getID();
      setAttribute("FIELDNAM", "GPS_Alt", VARIABLE_SCOPE, id);
      setAttribute(
         "CATDESC", "GPS Altitude returned every four seconds.", 
         VARIABLE_SCOPE, id
      );
      setAttribute("VAR_TYPE", "data", VARIABLE_SCOPE, id);
      setAttribute("DEPEND_0", "Epoch", VARIABLE_SCOPE, id);
      setAttribute("FORMAT", "%f", VARIABLE_SCOPE, id);
      setAttribute("UNITS", "km", VARIABLE_SCOPE, id);
      setAttribute("SCALETYP", "linear", VARIABLE_SCOPE, id);
      setAttribute("DISPLAY_TYPE", "time_series", VARIABLE_SCOPE, id);
      setAttribute("VALIDMIN", 0f, VARIABLE_SCOPE, id, CDF_FLOAT);
      setAttribute("VALIDMAX", 50f, VARIABLE_SCOPE, id, CDF_FLOAT);
      setAttribute(
         "FILLVAL", Constants.FLOAT_FILL, VARIABLE_SCOPE, id, CDF_FLOAT
      );
      setAttribute("LABLAXIS", "Alt", VARIABLE_SCOPE, id);

      var = 
         Variable.create(
            cdf, "MLT_Kp2", CDF_FLOAT, 1L, 0L, new  long[] {1}, 
            VARY, new long[] {NOVARY}
         );   
      id = var.getID();
      setAttribute(
         "FIELDNAM", "MLT for Kp=2", VARIABLE_SCOPE, id
      );
      setAttribute(
         "CATDESC", "Magnetic local time for Kp=2 in hours.", VARIABLE_SCOPE, id
      );
      setAttribute("VAR_TYPE", "data", VARIABLE_SCOPE, id);
      setAttribute("DEPEND_0", "Epoch", VARIABLE_SCOPE, id);
      setAttribute("FORMAT", "%f", VARIABLE_SCOPE, id);
      setAttribute("SCALETYP", "linear", VARIABLE_SCOPE, id);
      setAttribute("UNITS", "hr", VARIABLE_SCOPE, id);
      setAttribute("DISPLAY_TYPE", "time_series", VARIABLE_SCOPE, id);
      setAttribute("VALIDMIN", 0f, VARIABLE_SCOPE, id, CDF_FLOAT);
      setAttribute("VALIDMAX", 1e27f, VARIABLE_SCOPE, id, CDF_FLOAT);
      setAttribute(
         "FILLVAL", Constants.FLOAT_FILL, VARIABLE_SCOPE, id, CDF_FLOAT
      );
      setAttribute("LABLAXIS", "MLT_Kp2", VARIABLE_SCOPE, id);

      var = 
         Variable.create(
            cdf, "MLT_Kp6", CDF_FLOAT, 1L, 0L, new  long[] {1}, 
            VARY, new long[] {NOVARY}
         );   
      id = var.getID();
      setAttribute(
         "FIELDNAM", "MLT for Kp=6", VARIABLE_SCOPE, id
      );
      setAttribute(
         "CATDESC", "Magnetic local time for Kp=6 in hours", VARIABLE_SCOPE, id
      );
      setAttribute("VAR_TYPE", "data", VARIABLE_SCOPE, id);
      setAttribute("DEPEND_0", "Epoch", VARIABLE_SCOPE, id);
      setAttribute("FORMAT", "%f", VARIABLE_SCOPE, id);
      setAttribute("SCALETYP", "linear", VARIABLE_SCOPE, id);
      setAttribute("UNITS", "hr", VARIABLE_SCOPE, id);
      setAttribute("DISPLAY_TYPE", "time_series", VARIABLE_SCOPE, id);
      setAttribute("VALIDMIN", 0f, VARIABLE_SCOPE, id, CDF_FLOAT);
      setAttribute("VALIDMAX", 1e27f, VARIABLE_SCOPE, id, CDF_FLOAT);
      setAttribute(
         "FILLVAL", Constants.FLOAT_FILL, VARIABLE_SCOPE, id, CDF_FLOAT
      );
      setAttribute("LABLAXIS", "MLT_Kp6", VARIABLE_SCOPE, id);

      var = 
         Variable.create(
            cdf, "L_Kp2", CDF_FLOAT, 1L, 0L, new  long[] {1}, 
            VARY, new long[] {NOVARY}
         );   
      id = var.getID();
      setAttribute(
         "FIELDNAM", "L for Kp=2", VARIABLE_SCOPE, id
      );
      setAttribute(
         "CATDESC", "L shell for Kp=2", VARIABLE_SCOPE, id
      );
      setAttribute("VAR_TYPE", "data", VARIABLE_SCOPE, id);
      setAttribute("DEPEND_0", "Epoch", VARIABLE_SCOPE, id);
      setAttribute("FORMAT", "%f", VARIABLE_SCOPE, id);
      setAttribute("SCALETYP", "linear", VARIABLE_SCOPE, id);
      setAttribute("DISPLAY_TYPE", "time_series", VARIABLE_SCOPE, id);
      setAttribute("VALIDMIN", 0f, VARIABLE_SCOPE, id, CDF_FLOAT);
      setAttribute("VALIDMAX", 1e27f, VARIABLE_SCOPE, id, CDF_FLOAT);
      setAttribute(
         "FILLVAL", Constants.FLOAT_FILL, VARIABLE_SCOPE, id, CDF_FLOAT
      );
      setAttribute("LABLAXIS", "L_Kp2", VARIABLE_SCOPE, id);

      var = 
         Variable.create(
            cdf, "L_Kp6", CDF_FLOAT, 1L, 0L, new  long[] {1}, 
            VARY, new long[] {NOVARY}
         );   
      id = var.getID();
      setAttribute(
         "FIELDNAM", "L for Kp=6", VARIABLE_SCOPE, id
      );
      setAttribute(
         "CATDESC", "L shell for Kp=6", VARIABLE_SCOPE, id
      );
      setAttribute("VAR_TYPE", "data", VARIABLE_SCOPE, id);
      setAttribute("DEPEND_0", "Epoch", VARIABLE_SCOPE, id);
      setAttribute("FORMAT", "%f", VARIABLE_SCOPE, id);
      setAttribute("SCALETYP", "linear", VARIABLE_SCOPE, id);
      setAttribute("DISPLAY_TYPE", "time_series", VARIABLE_SCOPE, id);
      setAttribute("VALIDMIN", 0f, VARIABLE_SCOPE, id, CDF_FLOAT);
      setAttribute("VALIDMAX", 1e27f, VARIABLE_SCOPE, id, CDF_FLOAT);
      setAttribute(
         "FILLVAL", Constants.FLOAT_FILL, VARIABLE_SCOPE, id, CDF_FLOAT
      );
      setAttribute("LABLAXIS", "L_Kp6", VARIABLE_SCOPE, id);
   }
}
