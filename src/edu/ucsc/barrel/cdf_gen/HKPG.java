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
import java.util.Map;
import java.util.HashMap;
import java.util.Vector;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;
import java.util.ListIterator;


public class HKPG extends DataProduct{
   private int date, lvl;
   private String payload_id;
   List<HkpgVar> vars;

   public static final int
      V0 = 0, I0 = 1, V1 = 2, I1 = 3, V2 = 4, I2 = 5, V3 = 6, I3 = 7, V4 = 8, 
      I4 = 9, V5 = 10, I5 = 11, V6 = 12, I6 = 13, V7 = 14, I7 = 15, T0 = 16, 
      T8 = 17, T1 = 18, T9 = 19, T2 = 20, T10 = 21, T3 = 22, T11 = 23, T4 = 24, 
      T12 = 25, T5 = 26, T13 = 27, T6 = 28, T14 = 29, T7 = 30, T15 = 31, 
      V8 = 32, V9 = 33, V10 = 34, V11 = 35, 
      SATSOFF = 36, WEEK = 37, CMDCNT = 38, MDMCNT = 39;
   
   //array showing indexes of mod40 data
   public static final String[] IDS = {
      "V0",  "I0", "V1",  "I1",  "V2",  "I2",  "V3", "I3",  "V4", 
      "I4",  "V5", "I5",  "V6",  "I6",  "V7",  "I7", "T0",  "T8",
      "T1",  "T9", "T2",  "T10", "T3",  "T11", "T4", "T12", "T5",
      "T13", "T6", "T14", "T7 ", "T15", "V8",  "V9", "V10", "V11", 
      "SATSOFF", "WEEK", "CMDCNT", "MDMCNT"
   };

   public static final Map<String, Float> SCALE_FACTORS;
   static {
      Map<String, Float> scale = new HashMap<String, Float>();
      scales.put("V0" ,  0.0003052f);
      scales.put("V1" ,  0.0003052f);
      scales.put("V2" ,  0.0006104f);
      scales.put("V3" ,  0.0001526f);
      scales.put("V4" ,  0.0001526f);
      scales.put("V5" ,  0.0003052f);
      scales.put("V6" , -0.0001526f);
      scales.put("V7" , -0.0001526f);
      scales.put("V8" ,  0.0001526f);
      scales.put("V9" ,  0.0006104f);
      scales.put("V10",  0.0006104f);
      scales.put("V11",  0.0006104f);
      scales.put("I0" ,  0.05086f  );
      scales.put("I1" ,  0.06104f  );
      scales.put("I2" ,  0.06104f  );
      scales.put("I3" ,  0.01017f  );
      scales.put("I4" ,  0.001017f );
      scales.put("I5" ,  0.05086f  );
      scales.put("I6" , -0.0001261f);
      scales.put("I7" , -0.001017f );
      scales.put("T0" ,  0.007629f );
      scales.put("T1" ,  0.007629f );
      scales.put("T2" ,  0.007629f );
      scales.put("T3" ,  0.007629f );
      scales.put("T4" ,  0.007629f );
      scales.put("T5" ,  0.007629f );
      scales.put("T6" ,  0.007629f );
      scales.put("T7" ,  0.007629f );
      scales.put("T8" ,  0.007629f );
      scales.put("T9" ,  0.007629f );
      scales.put("T10",  0.007629f );
      scales.put("T11",  0.007629f );
      scales.put("T12",  0.007629f );
      scales.put("T13",  0.0003052f);
      scales.put("T14",  0.0003052f);
      scales.put("T15",  0.0001526f);
      SCALE_FACTORS = Collections.unmodifiableMap(scale);
   }
   public static final Map<String, Float> OFFSETS;
   static {
      Map<String, Float> offsets = new HashMap<String, Float>();
      offsets.put("T1" ,  -273.15f);
      offsets.put("T2" ,  -273.15f);
      offsets.put("T3" ,  -273.15f);
      offsets.put("T4" ,  -273.15f);
      offsets.put("T5" ,  -273.15f);
      offsets.put("T6" ,  -273.15f);
      offsets.put("T7" ,  -273.15f);
      offsets.put("T8" ,  -273.15f);
      offsets.put("T9" ,  -273.15f);
      offsets.put("T10",  -273.15f);
      offsets.put("T11",  -273.15f);
      offsets.put("T12",  -273.15f);
      offsets.put("T13",  -273.15f);
      offsets.put("T14",  -273.15f);
      offsets.put("T15",  -273.15f);
      OFFSETS = Collections.unmodifiableMap(offsets);
   }
   public static final Map<String, String> LABELS;
   static {
      Map<String, String> labels = new HashMap<String, String>();
      labels.put("V0" , "V0_VoltAtLoad");
      labels.put("V1" , "V1_Battery");
      labels.put("V2" , "V2_Solar1");
      labels.put("V3" , "V3_POS_DPU");
      labels.put("V4" , "V4_POS_XRayDet");
      labels.put("V5" , "V5_Modem");
      labels.put("V6" , "V6_NEG_XRayDet");
      labels.put("V7" , "V7_NEG_DPU");
      labels.put("V8" , "V8_Mag");
      labels.put("V9" , "V9_Solar2");
      labels.put("V10", "V10_Solar3");
      labels.put("V11", "V11_Solar4");
      labels.put("I0" , "I0_TotalLoad");
      labels.put("I1" , "I1_TotalSolar");
      labels.put("I2" , "I2_Solar1");
      labels.put("I3" , "I3_POS_DPU");
      labels.put("I4" , "I4_POS_XRayDet");
      labels.put("I5" , "I5_Modem");
      labels.put("I6" , "I6_NEG_XRayDet");
      labels.put("I7" , "I7_NEG_DPU");
      labels.put("T0" , "T0_Scint");
      labels.put("T1" , "T1_Mag");
      labels.put("T2" , "T2_ChargeCont");
      labels.put("T3" , "T3_Battery");
      labels.put("T4" , "T4_PowerConv");
      labels.put("T5" , "T5_DPU");
      labels.put("T6" , "T6_Modem");
      labels.put("T7" , "T7_Structure");
      labels.put("T8" , "T8_Solar1");
      labels.put("T9" , "T9_Solar2");
      labels.put("T10", "T10_Solar3");
      labels.put("T11", "T11_Solar4");
      labels.put("T12", "T12_TermTemp");
      labels.put("T13", "T13_TermBatt");
      labels.put("T14", "T14_TermCap");
      labels.put("T15", "T15_CCStat");
      LABELS = Collections.unmodifiableMap(labels);
   }

