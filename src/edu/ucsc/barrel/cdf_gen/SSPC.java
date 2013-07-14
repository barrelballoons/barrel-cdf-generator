/*
SSPC.java

Description:
   Creates SSPC CDF files.

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

public class SSPC extends BarrelCDF{
   private CDF cdf;
   private Variable var;
   private long id;

   private double[] 
      BIN_START = {
         0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 
         16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 
         30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 
         44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 
         58, 59, 60, 61, 62, 63, 64, 66, 68, 70, 72, 74, 76, 78, 
         80, 82, 84, 86, 88, 90, 92, 94, 96, 98, 100, 102, 104, 
         106, 108, 110, 112, 114, 116, 118, 120, 122, 124, 126, 
         128, 132, 136, 140, 144, 148, 152, 156, 160, 164, 168, 
         172, 176, 180, 184, 188, 192, 196, 200, 204, 208, 212, 
         216, 220, 224, 228, 232, 236, 240, 244, 248, 252, 256, 
         264, 272, 280, 288, 296, 304, 312, 320, 328, 336, 344, 
         352, 360, 368, 376, 384, 392, 400, 408, 416, 424, 432, 
         440, 448, 456, 464, 472, 480, 488, 496, 504, 512, 528, 
         544, 560, 576, 592, 608, 624, 640, 656, 672, 688, 704, 
         720, 736, 752, 768, 784, 800, 816, 832, 848, 864, 880, 
         896, 912, 928, 944, 960, 976, 992, 1008, 1024, 1056, 
         1088, 1120, 1152, 1184, 1216, 1248, 1280, 1312, 1344, 
         1376, 1408, 1440, 1472, 1504, 1536, 1568, 1600, 1632, 
         1664, 1696, 1728, 1760, 1792, 1824, 1856, 1888, 1920, 
         1952, 1984, 2016, 2048, 2112, 2176, 2240, 2304, 2368, 
         2432, 2496, 2560, 2624, 2688, 2752, 2816, 2880, 2944, 
         3008, 3072, 3136, 3200, 3264, 3328, 3392, 3456, 3520, 
         3584, 3648, 3712, 3776, 3840, 3904, 3968, 4032
      },
      BIN_WIDTH = {
         1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 
         1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 
         1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 
         1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 
         2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 
         2, 2, 2, 2, 2, 2, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 
         4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 
         4, 4, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 
         8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 16, 
         16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 
         16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 
         16, 16, 16, 16, 16, 32, 32, 32, 32, 32, 32, 32, 32, 
         32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 
         32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 64, 64, 
         64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 
         64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 
         64, 64, 64, 64
      };

   private double scale = 2.4414; // keV/bin

   public SSPC(
      final String path, final int date, final int lvl
   ) throws CDFException{
      super(path);
      cdf = super.getCDF();
      Attribute attr;

      //Set global attributes specific to this type of CDF
      attr = cdf.getAttribute("Data_type");
      Entry.create(attr, 0, CDF_CHAR, "l" + lvl + ">Level-" + lvl);

      attr = cdf.getAttribute("Logical_source_description");
      Entry.create(
         attr, 0, CDF_CHAR, "Slow time resolution X-ray spectrum"
      );

      attr = cdf.getAttribute("TEXT");
      Entry.create(
         attr, 0, CDF_CHAR, 
         "X-ray spectra each made of 256 energy bins " + 
         "transmitted over 32 frames."
      );

      attr = cdf.getAttribute("Instrument_type");
      Entry.create(attr, 0, CDF_CHAR, "Gamma and X-Rays");
      
      attr = cdf.getAttribute("Descriptor");
      Entry.create(attr, 0, CDF_CHAR, "Scintillator");
      
      attr = cdf.getAttribute("Time_resolution");
      Entry.create(attr, 0, CDF_CHAR, "32s");
      
      attr = cdf.getAttribute("Logical_source");
      Entry.create(attr, 0, CDF_CHAR, "payload_id_l2_scintillator");
      
      attr = cdf.getAttribute("Logical_file_id");
      Entry.create(
         attr, 0, CDF_CHAR, 
         "payload_id_l2_scintillator_20" + date  + 
         "_V" + CDF_Gen.getSetting("rev")
      );

      //create SSPC variable
      //This variable will contain the slow spectrum that is returned over
      //32 frames.
      var = 
         Variable.create(
            cdf, "SSPC", CDF_DOUBLE, 1L, 1L, new  long[] {256}, 
            VARY, new long[] {VARY}
         );   
      id = var.getID();

      attr = cdf.getAttribute("FIELDNAM");
      Entry.create(attr, id, CDF_CHAR, "SSPC");

      attr = cdf.getAttribute("CATDESC");
      Entry.create(attr, id, CDF_CHAR, "SSPC");

      attr = cdf.getAttribute("VAR_NOTES");
      Entry.create(
         attr, id, CDF_CHAR, 
         "Rebinned, divided by energy bin widths and " +
         "adjusted to /sec time scale."
      );

      attr = cdf.getAttribute("VAR_TYPE");
      Entry.create(attr, id, CDF_CHAR, "data");

      attr = cdf.getAttribute("DEPEND_0");
      Entry.create(attr, id, CDF_CHAR, "Epoch");

      attr = cdf.getAttribute("FORMAT");
      Entry.create(attr, id, CDF_CHAR, "%f");

      attr = cdf.getAttribute("UNITS");
      Entry.create(attr, id, CDF_CHAR, "cnts/keV/sec");

      attr = cdf.getAttribute("SCALETYP");
      Entry.create(attr, id, CDF_CHAR, "log");

      attr = cdf.getAttribute("DISPLAY_TYPE");
      Entry.create(attr, id, CDF_CHAR, "spectrogram");

      attr = cdf.getAttribute("VALIDMIN");
      Entry.create(attr, id, CDF_DOUBLE, 0.0);

      attr = cdf.getAttribute("VALIDMAX");
      Entry.create(attr, id, CDF_DOUBLE, 59391.0);

      attr = cdf.getAttribute("FILLVAL");
      Entry.create(attr, id, CDF_DOUBLE, Constants.DOUBLE_FILL);

      attr = cdf.getAttribute("LABLAXIS");
      Entry.create(attr, id, CDF_CHAR, "SSPC");

      attr = cdf.getAttribute("DEPEND_1");
      Entry.create(attr, id, CDF_CHAR, "energy");

      //Create the "energy" variable
      //This variable lists the starting energy for each channel in keV
      var = 
         Variable.create(
            cdf, "energy", CDF_DOUBLE, 1L, 1L, new  long[] {256}, 
            NOVARY, new long[] {VARY}
         );
      id = var.getID();

      attr = cdf.getAttribute("FIELDNAM");
      Entry.create(attr, id, CDF_CHAR, "Energy Level");

      attr = cdf.getAttribute("CATDESC");
      Entry.create(attr, id, CDF_CHAR, "Energy Level");

      attr = cdf.getAttribute("VAR_NOTES");
      Entry.create(
         attr, id, CDF_CHAR, 
         "Start of each slow spectrum var channel."
      );

      attr = cdf.getAttribute("VAR_TYPE");
      Entry.create(attr, id, CDF_CHAR, "support_data");

      attr = cdf.getAttribute("DEPEND_0");
      Entry.create(attr, id, CDF_CHAR, "Epoch");

      attr = cdf.getAttribute("FORMAT");
      Entry.create(attr, id, CDF_CHAR, "%f");

      attr = cdf.getAttribute("UNITS");
      Entry.create(attr, id, CDF_CHAR, "keV");

      attr = cdf.getAttribute("SCALETYP");
      Entry.create(attr, id, CDF_CHAR, "linear");

      attr = cdf.getAttribute("VALIDMIN");
      Entry.create(attr, id, CDF_DOUBLE, 0.0);

      attr = cdf.getAttribute("VALIDMAX");
      Entry.create(attr, id, CDF_DOUBLE, 10000.0);

      attr = cdf.getAttribute("FILLVAL");
      Entry.create(attr, id, CDF_DOUBLE, Constants.DOUBLE_FILL);

      attr = cdf.getAttribute("LABLAXIS");
      Entry.create(attr, id, CDF_CHAR, "Energy");

      attr = cdf.getAttribute("DELTA_PLUS_VAR");
      Entry.create(attr, id, CDF_CHAR, "BinWidth");

      //Fill the "energy" variable
      for(int bin_i = 0; bin_i < BIN_START.length; bin_i++){
         var.putSingleData(
            0L, new long[] {bin_i}, BIN_START[bin_i] * scale
         );
      }

      //Create a variable that will track each energy channel width
      var = 
         Variable.create(
            cdf, "BinWidth", CDF_DOUBLE, 1L, 1L, new  long[] {256}, 
            NOVARY, new long[] {VARY}
         );
      id = var.getID();

      attr = cdf.getAttribute("FIELDNAM");
      Entry.create(attr, id, CDF_CHAR, "Bin Width");

      attr = cdf.getAttribute("CATDESC");
      Entry.create(
         attr, id, CDF_CHAR, "Width of energy channel"
      );

      attr = cdf.getAttribute("VAR_TYPE");
      Entry.create(attr, id, CDF_CHAR, "support_data");

      attr = cdf.getAttribute("DEPEND_0");
      Entry.create(attr, id, CDF_CHAR, "Epoch");

      attr = cdf.getAttribute("FORMAT");
      Entry.create(attr, id, CDF_CHAR, "%f");

      attr = cdf.getAttribute("UNITS");
      Entry.create(attr, id, CDF_CHAR, "keV");

      attr = cdf.getAttribute("SCALETYP");
      Entry.create(attr, id, CDF_CHAR, "linear");

      attr = cdf.getAttribute("VALIDMIN");
      Entry.create(attr, id, CDF_DOUBLE, 0.0);

      attr = cdf.getAttribute("VALIDMAX");
      Entry.create(attr, id, CDF_DOUBLE, 200.0);

      attr = cdf.getAttribute("FILLVAL");
      Entry.create(attr, id, CDF_DOUBLE, Constants.DOUBLE_FILL);

      attr = cdf.getAttribute("LABLAXIS");
      Entry.create(attr, id, CDF_CHAR, "Width");

      //Fill the "BinWidth" variable
      for(int bin_i = 0; bin_i < BIN_WIDTH.length; bin_i++){
         var.putSingleData(
            0L, new long[] {bin_i}, BIN_WIDTH[bin_i] * scale
         );
      }

      //Create a variable that will track the 511 line peak
      var = 
         Variable.create(
            cdf, "Peak_511", CDF_DOUBLE, 1L, 0L, new  long[] {1}, 
            VARY, new long[] {NOVARY}
         );
      id = var.getID();

      attr = cdf.getAttribute("FIELDNAM");
      Entry.create(attr, id, CDF_CHAR, "Peak_511");

      attr = cdf.getAttribute("CATDESC");
      Entry.create(
         attr, id, CDF_CHAR, "Location of the 511 line"
      );

      attr = cdf.getAttribute("VAR_TYPE");
      Entry.create(attr, id, CDF_CHAR, "data");

      attr = cdf.getAttribute("VAR_NOTES");
      Entry.create(
         attr, id, CDF_CHAR, 
         "This is the detector channel (0-4096) " + 
         "which appears to contain the 511"
      );

      attr = cdf.getAttribute("DEPEND_0");
      Entry.create(attr, id, CDF_CHAR, "Epoch");

      attr = cdf.getAttribute("FORMAT");
      Entry.create(attr, id, CDF_CHAR, "%f");

      attr = cdf.getAttribute("UNITS");
      Entry.create(attr, id, CDF_CHAR, "ch");

      attr = cdf.getAttribute("SCALETYP");
      Entry.create(attr, id, CDF_CHAR, "linear");

      attr = cdf.getAttribute("DISPLAY_TYPE");
      Entry.create(attr, id, CDF_CHAR, "time_series");

      attr = cdf.getAttribute("VALIDMIN");
      Entry.create(attr, id, CDF_DOUBLE, 0.0);

      attr = cdf.getAttribute("VALIDMAX");
      Entry.create(attr, id, CDF_DOUBLE, 4096.0);

      attr = cdf.getAttribute("FILLVAL");
      Entry.create(attr, id, CDF_DOUBLE, Constants.DOUBLE_FILL);

      attr = cdf.getAttribute("LABLAXIS");
      Entry.create(attr, id, CDF_CHAR, "Peak_511");
   }
}
