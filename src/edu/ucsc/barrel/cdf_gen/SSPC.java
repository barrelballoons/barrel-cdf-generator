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

public class SSPC extends DataProduct{
   private int date, lvl;
   private String payload_id;
   private float scale = 2.4414f; // keV/bin

   public final static float[] 
      BIN_EDGES = {
         0f, 1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f, 11f, 12f, 13f, 14f, 15f, 
         16f, 17f, 18f, 19f, 20f, 21f, 22f, 23f, 24f, 25f, 26f, 27f, 28f, 29f, 
         30f, 31f, 32f, 33f, 34f, 35f, 36f, 37f, 38f, 39f, 40f, 41f, 42f, 43f, 
         44f, 45f, 46f, 47f, 48f, 49f, 50f, 51f, 52f, 53f, 54f, 55f, 56f, 57f, 
         58f, 59f, 60f, 61f, 62f, 63f, 64f, 66f, 68f, 70f, 72f, 74f, 76f, 78f, 
         80f, 82f, 84f, 86f, 88f, 90f, 92f, 94f, 96f, 98f, 100f, 102f, 104f, 
         106f, 108f, 110f, 112f, 114f, 116f, 118f, 120f, 122f, 124f, 126f, 
         128f, 132f, 136f, 140f, 144f, 148f, 152f, 156f, 160f, 164f, 168f, 
         172f, 176f, 180f, 184f, 188f, 192f, 196f, 200f, 204f, 208f, 212f, 
         216f, 220f, 224f, 228f, 232f, 236f, 240f, 244f, 248f, 252f, 256f, 
         264f, 272f, 280f, 288f, 296f, 304f, 312f, 320f, 328f, 336f, 344f, 
         352f, 360f, 368f, 376f, 384f, 392f, 400f, 408f, 416f, 424f, 432f, 
         440f, 448f, 456f, 464f, 472f, 480f, 488f, 496f, 504f, 512f, 528f, 
         544f, 560f, 576f, 592f, 608f, 624f, 640f, 656f, 672f, 688f, 704f, 
         720f, 736f, 752f, 768f, 784f, 800f, 816f, 832f, 848f, 864f, 880f, 
         896f, 912f, 928f, 944f, 960f, 976f, 992f, 1008f, 1024f, 1056f, 
         1088f, 1120f, 1152f, 1184f, 1216f, 1248f, 1280f, 1312f, 1344f, 
         1376f, 1408f, 1440f, 1472f, 1504f, 1536f, 1568f, 1600f, 1632f, 
         1664f, 1696f, 1728f, 1760f, 1792f, 1824f, 1856f, 1888f, 1920f, 
         1952f, 1984f, 2016f, 2048f, 2112f, 2176f, 2240f, 2304f, 2368f, 
         2432f, 2496f, 2560f, 2624f, 2688f, 2752f, 2816f, 2880f, 2944f, 
         3008f, 3072f, 3136f, 3200f, 3264f, 3328f, 3392f, 3456f, 3520f, 
         3584f, 3648f, 3712f, 3776f, 3840f, 3904f, 3968f, 4032f, 4096
      },
      BIN_CENTERS = {
         0.5f, 1.5f, 2.5f, 3.5f, 4.5f, 5.5f, 6.5f, 7.5f, 8.5f, 9.5f, 10.5f, 
         11.5f, 12.5f, 13.5f, 14.5f, 15.5f, 16.5f, 17.5f, 18.5f, 19.5f, 
         20.5f, 21.5f, 22.5f, 23.5f, 24.5f, 25.5f, 26.5f, 27.5f, 28.5f, 
         29.5f, 30.5f, 31.5f, 32.5f, 33.5f, 34.5f, 35.5f, 36.5f, 37.5f, 
         38.5f, 39.5f, 40.5f, 41.5f, 42.5f, 43.5f, 44.5f, 45.5f, 46.5f, 
         47.5f, 48.5f, 49.5f, 50.5f, 51.5f, 52.5f, 53.5f, 54.5f, 55.5f, 
         56.5f, 57.5f, 58.5f, 59.5f, 60.5f, 61.5f, 62.5f, 63.5f, 65f, 
         67f, 69f, 71f, 73f, 75f, 77f, 79f, 81f, 83f, 85f, 87f, 89f, 91f, 
         93f, 95f, 97f, 99f, 101f, 103f, 105f, 107f, 109f, 111f, 113f, 
         115f, 117f, 119f, 121f, 123f, 125f, 127f, 130f, 134f, 138f, 
         142f, 146f, 150f, 154f, 158f, 162f, 166f, 170f, 174f, 178f, 
         182f, 186f, 190f, 194f, 198f, 202f, 206f, 210f, 214f, 218f, 
         222f, 226f, 230f, 234f, 238f, 242f, 246f, 250f, 254f, 260f, 
         268f, 276f, 284f, 292f, 300f, 308f, 316f, 324f, 332f, 340f, 
         348f, 356f, 364f, 372f, 380f, 388f, 396f, 404f, 412f, 420f, 
         428f, 436f, 444f, 452f, 460f, 468f, 476f, 484f, 492f, 500f, 
         508f, 520f, 536f, 552f, 568f, 584f, 600f, 616f, 632f, 648f, 
         664f, 680f, 696f, 712f, 728f, 744f, 760f, 776f, 792f, 808f, 
         824f, 840f, 856f, 872f, 888f, 904f, 920f, 936f, 952f, 968f, 
         984f, 1000f, 1016f, 1040f, 1072f, 1104f, 1136f, 1168f, 1200f, 
         1232f, 1264f, 1296f, 1328f, 1360f, 1392f, 1424f, 1456f, 1488f, 
         1520f, 1552f, 1584f, 1616f, 1648f, 1680f, 1712f, 1744f, 1776f, 
         1808f, 1840f, 1872f, 1904f, 1936f, 1968f, 2000f, 2032f, 2080f, 
         2144f, 2208f, 2272f, 2336f, 2400f, 2464f, 2528f, 2592f, 2656f,
         2720f, 2784f, 2848f, 2912f, 2976f, 3040f, 3104f, 3168f, 3232f, 
         3296f, 3360f, 3424f, 3488f, 3552f, 3616f, 3680f, 3744f, 3808f, 
         3872f, 3936f, 4000f, 4064
      },
      BIN_WIDTHS = {
         1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 
         1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 
         1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 
         1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 
         2f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 
         2f, 2f, 2f, 2f, 2f, 2f, 4f, 4f, 4f, 4f, 4f, 4f, 4f, 4f, 4f, 4f, 4f, 4f, 
         4f, 4f, 4f, 4f, 4f, 4f, 4f, 4f, 4f, 4f, 4f, 4f, 4f, 4f, 4f, 4f, 4f, 4f, 
         4f, 4f, 8f, 8f, 8f, 8f, 8f, 8f, 8f, 8f, 8f, 8f, 8f, 8f, 8f, 8f, 8f, 8f, 
         8f, 8f, 8f, 8f, 8f, 8f, 8f, 8f, 8f, 8f, 8f, 8f, 8f, 8f, 8f, 8f, 16f, 
         16f, 16f, 16f, 16f, 16f, 16f, 16f, 16f, 16f, 16f, 16f, 16f, 16f, 
         16f, 16f, 16f, 16f, 16f, 16f, 16f, 16f, 16f, 16f, 16f, 16f, 16f, 
         16f, 16f, 16f, 16f, 16f, 32f, 32f, 32f, 32f, 32f, 32f, 32f, 32f, 
         32f, 32f, 32f, 32f, 32f, 32f, 32f, 32f, 32f, 32f, 32f, 32f, 32f, 
         32f, 32f, 32f, 32f, 32f, 32f, 32f, 32f, 32f, 32f, 32f, 64f, 64f, 
         64f, 64f, 64f, 64f, 64f, 64f, 64f, 64f, 64f, 64f, 64f, 64f, 64f, 
         64f, 64f, 64f, 64f, 64f, 64f, 64f, 64f, 64f, 64f, 64f, 64f, 64f, 
         64f, 64f, 64f, 64f
      };

