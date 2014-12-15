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

public class Ephm extends DataProduct{
   private int date, lvl;
   private String payload_id;

   public Ephm(final String path, final String pay, int d, int l){
      this.date = d;
      this.lvl = l;
      this.payload_id = pay;

      setCDF(new BarrelCDF(path, this.payload_id, this.lvl));

      //if this is a new cdf file, fill it with the default attributes
      if(getCDF().newFile == true){
         addGAttributes();
      }
      addVars();
   }
   
   @Override
   protected void addGAttributes(){
      //Set global attributes specific to this type of CDF
      cdf.attribute("Logical_source_description", "Coordinates");
      cdf.attribute(
         "TEXT", 
         "Geographic and magnetic corrdinates. Geographic coordinates are " +
         "obtained from onboard GPS unit, magnegic coordinates are derived " +
         "using the IRBEM FORTRAN library. Ephemeris data products " +
         "(Lat, Long, Alt, and Time) are each returned from the payload " + 
         "once every 4s."
      );
      cdf.attribute("Instrument_type", "Ephemeris");
      cdf.attribute("Descriptor", "ephm>EPHeMeris");
      cdf.attribute("Time_resolution", "4s");
      cdf.attribute(
         "Logical_source", this.payload_id + "_l" + this.lvl  + "_ephm"
      );
      cdf.attribute(
         "Logical_file_id",
         this.payload_id + "_l" + this.lvl  + "_ephm_20" + this.date +
         "_V" + CDF_Gen.getSetting("rev")
      );
   }
   
