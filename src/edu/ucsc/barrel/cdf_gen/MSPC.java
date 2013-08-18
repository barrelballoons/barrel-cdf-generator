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
   private int date, lvl;

   private double scale = 2.4414; // keV/bin

   public final static double[] 
      BIN_EDGES = {
         42, 46, 50, 53, 57, 60, 64, 70, 78, 84, 92, 100, 
         106, 114, 120, 128, 140, 156, 168, 184, 200, 212, 
         228, 240, 256, 280, 312, 336, 368, 400, 424, 456, 
         480, 512, 560, 624, 672, 736, 800, 848, 912, 960, 
         1024, 1120, 1248, 1344, 1472, 1600, 1696
      },
      BIN_CENTERS = {
         44, 48, 51.5, 55, 58.5, 62, 67, 74, 81, 88, 96, 103, 
         110, 117, 124, 134, 148, 162, 176, 192, 206, 220, 234, 
         248, 268, 296, 324, 352, 384, 412, 440, 468, 496, 536, 
         592, 648, 704, 768, 824, 880, 936, 992, 1072, 1184, 1296, 
         1408, 1536, 1648
      },
      BIN_WIDTHS = {
         4, 4, 3, 4, 3, 4, 6, 8, 6, 8, 8, 6, 8, 6, 8, 12, 16, 
         12, 16, 16, 12, 16, 12, 16, 24, 32, 24, 32, 32, 24, 
         32, 24, 32, 48, 64, 48, 64, 64, 48, 64, 48, 64, 96, 
         128, 96, 128, 128, 96
      };

   public MSPC(final String p, final int d, final int l){
      setCDF(new BarrelCDF(p, l));

      this.date = d;
      this.lvl = l;

      //if this is a new cdf file, fill it with the default attributes
      if(getCDF().newFile == true){
         addGAttributes();
      }
      addVars();
   }

   @Override
   protected void addGAttributes(){
      //Set global attributes specific to this type of CDF
      this.cdf.attribute(
         "Logical_source_description", "Slow time resolution X-ray spectrum"
      );
      this.cdf.attribute(
         "TEXT",
         "Bremsstrahlung X-ray spectra each made of 48 energy bins " +
         "transmitted over 4 frames." 
      );
      this.cdf.attribute("Instrument_type", "Gamma and X-Rays");
      this.cdf.attribute("Descriptor", "MSPC>Medium SPeCtrum");
      this.cdf.attribute("Time_resolution", "4s");
      this.cdf.attribute(
         "Logical_source", "payload_id_l" + this.lvl + "_scintillator"
      );
      this.cdf.attribute(
         "Logical_file_id",
         "payload_id_l" + this.lvl + "_scintillator_20" + this.date  + 
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
         this.cdf, "MSPC", CDFConstants.CDF_DOUBLE,
         true, new long[] {BIN_CENTERS.length}
      );

      var.attribute("FIELDNAM", "MSPC");
      var.attribute("CATDESC", "MSPC");
      var.attribute(
         "VAR_NOTES", 
         "Rebinned, divided by energy bin widths and " +
         "adjusted to /sec time scale." 
      );
      var.attribute("LABLAXIS", "MSPC");
      var.attribute("VAR_TYPE", "data");
      var.attribute("DEPEND_0", "Epoch");
      var.attribute("DEPEND_1", "energy");
      var.attribute("FORMAT", "%f");
      var.attribute("UNITS", "cnts/keV/sec");
      var.attribute("SCALETYP", "log");
      var.attribute("DISPLAY_TYPE", "spectrogram");
      var.attribute("VALIDMIN", 0.0);
      var.attribute("VALIDMAX", 1707.0);
      var.attribute("DELTA_PLUS_VAR", "cnt_error");
      var.attribute("DELTA_MINUS_VAR", "cnt_error");
      var.attribute("FILLVAL", CDFVar.getIstpVal("DOUBLE_FILL"));
      this.cdf.addVar("MSPC", var);

      //Create the "energy" variable
      //This variable lists the starting energy for each channel in keV
      var = new CDFVar(
         this.cdf, "energy", CDFConstants.CDF_DOUBLE,
         false, new long[] {BIN_CENTERS.length}
      );

      var.attribute("FIELDNAM", "energy");
      var.attribute("CATDESC", "Energy Level");
      var.attribute("LABLAXIS", "Energy");
      var.attribute("VAR_NOTES", "Center of each medium spectrum channel.");
      var.attribute("VAR_TYPE", "support_data");
      var.attribute("DEPEND_0", "Epoch");
      var.attribute("FORMAT", "%f");
      var.attribute("UNITS", "keV");
      var.attribute("SCALETYP", "log");
      var.attribute("VALIDMIN", 100.0);
      var.attribute("VALIDMAX", 4100.0);
      var.attribute("FILLVAL", CDFVar.getIstpVal("DOUBLE_FILL"));
      var.attribute("DELTA_PLUS_VAR", "HalfBinWidth");
      var.attribute("DELTA_MINUS_VAR", "HalfBinWidth");
      this.cdf.addVar("energy", var);

      //Fill the "energy" variable
      double[][] energy = new double[1][BIN_CENTERS.length];
      for(int bin_i = 0; bin_i < BIN_CENTERS.length; bin_i++){
         energy[0][bin_i] = BIN_CENTERS[bin_i] * scale;
      }
      var.writeData("energy", energy);
      energy = null;

      //Create the count error variable
      var = new CDFVar(
            cdf, "cnt_error", CDFConstants.CDF_DOUBLE, 
            true, new  long[] {BIN_CENTERS.length} 
         );   

      var.attribute("FIELDNAM", "Count Error");
      var.attribute("CATDESC", "Count error based on Poisson statistics.");
      var.attribute("LABLAXIS", "Error");
      var.attribute("VAR_NOTES", "Error only valid for large count values.");
      var.attribute("VAR_TYPE", "support_data");
      var.attribute("DEPEND_0", "Epoch");
      var.attribute("FORMAT", "%f");
      var.attribute("UNITS", "keV");
      var.attribute("SCALETYP", "linear");
      var.attribute("VALIDMIN", 0.0);
      var.attribute("VALIDMAX", 10000.0);
      var.attribute("FILLVAL", CDFVar.getIstpVal("DOUBLE_FILL"));
      this.cdf.addVar("cnt_error", var);
      
      //Create a variable that will track each energy channel width
      var = new CDFVar(
         this.cdf, "HalfBinWidth", CDFConstants.CDF_DOUBLE,
         false, new long[] {BIN_WIDTHS.length}
      );

      var.attribute("FIELDNAM", "Bin Width");
      var.attribute("CATDESC", "Width of energy channel");
      var.attribute("LABLAXIS", "Width");
      var.attribute("VAR_TYPE", "support_data");
      var.attribute("DEPEND_0", "Epoch");
      var.attribute("FORMAT", "%f");
      var.attribute("UNITS", "keV");
      var.attribute("SCALETYP", "linear");
      var.attribute("VALIDMIN", 3.0);
      var.attribute("VALIDMAX", 157.0);
      var.attribute("FILLVAL", CDFVar.getIstpVal("DOUBLE_FILL"));
      this.cdf.addVar("HalfBinWidth", var);

      //Fill the "BinWidth" variable
      double[][] bin_width = new double[1][BIN_WIDTHS.length];
      for(int bin_i = 0; bin_i < BIN_WIDTHS.length; bin_i++){
         bin_width[0][bin_i] = BIN_WIDTHS[bin_i] * scale / 2;
      }
      var.writeData("HalfBinWidth", bin_width);
      bin_width = null;
   }
}
