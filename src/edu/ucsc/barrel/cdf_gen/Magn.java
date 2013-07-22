/*
Magn.java

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

public class Magn extends BarrelCDF{
   private CDF cdf;
   private Variable var;
   private long id;
   private String path;
   private int date, lvl;

   public Magn(final String p, final int d, final int l){
      super(p, l);
      path = p;
      date = d;
      lvl = l;

      try{
         cdf = super.getCDF();
      
         addMagGlobalAtts();
         addMagVar("X");
         addMagVar("Y");
         addMagVar("Z");
         addTotalVar();
      }catch(CDFException e){
         System.out.println(e.getMessage());
      }
   }

   private void addMagGlobalAtts() throws CDFException{
      //Set global attributes specific to this type of CDF
      setAttribute(
         "Logical_source_description", "MAG X, Y, and Z"
      );
      setAttribute(
         "TEXT", "Three axis magnetometer reading with nominal conversion. " +
         "Data are neither gain corrected nor despun."
      );
      setAttribute("Instrument_type", "Magnetic Fields (space)");
      setAttribute("Descriptor", "Magnetometer");
      setAttribute("Time_resolution", "4Hz");
      setAttribute("Logical_source", "payload_id_l" + lvl  + "_magnetometer");
      setAttribute(
         "Logical_file_id",
         "payload_id_l" + lvl  + "_magnetometer_20" + date  + 
         "_V" + CDF_Gen.getSetting("rev")
      );
   }

   private void addMagVar(final String axis) throws CDFException{
      //create mag variable
      var = 
         Variable.create(
            cdf, "MAG_" + axis, CDF_FLOAT, 1L, 0L, new  long[] {1}, 
            VARY, new long[] {NOVARY}
         );   
      id = var.getID();

      setAttribute("FIELDNAM", axis + "_axis", VARIABLE_SCOPE, id);
      setAttribute(
         "CATDESC", axis + " axis of magnetic field", VARIABLE_SCOPE, id
      );
      setAttribute(
         "VAR_NOTES", 
         "Calculated as (raw_value - 8388608) / 83886.070. " +
         "Contains fluctuations due to payload rotations.", 
         VARIABLE_SCOPE, id
      );
      setAttribute("VAR_TYPE", "data", VARIABLE_SCOPE, id);
      setAttribute("DEPEND_0", "Epoch", VARIABLE_SCOPE, id);
      setAttribute("FORMAT", "%f", VARIABLE_SCOPE, id);
      setAttribute("UNITS", "uT", VARIABLE_SCOPE, id);
      setAttribute("SCALETYP", "linear", VARIABLE_SCOPE, id);
      setAttribute("DISPLAY_TYPE", "time_series", VARIABLE_SCOPE, id);
      setAttribute("VALIDMIN", -1e31f, VARIABLE_SCOPE, id, CDF_FLOAT);
      setAttribute("VALIDMAX", 1e31f, VARIABLE_SCOPE, id, CDF_FLOAT);
      setAttribute(
         "FILLVAL", Constants.FLOAT_FILL, VARIABLE_SCOPE, id, CDF_FLOAT
      );
      setAttribute("LABLAXIS", "B_" + axis, VARIABLE_SCOPE, id);
   }

   private void addTotalVar() throws CDFException{
      var = 
         Variable.create(
            cdf, "Total", CDF_FLOAT, 1L, 0L, new  long[] {1}, 
            VARY, new long[] {NOVARY}
         );   
      id = var.getID();

      setAttribute("FIELDNAM", "Total Magnetic Field", VARIABLE_SCOPE, id);
      setAttribute(
         "CATDESC", "Magnitude of magnetic field.", 
         VARIABLE_SCOPE, id
      );
      setAttribute(
         "VAR_NOTES", 
         "Bt = sqrt(Bx^2 + By^2 + Bz^2)..Value has variations due to payload" +
         " rotations and Bx, By, and Bz not being gain corrected.", 
         VARIABLE_SCOPE, id
      );
      setAttribute("VAR_TYPE", "data", VARIABLE_SCOPE, id);
      setAttribute("DEPEND_0", "Epoch", VARIABLE_SCOPE, id);
      setAttribute("FORMAT", "%f", VARIABLE_SCOPE, id);
      setAttribute("UNITS", "uT", VARIABLE_SCOPE, id);
      setAttribute("SCALETYP", "linear", VARIABLE_SCOPE, id);
      setAttribute("DISPLAY_TYPE", "time_series", VARIABLE_SCOPE, id);
      setAttribute("VALIDMIN", -1e31f, VARIABLE_SCOPE, id, CDF_FLOAT);
      setAttribute("VALIDMAX", 1e31f, VARIABLE_SCOPE, id, CDF_FLOAT);
      setAttribute(
         "FILLVAL", Constants.FLOAT_FILL, VARIABLE_SCOPE, id, CDF_FLOAT
      );
      setAttribute("LABLAXIS", "B_tot", VARIABLE_SCOPE, id);
   }
}
