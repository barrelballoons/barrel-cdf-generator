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

public class FSPC extends BarrelCDF{
   private CDF cdf;
   private Variable var;
   private long id;
   private String path;
   private int date, lvl;

   private double scale = 2.4414; // keV/bin

   public final static double[] 
      BIN_EDGES = {0, 75, 230, 350, 620},
      BIN_CENTERS = {37.5, 77.5, 410, 485},
      BIN_WIDTHS = {75, 155, 120, 250};

   public FSPC(final String p, final int d, final int l){
      super(p, l);
      path = p;
      date = d;
      lvl = l;

      try{
         cdf = super.getCDF();
      
         addFspcGlobalAtts();
         addLC(1);
         addLC(2);
         addLC(3);
         addLC(4);
      }catch(CDFException e){
         System.out.println(e.getMessage());
      }
   }

   private void addFspcGlobalAtts() throws CDFException{
      //Set global attributes specific to this type of CDF
      setAttribute(
         "Logical_source_description", "Fast time resolution X-ray spectrum"
      );
      setAttribute(
         "TEXT", "Four channels of fast spectral data are returned at 20Hz." 
      );
      setAttribute("Instrument_type", "Gamma and X-Rays");
      setAttribute("Descriptor", "Scintillator");
      setAttribute("Time_resolution", "20Hz");
      setAttribute("Logical_source", "payload_id_l" + lvl  + "_scintillator");
      setAttribute(
         "Logical_file_id",
         "payload_id_l" + lvl  + "_scintillator_20" + date  + 
         "_V" + CDF_Gen.getSetting("rev")
      );
   }

   private void addLC(final int ch) throws CDFException{
      //create FSPC variable
      var = 
         Variable.create(
            cdf, "LC" + ch, CDF_INT4, 1L, 0L, new  long[] {1}, 
            VARY, new long[] {NOVARY}
         );   
      id = var.getID();

      setAttribute("FIELDNAM", "LC" + ch, VARIABLE_SCOPE, id);
      setAttribute("CATDESC", "FSPC channel " + ch, VARIABLE_SCOPE, id);
      setAttribute("VAR_TYPE", "data", VARIABLE_SCOPE, id);
      setAttribute("DEPEND_0", "Epoch", VARIABLE_SCOPE, id);
      setAttribute("FORMAT", "%u", VARIABLE_SCOPE, id);
      setAttribute("UNITS", "cnts/50ms", VARIABLE_SCOPE, id);
      setAttribute("SCALETYP", "log", VARIABLE_SCOPE, id);
      setAttribute("DISPLAY_TYPE", "time_series", VARIABLE_SCOPE, id);
      setAttribute("VALIDMIN", 0.0, VARIABLE_SCOPE, id, CDF_INT4);
      setAttribute("VALIDMAX", 65535, VARIABLE_SCOPE, id, CDF_INT4);
      setAttribute(
         "FILLVAL", Constants.INT4_FILL, VARIABLE_SCOPE, id, CDF_INT4
      );
      setAttribute("LABLAXIS", "LC" + ch, VARIABLE_SCOPE, id);
   }
}
