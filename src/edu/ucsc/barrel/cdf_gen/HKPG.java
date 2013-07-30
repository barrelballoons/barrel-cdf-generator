/*
HKPG.java

Description:
   Creates HKPG CDF files.

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
import java.util.List;
import java.util.LinkedList;
import java.util.ListIterator;


public class HKPG{
   private BarrelCDF cdf;
   private String path;
   private int date, lvl;
   List<HkpgVar> vars;   
   
   //define a data object to hold housekeeping variable attirbutes
   private class HkpgVar{
      private String name, desc, units;
      private long type;
      private int mod_index;
      private float min, max;
      public HkpgVar(
         final String n, final String d, final String u,
         final int i, final float mi, final float ma, final long t 
      ){
         this.name = n;
         this.desc = d;
         this.units = u;
         this.mod_index = i;
         this.min = mi;
         this.max = ma;
         this.type = t;
      }

      public String getName(){return name;}
      public String getDesc(){return desc;}
      public String getUnits(){return units;}
      public int getModIndex(){return mod_index;}
      public float getMin(){return min;}
      public float getMax(){return max;}
      public long getType(){return type;}
   }

   public HKPG(final String p, final int d, final int l){
      cdf = new BarrelCDF(p, l);
      this.path = p;
      this.date = d;
      this.lvl = l;
      
      addHkpgGlobalAtts();
      addHkpgVars();
   }

   private void fillVarArray(){
      vars = new LinkedList<HkpgVar>();

      vars.add(new HkpgVar(
         "V0_VoltAtLoad", "Volatge at load", 
         "V", 0, 0, 50, CDFConstants.CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "V1_Battery", "Battery Voltage", 
         "V", 2, 0, 50, CDFConstants.CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "V2_Solar1", "Solar Panel One Voltage", 
         "V", 4, 0, 50, CDFConstants.CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "V3_POS_DPU", "DPU Voltage (Positive)", 
         "V", 6, 0, 50, CDFConstants.CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "V4_POS_XRayDet", "X-ray Detector Voltage (Positive)", 
         "V", 8, 0, 50, CDFConstants.CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "V5_Modem", "Modem Voltage", 
         "V", 10, 0, 50, CDFConstants.CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "V6_NEG_XRayDet", "X-ray Detector Voltage (Negative)", 
         "V", 12, -50, 0, CDFConstants.CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "V7_NEG_DPU", "DPU Voltage (Negative)", 
         "V", 14, -50, 0, CDFConstants.CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "V8_Mag", "Magnetometer Voltage", 
         "V", 32, 0, 50, CDFConstants.CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "V9_Solar2", "Solar Panel Two Voltage", 
         "V", 33, 0, 50, CDFConstants.CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "V10_Solar3","Solar Panel Three Voltage", 
         "V", 34, 0, 50, CDFConstants.CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "V11_Solar4", "Solar Panel Four Voltage", 
         "V", 35, 0, 50, CDFConstants.CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "I0_TotalLoad", "Total Load Current", 
         "mA", 1, 0, 2000, CDFConstants.CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "I1_TotalSolar", "Total Solar Current", 
         "mA", 3, 0, 2000, CDFConstants.CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "I2_Solar1", "Solar Panel One Current", 
         "mA", 5, 0, 2000, CDFConstants.CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "I3_POS_DPU", "DPU Current (Positive)", 
         "mA", 7, 0, 2000, CDFConstants.CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "I4_POS_XRayDet", "X-ray Detector Current (Positive)", 
         "mA", 9, 0, 2000, CDFConstants.CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "I5_Modem", "Modem Current", 
         "mA", 11, 0, 2000, CDFConstants.CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "I6_NEG_XRayDet", "X-ray Detector Current (Negative)", 
         "mA", 13, -2000, 0, CDFConstants.CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "I7_NEG_DPU", "DPU Current (Negative)", 
         "mA", 15, -2000, 0, CDFConstants.CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "T0_Scint", "Scintillator Temperature", 
         "deg. C", 16, -273, 273, CDFConstants.CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "T1_Mag", "Magnetometer Temperature", 
         "deg. C", 18, -273, 273, CDFConstants.CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "T2_ChargeCont", "Charge Controller Temperature", 
         "deg. C", 20, -273, 273, CDFConstants.CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "T3_Battery", "Battery Temperature", 
         "deg. C", 22, -273, 273, CDFConstants.CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "T4_PowerConv", "Power Converter Temperature", 
         "deg. C", 24, -273, 273, CDFConstants.CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "T5_DPU", "DPU Temperature", 
         "deg. C", 26, -273, 273, CDFConstants.CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "T6_Modem", "Modem Temperature",
         "deg. C", 28, -273, 273, CDFConstants.CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "T7_Structure", "Structure Temperature",
         "deg. C", 30, -273, 273, CDFConstants.CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "T8_Solar1", "Solar Panel One Temperature",
         "deg. C", 17, -273, 273, CDFConstants.CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "T9_Solar2", "Solar Panel Two Temperature",
         "deg. C", 19, -273, 273, CDFConstants.CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "T10_Solar3", "Solar Panel Three Temperature",
         "deg. C", 21, -273, 273, CDFConstants.CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "T11_Solar4", "Solar Panel Four Temperature",
         "deg. C", 23, -273, 273, CDFConstants.CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "T12_TermTemp", "Terminate Temperature",
         "deg. C", 25, -273, 273, CDFConstants.CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "T13_TermBatt", "Terminate Battery", "deg. C", 
         27, -273, 273, CDFConstants.CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "T14_TermCap", "Terminate Capacitor", " deg. C", 
         29, -273, 273, CDFConstants.CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "T15_CCStat", "CC Status", "deg. C",
         31, -273, 273, CDFConstants.CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "numOfSats", "Number of GPS Satellites", "sats", 
         0, 0, 31, CDFConstants.CDF_INT2
      ));
      vars.add(new HkpgVar(
         "timeOffset", "Leap Seconds", "s", 
         0, 0, 255, CDFConstants.CDF_INT2
      ));
      vars.add(new HkpgVar(
         "termStatus", "Terminate Status", " ", 
         0, 0, 1, CDFConstants.CDF_INT2
      ));
      vars.add(new HkpgVar(
         "cmdCounter", "Command Counter", "commands", 
         0, 0, 32767, CDFConstants.CDF_INT4
      ));
      vars.add(new HkpgVar(
         "modemCounter", "Modem Reset Counter", "resets", 
         0, 0, 255, CDFConstants.CDF_INT2
      ));
      vars.add(new HkpgVar(
         "dcdCounter", "Numebr of time the DCD has been de-asserted.", "calls", 
         0, 0, 255, CDFConstants.CDF_INT2
      ));
      vars.add(new HkpgVar(
         "weeks", "Weeks Since 6 Jan 1980", "weeks", 
         0, 0, 65535, CDFConstants.CDF_INT4
      ));
   }

   private void addHkpgGlobalAtts(){
      //Set global attributes specific to this type of CDF
      this.cdf.attribute(
         "Logical_source_description", "Analog Housekeeping Data"
      );
      this.cdf.attribute(
         "TEXT", 
         "Voltage, temperature, current, and payload status values returned " + 
         "every 40s." 
      );
      this.cdf.attribute("Instrument_type", "Housekeeping");
      this.cdf.attribute("Descriptor", "EDI");
      this.cdf.attribute("Time_resolution", "40s");
      this.cdf.attribute("Logical_source", "payload_id_l" + lvl  + "_edi");
      this.cdf.attribute(
         "Logical_file_id",
         "payload_id_l" + lvl  + "_edi_20" + date  + 
         "_V" + CDF_Gen.getSetting("rev")
      );
   }

   private void addHkpgVars(){
      //create an array containing the details of these variables
      fillVarArray();

      //loop through all of the hkpg variables
      ListIterator<HkpgVar> var_i = vars.listIterator();
      while(var_i.hasNext()){createVar(var_i.next());}
   }

   private void createVar(final HkpgVar v){
      CDFVar var = new CDFVar(this.cdf, v.getName(), this.type);

      if(v.getType() == CDFConstants.CDF_INT4){
         var.attribute("FORMAT", "%i");
         var.attribute("FILLVAL", CDFVar.getIstpVal("INT4_FILL"));
      }else if(type == CDFConstants.CDF_INT2){
         var.attribute("FORMAT", "%i");
         var.attribute("FILLVAL", CDFVar.getIstpVal("INT2_FILL"));
      }else{
         var.attribute("FORMAT", "%f");
         var.attribute("FILLVAL", CDFVar.getIstpVal("FLOAT_FILL"));
      }

      var.attribute("VALIDMIN", v.getMin());
      var.attribute("VALIDMAX", v.getMax());
      var.attribute("FIELDNAM", v.getName());
      var.attribute("CATDESC", v.getDesc());
      var.attribute("LABLAXIS", v.getName());
      var.attribute("VAR_TYPE", "data");
      var.attribute("DEPEND_0", "Epoch");
      var.attribute("UNITS", v.getUnits());
      var.attribute("SCALETYP", "linear");
      var.attribute("DISPLAY_TYPE", "time_series");
   }
   
   public CDFFile getCDF(){
      return this.cdf;
   }

   public void close(){
      this.cdf.close();
   }
}
