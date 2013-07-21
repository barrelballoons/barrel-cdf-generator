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

public class HKPG extends BarrelCDF{
   private CDF cdf;
   private Variable var;
   private long id;
   private String path;
   private int date, lvl;
   
   //define a data object to hold housekeeping variable attirbutes
   private class HkpgVar {
      private String name, desc, units;
      private int mod_index;
      private float min, max;
      public HkpgVar(
         final String n, final String d, final String u,
         final int i, final float mi, final float ma
      ){
         name = n;
         desc = d;
         units = u;
         mod_index = i;
         min = mi;
         max = ma;
      }

      public String getName(){return name;}
      public String getDesc(){return desc;}
      public String getUnits(){return units;}
      public int getModIndex(){return mod_index;}
      public float getMin(){return min;}
      public float getMax(){return max;}
   }
   
   private HkpgVar[] vars;

   public HKPG(final String p, final int d, final int l){
      super(p, l);
      path = p;
      date = d;
      lvl = l;
      
      fillHkpgObjects();

      try{
         cdf = super.getCDF();
      
         addHkpgGlobalAtts();
         addHkpgVars();
      }catch(CDFException e){
         System.out.println(e.getMessage());
      }
   }

   private void fillHkpgObjects(){
      vars = new HkpgVar[36];

      vars[0] = new HkpgVar(
         "V0_VoltAtLoad", "Volatge at load", "V", 0, 0, 50
      );
      vars[1] = new HkpgVar(
         "V1_Battery", "Battery Voltage", "V", 2, 0, 50
      );
      vars[2] = new HkpgVar(
         "V2_Solar1",
         "Solar Panel One Voltage", "V", 4, 0, 50
      );
      vars[3] = new HkpgVar(
         "V3_POS_DPU", 
         "DPU Voltage (Positive)", "V", 6, 0, 50
      );
      vars[4] = new HkpgVar(
         "V4_POS_XRayDet", 
         "X-ray Detector Voltage (Positive)", "V", 8, 0, 50
      );
      vars[5] = new HkpgVar(
         "V5_Modem", 
         "Modem Voltage", "V", 10, 0, 50
      );
      vars[6] = new HkpgVar(
         "V6_NEG_XRayDet", 
         "X-ray Detector Voltage (Negative)", "V", 12, -50, 0
      );
      vars[7] = new HkpgVar(
         "V7_NEG_DPU", 
         "DPU Voltage (Negative)", "V", 14, -50, 0
      );
      vars[8] = new HkpgVar(
         "V8_Mag", 
         "Magnetometer Voltage", "V", 32, 0, 50
      );
      vars[9] = new HkpgVar(
         "V9_Solar2", 
         "Solar Panel Two Voltage", "V", 33, 0, 50
      );
      vars[10] = new HkpgVar(
         "V10_Solar3", 
         "Solar Panel Three Voltage", "V", 34, 0, 50
      );
      vars[11] = new HkpgVar(
         "V11_Solar4", 
         "Solar Panel Four Voltage", "V", 35, 0, 50
      );
      vars[12] = new HkpgVar(
         "I0_TotalLoad", 
         "Total Load Current", "mA", 1, 0, 20000
      );
      vars[13] = new HkpgVar(
         "I1_TotalSolar", 
         "Total Solar Current", "mA", 3, 0, 20000
      );
      vars[14] = new HkpgVar(
         "I2_Solar1", 
         "Solar Panel One Current", "mA", 5, 0, 20000
      );
      vars[15] = new HkpgVar(
         "I3_POS_DPU", 
         "DPU Current (Positive)", "mA", 7, 0, 20000
      );
      vars[16] = new HkpgVar(
         "I4_POS_XRayDet", 
         "X-ray Detector Current (Positive)", "mA", 9, 0, 20000
      );
      vars[17] = new HkpgVar(
         "I5_Modem", 
         "Modem Current", "mA", 11, 0, 20000
      );
      vars[18] = new HkpgVar(
         "I6_NEG_XRayDet", 
         "X-ray Detector Current (Negative)", "mA", 13, -20000, 0
      );
      vars[19] = new HkpgVar(
         "I7_NEG_DPU", 
         "DPU Current (Negative)", "mA", 15, -20000, 0
      );
      vars[20] = new HkpgVar(
         "T0_Scint", 
         "Scintillator Temperature", "deg. C", 16, -273, 273
      );
      vars[21] = new HkpgVar(
         "T1_Mag", 
         "Magnetometer Temperature", "deg. C", 18, -273, 273
      );
      vars[22] = new HkpgVar(
         "T2_ChargeCont", "Charge Controller Temperature", "deg. C", 20, -273, 273
      );
      vars[23] = new HkpgVar(
         "T3_Battery", "Battery Temperature", "deg. C", 22, -273, 273
      );
      vars[24] = new HkpgVar(
         "T4_PowerConv", "Power Converter Temperature", "deg. C", 24, -273, 273
      );
      vars[25] = new HkpgVar(
         "T5_DPU", "DPU Temperature", "deg. C", 26, -273, 273
      );
      vars[26] = new HkpgVar(
         "T6_Modem", "Modem Temperature", "deg. C", 28, -273, 273
      );
      vars[27] = new HkpgVar(
         "T7_Structure", "Structure Temperature", "deg. C", 30, -273, 273
      );
      vars[28] = new HkpgVar(
         "T8_Solar1", "Solar Panel One Temperature", "deg. C", 17, -273, 273
      );
      vars[29] = new HkpgVar(
         "T9_Solar2", "Solar Panel Two Temperature", "deg. C", 19, -273, 273
      );
      vars[30] = new HkpgVar(
         "T10_Solar3", "Solar Panel Three Temperature", "deg. C", 21, -273, 273
      );
      vars[31] = new HkpgVar(
         "T11_Solar4", "Solar Panel Four Temperature", "deg. C", 23, -273, 273
      );
      vars[32] = new HkpgVar(
         "T12_TermTemp", "Terminate Temperature", "deg. C", 25, -273, 273
      );
      vars[33] = new HkpgVar(
         "T13_TermBatt", "Terminate Battery", " ", 27, -273, 273
      );
      vars[34] = new HkpgVar(
         "T14_TermCap", "Terminate Capacitor", " ", 29, -273, 273
      );
      vars[35] = new HkpgVar(
         "T15_CCStat", "CC Status", " ", 31, -273, 273
      );
   }

