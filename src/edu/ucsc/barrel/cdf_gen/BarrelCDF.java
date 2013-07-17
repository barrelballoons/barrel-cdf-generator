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

public class BarrelCDF implements CDFConstants{
      private CDF cdf;
      private String path;
      private int lastRec;

   public BarrelCDF(String p) throws CDFException{
      path = p;

      //create a new CDF or open an existing one. 
      if(!(new File(path)).exists()){create();}
      if(cdf == null){cdf = CDF.open(path);}

   }

   public void create() throws CDFException{
      cdf = CDF.create(path);

      //get today's date
      DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
      Calendar cal = Calendar.getInstance();
      String date = dateFormat.format(cal.getTime());
      
      //calculate min and max epochs
      long min_epoch = CDFTT2000.fromUTCparts(2012, 00, 01);
      long max_epoch = CDFTT2000.fromUTCparts(2015, 11, 31);
      
      //fill global_attrs HashMap with attribute info used by all CDFs
      setAttribute("File_naming_convention", "source_datatype_descriptor");
      setAttribute("Mission_group", "RBSP");
      setAttribute("PI_affiliation","Dartmouth College");
      setAttribute("Source_name", "Payload_ID");
      setAttribute("Project","LWS>Living With a Star>BARREL");
      setAttribute("PI_name", "Robyn Millan");
      setAttribute("Data_version", CDF_Gen.getSetting("rev"));
      setAttribute("Discipline", "Space Physics>Magnetospheric Science");
      setAttribute("LINK_TITLE", "BARREL Data Repository");
      setAttribute("Generated_by", "BARREL CDF Generator");
      setAttribute("test", Long.valueOf(11111111));
      setAttribute(
         "Rules_of_use",  
         "BARREL will make all its scientific data products quickly and " +
         "publicly available but all users are expected to read and follow " +
         "the \"BARREL Mission Data Usage Policy\" which can be found in " +
         "the BARREL data repository or obtained by contacting " + 
         "barrelballoons@gmail.com"
      );
      setAttribute("Generation_date", String.valueOf(date));
      setAttribute("HTTP_LINK","http://barreldata.ucsc.edu");
      
      //unused global variables.
      /*
      setAttribute("Acknowledgement", " "); 
      setAttribute(cdf, "MODS", " "); 
      setAttribute(cdf, "Time_resolution", " "); 
      setAttribute(cdf, "ADID_ref", " "); 
      setAttribute(cdf, "Logical_source", " "); 
      setAttribute(cdf, "Logical_file_id", " "); 
      setAttribute(cdf, "TEXT", " "); 
      setAttribute(cdf, "Instrument_type", " "); 
      setAttribute(cdf, "Descriptor", " "); 
      setAttribute(cdf, "Data_type", " "); 
      setAttribute(cdf, "Logical_source_description", " ");
      */

      /*
      //create variable attributes
      Attribute.create(cdf, "FIELDNAM", VARIABLE_SCOPE);          
      Attribute.create(cdf, "CATDESC", VARIABLE_SCOPE);          
      Attribute.create(cdf, "VAR_NOTES", VARIABLE_SCOPE);          
      Attribute.create(cdf, "VAR_TYPE", VARIABLE_SCOPE);          
      Attribute.create(cdf, "DEPEND_0", VARIABLE_SCOPE);          
      Attribute.create(cdf, "FROM_PTR", VARIABLE_SCOPE);          
      Attribute.create(cdf, "FORMAT", VARIABLE_SCOPE);          
      Attribute.create(cdf, "UNIT_PTR", VARIABLE_SCOPE);          
      Attribute.create(cdf, "UNITS", VARIABLE_SCOPE);          
      Attribute.create(cdf, "SCAL_PTR", VARIABLE_SCOPE);          
      Attribute.create(cdf, "SCALETYP", VARIABLE_SCOPE);          
      Attribute.create(cdf, "DISPLAY_TYPE", VARIABLE_SCOPE);          
      Attribute.create(cdf, "VALIDMIN", VARIABLE_SCOPE);          
      Attribute.create(cdf, "VALIDMAX", VARIABLE_SCOPE);          
      Attribute.create(cdf, "FILLVAL", VARIABLE_SCOPE);          
      Attribute.create(cdf, "LABLAXIS", VARIABLE_SCOPE);          
      Attribute.create(cdf, "MONOTON", VARIABLE_SCOPE);          
      Attribute.create(cdf, "LEAP_SECONDS_INCLUDED", VARIABLE_SCOPE); 
      Attribute.create(cdf, "RESOLUTION", VARIABLE_SCOPE);          
      Attribute.create(cdf, "Bin_Location", VARIABLE_SCOPE);          
      Attribute.create(cdf, "TIME_BASE", VARIABLE_SCOPE);          
      Attribute.create(cdf, "TIME_SCALE", VARIABLE_SCOPE);          
      Attribute.create(cdf, "REFERENCE_POSITION", VARIABLE_SCOPE);
      Attribute.create(cdf, "ABSOLUTE_ERROR", VARIABLE_SCOPE);          
      Attribute.create(cdf, "RELATIVE_ERROR", VARIABLE_SCOPE);          
      Attribute.create(cdf, "DEPEND_1", VARIABLE_SCOPE);          
      Attribute.create(cdf, "DELTA_PLUS_VAR", VARIABLE_SCOPE);          
      Attribute.create(cdf, "DELTA_MINUS_VAR", VARIABLE_SCOPE);
      */

      //create variables used by all CDFs
      Variable var;
      long id;

      var = 
         Variable.create(
            cdf, "Epoch", CDF_TIME_TT2000, 1L, 0L, new  long[] {1}, 
            VARY, new long[] {NOVARY}
      ); 
      id = var.id();

      var = 
         Variable.create(
            cdf, "FrameGroup", CDF_INT4, 1L, 0L, new  long[] {1}, 
            VARY, new long[] {NOVARY}
      );
      id = var.id();
      
      var = 
         Variable.create(
            cdf, "Q", CDF_INT4, 1L, 0L, new  long[] {1}, 
            VARY, new long[] {NOVARY}
      );
      id = var.id();
/*
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
      Entry.create(var_type, frameGroup.getID(), CDF_CHAR, "data");
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
      */
   }
   public CDF getCDF(){return cdf;}
   
