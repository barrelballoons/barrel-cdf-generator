/*
FSPC.java

Description:
   Creates FSPC CDF files.

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

public class FSPC extends DataProduct{
   private int date, lvl, version;
   private String payload_id;
   private float scale = 2.4414f; // keV/bin

   public final static float[] 
      BIN_EDGES = {0f, 20f, 40f, 75f, 230f, 350f, 620f}, 
      BIN_CENTERS = {10f, 30f, 57.5f, 152.5f, 290f, 485f}, 
      BIN_WIDTHS = {20f, 20f, 35f, 155f, 120f, 250f},
      OLD_BIN_EDGES = {0f, 75f, 230f, 350f, 620f}, 
      OLD_BIN_CENTERS = {37.5f, 152.5f, 290f, 485f}, 
      OLD_BIN_WIDTHS = {75f, 155f, 120f, 250f};

   public FSPC(final String path, final String pay, int d, int l, int v){
      this.date = d;
      this.lvl = l;
      this.payload_id = pay;
      this.version = v;

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
         "Fast time resolution (50ms) Bremsstrahlung X-ray spectrum."
      );
      cdf.attribute(
         "TEXT", 
         "Fast time resolution Bremsstrahlung X-ray spectrum from " +
         "NaI Scintillator, four channels at 20 MHz."
      );
      cdf.attribute("Instrument_type", "Electron Precipitation Bremsstrahlung");
      cdf.attribute("Descriptor", "FSPC>Fast SPeCtrum");
      cdf.attribute("Time_resolution", "20Hz");
      cdf.attribute(
         "Logical_source", this.payload_id + "_l" + this.lvl  + "_fspc"
      );
      cdf.attribute(
         "Logical_file_id",
         this.payload_id + "_l" + this.lvl  + "_fspc_20" + this.date  + 
         "_V" + CDF_Gen.getSetting("rev")
      );
   }

   @Override
   protected void addVars(){
      if(this.version <= 3){
         addFSPC("1");
      }else{
         addFSPC("1a");
         addFSPC("1b");
         addFSPC("1c");
      }
      addFSPC("2");
      addFSPC("3");
      addFSPC("4");

      //create variable for tracking energy edges
      CDFVar var;
      var = new CDFVar(
            cdf, "FSPC_Edges", CDFConstants.CDF_FLOAT, 
            true, new  long[] {7L} 
         );   

      var.attribute("FIELDNAM", "FPSC_Edges");
      var.attribute("CATDESC", "Fast Spectrum Channel Boundaries");
      var.attribute("LABLAXIS", "FSPC_Edges");
      var.attribute(
         "VAR_NOTES", 
         "Each element of the array represents the boundaries that separate " +
         "each energy channel. That is, FSPC1 lies between elements 0 and 1, " +
         "FSPC2 between 1 and 2, FSPC3 between 2 and 3, and FSPC4 between 3 " +
         "and 4." 
      );
      var.attribute("VAR_TYPE", "support_data");
      var.attribute("DEPEND_0", "Epoch");
      //var.attribute("DEPEND_1", "energy");
      var.attribute("FORMAT", "F8.3");
      var.attribute("UNITS", "keV");
      var.attribute("SCALETYP", "linear");
      var.attribute("VALIDMIN", 0.0f);
      var.attribute("VALIDMAX", 1e30f);
      var.attribute("FILLVAL", CDFVar.getIstpVal("FLOAT_FILL"));
      this.cdf.addVar("FSPC_Edges", var);

   }

   private void addFSPC(final String ch){
      CDFVar var;

      //create FSPC variable
      var = new CDFVar(cdf, "FSPC" + ch, CDFConstants.CDF_INT4);

      var.attribute("FIELDNAM", "FSPC" + ch);
      var.attribute("CATDESC", "Fast spectra (50ms) ch. " + ch);
      var.attribute("LABLAXIS", "FSPC" + ch);
      var.attribute("VAR_TYPE", "data");
      var.attribute("DEPEND_0", "Epoch");
      var.attribute("FORMAT", "I5");
      var.attribute("UNITS", "cnts/50ms");
      var.attribute("SCALETYP", "linear");
      var.attribute("DISPLAY_TYPE", "time_series");
      var.attribute("VALIDMIN", 0);
      var.attribute("VALIDMAX", 65535);
      var.attribute("FILLVAL", CDFVar.getIstpVal("INT4_FILL"));
      var.attribute("DELTA_PLUS_VAR", "cnt_error" + ch);
      var.attribute("DELTA_MINUS_VAR", "cnt_error" + ch);
      this.cdf.addVar("FSPC" + ch, var);

      //Create the count error variable
      var = new CDFVar(cdf, "cnt_error" + ch, CDFConstants.CDF_FLOAT);
      var.attribute("FIELDNAM", "Count Error " + ch);
      var.attribute(
         "CATDESC", 
         "Count error based on Poisson statistics for FSPC " + ch + "."
      );
      var.attribute("LABLAXIS", "Error");
      var.attribute("VAR_NOTES", "Error only valid for large count values.");
      var.attribute("VAR_TYPE", "support_data");
      var.attribute("DEPEND_0", "Epoch");
      var.attribute("FORMAT", "F6.3");
      var.attribute("UNITS", "cnts/50ms");
      var.attribute("SCALETYP", "linear");
      var.attribute("VALIDMIN", 0.0f);
      var.attribute("VALIDMAX", 10000.0f);
      var.attribute("FILLVAL", CDFVar.getIstpVal("FLOAT_FILL"));
      this.cdf.addVar("cnt_error" + ch, var);
   }

   public static Channel[] getChannels(int version){
      if(version < 3){
         return {
            new Channel(32, 65535),
            new Channel(16, 65535),
            new Channel(8, 255),
            new Channel(0, 255)
         }
      } else {
         return {
            new Channel(39, 511),
            new Channel(30, 511),
            new Channel(22, 255),
            new Channel(13, 511),
            new Channel(6, 127),
            new Channel(0, 63)
         }
      }
   }
}

class Channel{
   public int start = 0;
   public int width = 0;

   public Channel(int s, int w){
      start = s;
      width = w;
   }
}
