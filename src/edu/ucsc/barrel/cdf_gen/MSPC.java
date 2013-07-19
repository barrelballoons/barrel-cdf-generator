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

public class MSPC extends BarrelCDF{
   private CDF cdf;
   private Variable var;
   private long id;
   private String path;
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
      super(p, l);
      path = p;
      date = d;
      lvl = l;

      try{
         cdf = super.getCDF();
      
         addMspcGlobalAtts();
         addMspcVars();
      }catch(CDFException e){
         System.out.println(e.getMessage());
      }
   }

   private void addMspcGlobalAtts() throws CDFException{
      //Set global attributes specific to this type of CDF
      setAttribute(
         "Logical_source_description", "Slow time resolution X-ray spectrum"
      );
      setAttribute(
         "TEXT",
         "X-ray spectra each made of 48 energy bins transmitted over 4 frames." 
      );
      setAttribute("Instrument_type", "Gamma and X-Rays");
      setAttribute("Descriptor", "Scintillator");
      setAttribute("Time_resolution", "4s");
      setAttribute("Logical_source", "payload_id_l" + lvl + "_scintillator");
      setAttribute(
         "Logical_file_id",
         "payload_id_l" + lvl + "_scintillator_20" + date  + 
         "_V" + CDF_Gen.getSetting("rev")
      );
   }

   private void addMspcVars() throws CDFException{
      //create MSPC variable
      //This variable will contain the slow spectrum that is returned over
      //32 frames.
      var = 
         Variable.create(
            cdf, "MSPC", CDF_DOUBLE, 1L, 1L, new  long[] {BIN_CENTERS.length}, 
            VARY, new long[] {VARY}
         );   
      id = var.getID();

      setAttribute("FIELDNAM", "MSPC", VARIABLE_SCOPE, id);
      setAttribute("CATDESC", "MSPC", VARIABLE_SCOPE, id);
      setAttribute(
         "VAR_NOTES", 
         "Rebinned, divided by energy bin widths and " +
         "adjusted to /sec time scale.", 
         VARIABLE_SCOPE, id
      );
      setAttribute("VAR_TYPE", "data", VARIABLE_SCOPE, id);
      setAttribute("DEPEND_0", "Epoch", VARIABLE_SCOPE, id);
      setAttribute("FORMAT", "%f", VARIABLE_SCOPE, id);
      setAttribute("UNITS", "cnts/keV/sec", VARIABLE_SCOPE, id);
      setAttribute("SCALETYP", "linear", VARIABLE_SCOPE, id);
      setAttribute("DISPLAY_TYPE", "spectrogram", VARIABLE_SCOPE, id);
      setAttribute("VALIDMIN", 0.0, VARIABLE_SCOPE, id, CDF_DOUBLE);
      setAttribute("VALIDMAX", 1707.0, VARIABLE_SCOPE, id, CDF_DOUBLE);
      setAttribute(
         "FILLVAL", Constants.DOUBLE_FILL, VARIABLE_SCOPE, id, CDF_DOUBLE
      );
      setAttribute("LABLAXIS", "MSPC", VARIABLE_SCOPE, id);
      setAttribute("DEPEND_1", "energy", VARIABLE_SCOPE, id);

      //Create the "energy" variable
      //This variable lists the starting energy for each channel in keV
      var = 
         Variable.create(
            cdf, "energy", CDF_DOUBLE, 1L, 1L, new  long[] {BIN_CENTERS.length}, 
            NOVARY, new long[] {VARY}
         );
      id = var.getID();

      setAttribute("FIELDNAM", "Energy Level", VARIABLE_SCOPE, id);
      setAttribute("CATDESC", "Energy Level", VARIABLE_SCOPE, id);
      setAttribute(
         "VAR_NOTES", "Start of each slow spectrum var channel.",
         VARIABLE_SCOPE, id
      );
      setAttribute("VAR_TYPE", "support_data", VARIABLE_SCOPE, id);
      setAttribute("DEPEND_0", "Epoch", VARIABLE_SCOPE, id);
      setAttribute("FORMAT", "%f", VARIABLE_SCOPE, id);
      setAttribute("UNITS", "keV", VARIABLE_SCOPE, id);
      setAttribute("SCALETYP", "log", VARIABLE_SCOPE, id);
      setAttribute("VALIDMIN", 100.0, VARIABLE_SCOPE, id, CDF_DOUBLE);
      setAttribute("VALIDMAX", 4100.0, VARIABLE_SCOPE, id, CDF_DOUBLE);
      setAttribute(
         "FILLVAL", Constants.DOUBLE_FILL, VARIABLE_SCOPE, id, CDF_DOUBLE
      );
      setAttribute("LABLAXIS", "Energy", VARIABLE_SCOPE, id);
      setAttribute("DELTA_PLUS_VAR", "HalfBinWidth", VARIABLE_SCOPE, id);
      setAttribute("DELTA_MINUS_VAR", "HalfBinWidth", VARIABLE_SCOPE, id);

      //Fill the "energy" variable
      for(int bin_i = 0; bin_i < BIN_CENTERS.length; bin_i++){
         var.putSingleData(
            0L, new long[] {bin_i}, BIN_CENTERS[bin_i] * scale
         );
      }

      //Create a variable that will track each energy channel width
      var = 
         Variable.create(
            cdf, "HalfBinWidth", CDF_DOUBLE, 1L, 1L, 
            new  long[] {BIN_WIDTHS.length}, NOVARY, new long[] {VARY}
         );
      id = var.getID();

      setAttribute("FIELDNAM", "Bin Width", VARIABLE_SCOPE, id);
      setAttribute("CATDESC", "Width of energy channel", VARIABLE_SCOPE, id);
      
      setAttribute("VAR_TYPE", "support_data", VARIABLE_SCOPE, id);
      setAttribute("DEPEND_0", "Epoch", VARIABLE_SCOPE, id);
      setAttribute("FORMAT", "%f", VARIABLE_SCOPE, id);
      setAttribute("UNITS", "keV", VARIABLE_SCOPE, id);
      setAttribute("SCALETYP", "linear", VARIABLE_SCOPE, id);
      setAttribute("VALIDMIN", 4.0, VARIABLE_SCOPE, id, CDF_DOUBLE);
      setAttribute("VALIDMAX", 128.0, VARIABLE_SCOPE, id, CDF_DOUBLE);
      setAttribute(
         "FILLVAL", Constants.DOUBLE_FILL, VARIABLE_SCOPE, id, CDF_DOUBLE
      );
      setAttribute("LABLAXIS", "Width", VARIABLE_SCOPE, id);

      //Fill the "BinWidth" variable
      for(int bin_i = 0; bin_i < BIN_WIDTHS.length; bin_i++){
         var.putSingleData(
            0L, new long[] {bin_i}, BIN_WIDTHS[bin_i] * scale / 2
         );
      }

   }
}
