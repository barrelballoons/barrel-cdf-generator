package edu.ucsc.barrel.cdf_gen;

import java.math.BigInteger;

/*
DataHolder.java 13.01.04

Description:
   Stores the data frames that are being processed

v13.01.04
   -Changed "ms_since_epoch" or "ms_since_sys_epoch" for clairity
   
v12.11.27
   -Added members to hold time model info
   -Added holder for quality flag
   -Fixed fspc extraction
   
v12.11.22
   -Saves ms_of_week variable when reading it when processing a gps time frame

v12.11.20
   -Changed references to Level_Generator to CDF_Gen
   -Changed many of the objects to primitave types
   -Changed raw_* to *_raw
   
v12.11.05
   -Added a number of raw variables so the level 1 files could all
   be int or long values
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
      sats = new short[MAX_FRAMES / 40],
      offset = new short[MAX_FRAMES / 40],
      termStat = new short[MAX_FRAMES / 40],
      modemCnt = new short[MAX_FRAMES / 40],
      dcdCnt = new short[MAX_FRAMES / 40];
   public long[]
      epoch_1Hz = new long[MAX_FRAMES],
      epoch_4Hz = new long[MAX_FRAMES],
      epoch_20Hz = new long[MAX_FRAMES],
      epoch_mod4 = new long[MAX_FRAMES],
      epoch_mod32 = new long[MAX_FRAMES],
      epoch_mod40 = new long[MAX_FRAMES],
      ms_of_week = new long[MAX_FRAMES / 4],
   public long[][]
      hkpg_raw = new long[36][MAX_FRAMES];
      gps_raw = new long[4][MAX_FRAMES / 4],
      rcnt_raw = new long[4][MAX_FRAMES / 4];
   public Long[]
      magx_raw = new Long[MAX_FRAMES * 4],
      magy_raw = new Long[MAX_FRAMES * 4],
      magz_raw = new Long[MAX_FRAMES * 4];
   public double[]
      gps = new double[MAX_FRAMES / 4],
      time_model_rate = new double[MAX_FRAMES],
      time_model_offset = new double[MAX_FRAMES],
      ms_since_sys_epoch = new double[MAX_FRAMES];
   public double[][]
      hkpg = new double[36][MAX_FRAMES / 40],
   public Double[]
      magx = new Double[MAX_FRAMES * 4],
      magy = new Double[MAX_FRAMES * 4],
      magz = new Double[MAX_FRAMES * 4];
   public int[]
      frame_1Hz = new int[MAX_FRAMES],
      frame_4Hz = new int[MAX_FRAMES],
      frame_20Hz = new int[MAX_FRAMES],
      frame_mod4 = new int[MAX_FRAMES],
      frame_mod32 = new int[MAX_FRAMES],
      frame_mod40 = new int[MAX_FRAMES],
      weeks = new int[MAX_FRAMES / 40],
      pps = new int[MAX_FRAMES],
      cmdCnt = new int[MAX_FRAMES / 40],
      gps_q = new int[MAX_FRAMES / 4],
      pps_q = new int[MAX_FRAMES],
      magn_q = new int[MAX_FRAMES * 4],
      hkpg_q = new int[MAX_FRAMES / 40],
      rcnt_q = new int[MAX_FRAMES / 4],
      fspc_q = new int[MAX_FRAMES * 20],
      mspc_q = new int[MAX_FRAMES / 4],
      sspc_q = new int[MAX_FRAMES / 32];
   public Integer[][]
      lc1_raw = new Integer[MAX_FRAMES * 20],
      lc2_raw = new Integer[MAX_FRAMES * 20],
      lc3_raw = new Integer[MAX_FRAMES * 20],
      lc4_raw = new Integer[MAX_FRAMES * 20],
      mspc_raw = new Integer[MAX_FRAMES / 4][48],
      sspc_raw = new Integer[MAX_FRAMES / 32][256];

   public int 
      rec_num_1Hz = 0, rec_num_4Hz = 0, rec_num_20Hz = 0,
      rec_num_mod4 = 0, rec_num_mod32 = 0, rec_num_mod40 = 0;
   
   public int getSize(){
      return rec_num;
   }
   
   public void addFrame(BigInteger frame){
      int fc_mod4, fc_mod32, fc_mod40;
      
      //sync word
      //frame.shiftRight(1696);
      
      //breakdown frame counter words: 
      //First 5 bits are version, next 6 are id, last 21 are FC
      ver[rec_num] = 
         frame.shiftRight(1691).and(BigInteger.valueOf(31)).shortValue();
      payID[rec_num] = 
         frame.shiftRight(1685).and(BigInteger.valueOf(63)).shortValue();
      frame_1Hz[rec_num] = 
         frame.shiftRight(1664).and(BigInteger.valueOf(2097151)).intValue();
      
      //figure out the other time scale frame counters
      for(int rec_i = (rec_num * 4); rec_i < (frm_i * 5); rec_i++){
         frame_4Hz[rec_i] = frame_1Hz[rec_num];
      }
      for(int rec_i = (rec_num * 20); rec_i < (frm_i * 21); rec_i++){
         frame_20Hz[rec_i] = frame_1Hz[rec_num];
      }
      frame_mod4[rec_num % 4] = frame_1Hz[frm_i];
      frame_mod32[rec_num % 32] = frame_1Hz[frm_i];
      frame_mod40[rec_num % 40] = frame_1Hz[frm_i];
      
      //get mod info
      fc_mod4 = frameNum[rec_num] % 4;
      fc_mod32 = frameNum[rec_num] % 32;
      fc_mod40 = frameNum[rec_num] % 40;
      
      //get gps info: 32 bits of mod4 gps data followed by 16 bits of pps data
      gps_raw[mod4][rec_num] = 
         frame.shiftRight(1632).and(BigInteger.valueOf(4294967295L)).longValue();
      switch(mod4){
         case 0: // alt
            gps[rec_num] = gps_raw[frm_i] + 0.0;
            break;
         case 1: // time
            gps[rec_num] = gps_raw[frm_i] + 0.0;
            ms_of_week[rec_num] = gps_raw[frm_i];
            break;
         default: // coord
            if(gps_raw[rec_num] > 2147483648L){
               gps_raw[rec_num] -= 4294967296L;
            }
            gps[rec_num] = 
               gps_raw[rec_num] * 8.38190317154 * Math.pow(10,-8);
            break;
      }
      pps[rec_num] = 
         frame.shiftRight(1616).and(BigInteger.valueOf(65535)).intValue();
      
      //mag data 4 sets of xyz vectors. 24 bits/component
      magx_raw[rec_num][0] = 
         frame.shiftRight(1592).and(BigInteger.valueOf(16777215)).longValue();
      magy_raw[rec_num][0] = 
         frame.shiftRight(1568).and(BigInteger.valueOf(16777215)).longValue();
      magz_raw[rec_num][0] = 
         frame.shiftRight(1544).and(BigInteger.valueOf(16777215)).longValue();
      magx_raw[rec_num][1] = 
         frame.shiftRight(1520).and(BigInteger.valueOf(16777215)).longValue();
      magy_raw[rec_num][1] = 
         frame.shiftRight(1496).and(BigInteger.valueOf(16777215)).longValue();
      magz_raw[rec_num][1] = 
         frame.shiftRight(1472).and(BigInteger.valueOf(16777215)).longValue();
      magx_raw[rec_num][2] = 
         frame.shiftRight(1448).and(BigInteger.valueOf(16777215)).longValue();
      magy_raw[rec_num][2] = 
         frame.shiftRight(1424).and(BigInteger.valueOf(16777215)).longValue();
      magz_raw[rec_num][2] = 
         frame.shiftRight(1400).and(BigInteger.valueOf(16777215)).longValue();
      magx_raw[rec_num][3] = 
         frame.shiftRight(1376).and(BigInteger.valueOf(16777215)).longValue();
      magy_raw[rec_num][3] = 
         frame.shiftRight(1352).and(BigInteger.valueOf(16777215)).longValue();
      magz_raw[rec_num][3] = 
         frame.shiftRight(1328).and(BigInteger.valueOf(16777215)).longValue();
      
      //mod40 housekeeping data: 16bits
      hkpg_raw[rec_num] = 
         frame.shiftRight(1312).and(BigInteger.valueOf(65535)).longValue();
      switch(frameNum[rec_num] % 40 ){
         case 36:
            sats[rec_num] = (short)(hkpg_raw[frm_i] >> 8);
            offset[rec_num] = (short)(hkpg_raw[frm_i] & 255);
            break;
         case 37:
            weeks[rec_num] = (int) hkpg_raw[frm_i];
            break;
         case 38:
            termStat[rec_num] = (short)(hkpg_raw[frm_i] >> 15);
            cmdCnt[rec_num] = (int)(hkpg_raw[frm_i] & 32768);
            break;
         case 39:
            dcdCnt[rec_num] = (short)(hkpg_raw[frm_i] >> 8);
            modemCnt[rec_num] = (short)(hkpg_raw[frm_i] & 255);
            break;
         default:
            break;
      }
         
      //fast spectra: 20 sets of 4 channel data. 
      //ch1 and ch2 are 16 bits, ch3 and ch4 are 8bits 
      for(int lc_i = 0; lc_i < 20; lc_i++){
         lc1_raw[rec_num][lc_i] =
            frame.shiftRight(1296 - (48 * lc_i))
               .and(BigInteger.valueOf(65535)).intValue();
         lc2_raw[rec_num][lc_i] =
            frame.shiftRight(1280 - (48 * lc_i))
               .and(BigInteger.valueOf(65535)).intValue();
         lc3_raw[rec_num][lc_i] =
            frame.shiftRight(1272 - (48 * lc_i))
               .and(BigInteger.valueOf(255)).intValue();
         lc4_raw[rec_num][lc_i] =
            frame.shiftRight(1264 - (48 * lc_i))
               .and(BigInteger.valueOf(255)).intValue();
      }
       
      //medium spectra: 12 channels per frame, 16 bits/channels
      for(int mspc_i = 0; mspc_i < 12; mspc_i++){
         mspc_raw[rec_num][mspc_i] =
               frame.shiftRight(336 - (16 * mspc_i))
                  .and(BigInteger.valueOf(65535)).intValue();
      }
    
      //slow spectra: 8 channels per frame, 16 bits/channels
      for(int sspc_i = 0; sspc_i < 8; sspc_i++){
         sspc_raw[rec_num][sspc_i] =
               frame.shiftRight(144 - (16 * sspc_i))
                  .and(BigInteger.valueOf(65535)).intValue();
      }
      
      //rate counter: mod4 data, 16bits
      rc_raw[rec_num] = 
         frame.shiftRight(16).and(BigInteger.valueOf(65535)).longValue();
         
      //checksum: 16bits
      //frame.and(BigInteger.valueOf(65535));
      
      rec_num++;
   }
}
