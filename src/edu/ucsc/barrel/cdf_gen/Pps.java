/*
Pps.java

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

public class Pps extends BarrelCDF{
   private CDF cdf;
   private Variable var;
   private long id;
   private String path;
   private int date, lvl;

   public Pps(final String p, final int d, final int l){
      super(p, l);
      path = p;
      date = d;
      lvl = l;

      try{
         cdf = super.getCDF();
      
         addPpsGlobalAtts();
         addPpsVars();
      }catch(CDFException e){
         System.out.println(e.getMessage());
      }
   }

   private void addPpsGlobalAtts() throws CDFException{
      //Set global attributes specific to this type of CDF
      setAttribute(
         "Logical_source_description", "Pulse Per Second"
      );
      setAttribute(
         "TEXT", 
         "Number of milliseconds into the frame when " + 
         "the GPS pulse per second arrived."
      );
      setAttribute("Instrument_type", "GPS");
      setAttribute("Descriptor", "GPS");
      setAttribute("Time_resolution", "1Hz");
      setAttribute("Logical_source", "payload_id_l" + lvl  + "_gps");
      setAttribute(
         "Logical_file_id",
         "payload_id_l" + lvl  + "_gps_20" + date  + 
         "_V" + CDF_Gen.getSetting("rev")
      );
   }

   private void addPpsVars() throws CDFException{
      var = 
         Variable.create(
            cdf, "GPS_PPS", CDF_INT2, 1L, 0L, new  long[] {1}, 
            VARY, new long[] {NOVARY}
         );   
      id = var.getID();
      setAttribute("FIELDNAM", "Pulse Per Second", VARIABLE_SCOPE, id);
      setAttribute(
         "CATDESC", "Milliseconds before GPS pulse arrived.", 
         VARIABLE_SCOPE, id
      );
      setAttribute("VAR_TYPE", "data", VARIABLE_SCOPE, id);
      setAttribute("DEPEND_0", "Epoch", VARIABLE_SCOPE, id);
      setAttribute("FORMAT", "%i", VARIABLE_SCOPE, id);
      setAttribute("UNITS", "ms", VARIABLE_SCOPE, id);
      setAttribute("SCALETYP", "linear", VARIABLE_SCOPE, id);
      setAttribute("DISPLAY_TYPE", "time_series", VARIABLE_SCOPE, id);
      setAttribute("VALIDMIN", 0, VARIABLE_SCOPE, id, CDF_INT2);
      setAttribute("VALIDMAX", 1000, VARIABLE_SCOPE, id, CDF_INT2);
      setAttribute(
         "FILLVAL", Constants.INT2_FILL, VARIABLE_SCOPE, id, CDF_INT2
      );
      setAttribute("LABLAXIS", "PPS", VARIABLE_SCOPE, id);

      var = 
         Variable.create(
            cdf, "Version", CDF_INT2, 1L, 0L, new  long[] {1}, 
            VARY, new long[] {NOVARY}
         );   
      id = var.getID();
      setAttribute("FIELDNAM", "Software Version", VARIABLE_SCOPE, id);
      setAttribute("CATDESC", "Software Version.", VARIABLE_SCOPE, id);
      setAttribute("VAR_TYPE", "data", VARIABLE_SCOPE, id);
      setAttribute("DEPEND_0", "Epoch", VARIABLE_SCOPE, id);
      setAttribute("FORMAT", "%i", VARIABLE_SCOPE, id);
      setAttribute("SCALETYP", "linear", VARIABLE_SCOPE, id);
      setAttribute("DISPLAY_TYPE", "time_series", VARIABLE_SCOPE, id);
      setAttribute("VALIDMIN", 0, VARIABLE_SCOPE, id, CDF_INT2);
      setAttribute("VALIDMAX", 32, VARIABLE_SCOPE, id, CDF_INT2);
      setAttribute(
         "FILLVAL", Constants.INT2_FILL, VARIABLE_SCOPE, id, CDF_INT2
      );
      setAttribute("LABLAXIS", "Version", VARIABLE_SCOPE, id);

      var = 
         Variable.create(
            cdf, "Payload_ID", CDF_INT2, 1L, 0L, new  long[] {1}, 
            VARY, new long[] {NOVARY}
         );   
      id = var.getID();
      setAttribute("FIELDNAM", "Payload ID", VARIABLE_SCOPE, id);
      setAttribute(
         "CATDESC", "ID transmitted by the payload's DPU.", VARIABLE_SCOPE, id
      );
      setAttribute("VAR_TYPE", "data", VARIABLE_SCOPE, id);
      setAttribute("DEPEND_0", "Epoch", VARIABLE_SCOPE, id);
      setAttribute("FORMAT", "%i", VARIABLE_SCOPE, id);
      setAttribute("SCALETYP", "linear", VARIABLE_SCOPE, id);
      setAttribute("DISPLAY_TYPE", "time_series", VARIABLE_SCOPE, id);
      setAttribute("VALIDMIN", 0, VARIABLE_SCOPE, id, CDF_INT2);
      setAttribute("VALIDMAX", 64, VARIABLE_SCOPE, id, CDF_INT2);
      setAttribute(
         "FILLVAL", Constants.INT2_FILL, VARIABLE_SCOPE, id, CDF_INT2
      );
      setAttribute("LABLAXIS", "ID", VARIABLE_SCOPE, id);
   }
}