   public SSPC(final String path, final String pay, int d, int l){
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
         var.attribute("VALIDMIN", 15999999999L);
         var.attribute("VALIDMAX", 16000000001L);
         var.attribute("FILLVAL", Long.MIN_VALUE);
         this.cdf.addVar("HalfAccumTime", var);

         //Fill the "HalfAccumTime" variable
         var.writeData("HalfAccumTime", new long[] {16000000000L});

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
         "Slow time resolution (32s) X-ray spectrum"
      );
      this.cdf.attribute(
         "TEXT", 
         "Bremsstrahlung X-ray spectra each made of 256 energy bins " + 
         "transmitted over 32 frames."
      );
      this.cdf.attribute(
         "Instrument_type", "Electron Precipitation Bremsstrahlung"
      );
      this.cdf.attribute("Descriptor", "SSPC>Slow SPeCtrum");
      this.cdf.attribute("Time_resolution", "32s");
      this.cdf.attribute(
         "Logical_source", this.payload_id + "_l" + this.lvl  + "_sspc"
      );
      this.cdf.attribute(
         "Logical_file_id",
         this.payload_id + "_l" + this.lvl  + "_sspc_20" + this.date  + 
         "_V" + CDF_Gen.getSetting("rev")
      );
   }

   @Override
   protected void addVars(){
      CDFVar var;

      //create SSPC variable
      //This variable will contain the slow spectrum that is returned over
      //32 frames.
      var = new CDFVar(
            cdf, "SSPC", CDFConstants.CDF_FLOAT, 
            true, new  long[] {BIN_CENTERS.length} 
         );   

      var.attribute("FIELDNAM", "SSPC");
      var.attribute("CATDESC", "Slow Spectrum (32s)");
      var.attribute("LABLAXIS", "SSPC");
      var.attribute(
         "VAR_NOTES", 
         "Rebinned, divided by energy bin widths and " +
         "adjusted to /sec time scale." 
      );
      var.attribute("VAR_TYPE", "data");
      var.attribute("DEPEND_0", "Epoch");
      var.attribute("DEPEND_1", "energy");
      var.attribute("FORMAT", "F8.3");
      var.attribute("UNITS", "cnts/keV/sec");
      var.attribute("SCALETYP", "log");
      var.attribute("DISPLAY_TYPE", "spectrogram");
      var.attribute("VALIDMIN", 0.0f);
      var.attribute("VALIDMAX", 1e30f);
      var.attribute("FILLVAL", CDFVar.getIstpVal("FLOAT_FILL"));
      var.attribute("DELTA_PLUS_VAR", "cnt_error");
      var.attribute("DELTA_MINUS_VAR", "cnt_error");
      this.cdf.addVar("SSPC", var);

      //Create the "energy" variable
      //This variable lists the starting energy for each channel in keV
      var = new CDFVar(
            cdf, "energy", CDFConstants.CDF_FLOAT, 
            false, new  long[] {BIN_CENTERS.length} 
         );   

      var.attribute("FIELDNAM", "Energy Level");
      var.attribute("CATDESC", "Energy Level");
      var.attribute("LABLAXIS", "Energy");
      var.attribute("VAR_NOTES", "Center of each slow spectrum channel.");
      var.attribute("VAR_TYPE", "support_data");
      var.attribute("DEPEND_0", "Epoch");
      var.attribute("FORMAT", "F8.3");
      var.attribute("UNITS", "keV");
      var.attribute("SCALETYP", "log");
      var.attribute("VALIDMIN", 20.0f);
      var.attribute("VALIDMAX", 10000.0f);
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
      
      //Create the "channel" variable
      //Depend 1 variable for cnt_error.
      var = new CDFVar(
         this.cdf, "channel", CDFConstants.CDF_UINT2,
         false, new long[] {BIN_CENTERS.length}
      );

      var.attribute("FIELDNAM", "channel");
      var.attribute("CATDESC", "Channel Number");
      var.attribute("LABLAXIS", "Channel");
      var.attribute("VAR_NOTES", "Channel number 0 - 255.");
      var.attribute("VAR_TYPE", "support_data");
      var.attribute("DEPEND_0", "Epoch");
      var.attribute("FORMAT", "I3");
      var.attribute("UNITS", "channel");
      var.attribute("SCALETYP", "linear");
      var.attribute("VALIDMIN", 0);
      var.attribute("VALIDMAX", 255);
      var.attribute("FILLVAL", CDFVar.getIstpVal("UINT2_FILL"));
      this.cdf.addVar("channel", var);

      //Fill the "channel" variable
      int[][] channel = {{
         0,1,2,3,4,5,6,7,8,9,
         10,11,12,13,14,15,16,17,18,19,
         20,21,22,23,24,25,26,27,28,29,
         30,31,32,33,34,35,36,37,38,39,
         40,41,42,43,44,45,46,47,48,49,
         50,51,52,53,54,55,56,57,58,59,
         60,61,62,63,64,65,66,67,68,69,
         70,71,72,73,74,75,76,77,78,79,
         80,81,82,83,84,85,86,87,88,89,
         90,91,92,93,94,95,96,97,98,99,
         100,101,102,103,104,105,106,107,108,109,
         110,111,112,113,114,115,116,117,118,119,
         120,121,122,123,124,125,126,127,128,129,
         130,131,132,133,134,135,136,137,138,139,
         140,141,142,143,144,145,146,147,148,149,
         150,151,152,153,154,155,156,157,158,159,
         160,161,162,163,164,165,166,167,168,169,
         170,171,172,173,174,175,176,177,178,179,
         180,181,182,183,184,185,186,187,188,189,
         190,191,192,193,194,195,196,197,198,199,
         200,201,202,203,204,205,206,207,208,209,
         210,211,212,213,214,215,216,217,218,219,
         220,221,222,223,224,225,226,227,228,229,
         230,231,232,233,234,235,236,237,238,239,
         240,241,242,243,244,245,246,247,248,249,
         250,251,252,253,254,255
      }};
      var.writeData("channel", channel);
      channel = null;


      //Create a variable that will track each energy channel width
      var = new CDFVar(
            cdf, "HalfBinWidth", CDFConstants.CDF_FLOAT, 
            false, new  long[] {BIN_CENTERS.length} 
         );   

      var.attribute("FIELDNAM", "Bin Width");
      var.attribute("CATDESC", "Width of energy channel");
      var.attribute("LABLAXIS", "Width");
      var.attribute("VAR_TYPE", "support_data");
      var.attribute("DEPEND_0", "Epoch");
      var.attribute("FORMAT", "F6.3");
      var.attribute("UNITS", "keV");
      var.attribute("SCALETYP", "linear");
      var.attribute("VALIDMIN", 0.0f);
      var.attribute("VALIDMAX", 200.0f);
      var.attribute("FILLVAL", CDFVar.getIstpVal("FLOAT_FILL"));
      this.cdf.addVar("HalfBinWidth", var);

      //Fill the "BinWidth" variable
      float[][] bin_width = new float[1][BIN_WIDTHS.length];
      for(int bin_i = 0; bin_i < BIN_WIDTHS.length; bin_i++){
         bin_width[0][bin_i] = BIN_WIDTHS[bin_i] * scale / 2;
      }
      var.writeData("HalfBinWidth", bin_width);
      bin_width = null;

      //Create a variable that will track the 511 line peak
      var = new CDFVar(cdf, "Peak_511", CDFConstants.CDF_FLOAT);
      var.attribute("FIELDNAM", "Peak_511");
      var.attribute("CATDESC", "Peak location of the 511keV line.");
      var.attribute("VAR_TYPE", "data");
      var.attribute("LABLAXIS", "Peak_511");  
      var.attribute(
         "VAR_NOTES", 
         "This is the detector channel (0-4096) " + 
         "which appears to contain the 511"
      );
      var.attribute("DEPEND_0", "Epoch");
      var.attribute("FORMAT", "F6.3");
      var.attribute("UNITS", "channel");
      var.attribute("SCALETYP", "linear");
      var.attribute("DISPLAY_TYPE", "time_series");
      var.attribute("VALIDMIN", 0.0f);
      var.attribute("VALIDMAX", 4096.0f);
      var.attribute("FILLVAL", CDFVar.getIstpVal("FLOAT_FILL"));
      this.cdf.addVar("Peak_511", var);

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
   }
}
