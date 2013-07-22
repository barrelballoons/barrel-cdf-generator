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
import java.util.List;
import java.util.LinkedList;
import java.util.ListIterator;


public class HKPG extends BarrelCDF{
   private CDF cdf;
   private String path;
   private int date, lvl;
   
   //define a data object to hold housekeeping variable attirbutes
   private class HkpgVar {
      private String name, desc, units;
      private long type;
      private int mod_index;
      private float min, max;
      public HkpgVar(
         final String n, final String d, final String u,
         final int i, final float mi, final float ma, final long t 
      ){
         name = n;
         desc = d;
         units = u;
         mod_index = i;
         min = mi;
         max = ma;
         type = t;
      }

      public String getName(){return name;}
      public String getDesc(){return desc;}
      public String getUnits(){return units;}
      public int getModIndex(){return mod_index;}
      public float getMin(){return min;}
      public float getMax(){return max;}
      public long getType(){return type;}
   }

   List<HkpgVar> vars;   

   public HKPG(final String p, final int d, final int l){
      super(p, l);
      path = p;
      date = d;
      lvl = l;
      
      try{
         cdf = super.getCDF();
      
         addHkpgGlobalAtts();
         addHkpgVars();
      }catch(CDFException e){
         System.out.println(e.getMessage());
      }
   }

   private void fillVarArray(){
      vars = new LinkedList<HkpgVar>();

      vars.add(new HkpgVar(
         "V0_VoltAtLoad", "Volatge at load", "V", 0, 0, 50, CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "V1_Battery", "Battery Voltage", "V", 2, 0, 50, CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "V2_Solar1",
         "Solar Panel One Voltage", "V", 4, 0, 50, CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "V3_POS_DPU", 
         "DPU Voltage (Positive)", "V", 6, 0, 50, CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "V4_POS_XRayDet", 
         "X-ray Detector Voltage (Positive)", "V", 8, 0, 50, CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "V5_Modem", 
         "Modem Voltage", "V", 10, 0, 50, CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "V6_NEG_XRayDet", 
         "X-ray Detector Voltage (Negative)", "V", 12, -50, 0, CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "V7_NEG_DPU", 
         "DPU Voltage (Negative)", "V", 14, -50, 0, CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "V8_Mag", 
         "Magnetometer Voltage", "V", 32, 0, 50, CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "V9_Solar2", 
         "Solar Panel Two Voltage", "V", 33, 0, 50, CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "V10_Solar3", 
         "Solar Panel Three Voltage", "V", 34, 0, 50, CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "V11_Solar4", 
         "Solar Panel Four Voltage", "V", 35, 0, 50, CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "I0_TotalLoad", 
         "Total Load Current", "mA", 1, 0, 2000, CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "I1_TotalSolar", 
         "Total Solar Current", "mA", 3, 0, 2000, CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "I2_Solar1", 
         "Solar Panel One Current", "mA", 5, 0, 2000, CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "I3_POS_DPU", 
         "DPU Current (Positive)", "mA", 7, 0, 2000, CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "I4_POS_XRayDet", 
         "X-ray Detector Current (Positive)", "mA", 9, 0, 2000, CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "I5_Modem", 
         "Modem Current", "mA", 11, 0, 2000, CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "I6_NEG_XRayDet", 
         "X-ray Detector Current (Negative)", "mA", 13, -2000, 0, CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "I7_NEG_DPU", 
         "DPU Current (Negative)", "mA", 15, -2000, 0, CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "T0_Scint", 
         "Scintillator Temperature", "deg. C", 16, -273, 273, CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "T1_Mag", 
         "Magnetometer Temperature", "deg. C", 18, -273, 273, CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "T2_ChargeCont", "Charge Controller Temperature", 
         "deg. C", 20, -273, 273, CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "T3_Battery", "Battery Temperature", 
         "deg. C", 22, -273, 273, CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "T4_PowerConv", "Power Converter Temperature", 
         "deg. C", 24, -273, 273, CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "T5_DPU", "DPU Temperature", 
         "deg. C", 26, -273, 273, CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "T6_Modem", "Modem Temperature",
         "deg. C", 28, -273, 273, CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "T7_Structure", "Structure Temperature",
         "deg. C", 30, -273, 273, CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "T8_Solar1", "Solar Panel One Temperature",
         "deg. C", 17, -273, 273, CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "T9_Solar2", "Solar Panel Two Temperature",
         "deg. C", 19, -273, 273, CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "T10_Solar3", "Solar Panel Three Temperature",
         "deg. C", 21, -273, 273, CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "T11_Solar4", "Solar Panel Four Temperature",
         "deg. C", 23, -273, 273, CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "T12_TermTemp", "Terminate Temperature",
         "deg. C", 25, -273, 273, CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "T13_TermBatt", "Terminate Battery", "deg. C", 
         27, -273, 273, CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "T14_TermCap", "Terminate Capacitor", " deg. C", 
         29, -273, 273, CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "T15_CCStat", "CC Status", "deg. C",
         31, -273, 273, CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "numOfSats", "Number of GPS Satellites", "sats", 
         0, 0, 31, CDF_INT2
      ));
      vars.add(new HkpgVar(
         "timeOffset", "Leap Seconds", "s", 
         0, 0, 255, CDF_INT2
      ));
      vars.add(new HkpgVar(
         "termStatus", "Terminate Status", " ", 
         0, 0, 1, CDF_INT2
      ));
      vars.add(new HkpgVar(
         "cmdCounter", "Command Counter", "commands", 
         0, 0, 32767, CDF_INT4
      ));
      vars.add(new HkpgVar(
         "modemCounter", "Modem Reset Counter", "resets", 
         0, 0, 255, CDF_INT2
      ));
      vars.add(new HkpgVar(
         "dcdCounter", "Numebr of time the DCD has been de-asserted.", "calls", 
         0, 0, 255, CDF_INT2
      ));
      vars.add(new HkpgVar(
         "weeks", "Weeks Since 6 Jan 1980", "weeks", 
         0, 0, 65535, CDF_INT4
      ));
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
      //create an array containing the details of these variables
      fillVarArray();

      //loop through all of the hkpg variables
      ListIterator<HkpgVar> var_i = vars.listIterator();
      while(var_i.hasNext()){
         createVar(var_i.next());
      }
   }