   @Override
   protected void addVars(){
      CDFVar var;
      
      var = new CDFVar(this.cdf, "GPS_Lat", CDFConstants.CDF_FLOAT);
      var.attribute("FIELDNAM", "GPS Lat");
      var.attribute("CATDESC", "GPS Latitude.");
      var.attribute("LABLAXIS", "Lat");
      var.attribute(
         "VAR_NOTES", 
         "Converted from raw int value by multiplying by a scaling factor: " +
         "8.38190317154 * 10^-8" 
      );
      var.attribute("VAR_TYPE", "data");
      var.attribute("DEPEND_0", "Epoch");
      var.attribute("FORMAT", "F6.3");
      var.attribute("UNITS", "degrees North");
      var.attribute("SCALETYP", "linear");
      var.attribute("DISPLAY_TYPE", "time_series");
      var.attribute("VALIDMIN", -90f);
      var.attribute("VALIDMAX", 90f);
      var.attribute("FILLVAL", CDFVar.getIstpVal("FLOAT_FILL"));
      this.cdf.addVar("GPS_Lat", var);

      var = new CDFVar(this.cdf, "GPS_Lon", CDFConstants.CDF_FLOAT);
      var.attribute("FIELDNAM", "GPS Lon");
      var.attribute("CATDESC", "GPS Longitude.");
      var.attribute("LABLAXIS", "Lon");
      var.attribute(
         "VAR_NOTES", 
         "Converted from raw int value by multiplying by a scaling factor: " +
         "8.38190317154 * 10^-8"
      );
      var.attribute("VAR_TYPE", "data");
      var.attribute("DEPEND_0", "Epoch");
      var.attribute("FORMAT", "F6.3");
      var.attribute("UNITS", "degrees East");
      var.attribute("SCALETYP", "linear");
      var.attribute("DISPLAY_TYPE", "time_series");
      var.attribute("VALIDMIN", -180f);
      var.attribute("VALIDMAX", 180f);
      var.attribute("FILLVAL", CDFVar.getIstpVal("FLOAT_FILL"));
      this.cdf.addVar("GPS_Lon", var);

      var = new CDFVar(this.cdf, "GPS_Alt", CDFConstants.CDF_FLOAT);
      var.attribute("FIELDNAM", "GPS_Alt");
      var.attribute("CATDESC", "GPS Altitude.");
      var.attribute("LABLAXIS", "Alt");
      var.attribute("VAR_TYPE", "data");
      var.attribute("DEPEND_0", "Epoch");
      var.attribute("FORMAT", "F8.3");
      var.attribute("UNITS", "km");
      var.attribute("SCALETYP", "linear");
      var.attribute("DISPLAY_TYPE", "time_series");
      var.attribute("VALIDMIN", 0f, CDFConstants.CDF_FLOAT);
      var.attribute("VALIDMAX", 50f, CDFConstants.CDF_FLOAT);
      var.attribute("FILLVAL", CDFVar.getIstpVal("FLOAT_FILL"));
      this.cdf.addVar("GPS_Alt", var);

      var = new CDFVar(this.cdf, "MLT_Kp2_T89c", CDFConstants.CDF_FLOAT);
      var.attribute("FIELDNAM", "MLT for Kp=2 (T89c)");
      var.attribute(
         "CATDESC", "Magnetic local time for Kp=2 in hours (using T89c)."
      );
      var.attribute("LABLAXIS", "MLT_Kp2");
      var.attribute(
         "VAR_NOTES", 
         "Calculated using T89c model with the IRBEM FORTRAN library"
      ); 
      var.attribute("VAR_TYPE", "data");
      var.attribute("DEPEND_0", "Epoch");
      var.attribute("FORMAT", "F4.2");
      var.attribute("SCALETYP", "linear");
      var.attribute("UNITS", "hr");
      var.attribute("DISPLAY_TYPE", "time_series");
      var.attribute("VALIDMIN", 0.0f);
      var.attribute("VALIDMAX", 24.0f);
      var.attribute("FILLVAL", CDFVar.getIstpVal("FLOAT_FILL"));
      this.cdf.addVar("MLT_Kp2", var);

      var = new CDFVar(this.cdf, "MLT_Kp6_T89c", CDFConstants.CDF_FLOAT);
      var.attribute("FIELDNAM", "MLT for Kp=6 (T89c)");
      var.attribute(
         "CATDESC", "Magnetic local time for Kp=6 in hours (using T89c)."
      );
      var.attribute("LABLAXIS", "MLT_Kp6");
      var.attribute(
         "VAR_NOTES", 
         "Calculated using T89c model with the IRBEM FORTRAN library"
      ); 
      var.attribute("VAR_TYPE", "data");
      var.attribute("DEPEND_0", "Epoch");
      var.attribute("FORMAT", "F4.2");
      var.attribute("SCALETYP", "linear");
      var.attribute("UNITS", "hr");
      var.attribute("DISPLAY_TYPE", "time_series");
      var.attribute("VALIDMIN", 0.0f, CDFConstants.CDF_FLOAT);
      var.attribute("VALIDMAX", 24.0f, CDFConstants.CDF_FLOAT);
      var.attribute("FILLVAL", CDFVar.getIstpVal("FLOAT_FILL"));
      this.cdf.addVar("MLT_Kp6", var);

      var = new CDFVar(this.cdf, "L_Kp2", CDFConstants.CDF_FLOAT);
      var.attribute("FIELDNAM", "L for Kp=2 (T89c)");
      var.attribute("CATDESC", "L shell for Kp=2 (using T89c).");
      var.attribute("LABLAXIS", "L_Kp2_T89c");
      var.attribute(
         "VAR_NOTES", 
         "Calculated using T89c model with the IRBEM FORTRAN library. " +
         "9999.0 indicates an open field line."
      ); 
      var.attribute("VAR_TYPE", "data");
      var.attribute("DEPEND_0", "Epoch");
      var.attribute("FORMAT", "F8.3");
      var.attribute("SCALETYP", "linear");
      var.attribute("DISPLAY_TYPE", "time_series");
      var.attribute("VALIDMIN", 0f, CDFConstants.CDF_FLOAT);
      var.attribute("VALIDMAX", 1000.0f, CDFConstants.CDF_FLOAT);
      var.attribute("FILLVAL", CDFVar.getIstpVal("FLOAT_FILL"));
      this.cdf.addVar("L_Kp2", var);

      var = new CDFVar(this.cdf, "L_Kp6", CDFConstants.CDF_FLOAT);
      var.attribute("FIELDNAM", "L for Kp=6 (T89c)");
      var.attribute("CATDESC", "L shell for Kp=6 (using T89c).");
      var.attribute("LABLAXIS", "L_Kp6_T89c");
      var.attribute(
         "VAR_NOTES", 
         "Calculated using T89c model with the IRBEM FORTRAN library. " +
         "9999.0 indicates an open field line."
      ); 
      var.attribute("VAR_TYPE", "data");
      var.attribute("DEPEND_0", "Epoch");
      var.attribute("FORMAT", "F8.3");
      var.attribute("SCALETYP", "linear");
      var.attribute("DISPLAY_TYPE", "time_series");
      var.attribute("VALIDMIN", 0f);
      var.attribute("VALIDMAX", 1000.0f);
      var.attribute("FILLVAL", CDFVar.getIstpVal("FLOAT_FILL"));
      this.cdf.addVar("L_Kp6", var);
   }
}
