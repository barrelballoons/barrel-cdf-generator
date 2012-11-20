package edu.ucsc.barrel.cdf_gen;

import java.math.BigInteger;

/*
DataHolder.java 12.11.15

Description:
   Stores the data frames that are being processed

v12.11.15
   -Changed references to Level_Generator to CDF_Gen
   -Changed many of the objects to primitave types
   -Changed raw_* to *_raw
   
v12.11.05
   -Added a number of raw variables so the level 1 files could all be int or long values
   -Added static "constant" variables to index different mod values

v12.10.11
   -Takes a BigInteger frame as input, breaks the frame apart, and stores the 
   different data types as public members

*/

public class DataHolder{
   ///Largest number of frames we can store.
   //Need to add error checking for to make sure that the total amount of data never exceeds this
   final static int MAX_FRAMES = 172800;
   
   //Index references for 2d arrays that hold different data types
   static public final int 
      //gps index
      ALT = 0, TIME = 1, LAT = 2, LON = 3, 
      //housekeeping index
      V0 = 0, I0 = 1, V1 = 2, I1 = 3, V2 = 4, I2 = 5, V3 = 6, I3 = 7, V4 = 8, 
      I4 = 9, V5 = 10, I5 = 11, V6 = 12, I6 = 13, V7 = 14, I7 = 15, T0 = 16, 
      T8 = 17, T1 = 18, T9 = 19, T2 = 20, T10 = 21, T3 = 22, T11 = 23, T4 = 24, 
      T12 = 25, T5 = 26, T13 = 27, T6 = 28, T14 = 29, T7 = 30, T15 = 31, 
      V8 = 32, V9 = 33, V10 = 34, V11 = 35, 
      SATSOFF = 36, WEEK = 37, CMDCNT = 38, MDMCNT = 39,
      //rate counter index
      INTER = 0, LL = 1, PD = 2, HL = 3;
   
   static public final String[] hkpg_label = { 
      "V0_VoltAtLoad", "I0_TotalLoad", "V1_Battery", "I1_TotalSolar", 
      "V2_Solar1", "I2_Solar1", "V3_POS_DPU", "I3_POS_DPU", "V4_POS_XRayDet",
      "I4_POS_XRayDet","V5_Modem", "I5_Modem", "V6_NEG_XRayDet", 
      "I6_NEG_XRayDet", "V7_NEG_DPU", "I7_NEG_DPU", "T0_Scint", "T8_Solar1",
      "T1_Mag", "T9_Solar2", "T2_ChargeCont", "T10_Solar3", "T3_Battery", 
      "T11_Solar4", "T4_PowerConv", "T12_TermTemp", "T5_DPU", "T13_TermBatt",
      "T6_Modem", "T14_TermCap", "T7_Structure", "T15_CCStat", "V8_Mag", 
      "V9_Solar2", "V10_Solar3", "V11_Solar4" 
   };
   
   static public final String[] rc_label = {
	   "Interrupt", "LowLevel", "PeakDet", "HighLevel"
   };
      
   public short[] 
      payID = new short[MAX_FRAMES], 
      ver = new short[MAX_FRAMES],
      sats = new short[MAX_FRAMES],
      offset = new short[MAX_FRAMES],
      termStat = new short[MAX_FRAMES],
      modemCnt = new short[MAX_FRAMES],
      dcdCnt = new short[MAX_FRAMES];
   public long[]
      epoch = new long[MAX_FRAMES],
      rc_raw = new long[MAX_FRAMES],
      gps_raw = new long[MAX_FRAMES],
      hkpg_raw = new long[MAX_FRAMES],
      ms_of_week = new long[MAX_FRAMES];
   public Long[][]
      magx_raw = new Long[MAX_FRAMES][4],
      magy_raw = new Long[MAX_FRAMES][4],
      magz_raw = new Long[MAX_FRAMES][4];
   public double[]
      gps = new double[MAX_FRAMES],
      hkpg = new double[MAX_FRAMES];
   public Double[][]
      magx = new Double[MAX_FRAMES][4],
      magy = new Double[MAX_FRAMES][4],
      magz = new Double[MAX_FRAMES][4];
   public int[]
      frameNum = new int[MAX_FRAMES],
      weeks = new int[MAX_FRAMES],
      pps = new int[MAX_FRAMES],
      cmdCnt = new int[MAX_FRAMES];
   public Integer[][]
      lc1_raw = new Integer[MAX_FRAMES][20],
      lc2_raw = new Integer[MAX_FRAMES][20],
      lc3_raw = new Integer[MAX_FRAMES][20],
      lc4_raw = new Integer[MAX_FRAMES][20],
      mspc_raw = new Integer[MAX_FRAMES][12],
      sspc_raw = new Integer[MAX_FRAMES][8];
   public int frame_i = 0;
   
   public int getSize(){
      return frame_i;
   }
   
