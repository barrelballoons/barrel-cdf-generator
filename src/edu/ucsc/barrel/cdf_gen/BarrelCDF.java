/*
BarrelCDF.java

Description:
   Superclass for creating individual CDF files.

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

public class BarrelCDF extends File implements CDFConstants{
      private CDF cdf;

   public BarrelCDF(String path) throws CDFException{
      super(path);
      cdf = CDF.create(path);

      //get today's date
      DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
      Calendar cal = Calendar.getInstance();
      String date = dateFormat.format(cal.getTime());
      
      //calculate min and max epochs
      long min_epoch = CDFTT2000.fromUTCparts(2012, 00, 01);
      long max_epoch = CDFTT2000.fromUTCparts(2015, 11, 31);

      //Set all of the global attributes used by all BARREL CDF files
      Attribute 
         file_name_con = 
            Attribute.create(cdf, "File_naming_convention", GLOBAL_SCOPE), 
         data_type = 
            Attribute.create(cdf, "Data_type", GLOBAL_SCOPE), 
         src_desc = 
            Attribute.create(cdf, "Logical_source_description", GLOBAL_SCOPE), 
         mission_grp = 
            Attribute.create(cdf, "Mission_group", GLOBAL_SCOPE), 
         pi_aff = 
            Attribute.create(cdf, "PI_affiliation", GLOBAL_SCOPE), 
         src_name = 
            Attribute.create(cdf, "Source_name", GLOBAL_SCOPE), 
         project = 
            Attribute.create(cdf, "Project", GLOBAL_SCOPE), 
         pi = 
            Attribute.create(cdf, "PI_name", GLOBAL_SCOPE), 
         data_ver = 
            Attribute.create(cdf, "Data_version", GLOBAL_SCOPE), 
         text = 
            Attribute.create(cdf, "TEXT", GLOBAL_SCOPE), 
         instrument_type = 
            Attribute.create(cdf, "Instrument_type", GLOBAL_SCOPE), 
         descriptor = 
            Attribute.create(cdf, "Descriptor", GLOBAL_SCOPE), 
         discipline = 
            Attribute.create(cdf, "Discipline", GLOBAL_SCOPE), 
         ack = 
            Attribute.create(cdf, "Acknowledgement", GLOBAL_SCOPE), 
         link_title = 
            Attribute.create(cdf, "LINK_TITLE", GLOBAL_SCOPE), 
         gen_by = 
            Attribute.create(cdf, "Generated_by", GLOBAL_SCOPE), 
         rules = 
            Attribute.create(cdf, "Rules_of_use", GLOBAL_SCOPE), 
         gen_date = 
            Attribute.create(cdf, "Generation_date", GLOBAL_SCOPE), 
         link_http = 
            Attribute.create(cdf, "HTTP_LINK", GLOBAL_SCOPE), 
         mods = 
            Attribute.create(cdf, "MODS", GLOBAL_SCOPE), 
         time_res = 
            Attribute.create(cdf, "Time_resolution", GLOBAL_SCOPE), 
         adid_ref = 
            Attribute.create(cdf, "ADID_ref", GLOBAL_SCOPE), 
         logical_src = 
            Attribute.create(cdf, "Logical_source", GLOBAL_SCOPE), 
         loical_id = 
            Attribute.create(cdf, "Logical_file_id", GLOBAL_SCOPE); 

      Entry.create(file_name_con, 0, CDF_CHAR, "source_datatype_descriptor");
      Entry.create(mission_grp, 0, CDF_CHAR, "RBSP");
      Entry.create(pi_aff, 0, CDF_CHAR, "Dartmouth College");
      Entry.create(src_name, 0, CDF_CHAR, "Payload_ID");
      Entry.create(project, 0, CDF_CHAR, "LWS>Living With a Star>BARREL");
      Entry.create(pi, 0, CDF_CHAR, "Robyn Millan");
      Entry.create(data_ver, 0, CDF_CHAR, CDF_Gen.getSetting("rev"));
      Entry.create(
         discipline, 0, CDF_CHAR, "Space Physics>Magnetospheric Science"
      );
      Entry.create(link_title, 0, CDF_CHAR, "BARREL Data Repository");
      Entry.create(gen_by, 0, CDF_CHAR, "barreldata.ucsc.edu");
      Entry.create(
         rules, 0, CDF_CHAR, 
         "BARREL will make all its scientific data products quickly and " +
         "publicly available but all users are expected to read and follow " +
         "the \"BARREL Mission Data Usage Policy\" which can be " +
         "found on the BARREL website or obtained by contacting the BARREL " +
         "PI at Robyn.Millan@dartmouth.edu"
      );
      Entry.create(gen_date, 0, CDF_CHAR, date);
      Entry.create(link_http, 0, CDF_CHAR, "http://barreldata.ucsc.edu");

      //create variable attributes
      Attribute 
         field = 
            Attribute.create(cdf, "FIELDNAM", VARIABLE_SCOPE), 
         cat_desc = 
            Attribute.create(cdf, "CATDESC", VARIABLE_SCOPE), 
         var_notes = 
            Attribute.create(cdf, "VAR_NOTES", VARIABLE_SCOPE), 
         var_type = 
            Attribute.create(cdf, "VAR_TYPE", VARIABLE_SCOPE), 
         depend_0 = 
            Attribute.create(cdf, "DEPEND_0", VARIABLE_SCOPE), 
         from_ptr = 
            Attribute.create(cdf, "FROM_PTR", VARIABLE_SCOPE), 
         format = 
            Attribute.create(cdf, "FORMAT", VARIABLE_SCOPE), 
         unit_ptr = 
            Attribute.create(cdf, "UNIT_PTR", VARIABLE_SCOPE), 
         units = 
            Attribute.create(cdf, "UNITS", VARIABLE_SCOPE), 
         scal_ptr = 
            Attribute.create(cdf, "SCAL_PTR", VARIABLE_SCOPE), 
         scale_type = 
            Attribute.create(cdf, "SCALETYP", VARIABLE_SCOPE), 
         disp_type = 
            Attribute.create(cdf, "DISPLAY_TYPE", VARIABLE_SCOPE), 
         valid_min = 
            Attribute.create(cdf, "VALIDMIN", VARIABLE_SCOPE), 
         valid_max = 
            Attribute.create(cdf, "VALIDMAX", VARIABLE_SCOPE), 
         fill_val = 
            Attribute.create(cdf, "FILLVAL", VARIABLE_SCOPE), 
         label_axis = 
            Attribute.create(cdf, "LABLAXIS", VARIABLE_SCOPE), 
         monotonic = 
            Attribute.create(cdf, "MONOTON", VARIABLE_SCOPE), 
         leap_second_inc = 
            Attribute.create(cdf, "LEAP_SECONDS_INCLUDED", VARIABLE_SCOPE), 
         res = 
            Attribute.create(cdf, "RESOLUTION", VARIABLE_SCOPE), 
         bin_location = 
            Attribute.create(cdf, "Bin_Location", VARIABLE_SCOPE), 
         time_base = 
            Attribute.create(cdf, "TIME_BASE", VARIABLE_SCOPE), 
         time_scale = 
            Attribute.create(cdf, "TIME_SCALE", VARIABLE_SCOPE), 
         ref_pos = 
            Attribute.create(cdf, "REFERENCE_POSITION", VARIABLE_SCOPE),
         abs_err = 
            Attribute.create(cdf, "ABSOLUTE_ERROR", VARIABLE_SCOPE), 
         rel_err = 
            Attribute.create(cdf, "RELATIVE_ERROR", VARIABLE_SCOPE), 
         depend_1 = 
            Attribute.create(cdf, "DEPEND_1", VARIABLE_SCOPE), 
         delta_plus = 
            Attribute.create(cdf, "DELTA_PLUS_VAR", VARIABLE_SCOPE), 
         delta_minus = 
            Attribute.create(cdf, "DELTA_MINUS_VAR", VARIABLE_SCOPE); 

      //create variables used by all CDFs
      Variable
         epoch = 
            Variable.create(
               cdf, "Epoch", CDF_TIME_TT2000, 1L, 0L, new  long[] {1}, 
               VARY, new long[] {NOVARY}
         ),   
         frameGroup = 
            Variable.create(
               cdf, "FrameGroup", CDF_INT4, 1L, 0L, new  long[] {1}, 
               VARY, new long[] {NOVARY}
         ),
         q = 
            Variable.create(
               cdf, "Q", CDF_INT4, 1L, 0L, new  long[] {1}, 
               VARY, new long[] {NOVARY}
         );

      //fill the attributes for the variables in each file
      Entry.create(field, epoch.getID(), CDF_CHAR, "Epoch");
      Entry.create(cat_desc, epoch.getID(), CDF_CHAR, "Default time");
      Entry.create(var_type, epoch.getID(), CDF_CHAR, "support_data");
      Entry.create(units, epoch.getID(), CDF_CHAR, "ns");
      Entry.create(scale_type, epoch.getID(), CDF_CHAR, "linear");
      Entry.create(valid_min, epoch.getID(), CDF_TIME_TT2000, min_epoch);
      Entry.create(valid_max, epoch.getID(), CDF_TIME_TT2000, max_epoch);
      Entry.create(
         fill_val, epoch.getID(), CDF_TIME_TT2000, 
         -9223372036854775808L
      );
      Entry.create(label_axis, epoch.getID(), CDF_CHAR, "Epoch");
      Entry.create(monotonic, epoch.getID(), CDF_CHAR, "INCREASE");
      Entry.create(time_base, epoch.getID(), CDF_CHAR, "J2000");
      Entry.create(time_scale, epoch.getID(), CDF_CHAR, "Terrestrial Time");
      Entry.create(ref_pos, epoch.getID(), CDF_CHAR, "Rotating Earth Geoid");

      Entry.create(field, frameGroup.getID(), CDF_CHAR, "Frame Number");
      Entry.create(
         cat_desc, frameGroup.getID(), CDF_CHAR, "DPU Frame Counter."
      );
      Entry.create(var_type, frameGroup.getID(), CDF_CHAR, "support_data");
      Entry.create(depend_0, frameGroup.getID(), CDF_CHAR,  "Epoch");
      Entry.create(format, frameGroup.getID(), CDF_CHAR,  "%u");
      Entry.create(scale_type, frameGroup.getID(), CDF_CHAR,  "linear");
      Entry.create(disp_type, frameGroup.getID(), CDF_CHAR,  "time_series");
      Entry.create(valid_min, frameGroup.getID(), CDF_INT4,  0);
      Entry.create(valid_max, frameGroup.getID(), CDF_INT4,  2147483647);
      Entry.create(fill_val, frameGroup.getID(), CDF_INT4,  -2147483648);
      Entry.create(label_axis, frameGroup.getID(), CDF_CHAR,  "Frame");

      Entry.create(field, q.getID(), CDF_CHAR, "Data Quality");
      Entry.create(
         cat_desc, q.getID(), CDF_CHAR, 
         "32 bit flag used to indicate data quality."
      );
      Entry.create(var_type, q.getID(), CDF_CHAR, "data");
      Entry.create(depend_0, q.getID(), CDF_CHAR, "Epoch");
      Entry.create(format, q.getID(), CDF_CHAR, "%u");
      Entry.create(scale_type, q.getID(), CDF_CHAR, "linear");
      Entry.create(disp_type, q.getID(), CDF_CHAR, "time_series");
      Entry.create(valid_min, q.getID(), CDF_INT4, -2147483648);
      Entry.create(valid_max, q.getID(), CDF_INT4, 2147483647);
      Entry.create(fill_val, q.getID(), CDF_INT4, -2147483648);
      Entry.create(label_axis, q.getID(), CDF_CHAR, "Q");
   }

   public CDF getCDF(){return cdf;}

   public void writeData(String name, int[] data) throws CDFException{
      Variable var = this.cdf.getVariable(name);
      long start = var.getNumWrittenRecords();
      long size = data.length;
      var.putHyperData(
         start, size, 1, 
         new long[] {0}, new long[] {1}, new long[] {1},
         data
      );
   }
   public void writeData(String name, long[] data) throws CDFException{
      Variable var = this.cdf.getVariable(name);
      long start = var.getNumWrittenRecords();
      long size = data.length;
      var.putHyperData(
         start, size, 1, 
         new long[] {0}, new long[] {1}, new long[] {1},
         data
      );
   }
   public void writeData(String name, float[] data) throws CDFException{
      Variable var = this.cdf.getVariable(name);
      long start = var.getNumWrittenRecords();
      long size = data.length;
      var.putHyperData(
         start, size, 1, 
         new long[] {0}, new long[] {1}, new long[] {1},
         data
      );
   }
   public void writeData(String name, double[] data) throws CDFException{
      Variable var = this.cdf.getVariable(name);
      long start = var.getNumWrittenRecords();
      long size = data.length;
      var.putHyperData(
         start, size, 1, 
         new long[] {0}, new long[] {1}, new long[] {1},
         data
      );
   }
   public void writeData(String name, String[] data) throws CDFException{
      Variable var = this.cdf.getVariable(name);
      long start = var.getNumWrittenRecords();
      long size = data.length;
      var.putHyperData(
         start, size, 1, 
         new long[] {0}, new long[] {1}, new long[] {1},
         data
      );
   }
   public void writeData(String name, int[][] data) throws CDFException{
      Variable var = this.cdf.getVariable(name);
      long start = var.getNumWrittenRecords();
      long size = data.length;
      long[] dimCnts = {data[0].length, 1};
      var.putHyperData(
         start, size, 1, 
         new long[] {0}, new long[] {256, 1}, new long[] {1},
         data
      );
   }
   public void writeData(String name, long[][] data) throws CDFException{
      Variable var = this.cdf.getVariable(name);
      long start = var.getNumWrittenRecords();
      long size = data.length;
      long[] dimCnts = {data[0].length, 1};
      var.putHyperData(
         start, size, 1, 
         new long[] {0}, new long[] {256, 1}, new long[] {1},
         data
      );
   }
   public void writeData(String name, float[][] data) throws CDFException{
      Variable var = this.cdf.getVariable(name);
      long start = var.getNumWrittenRecords();
      long size = data.length;
      long[] dimCnts = {data[0].length, 1};
      var.putHyperData(
         start, size, 1, 
         new long[] {0}, new long[] {256, 1}, new long[] {1},
         data
      );
   }
   public void writeData(String name, double[][] data) throws CDFException{
      Variable var = this.cdf.getVariable(name);
      long start = var.getNumWrittenRecords();
      long size = data.length;
      long[] dimCnts = {data[0].length, 1};
      var.putHyperData(
         start, size, 1, 
         new long[] {0}, new long[] {256, 1}, new long[] {1},
         data
      );
   }

   public void close() throws CDFException{
      cdf.close();
   }
}
