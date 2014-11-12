/*
RCNT.java

Description:
   Creates RCNT CDF files.

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

public class RCNT extends DataProduct{
   static public final int
      INTER = 0, LL = 1, PD = 2, HL = 3;
   static public final String[] LABELS = {
      "Interrupt", "LowLevel", "PeakDet", "HighLevel"
   };
   static public final int
      RAW_CNT_FILL = CDFVar.UINT2_FILL;

   static public final float
      CNT_FILL     = CDFVar.FLOAT_FILL;

   private int date, lvl;
   private String payload_id;

   public RCNT(final String path, final String pay, int d, int l){
      this.payload_id = pay;
      this.date = d;
      this.lvl = l;

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
      cdf.attribute(
         "Logical_source_description", 
         "Rate counters for scintillator diagnostics."
      );
      cdf.attribute(
         "TEXT", 
         "Rate counters record interrupt, low level, peak detect, and high " + 
         "level. Each value is a four second accumulation" 
      );
      cdf.attribute(
         "Instrument_type", "Electron Precipitation Bremsstrahlung"
      );
      cdf.attribute("Descriptor", "RCNT>Rate CouNTers");
      cdf.attribute("Time_resolution", "4s");
      cdf.attribute(
         "Logical_source", this.payload_id + "_l" + this.lvl  + "_rcnt"
      );
      cdf.attribute(
         "Logical_file_id",
         this.payload_id + "_l" + this.lvl  + "_rcnt_20" + this.date  + 
         "_V" + CDF_Gen.getSetting("rev")
      );
   }

   @Override
   protected void addVars(){
      CDFVar var;

      var = new CDFVar(cdf, "PeakDet", CDFConstants.CDF_FLOAT);
      var.attribute("FIELDNAM", "Peak Detect");
      var.attribute("CATDESC", "Peak detect rate counter.");
      var.attribute("LABLAXIS", "PeakDet");
      var.attribute("VAR_TYPE", "data");
      var.attribute("DEPEND_0", "Epoch");
      var.attribute("FORMAT", "F7.3");
      var.attribute("UNITS", "cnts/s");
      var.attribute("SCALETYP", "log");
      var.attribute("DISPLAY_TYPE", "time_series");
      var.attribute("VALIDMIN", 0.0f);
      var.attribute("VALIDMAX", 1.0e17f);
      var.attribute("FILLVAL", CNT_FILL);
      this.cdf.addVar("PeakDet", var);

      var = new CDFVar(cdf, "LowLevel", CDFConstants.CDF_FLOAT);
      var.attribute("FIELDNAM", "Low Level");
      var.attribute("CATDESC", "Low level rate counter.");
      var.attribute("LABLAXIS", "LowLevel");
      var.attribute("VAR_TYPE", "data");
      var.attribute("DEPEND_0", "Epoch");
      var.attribute("FORMAT", "F7.3");
      var.attribute("UNITS", "cnts/s");
      var.attribute("SCALETYP", "log");
      var.attribute("DISPLAY_TYPE", "time_series");
      var.attribute("VALIDMIN", 0.0f);
      var.attribute("VALIDMAX", 1.0e17f);
      var.attribute("FILLVAL", CNT_FILL);
      this.cdf.addVar("LowLevel", var);

      var = new CDFVar(cdf, "HighLevel", CDFConstants.CDF_FLOAT);
      var.attribute("FIELDNAM", "High Level");
      var.attribute("CATDESC", "High level rate counter.");
      var.attribute("LABLAXIS", "HighLevel");
      var.attribute("VAR_TYPE", "data");
      var.attribute("DEPEND_0", "Epoch");
      var.attribute("FORMAT", "F7.3");
      var.attribute("UNITS", "cnts/s");
      var.attribute("SCALETYP", "log");
      var.attribute("DISPLAY_TYPE", "time_series");
      var.attribute("VALIDMIN", 0.0f);
      var.attribute("VALIDMAX", 1.0e17f);
      var.attribute("FILLVAL", CNT_FILL);
      this.cdf.addVar("HighLevel", var);

      var = new CDFVar(cdf, "Interrupt", CDFConstants.CDF_FLOAT);
      var.attribute("FIELDNAM", "Interrupt");
      var.attribute("CATDESC", "Interrupt rate counter.");
      var.attribute("LABLAXIS", "IRQ");
      var.attribute("VAR_TYPE", "data");
      var.attribute("DEPEND_0", "Epoch");
      var.attribute("FORMAT", "F7.3");
      var.attribute("UNITS", "cnts/s");
      var.attribute("SCALETYP", "log");
      var.attribute("DISPLAY_TYPE", "time_series");
      var.attribute("VALIDMIN", 0.0f);
      var.attribute("VALIDMAX", 1.0e17f);
      var.attribute("FILLVAL", CNT_FILL);
      this.cdf.addVar("Interrupt", var);
   }
}
