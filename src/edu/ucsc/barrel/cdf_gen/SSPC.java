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
   static public CDF create(
      final String path,final  String date, final int lvl
   ){
      CDF cdf = super.create(path);
      Attribute attr;

      //Set global attributes specific to this type of CDF
      attr = cdf.getAttribute("Data_type");
      Entry.create(attr, 1, CDF_CHAR, "l" + lvl + ">Level-" + lvl);

      attr = cdf.getAttribute("Logical_source_description");
      Entry.create(
         attr, 1, CDF_CHAR, "Slow time resolution X-ray spectrum"
      );

      attr = cdf.getAttribute("TEXT");
      Entry.create(
         attr, 1, CDF_CHAR, 
         "X-ray spectra each made of 256 energy bins " + 
         "transmitted over 32 frames."
      );

      attr = cdf.getAttribute("Instrument_type");
      Entry.create(attr, 1, CDF_CHAR, "Gamma and X-Rays");
      
      attr = cdf.getAttribute("Descriptor");
      Entry.create(attr, 1, CDF_CHAR, "Scintillator");
      
      attr = cdf.getAttribute("Time_resolution");
      Entry.create(attr, 1, CDF_CHAR, "32s");
      
      attr = cdf.getAttribute("Logical_source");
      Entry.create(attr, 1, CDF_CHAR, "payload_id_l2_scintillator");
      
      attr = cdf.getAttribute("Logical_file_id");
      Entry.create(
         attr, 1, CDF_CHAR, 
         "payload_id_l2_scintillator_00000000_v01"
      );

      //create SSPC variables
      Variable sspc = 
         Variable.create(
            cdf, "SSPC", CDF_DOUBLE, 1L, 2L, new  long[] {1, 256}, 
            VARY, new long[] {VARY, VARY}
         );   
      attr = cdf.getrAttribute("FIELDNAM");
      Entry.create(attr, epoch.getID(), CDF_CHAR, "SSPC");

      attr = cdf.getrAttribute("CATDESC");
      Entry.create(attr, epoch.getID(), CDF_CHAR, "SSPC");

      attr = cdf.getrAttribute("VAR_NOTES");
      Entry.create(
         attr, epoch.getID(), CDF_CHAR, 
         "Rebinned, divided by energy bin widths and " +
         "adjusted to /sec time scale."
      );

      attr = cdf.getrAttribute("VAR_TYPE");
      Entry.create(attr, epoch.getID(), CDF_CHAR, "data");

      attr = cdf.getrAttribute("DEPEND_0");
      Entry.create(attr, epoch.getID(), CDF_CHAR, "Epoch");

      attr = cdf.getrAttribute("FORMAT");
      Entry.create(attr, epoch.getID(), CDF_CHAR, "%f");

      attr = cdf.getrAttribute("UNITS");
      Entry.create(attr, epoch.getID(), CDF_CHAR, "cnts/kev/sec");

      attr = cdf.getrAttribute("SCALETYP");
      Entry.create(monotonic, epoch.getID(), CDF_CHAR, "log");

      attr = cdf.getrAttribute("DISPLAY_TYPE");
      Entry.create(time_base, epoch.getID(), CDF_CHAR, "spectrogram");

      attr = cdf.getrAttribute("VALIDMIN");
      Entry.create(time_scale, epoch.getID(), CDF_DOUBLE, 0.0);

      attr = cdf.getrAttribute("VALIDMAX");
      Entry.create(ref_pos, epoch.getID(), CDF_DOUBLE, 59391.0);

      attr = cdf.getrAttribute("FILLVAL");
      Entry.create(ref_pos, epoch.getID(), CDF_DOUBLE, -1.0e+31);

      attr = cdf.getrAttribute("LABLAXIS");
      Entry.create(ref_pos, epoch.getID(), CDF_CHAR, "SSPC");

      attr = cdf.getrAttribute("DEPEND_1");
      Entry.create(ref_pos, epoch.getID(), CDF_CHAR, "energy");

      Variable frameGroup = 
         Variable.create(
            cdf, "FrameGroup", CDF_INT4, 1L, 0L, new  long[] {1}, 
            VARY, new long[] {NOVARY}
         );
      Variable q = 
         Variable.create(
            cdf, "Q", CDF_INT4, 1L, 0L, new  long[] {1}, 
            VARY, new long[] {NOVARY}
         );


      return cdf;
   }
}
