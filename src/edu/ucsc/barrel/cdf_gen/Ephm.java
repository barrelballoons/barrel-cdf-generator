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

   public Ephm(final String p, final int d, final int l){
      this.date = d;
      this.lvl = l;

      setCDF(new BarrelCDF(p, l));
      addGAttributes();
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
         "(Lat, Long, Alt, and Time) are each returned once every 4s."
         
      );
      cdf.attribute("Instrument_type", "GPS");
      cdf.attribute("Descriptor", "ephm>EPHeMeris");
      cdf.attribute("Time_resolution", "4s");
      cdf.attribute("Logical_source", "payload_id_l" + lvl  + "_ephm");
      cdf.attribute(
         "Logical_file_id",
         "payload_id_l" + lvl  + "_ephm_20" + date +
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
         "Converted from raw int value by multiplying by scaling factor " +
         "8.38190317154 * 10^-8" 
      );
      var.attribute("VAR_TYPE", "data");
      var.attribute("DEPEND_0", "Epoch");
      var.attribute("FORMAT", "%f");
      var.attribute("UNITS", "deg.");
      var.attribute("SCALETYP", "linear");
      var.attribute("DISPLAY_TYPE", "time_series");
      var.attribute("VALIDMIN", -180f);
      var.attribute("VALIDMAX", 180f);
      var.attribute("FILLVAL", CDFVar.getIstpVal("FLOAT_FILL"));
      this.cdf.addVar("GPS_Lat", var);

      var = new CDFVar(this.cdf, "GPS_Lon", CDFConstants.CDF_FLOAT);
      var.attribute("FIELDNAM", "GPS Lon");
      var.attribute("CATDESC", "GPS Longitude.");
      var.attribute("LABLAXIS", "Lon");
      var.attribute(
         "VAR_NOTES", 
         "Converted from raw int value by multiplying by scaling factor " +
         "8.38190317154 * 10^-8"
      );
      var.attribute("VAR_TYPE", "data");
      var.attribute("DEPEND_0", "Epoch");
      var.attribute("FORMAT", "%f");
      var.attribute("UNITS", "deg.");
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
      var.attribute("FORMAT", "%f");
      var.attribute("UNITS", "km");
      var.attribute("SCALETYP", "linear");
      var.attribute("DISPLAY_TYPE", "time_series");
      var.attribute("VALIDMIN", 0f, CDFConstants.CDF_FLOAT);
      var.attribute("VALIDMAX", 50f, CDFConstants.CDF_FLOAT);
      var.attribute("FILLVAL", CDFVar.getIstpVal("FLOAT_FILL"));
      this.cdf.addVar("GPS_Alt", var);

      var = new CDFVar(this.cdf, "MLT_Kp2", CDFConstants.CDF_FLOAT);
      var.attribute("FIELDNAM", "MLT for Kp=2");
      var.attribute("CATDESC", "Magnetic local time for Kp=2 in hours.");
      var.attribute("LABLAXIS", "MLT_Kp2");
      var.attribute("VAR_NOTES", "Calculated using IRBEM FORTRAN library"); 
      var.attribute("VAR_TYPE", "data");
      var.attribute("DEPEND_0", "Epoch");
      var.attribute("FORMAT", "%f");
      var.attribute("SCALETYP", "linear");
      var.attribute("UNITS", "hr");
      var.attribute("DISPLAY_TYPE", "time_series");
      var.attribute("VALIDMIN", 0f);
      var.attribute("VALIDMAX", 1e27f);
      var.attribute("FILLVAL", CDFVar.getIstpVal("FLOAT_FILL"));
      this.cdf.addVar("MLT_Kp2", var);

      var = new CDFVar(this.cdf, "MLT_Kp6", CDFConstants.CDF_FLOAT);
      var.attribute("FIELDNAM", "MLT for Kp=6");
      var.attribute("CATDESC", "Magnetic local time for Kp=6 in hours");
      var.attribute("LABLAXIS", "MLT_Kp6");
      var.attribute("VAR_NOTES", "Calculated using IRBEM FORTRAN library"); 
      var.attribute("VAR_TYPE", "data");
      var.attribute("DEPEND_0", "Epoch");
      var.attribute("FORMAT", "%f");
      var.attribute("SCALETYP", "linear");
      var.attribute("UNITS", "hr");
      var.attribute("DISPLAY_TYPE", "time_series");
      var.attribute("VALIDMIN", 0f, CDFConstants.CDF_FLOAT);
      var.attribute("VALIDMAX", 1e27f, CDFConstants.CDF_FLOAT);
      var.attribute("FILLVAL", CDFVar.getIstpVal("FLOAT_FILL"));
      this.cdf.addVar("MLT_Kp6", var);

      var = new CDFVar(this.cdf, "L_Kp2", CDFConstants.CDF_FLOAT);
      var.attribute("FIELDNAM", "L for Kp=2");
      var.attribute("CATDESC", "L shell for Kp=2");
      var.attribute("LABLAXIS", "L_Kp2");
      var.attribute("VAR_NOTES", "Calculated using IRBEM FORTRAN library"); 
      var.attribute("VAR_TYPE", "data");
      var.attribute("DEPEND_0", "Epoch");
      var.attribute("FORMAT", "%f");
      var.attribute("SCALETYP", "linear");
      var.attribute("DISPLAY_TYPE", "time_series");
      var.attribute("VALIDMIN", 0f, CDFConstants.CDF_FLOAT);
      var.attribute("VALIDMAX", 1e27f, CDFConstants.CDF_FLOAT);
      var.attribute("FILLVAL", CDFVar.getIstpVal("FLOAT_FILL"));
      this.cdf.addVar("L_Kp2", var);

      var = new CDFVar(this.cdf, "L_Kp6", CDFConstants.CDF_FLOAT);
      var.attribute("FIELDNAM", "L for Kp=6");
      var.attribute("CATDESC", "L shell for Kp=6");
      var.attribute("LABLAXIS", "L_Kp6");
      var.attribute("VAR_NOTES", "Calculated using IRBEM FORTRAN library"); 
      var.attribute("VAR_TYPE", "data");
      var.attribute("DEPEND_0", "Epoch");
      var.attribute("FORMAT", "%f");
      var.attribute("SCALETYP", "linear");
      var.attribute("DISPLAY_TYPE", "time_series");
      var.attribute("VALIDMIN", 0f);
      var.attribute("VALIDMAX", 1e27f);
      var.attribute("FILLVAL", CDFVar.getIstpVal("FLOAT_FILL"));
      this.cdf.addVar("L_Kp6", var);
   }
}
