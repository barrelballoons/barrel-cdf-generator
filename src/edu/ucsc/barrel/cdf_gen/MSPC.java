/*
MSPC.java

Description:
   Creates MSPC CDF files.

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

public class MSPC extends DataProduct{
   static public final short
      RAW_BIN_FILL = BarrelFrame.INT2_FILL;

   static public final int
      FC_FILL      = BarrelFrame.INT4_FILL,
      QUALITY_FILL = BarrelFrame.INT4_FILL;

   static public final long
      EPOCH_FILL   = BarrelFrame.TT2000_FILL;

   static public final float
      BIN_FILL     = BarrelFrame.FLOAT_FILL,
      ERROR_FILL   = BarrelFrame.FLOAT_FILL;

   static public final float[]
      BIN_EDGES = {
         42f, 46f, 50f, 53f, 57f, 60f, 64f, 70f, 78f, 84f, 92f, 100f, 
         106f, 114f, 120f, 128f, 140f, 156f, 168f, 184f, 200f, 212f, 
         228f, 240f, 256f, 280f, 312f, 336f, 368f, 400f, 424f, 456f, 
         480f, 512f, 560f, 624f, 672f, 736f, 800f, 848f, 912f, 960f, 
         1024f, 1120f, 1248f, 1344f, 1472f, 1600f, 1696
      },
      BIN_CENTERS = {
         44f, 48f, 51.5f, 55f, 58.5f, 62f, 67f, 74f, 81f, 88f, 96f, 103f, 
         110f, 117f, 124f, 134f, 148f, 162f, 176f, 192f, 206f, 220f, 234f, 
         248f, 268f, 296f, 324f, 352f, 384f, 412f, 440f, 468f, 496f, 536f, 
         592f, 648f, 704f, 768f, 824f, 880f, 936f, 992f, 1072f, 1184f, 1296f, 
         1408f, 1536f, 1648
      },
      BIN_WIDTHS = {
         4f, 4f, 3f, 4f, 3f, 4f, 6f, 8f, 6f, 8f, 8f, 6f, 8f, 6f, 8f, 12f, 16f, 
         12f, 16f, 16f, 12f, 16f, 12f, 16f, 24f, 32f, 24f, 32f, 32f, 24f, 
         32f, 24f, 32f, 48f, 64f, 48f, 64f, 64f, 48f, 64f, 48f, 64f, 96f, 
         128f, 96f, 128f, 128f, 96f
      };

   private int date, lvl;
   private String payload_id;

   private float scale = 2.4414f; // keV/bin
   

   public MSPC(final String path, final String pay, int d, int l){
      this.payload_id = pay;
      this.date = d;
      this.lvl = l;

      setCDF(new BarrelCDF(path, this.payload_id, this.lvl));

      //if this is a new cdf file, fill it with the default attributes
      if(getCDF().newFile == true){
         //set accumulaton time
         CDFVar var = 
            new CDFVar(
               cdf, "HalfAccumTime", CDFConstants.CDF_INT8, 
               false, new  long[] {0} 
            ); 

         var.attribute("FIELDNAM", "Half accumulation time.");
         var.attribute("CATDESC", "Period of time used to accumulate spectra.");
         var.attribute("LABLAXIS", "AccumTime");
         var.attribute("VAR_TYPE", "support_data");
         var.attribute("UNITS", "ns");
         var.attribute("SCALETYP", "linear");
         var.attribute("VALIDMIN", 1999999999L);
         var.attribute("VALIDMAX", 2000000001L);
         var.attribute("FILLVAL", Long.MIN_VALUE);
         this.cdf.addVar("HalfAccumTime", var);

         //Fill the "HalfAccumTime" variable
         var.writeData("HalfAccumTime", new long[] {2000000000L});

         //add the DELTA_PLUS/MINUS_VAR to Epoch so it will track HalfAccumTime
         var = this.cdf.getVar("Epoch");
         var.attribute("DELTA_MINUS_VAR", "HalfAccumTime");
         var.attribute("DELTA_PLUS_VAR", "HalfAccumTime");
         this.cdf.addVar("Epoch", var);
         

         addGAttributes();
      }
      addVars();
   }

   @Override
   protected void addGAttributes(){
      //Set global attributes specific to this type of CDF
      this.cdf.attribute(
         "Logical_source_description", 
         "Medium time resolution (4s) X-ray spectrum"
      );
      this.cdf.attribute(
         "TEXT",
         "Bremsstrahlung X-ray spectra each made of 48 energy bins " +
         "transmitted over 4 frames." 
      );
      this.cdf.attribute(
         "Instrument_type", "Electron Precipitation Bremsstrahlung"
      );
      this.cdf.attribute("Descriptor", "MSPC>Medium SPeCtrum");
      this.cdf.attribute("Time_resolution", "4s");
      this.cdf.attribute(
         "Logical_source", this.payload_id + "_l" + this.lvl + "_mspc"
      );
      this.cdf.attribute(
         "Logical_file_id",
         this.payload_id + "_l" + this.lvl + "_mspc_20" + this.date  + 
         "_V" + CDF_Gen.getSetting("rev")
      );
   }

   @Override
   protected void addVars(){
      CDFVar var;
      //create MSPC variable
      //This variable will contain the medium spectrum that is returned over
      //4 frames.
      var = new CDFVar(
         this.cdf, "MSPC", CDFConstants.CDF_FLOAT,
         true, new long[] {BIN_CENTERS.length}
      );

      var.attribute("FIELDNAM", "MSPC");
      var.attribute("CATDESC", "Medium Spectrum (4s)");
      var.attribute(
         "VAR_NOTES", 
         "Rebinned, divided by energy bin widths and " +
         "adjusted to /sec time scale." 
      );
      var.attribute("LABLAXIS", "MSPC");
      var.attribute("VAR_TYPE", "data");
      var.attribute("DEPEND_0", "Epoch");
      var.attribute("DEPEND_1", "energy");
      var.attribute("FORMAT", "F7.3");
      var.attribute("UNITS", "cnts/keV/sec");
      var.attribute("SCALETYP", "log");
      var.attribute("DISPLAY_TYPE", "spectrogram");
      var.attribute("VALIDMIN", 0.0f);
      var.attribute("VALIDMAX", 1707.0f);
      var.attribute("DELTA_PLUS_VAR", "cnt_error");
      var.attribute("DELTA_MINUS_VAR", "cnt_error");
      var.attribute("FILLVAL", CDFVar.getIstpVal("FLOAT_FILL"));
      this.cdf.addVar("MSPC", var);

      //Create the "energy" variable
      //This variable lists the starting energy for each channel in keV
      //Depend1 variable for the spectrum
      var = new CDFVar(
         this.cdf, "energy", CDFConstants.CDF_FLOAT,
         false, new long[] {BIN_CENTERS.length}
      );

      var.attribute("FIELDNAM", "energy");
      var.attribute("CATDESC", "Energy Level");
      var.attribute("LABLAXIS", "Energy");
      var.attribute("VAR_NOTES", "Center of each medium spectrum channel.");
      var.attribute("VAR_TYPE", "support_data");
      var.attribute("DEPEND_0", "Epoch");
      var.attribute("FORMAT", "F7.3");
      var.attribute("UNITS", "keV");
      var.attribute("SCALETYP", "log");
      var.attribute("VALIDMIN", 100.0f);
      var.attribute("VALIDMAX", 4100.0f);
      var.attribute("FILLVAL", CDFVar.getIstpVal("FLOAT_FILL"));
      var.attribute("DELTA_PLUS_VAR", "HalfBinWidth");
      var.attribute("DELTA_MINUS_VAR", "HalfBinWidth");
      this.cdf.addVar("energy", var);

      //Fill the "energy" variable
      float[][] energy = new float[1][BIN_CENTERS.length];
      for(int bin_i = 0; bin_i < BIN_CENTERS.length; bin_i++){
         energy[0][bin_i] = BIN_CENTERS[bin_i] * scale;
      }
      var.writeData("energy", energy);
      energy = null;

      //Create the count error variable
      var = new CDFVar(
            cdf, "cnt_error", CDFConstants.CDF_FLOAT, 
            true, new  long[] {BIN_CENTERS.length} 
         );   

      var.attribute("FIELDNAM", "Count Error");
      var.attribute("CATDESC", "Count error based on Poisson statistics.");
      var.attribute("LABLAXIS", "Error");
      var.attribute("VAR_NOTES", "Error only valid for large count values.");
      var.attribute("VAR_TYPE", "support_data");
      var.attribute("DEPEND_0", "Epoch");
      var.attribute("DEPEND_1", "channel");
      var.attribute("FORMAT", "F8.3");
      var.attribute("UNITS", "cnts/sec/keV");
      var.attribute("SCALETYP", "linear");
      var.attribute("DISPLAY_TYPE", "spectrogram");
      var.attribute("VALIDMIN", 0.0f);
      var.attribute("VALIDMAX", 10000.0f);
      var.attribute("FILLVAL", CDFVar.getIstpVal("FLOAT_FILL"));
      this.cdf.addVar("cnt_error", var);
      
      //Create the "channel" variable
      //Depend 1 variable for cnt_error.
      var = new CDFVar(
         this.cdf, "channel", CDFConstants.CDF_UINT1,
         false, new long[] {BIN_CENTERS.length}
      );

      var.attribute("FIELDNAM", "channel");
      var.attribute("CATDESC", "Channel Number");
      var.attribute("LABLAXIS", "Channel");
      var.attribute("VAR_NOTES", "Channel number 0 - 47.");
      var.attribute("VAR_TYPE", "support_data");
      var.attribute("DEPEND_0", "Epoch");
      var.attribute("FORMAT", "I2");
      var.attribute("UNITS", "channel");
      var.attribute("SCALETYP", "linear");
      var.attribute("VALIDMIN", 0);
      var.attribute("VALIDMAX", 48);
      var.attribute("FILLVAL", CDFVar.getIstpVal("UINT1_FILL"));
      this.cdf.addVar("channel", var);

      //Fill the "channel" variable
      short[][] channel = {{
         0,1,2,3,4,5,6,7,8,9,
         10,11,12,13,14,15,16,17,18,19,
         20,21,22,23,24,25,26,27,28,29,
         30,31,32,33,34,35,36,37,38,39,
         40,41,42,43,44,45,46,47
      }};
      var.writeData("channel", channel);
      channel = null;

      //Create a variable that will track each energy channel width
      var = new CDFVar(
         this.cdf, "HalfBinWidth", CDFConstants.CDF_FLOAT,
         false, new long[] {BIN_WIDTHS.length}
      );

      var.attribute("FIELDNAM", "Bin Width");
      var.attribute("CATDESC", "Width of energy channel");
      var.attribute("LABLAXIS", "Width");
      var.attribute("VAR_TYPE", "support_data");
      var.attribute("DEPEND_0", "Epoch");
      var.attribute("FORMAT", "F6.3");
      var.attribute("UNITS", "keV");
      var.attribute("SCALETYP", "linear");
      var.attribute("VALIDMIN", 3.0f);
      var.attribute("VALIDMAX", 157.0f);
      var.attribute("FILLVAL", CDFVar.getIstpVal("FLOAT_FILL"));
      this.cdf.addVar("HalfBinWidth", var);

      //Fill the "BinWidth" variable
      float[][] bin_width = new float[1][BIN_WIDTHS.length];
      for(int bin_i = 0; bin_i < BIN_WIDTHS.length; bin_i++){
         bin_width[0][bin_i] = BIN_WIDTHS[bin_i] * scale / 2;
      }
      var.writeData("HalfBinWidth", bin_width);
      bin_width = null;
   }
}
