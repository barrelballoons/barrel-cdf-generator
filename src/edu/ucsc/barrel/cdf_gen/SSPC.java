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

   public SSPC(
      final String path,final  String date, final int lvl
   ) throws CDFException{
      super(path);
      cdf = super.getCDF();
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

      //create SSPC variable
      //This variable will contain the slow spectrum that is returned over
      //32 frames.
      Variable sspc = 
         Variable.create(
            cdf, "SSPC", CDF_FLOAT, 1L, 2L, new  long[] {1, 256}, 
            VARY, new long[] {VARY, VARY}
         );   
      attr = cdf.getAttribute("FIELDNAM");
      Entry.create(attr, sspc.getID(), CDF_CHAR, "SSPC");

      attr = cdf.getAttribute("CATDESC");
      Entry.create(attr, sspc.getID(), CDF_CHAR, "SSPC");

      attr = cdf.getAttribute("VAR_NOTES");
      Entry.create(
         attr, sspc.getID(), CDF_CHAR, 
         "Rebinned, divided by energy bin widths and " +
         "adjusted to /sec time scale."
      );

      attr = cdf.getAttribute("VAR_TYPE");
      Entry.create(attr, sspc.getID(), CDF_CHAR, "data");

      attr = cdf.getAttribute("DEPEND_0");
      Entry.create(attr, sspc.getID(), CDF_CHAR, "Epoch");

      attr = cdf.getAttribute("FORMAT");
      Entry.create(attr, sspc.getID(), CDF_CHAR, "%f");

      attr = cdf.getAttribute("UNITS");
      Entry.create(attr, sspc.getID(), CDF_CHAR, "cnts/keV/sec");

      attr = cdf.getAttribute("SCALETYP");
      Entry.create(attr, sspc.getID(), CDF_CHAR, "log");

      attr = cdf.getAttribute("DISPLAY_TYPE");
      Entry.create(attr, sspc.getID(), CDF_CHAR, "spectrogram");

      attr = cdf.getAttribute("VALIDMIN");
      Entry.create(attr, sspc.getID(), CDF_FLOAT, 0.0);

      attr = cdf.getAttribute("VALIDMAX");
      Entry.create(attr, sspc.getID(), CDF_FLOAT, 59391.0);

      attr = cdf.getAttribute("FILLVAL");
      Entry.create(attr, sspc.getID(), CDF_FLOAT, -1.0e+31);

      attr = cdf.getAttribute("LABLAXIS");
      Entry.create(attr, sspc.getID(), CDF_CHAR, "SSPC");

      attr = cdf.getAttribute("DEPEND_1");
      Entry.create(attr, sspc.getID(), CDF_CHAR, "energy");


      //Create the "energy" variable
      //This variable lists the starting energy for each channel in keV
      Variable energy = 
         Variable.create(
            cdf, "energy", CDF_FLOAT, 1L, 2L, new  long[] {1, 256}, 
            NOVARY, new long[] {NOVARY}
         );
      attr = cdf.getAttribute("FIELDNAM");
      Entry.create(attr, energy.getID(), CDF_CHAR, "Energy Level");

      attr = cdf.getAttribute("CATDESC");
      Entry.create(attr, energy.getID(), CDF_CHAR, "Energy Level");

      attr = cdf.getAttribute("VAR_NOTES");
      Entry.create(
         attr, energy.getID(), CDF_CHAR, 
         "Start of each slow spectrum energy channel."
      );

      attr = cdf.getAttribute("VAR_TYPE");
      Entry.create(attr, energy.getID(), CDF_CHAR, "support_data");

      attr = cdf.getAttribute("DEPEND_0");
      Entry.create(attr, energy.getID(), CDF_CHAR, "Epoch");

      attr = cdf.getAttribute("FORMAT");
      Entry.create(attr, energy.getID(), CDF_CHAR, "%f");

      attr = cdf.getAttribute("UNITS");
      Entry.create(attr, energy.getID(), CDF_CHAR, "keV");

      attr = cdf.getAttribute("SCALETYP");
      Entry.create(attr, energy.getID(), CDF_CHAR, "linear");

      attr = cdf.getAttribute("VALIDMIN");
      Entry.create(attr, energy.getID(), CDF_FLOAT, 0.0);

      attr = cdf.getAttribute("VALIDMAX");
      Entry.create(attr, energy.getID(), CDF_FLOAT, 10000.0);

      attr = cdf.getAttribute("FILLVAL");
      Entry.create(attr, energy.getID(), CDF_FLOAT, -1.0e+31);

      attr = cdf.getAttribute("LABLAXIS");
      Entry.create(attr, energy.getID(), CDF_CHAR, "Energy");

      attr = cdf.getAttribute("DELTA_PLUS_VAR");
      Entry.create(attr, energy.getID(), CDF_CHAR, "BinWidth");

      //Create a variable that will track each energy channel width
      Variable bin_width = 
         Variable.create(
            cdf, "BinWidth", CDF_FLOAT, 1L, 2L, new  long[] {1, 256}, 
            NOVARY, new long[] {NOVARY}
         );
      attr = cdf.getAttribute("FIELDNAM");
      Entry.create(attr, bin_width.getID(), CDF_CHAR, "Bin Width");

      attr = cdf.getAttribute("CATDESC");
      Entry.create(attr, bin_width.getID(), CDF_CHAR, "Width of energy channel");

      attr = cdf.getAttribute("VAR_TYPE");
      Entry.create(attr, bin_width.getID(), CDF_CHAR, "support_data");

      attr = cdf.getAttribute("DEPEND_0");
      Entry.create(attr, bin_width.getID(), CDF_CHAR, "Epoch");

      attr = cdf.getAttribute("FORMAT");
      Entry.create(attr, bin_width.getID(), CDF_CHAR, "%f");

      attr = cdf.getAttribute("UNITS");
      Entry.create(attr, bin_width.getID(), CDF_CHAR, "keV");

      attr = cdf.getAttribute("SCALETYP");
      Entry.create(attr, bin_width.getID(), CDF_CHAR, "linear");

      attr = cdf.getAttribute("VALIDMIN");
      Entry.create(attr, bin_width.getID(), CDF_FLOAT, 0.0);

      attr = cdf.getAttribute("VALIDMAX");
      Entry.create(attr, bin_width.getID(), CDF_FLOAT, 200.0);

      attr = cdf.getAttribute("FILLVAL");
      Entry.create(attr, bin_width.getID(), CDF_FLOAT, -1.0e+31);

      attr = cdf.getAttribute("LABLAXIS");
      Entry.create(attr, bin_width.getID(), CDF_CHAR, "Width");
   }
}