   public void setAttribute(
      final String key, final Object val, 
      final long type, final long scope, final int id
   )throws CDFException{
      Attribute attr;

      //either create or get the attirbute
      try{
         attr = Attribute.create(
            cdf, String.valueOf(key), GLOBAL_SCOPE
         );
      }catch(CDFException e){
         if(e.getCurrentStatus() == ATTR_EXISTS){
            attr = cdf.getAttribute(key);
         }else{
            System.out.println("Error getting CDF attriubute: " + key);
            System.out.println("Error code = " + e.getCurrentStatus());
            return;
         }
      }
      
      Entry.create(attr, id, type, val);
   }
   public void setAttribute(
      final String key, final Object val, final long type
   )throws CDFException{
      //attributes without id or scope are assumed to be single instance globals
      setAttribute(key, val, type, GLOBAL_SCOPE, 0);
   }
   public void setAttribute(final String key, final Object val)
   throws CDFException{
      long type;
      //figure out what type of variable to store this as
      if(val instanceof String){type = CDF_CHAR;}
      else{
         double test_val = Double.valueOf(val.toString());
         if(test_val == (long)test_val){
            if(test_val < Integer.MAX_VALUE && test_val > Integer.MIN_VALUE){
               type = CDF_INT4;
            }
            else{type = CDF_INT8;}
         }else{
            type = CDF_DOUBLE;
         }
      }
      setAttribute(key, val, type);
   }

   public void writeGlobalAttributes() throws CDFException{
      //Set all of the global attributes used by all BARREL CDF files
      Attribute attr;
      
      Set attr_entries = global_attrs.entrySet();
      Iterator attr_i = attr_entries.iterator();

      while(attr_i.hasNext()){
         Map.Entry entry = (Map.Entry)attr_i.next();
      }
   }

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