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

public class FSPC{
   private BarrelCDF cdf;
   private CDFVar var;
   private String path;
   private int date, lvl;

   private double scale = 2.4414; // keV/bin

   public final static double[] 
      BIN_EDGES = {0, 75, 230, 350, 620},
      BIN_CENTERS = {37.5, 77.5, 410, 485},
      BIN_WIDTHS = {75, 155, 120, 250};

   public FSPC(final String p, final int d, final int l){
      this.cdf = new BarrelCDF(p, l);
      this.path = p;
      this.date = d;
      this.lvl = l;

      addFspcGlobalAtts();
      addLC(1);
      addLC(2);
      addLC(3);
      addLC(4);
   }

   private void addFspcGlobalAtts(){
      //Set global attributes specific to this type of CDF
      cdf.attribute(
         "Logical_source_description", "Fast time resolution X-ray spectrum"
      );
      cdf.attribute(
         "TEXT", "Four channels of fast spectral data are returned at 20Hz." 
      );
      cdf.attribute("Instrument_type", "Gamma and X-Rays");
      cdf.attribute("Descriptor", "Scintillator");
      cdf.attribute("Time_resolution", "20Hz");
      cdf.attribute("Logical_source", "payload_id_l" + lvl  + "_scintillator");
      cdf.attribute(
         "Logical_file_id",
         "payload_id_l" + lvl  + "_scintillator_20" + date  + 
         "_V" + CDF_Gen.getSetting("rev")
      );
   }

   private void addLC(final int ch){
      //create FSPC variable
      var = new CDFVar(cdf, "LC" + ch, CDFConstants.CDF_INT4);

      var.attribute("FIELDNAM", "LC" + ch);
      var.attribute("CATDESC", "FSPC channel " + ch);
      var.attribute("VAR_TYPE", "data");
      var.attribute("DEPEND_0", "Epoch");
      var.attribute("FORMAT", "%u");
      var.attribute("UNITS", "cnts/50ms");
      var.attribute("SCALETYP", "log");
      var.attribute("DISPLAY_TYPE", "time_series");
      
      var.attribute("VALIDMAX", 65535);
      var.attribute("FILLVAL", Constants.INT4_FILL);
      var.attribute("LABLAXIS", "LC" + ch);
   }

   public void close(){
      this.cdf.close();
   }
}