   private void createVar(final HkpgVar v) throws CDFException{
      long type = v.getType();
      Variable var = 
         Variable.create(
            cdf, v.getName(), type,
            1L, 0L, new  long[] {1}, VARY, new long[] {NOVARY}
         ); 
      long id = var.getID();

      if(type == CDF_INT4){
         setAttribute("FORMAT", "%i", VARIABLE_SCOPE, id);
         setAttribute("VALIDMIN", v.getMin(), VARIABLE_SCOPE, id, type);
         setAttribute("VALIDMAX", v.getMax(), VARIABLE_SCOPE, id, type);
         setAttribute(
            "FILLVAL", Constants.INT4_FILL, VARIABLE_SCOPE, id, type
         );
      }else if(type == CDF_INT2){
         setAttribute("FORMAT", "%i", VARIABLE_SCOPE, id);
         setAttribute("VALIDMIN", (short)v.getMin(), VARIABLE_SCOPE, id, type);
         setAttribute("VALIDMAX", (short)v.getMax(), VARIABLE_SCOPE, id, type);
         setAttribute(
            "FILLVAL", Constants.INT2_FILL, VARIABLE_SCOPE, id, type
         );
      }else{
         setAttribute("FORMAT", "%f", VARIABLE_SCOPE, id);
         setAttribute("VALIDMIN", v.getMin(), VARIABLE_SCOPE, id, type);
         setAttribute("VALIDMAX", v.getMax(), VARIABLE_SCOPE, id, type);
         setAttribute(
            "FILLVAL", Constants.FLOAT_FILL, VARIABLE_SCOPE, id, type
         );
      }

      setAttribute("FIELDNAM", v.getName(), VARIABLE_SCOPE, id);
      setAttribute("CATDESC", v.getDesc(), VARIABLE_SCOPE, id);
      setAttribute("VAR_TYPE", "data", VARIABLE_SCOPE, id);
      setAttribute("DEPEND_0", "Epoch", VARIABLE_SCOPE, id);
      setAttribute("UNITS", v.getUnits(), VARIABLE_SCOPE, id);
      setAttribute("SCALETYP", "linear", VARIABLE_SCOPE, id);
      setAttribute("DISPLAY_TYPE", "time_series", VARIABLE_SCOPE, id);
      setAttribute("LABLAXIS", v.getName(), VARIABLE_SCOPE, id);
   }
}