   private void addHkpgGlobalAtts() throws CDFException{
      //Set global attributes specific to this type of CDF
      setAttribute(
         "Logical_source_description", "Analog Housekeeping Data"
      );
      setAttribute(
         "TEXT", "Voltage, temperature, current, and payload status values " +
         "returned every 40 sec " 
      );
      setAttribute("Instrument_type", "Housekeeping");
      setAttribute("Descriptor", "EDI");
      setAttribute("Time_resolution", "40s");
      setAttribute("Logical_source", "payload_id_l" + lvl  + "_edi");
      setAttribute(
         "Logical_file_id",
         "payload_id_l" + lvl  + "_edi_20" + date  + 
         "_V" + CDF_Gen.getSetting("rev")
      );
   }

   private void addHkpgVars() throws CDFException{
      //loop through all of the hkpg variables
      for(int var_i = 0; var_i < vars.length; var_i++){
         var = 
            Variable.create(
               cdf, vars[var_i].getName(), CDF_FLOAT, 
               1L, 0L, new  long[] {1}, VARY, new long[] {NOVARY}
            ); 
         id = var.getID();
      
         setAttribute("FIELDNAM", vars[var_i].getName(), VARIABLE_SCOPE, id);
         setAttribute("CATDESC", vars[var_i].getDesc(), VARIABLE_SCOPE, id);
         setAttribute("VAR_TYPE", "data", VARIABLE_SCOPE, id);
         setAttribute("DEPEND_0", "Epoch", VARIABLE_SCOPE, id);
         setAttribute("FORMAT", "%f", VARIABLE_SCOPE, id);
         setAttribute("UNITS", vars[var_i].getUnits(), VARIABLE_SCOPE, id);
         setAttribute("SCALETYP", "linear", VARIABLE_SCOPE, id);
         setAttribute("DISPLAY_TYPE", "time_series", VARIABLE_SCOPE, id);
         setAttribute(
            "VALIDMIN", vars[var_i].getMin(), VARIABLE_SCOPE, id, CDF_FLOAT
         );
         setAttribute(
            "VALIDMAX", vars[var_i].getMax(), VARIABLE_SCOPE, id, CDF_FLOAT
         );
         setAttribute(
            "FILLVAL", Constants.FLOAT_FILL, VARIABLE_SCOPE, id, CDF_FLOAT
         );
         setAttribute("LABLAXIS", vars[var_i].getName(), VARIABLE_SCOPE, id);
      }
   }
}
