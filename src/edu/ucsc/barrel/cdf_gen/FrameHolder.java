/*
FrameHolder.java

Description:
   Stores the data frames that are being processed

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

import java.io.File;
import java.math.BigInteger;
import java.util.Arrays;

public class FrameHolder{
   
   static public float[] hkpg_scale = new float[36];
   static public float[] hkpg_offset = new float[36];
   static public String[] hkpg_label = new String[36];
   
   static public final String[] rc_label = {
	   "Interrupt", "LowLevel", "PeakDet", "HighLevel"
   };
   
   private String payload;
   
   //variables to keep track of valid altitude range
   private float min_alt;
   private boolean low_alt = true;
   
   //variables to  signal frame counter rollover
   private boolean fc_rollover = false;

   public FrameHolder(final String p){
      payload = (p.split(","))[0];
      
      //fill the housekeeping reference arrays
      hkpg_scale[Constants.V0] = 0.0003052f;
      hkpg_scale[Constants.V1] = 0.0003052f;
      hkpg_scale[Constants.V2] = 0.0006104f;
      hkpg_scale[Constants.V3] = 0.0001526f;
      hkpg_scale[Constants.V4] = 0.0001526f;
      hkpg_scale[Constants.V5] = 0.0003052f;
      hkpg_scale[Constants.V6] = -0.0001526f;
      hkpg_scale[Constants.V7] = -0.0001526f;
      hkpg_scale[Constants.V8] = 0.0001526f;
      hkpg_scale[Constants.V9] = 0.0006104f;
      hkpg_scale[Constants.V10] = 0.0006104f;
      hkpg_scale[Constants.V11] = 0.0006104f;
      hkpg_scale[Constants.I0] = 0.05086f;
      hkpg_scale[Constants.I1] = 0.06104f;
      hkpg_scale[Constants.I2] = 0.06104f;
      hkpg_scale[Constants.I3] = 0.01017f;
      hkpg_scale[Constants.I4] = 0.001017f;
      hkpg_scale[Constants.I5] = 0.05086f;
      hkpg_scale[Constants.I6] = -0.0001261f;
      hkpg_scale[Constants.I7] = -0.001017f;
      hkpg_scale[Constants.T0] = 0.007629f;
      hkpg_scale[Constants.T1] = 0.007629f;
      hkpg_scale[Constants.T2] = 0.007629f;
      hkpg_scale[Constants.T3] = 0.007629f;
      hkpg_scale[Constants.T4] = 0.007629f;
      hkpg_scale[Constants.T5] = 0.007629f;
      hkpg_scale[Constants.T6] = 0.007629f;
      hkpg_scale[Constants.T7] = 0.007629f;
      hkpg_scale[Constants.T8] = 0.007629f;
      hkpg_scale[Constants.T9] = 0.007629f;
      hkpg_scale[Constants.T10] = 0.007629f;
      hkpg_scale[Constants.T11] = 0.007629f;
      hkpg_scale[Constants.T12] = 0.007629f;
      hkpg_scale[Constants.T13] = 0.0003052f;
      hkpg_scale[Constants.T14] = 0.0003052f;
      hkpg_scale[Constants.T15] = 0.0001526f;

      hkpg_offset[Constants.T0] = -273.15f;
      hkpg_offset[Constants.T1] = -273.15f;
      hkpg_offset[Constants.T2] = -273.15f;
      hkpg_offset[Constants.T3] = -273.15f;
      hkpg_offset[Constants.T4] = -273.15f;
      hkpg_offset[Constants.T5] = -273.15f;
      hkpg_offset[Constants.T6] = -273.15f;
      hkpg_offset[Constants.T7] = -273.15f;
      hkpg_offset[Constants.T8] = -273.15f;
      hkpg_offset[Constants.T9] = -273.15f;
      hkpg_offset[Constants.T10] = -273.15f;
      hkpg_offset[Constants.T11] = -273.15f;
      hkpg_offset[Constants.T12] = -273.15f;

      hkpg_label[Constants.V0] = "V0_VoltAtLoad";
      hkpg_label[Constants.V1] = "V1_Battery";
      hkpg_label[Constants.V2] = "V2_Solar1";
      hkpg_label[Constants.V3] = "V3_POS_DPU";
      hkpg_label[Constants.V4] = "V4_POS_XRayDet";
      hkpg_label[Constants.V5] = "V5_Modem";
      hkpg_label[Constants.V6] = "V6_NEG_XRayDet";
      hkpg_label[Constants.V7] = "V7_NEG_DPU";
      hkpg_label[Constants.V8] = "V8_Mag";
      hkpg_label[Constants.V9] = "V9_Solar2";
      hkpg_label[Constants.V10] = "V10_Solar3";
      hkpg_label[Constants.V11] = "V11_Solar4";
      hkpg_label[Constants.I0] = "I0_TotalLoad";
      hkpg_label[Constants.I1] = "I1_TotalSolar";
      hkpg_label[Constants.I2] = "I2_Solar1";
      hkpg_label[Constants.I3] = "I3_POS_DPU";
      hkpg_label[Constants.I4] = "I4_POS_XRayDet";
      hkpg_label[Constants.I5] = "I5_Modem";
      hkpg_label[Constants.I6] = "I6_NEG_XRayDet";
      hkpg_label[Constants.I7] = "I7_NEG_DPU";
      hkpg_label[Constants.T0] = "T0_Scint";
      hkpg_label[Constants.T1] = "T1_Mag";
      hkpg_label[Constants.T2] = "T2_ChargeCont";
      hkpg_label[Constants.T3] = "T3_Battery";
      hkpg_label[Constants.T4] = "T4_PowerConv";
      hkpg_label[Constants.T5] = "T5_DPU";
      hkpg_label[Constants.T6] = "T6_Modem";
      hkpg_label[Constants.T7] = "T7_Structure";
      hkpg_label[Constants.T8] = "T8_Solar1";
      hkpg_label[Constants.T9] = "T9_Solar2";
      hkpg_label[Constants.T10] = "T10_Solar3";
      hkpg_label[Constants.T11] = "T11_Solar4";
      hkpg_label[Constants.T12] = "T12_TermTemp";
      hkpg_label[Constants.T13] = "T13_TermBatt";
      hkpg_label[Constants.T14] = "T14_TermCap";
      hkpg_label[Constants.T15] = "T15_CCStat";

      //set minimum altitude based on either command line argument or
      //default setting in the Constants class
      if(CDF_Gen.getSetting("min_alt").equals("")){
         min_alt = Constants.ALT_MIN;
      }else{
         min_alt = Float.parseFloat(CDF_Gen.getSetting("min_alt"));
      }
      System.out.println("Rejecting data bellow " + min_alt + " kilometers.");
      
      //Figure out if the previous CDF file had a frame counter rollover
      if(new File("fc_rollovers/" + payload).exists()){
        fc_rollover = true; 
      }
   }

   public int getVersion(){
      return this.version;
   }

   public void addFrame(BigInteger rawFrame){

   }

}