   public void addFrame(BigInteger frame){
      int mod4, mod40;
      
      //sync word
      //frame.shiftRight(1696);
      
      //breakdown frame counter words: 
      //First 5 bits are version, next 6 are id, last 21 are FC
      ver[frame_i] = 
         frame.shiftRight(1691).and(BigInteger.valueOf(31)).shortValue();
      payID[frame_i] = 
         frame.shiftRight(1685).and(BigInteger.valueOf(63)).shortValue();
      frameNum[frame_i] = 
         frame.shiftRight(1664).and(BigInteger.valueOf(2097151)).intValue();
      
      //get mod info
      mod4 = frameNum[frame_i] % 4;
      mod40 = frameNum[frame_i] % 40;
      
      //get gps info: 32 bits of mod4 gps data followed by 16 bits of pps data
      gps_raw[frame_i] = 
         frame.shiftRight(1632).and(BigInteger.valueOf(4294967295L)).longValue();
      switch(mod4){
         case 0: // alt
            gps[frame_i] = gps_raw[frame_i] + 0.0;
            break;
         case 1: // time
            gps[frame_i] = gps_raw[frame_i] + 0.0;
            break;
         default: // coord
            if(gps_raw[frame_i] > 2147483648L){
               gps_raw[frame_i] -= 4294967296L;
            }
            gps[frame_i] = 
               gps_raw[frame_i] * 8.38190317154 * Math.pow(10,-8);
            break;
      }
      pps[frame_i] = 
         frame.shiftRight(1616).and(BigInteger.valueOf(65535)).intValue();
      
      //mag data 4 sets of xyz vectors. 24 bits/component
      magx_raw[frame_i][0] = 
         frame.shiftRight(1592).and(BigInteger.valueOf(16777215)).longValue();
      magy_raw[frame_i][0] = 
         frame.shiftRight(1568).and(BigInteger.valueOf(16777215)).longValue();
      magz_raw[frame_i][0] = 
         frame.shiftRight(1544).and(BigInteger.valueOf(16777215)).longValue();
      magx_raw[frame_i][1] = 
         frame.shiftRight(1520).and(BigInteger.valueOf(16777215)).longValue();
      magy_raw[frame_i][1] = 
         frame.shiftRight(1496).and(BigInteger.valueOf(16777215)).longValue();
      magz_raw[frame_i][1] = 
         frame.shiftRight(1472).and(BigInteger.valueOf(16777215)).longValue();
      magx_raw[frame_i][2] = 
         frame.shiftRight(1448).and(BigInteger.valueOf(16777215)).longValue();
      magy_raw[frame_i][2] = 
         frame.shiftRight(1424).and(BigInteger.valueOf(16777215)).longValue();
      magz_raw[frame_i][2] = 
         frame.shiftRight(1400).and(BigInteger.valueOf(16777215)).longValue();
      magx_raw[frame_i][3] = 
         frame.shiftRight(1376).and(BigInteger.valueOf(16777215)).longValue();
      magy_raw[frame_i][3] = 
         frame.shiftRight(1352).and(BigInteger.valueOf(16777215)).longValue();
      magz_raw[frame_i][3] = 
         frame.shiftRight(1328).and(BigInteger.valueOf(16777215)).longValue();
      
      //mod40 housekeeping data: 16bits
      hkpg_raw[frame_i] = 
         frame.shiftRight(1312).and(BigInteger.valueOf(65535)).longValue();
         
      //fast spectra: 20 sets of 4 channel data. 
      //ch1 and ch2 are 16 bits, ch3 and ch4 are 8bits 
      for(int lc_i = 0; lc_i < 20; lc_i++){
         lc1_raw[frame_i][lc_i] =
            frame.shiftRight(1296 - (48 * lc_i))
               .and(BigInteger.valueOf(65535)).intValue();
         lc2_raw[frame_i][lc_i] =
            frame.shiftRight(1296 - (48 * lc_i) - 16)
               .and(BigInteger.valueOf(65535)).intValue();
         lc3_raw[frame_i][lc_i] =
            frame.shiftRight(1296 - (48 * lc_i) - 32)
               .and(BigInteger.valueOf(255)).intValue();
         lc4_raw[frame_i][lc_i] =
            frame.shiftRight(1296 - (48 * lc_i) - 40)
               .and(BigInteger.valueOf(255)).intValue();
      }
       
      //medium spectra: 12 channels per frame, 16 bits/channels
      for(int mspc_i = 0; mspc_i < 12; mspc_i++){
         mspc_raw[frame_i][mspc_i] =
               frame.shiftRight(336 - (16 * mspc_i))
                  .and(BigInteger.valueOf(65535)).intValue();
      }
    
      //slow spectra: 8 channels per frame, 16 bits/channels
      for(int sspc_i = 0; sspc_i < 8; sspc_i++){
         sspc_raw[frame_i][sspc_i] =
               frame.shiftRight(144 - (16 * sspc_i))
                  .and(BigInteger.valueOf(65535)).intValue();
      }
      
      //rate counter: mod4 data, 16bits
      rc_raw[frame_i] = 
         frame.shiftRight(16).and(BigInteger.valueOf(65535)).longValue();
         
      //checksum: 16bits
      //frame.and(BigInteger.valueOf(65535));
      
      frame_i++;
   }
}