   //define a data object to hold housekeeping variable attirbutes
   private class HkpgVar{
      private String name, desc, units;
      private long type;
      private int mod_index;
      private float min, max;
      private HkpgVar(
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

      private String getName(){return name;}
      private String getDesc(){return desc;}
      private String getUnits(){return units;}
      private int getModIndex(){return mod_index;}
      private float getMin(){return min;}
      private float getMax(){return max;}
      private long getType(){return type;}
   }
   
   public HKPG(final String path,final String pay, int d, int l){

      this.date = d;
      this.lvl = l;
      this.payload_id = pay;

      setCDF(new BarrelCDF(path, this.payload_id, this.lvl));

      //if this is a new cdf file, fill it with the default attributes
      if(getCDF().newFile == true){
         addGAttributes();
      }
      addVars();
   }

   @Override
   protected void addGAttributes(){
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
      this.cdf.attribute("Descriptor", "HKPG>HousKeePinG");
      this.cdf.attribute("Time_resolution", "40s");
      this.cdf.attribute(
         "Logical_source", this.payload_id + "_l" + this.lvl  + "_hkpg"
      );
      this.cdf.attribute(
         "Logical_file_id",
         this.payload_id + "_l" + this.lvl  + "_hkpg_20" + this.date  + 
         "_V" + CDF_Gen.getSetting("rev")
      );
   }

   @Override
   protected void addVars(){
      //create an array containing the details of these variables
      fillVarArray();

      //loop through all of the hkpg variables
      ListIterator<HkpgVar> var_i = vars.listIterator();
      while(var_i.hasNext()){createVar(var_i.next());}
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
         "timeOffset", "Leap Seconds", "sec", 
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
      vars.add(new HkpgVar(
         "Mag_ADC_Offset", "Magnetometer A-D Board Offset",
         " ", 19, 0, 273, CDFConstants.CDF_FLOAT
      ));
      vars.add(new HkpgVar(
         "Mag_ADC_Temp", "Magnetometer A-D Board Temp",
         " ", 23, -273, 273, CDFConstants.CDF_FLOAT
      ));
   }

   private void createVar(final HkpgVar v){
      CDFVar var = new CDFVar(this.cdf, v.getName(), v.getType());

      if(v.getType() == CDFConstants.CDF_INT4){
         var.attribute("FORMAT", "I10");
         var.attribute("FILLVAL", CDFVar.getIstpVal("INT4_FILL"));
      }else if(v.getType() == CDFConstants.CDF_INT2){
         var.attribute("FORMAT", "I5");
         var.attribute("FILLVAL", CDFVar.getIstpVal("INT2_FILL"));
      }else{
         var.attribute("FORMAT", "F4.3");
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
      this.cdf.addVar(v.getName(), var);
   }
}
