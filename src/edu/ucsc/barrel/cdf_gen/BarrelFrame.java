//BarrelFrame.java

/*
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

public class BarrelFrame {

   static public final int 
      LAST_DAY_FC = 2010752,
      FC_OFFSET   = 2097152;
   public int
      mod4, mod32, mod40;
   private int
      pps      = Misc.PPS_FILL,
      payID    = Misc.PAYLOADID_FILL, 
      ver      = Misc.VERSION_FILL,
      sats     = HKPG.SATS_FILL,
      offset   = HKPG.UTC_OFFSET_FILL,
      termStat = HKPG.TERM_STAT_FILL,
      modemCnt = HKPG.MODEM_CNT_FILL,
      cmdCnt   = HKPG.CMD_CNT_FILL,
      dcdCnt   = HKPG.DCD_CNT_FILL,
      hkpg     = HKPG.RAW_SENSOR_FILL,
      week     = HPKG.WEEK_FILL,
      rcnt     = RCNT.RAW_CNT_FILL,
      gps      = Ephm.RAW_GPS_FILL;
   public long
      fc       = BarrelCDF.FC_FILL;
   public int[]
      mspc     = {
                  MSPC.RAW_CNT_FILL, MSPC.RAW_CNT_FILL, MSPC.RAW_CNT_FILL,
                  MSPC.RAW_CNT_FILL, MSPC.RAW_CNT_FILL, MSPC.RAW_CNT_FILL,
                  MSPC.RAW_CNT_FILL, MSPC.RAW_CNT_FILL, MSPC.RAW_CNT_FILL,
                  MSPC.RAW_CNT_FILL, MSPC.RAW_CNT_FILL, MSPC.RAW_CNT_FILL
               },
      sspc     = {
                  SSPC.RAW_CNT_FILL, SSPC.RAW_CNT_FILL, SSPC.RAW_CNT_FILL,
                  SSPC.RAW_CNT_FILL, SSPC.RAW_CNT_FILL, SSPC.RAW_CNT_FILL,
                  SSPC.RAW_CNT_FILL, SSPC.RAW_CNT_FILL
               },
      mag      = {
                  {Magn.RAW_MAG_FILL, Magn.RAW_MAG_FILL, Magn.RAW_MAG_FILL},
                  {Magn.RAW_MAG_FILL, Magn.RAW_MAG_FILL, Magn.RAW_MAG_FILL},
                  {Magn.RAW_MAG_FILL, Magn.RAW_MAG_FILL, Magn.RAW_MAG_FILL},
                  {Magn.RAW_MAG_FILL, Magn.RAW_MAG_FILL, Magn.RAW_MAG_FILL}
               };
   public int[][]
      fspc        = null;
   private BigInteger
      rawFrame    = null;
   boolean valid  = true;
 
   public BarrelFrame(final BigInteger frame, final short dpuId){
      //Breakdown frame counter words: 
      //save the frame counter parts as temp variables,
      //they will be written to the main structure once rec_num is calculated.
      //First 5 bits are version, next 6 are id, last 21 are FC
      this.valid = this.setVersion(
         frame.shiftRight(1691).and(BigInteger.valueOf(31)).shortValue()
      );

      //make sure this frame belongs to this payload
      this.valid = this.setPayloadID(
         frame.shiftRight(1685).and(BigInteger.valueOf(63)).shortValue(), dpuId
      );
      if(!this.valid){
         CDF_Gen.log.writeln(
            "Found frame from dpu " + this.payId + " should be dpu " + dpuId
         );
         return;
      }

      this.valid = this.setFrameCounter(
         frame.shiftRight(1664).and(BigInteger.valueOf(2097151)).intValue()
      );

      this.valid = this.setGPS(
         frame.shiftRight(1632).and(BigInteger.valueOf(4294967295L)).intValue()
      );
/*
      //sets the current record number
      rec_num_1Hz++;
      rec_num_4Hz = (rec_num_1Hz) * 4;
      rec_num_20Hz = (rec_num_1Hz) * 20;
      try{
         if((tmpFC - mod4) != frame_mod4[rec_num_mod4]){
            //check if the medium spectrum is complete
            if(mspc_frames != 4){
               mspc_q[rec_num_mod4] = Constants.PART_SPEC;
            }
            mspc_frames = 0;

            rec_num_mod4++;
         }
         if((tmpFC - mod32) != frame_mod32[rec_num_mod32]){
            //check if the medium spectrum is complete
            if(sspc_frames != 32){
               sspc_q[rec_num_mod32] = Constants.PART_SPEC;
            }
            sspc_frames = 0;

            rec_num_mod32++;
         }
         if((tmpFC - mod40) != frame_mod40[rec_num_mod40]){
            rec_num_mod40++;
         }
      }catch(ArrayIndexOutOfBoundsException ex){
         rec_num_mod4 = 0;
         rec_num_mod32 = 0;
         rec_num_mod40 = 0;
      }

      //save the info from the frame counter word
      ver[rec_num_1Hz] = this.version;
      payID[rec_num_1Hz] = tmpPayID;
      frame_1Hz[rec_num_1Hz] = (int)tmpFC;

      //figure out the other time scale frame counters
      for(int rec_i = rec_num_4Hz; rec_i < rec_num_4Hz + 4; rec_i++){
         frame_4Hz[rec_i] = frame_1Hz[rec_num_1Hz];
      }
      for(int rec_i = rec_num_20Hz; rec_i < rec_num_20Hz + 20; rec_i++){
         frame_20Hz[rec_i] = frame_1Hz[rec_num_1Hz];
      }
     
      //calculate and save the first frame number of the current group
      frame_mod4[rec_num_mod4] = frame_1Hz[rec_num_1Hz] - mod4;
      frame_mod32[rec_num_mod32] = frame_1Hz[rec_num_1Hz] - mod32;
      frame_mod40[rec_num_mod40] = frame_1Hz[rec_num_1Hz] - mod40;
*/    
    
      //GPS PPS
      this.valid = this.setPPS(
         frame.shiftRight(1616).and(BigInteger.valueOf(65535)).shortValue()
      );

      //mag data 4 sets of xyz vectors. 24 bits/component
      for(int sample_i = 0; sample_i < this.mag.length; sample_i++){
         this.valid = this.setMag(
            Magnetometer.X_AXIS,
            sample_i,
            frame.shiftRight(1592 - (72 * i)).
               and(BigInteger.valueOf(16777215)).intValue()
         );
         this.valid = this.setMag(
            Magnetometer.Y_AXIS,
            sample_i,
            frame.shiftRight(1568 - (72 * i)).
               and(BigInteger.valueOf(16777215)).intValue()
         );
         this.valid = this.setMag(
            Magnetometer.Z_AXIS,
            sample_i,
            frame.shiftRight(1544 - (72 * i)).
               and(BigInteger.valueOf(16777215)).intValue()
         );
      }
      
      //mod40 housekeeping data: 16bits
      this.valid = this.setHousekeeping(
         frame.shiftRight(1312).and(BigInteger.valueOf(65535)).longValue()
      );
      
      //fast spectra: 20Hz data
      for(int sample = 0; sample < 20; sample++){
         this.valid = this.setFSPC(
            sample, 
            frame.shiftRight(1264 - sample * 48).and(
               BigInteger.valueOf(281474976710656L)
            ).intValue()
         );
      }
       
      //medium spectra: 12 channels per frame, 16 bits/channels
      for(int chan_i = 0; chan_i < this.mspc.length; chan_i++){
         this.setMspc(
            chan_i,
            frame.shiftRight(336 - (16 * chan_i)).
            and(BigInteger.valueOf(65535)).intValue()
         );
      }

      //slow spectra: 8 channels per frame, 16 bits/channels
      for(int chan_i = 0; chan_i < this.sspc.length; chan_i++){
         chan_i = (this.mod32 * 8) + sspc_i;
         this.valid = this.setSspc(
            chan_i,
            frame.shiftRight(144 - (16 * sspc_i))
               .and(BigInteger.valueOf(65535)).intValue()
         );
      }
      //add to the frame count
      sspc_frames++;
      
      //rate counter: mod4 data, 16bits
      this.valid = this.setRateCounter(
         frame.shiftRight(16).and(BigInteger.valueOf(65535)).longValue()
      );
   }
   public boolean setFrameCounter(final int fc){
   
      this.fc = fc;

      //validate frame number
      if(tmpFC <= Constants.FC_MIN || tmpFC > Constants.FC_MAX){
         return false;
      }

      //if there was a rollover, flag the data
      /*
      if(fc_rollover){
         this.gps_q[rec_num_mod4]   |= Constants.FC_ROLL;
         this.pps_q[rec_num_1Hz]    |= Constants.FC_ROLL;
         this.hkpg_q[rec_num_mod40] |= Constants.FC_ROLL;
         this.rcnt_q[rec_num_mod4]  |= Constants.FC_ROLL;
         this.mspc_q[rec_num_mod4]  |= Constants.FC_ROLL;
         this.sspc_q[rec_num_mod32] |= Constants.FC_ROLL;
         for(int lc_i = 0; lc_i < 20; lc_i++){
            this.fspc_q[rec_num_1Hz + lc_i] |= Constants.FC_ROLL;
         }
         for(int mag_i = 0; mag_i < 4; mag_i++){
            this.magn_q[rec_num_1Hz + mag_i] |= Constants.FC_ROLL;
         }
      }
      */

      //get multiplex info
      this.mod4 = (int)fc % 4;
      this.mod32 = (int)fc % 32;
      this.mod40 = (int)fc % 40;

      return true;
   }

   public boolean setPayloadID(final short payID, final short dpuID){
      this.payID = payID;
      if(this.payID != dpuID){
         return false;
      }
      return true;
   }

   public boolean setPPS(final short pps){
      this.pps = pps;
      if((this.pps < Constants.PPS_MIN) || (this.pps > Constants.PPS_MAX)){
         //make sure the value is not out of range because of an early pps
         if(this.pps != 65535){
            this.pps = Constants.PPS_FILL;
            //pps_q[rec_num_1Hz] |= Constants.OUT_OF_RANGE;
            return false;
         }
      }

      return true;

      /*
      //flag potentially bad gps and pps records
      if(
         mod4 > 0 && // make sure there is a previous record to compare to
         pps[rec_num_1Hz] == 65535 && //possible bad pps
         ms_of_week[rec_num_mod4] == ms_of_week[rec_num_mod4 - 1] //gps repeat
      ){
         pps_q[rec_num_1Hz] |= Constants.NO_GPS;
         gps_q[rec_num_mod4] |= Constants.NO_GPS;
      }
      */
   }

   public boolean setDPUVersion(final short ver){
      this.ver = ver;
      return true;
   }

   public boolean setNumSats(final short sats){
      this.sats = sats;
      return true;
   }

   public boolean setUTCOffset(final short offset){
      this.offset = offset;
      return true;
   }

   public boolean setTermStatus(final short termStat){
      this.termStat = termStat;
      return true;
   }

   public boolean setModemCount(final short modemCnt){
      this.modemCnt = modemCnt;
      return true;
   }

   public boolean setDcdCount(final short dcdCnt){
      this.dcdCnt = dcdCnt;
      return true;
   }

   public boolean setWeek(final int week){
      this.week = week;
      return true;
   }

   public boolean setCommandCounter(final int cmdCnt){
      this.cmdCnt = cmdCnt;
      return true;
   }

   public boolean setFSPC(final int sample, final BigInteger raw){
      int channels = FSPC.getChannels(this.version);
      int[] fspc = new int[channels.length];
      int value;
      boolean valid = true;

      for(int ch_i = 0; ch_i < channels.length; ch_i++){
          value = 
             raw.shiftRight(channels[ch_i].start)
             .and(BigInteger.valueOf(channels[ch_i].width)).intValue();
         if(
            (value < Constants.FSPC_RAW_MIN) ||
            (value > Constants.FSPC_RAW_MAX)
         ){
            fspc[ch_i] = Constants.FSPC_RAW_FILL;
            //fspc_q[ch_i] |= Constants.OUT_OF_RANGE;
            valid = false;
         } else {
            fspc[ch_i] = value;
         }
      }

      return valid;
/*
      lc1[rec_num_20Hz + lc_i] =
         frame.shiftRight(1296 - (48 * lc_i))
            .and(BigInteger.valueOf(65535)).intValue();
      lc2[rec_num_20Hz + lc_i] =
         frame.shiftRight(1280 - (48 * lc_i))
            .and(BigInteger.valueOf(65535)).intValue();
      lc3[rec_num_20Hz + lc_i] =
         frame.shiftRight(1272 - (48 * lc_i))
            .and(BigInteger.valueOf(255)).intValue();
      lc4[rec_num_20Hz + lc_i] =
         frame.shiftRight(1264 - (48 * lc_i))
            .and(BigInteger.valueOf(255)).intValue();

      lc1[rec_num_20Hz + lc_i] =
         frame.shiftRight(1303 - (48 * lc_i))
            .and(BigInteger.valueOf(511)).intValue();
      lc2[rec_num_20Hz + lc_i] =
         frame.shiftRight(1294 - (48 * lc_i))
            .and(BigInteger.valueOf(511)).intValue();
      lc3[rec_num_20Hz + lc_i] =
         frame.shiftRight(1286 - (48 * lc_i))
            .and(BigInteger.valueOf(255)).intValue();
      lc4[rec_num_20Hz + lc_i] =
         frame.shiftRight(1277 - (48 * lc_i))
            .and(BigInteger.valueOf(511)).intValue();
      lc5[rec_num_20Hz + lc_i] =
         frame.shiftRight(1270 - (48 * lc_i))
            .and(BigInteger.valueOf(127)).intValue();
      lc6[rec_num_20Hz + lc_i] =
         frame.shiftRight(1264 - (48 * lc_i))
            .and(BigInteger.valueOf(63)).intValue();
*/
   }

   public boolean setMSPC(final int chan_i, final int mspc){
      this.mspc[chan_i] = mspc;
      if(
         (this.mspc[chan_i] < Constants.MSPC_RAW_MIN) ||
         (this.mspc[chan_i] > Constants.MSPC_RAW_MAX)
      ){
         this.mspc[chan_i] = Constants.MSPC_RAW_FILL;
         //this.mspc_q |= Constants.OUT_OF_RANGE;
         return false;
      }
      return true;
   }

   public boolean setSSPC(final int chan_i, final int sspc){
      this.sspc[chan_i] = sspc;
      if(
         (sspc[chan_i] < Constants.SSPC_RAW_MIN) ||
         (sspc[chan_i] > Constants.SSPC_RAW_MAX)
      ){
         sspc[chan_i] = Constants.SSPC_RAW_FILL;
         //sspc_q |= Constants.OUT_OF_RANGE;
         return false;
      }
      return true;
   }

   public boolean setHousekeeping(final long hkpg){
      boolean valid = true;
      if((hkpg < Constants.HKPG_MIN) || (hkpg > Constants.HKPG_MAX)){
         this.hkpg = Constants.HKPG_FILL;
         //this.hkpg_q |= Constants.OUT_OF_RANGE;
         return false;
      } else {
         this.hkpg = hkpg;
      }

      switch(this.mod40){
         case 36:
            this.sats   = (short)(this.hkpg >> 8);
            this.offset = (short)(this.hkpg & 255);

            if((this.sats < Constants.SATS_MIN) ||
               (this.sats > Constants.SATS_MAX)
            ){
               this.sats = Constants.SATS_FILL;
               //this.hkpg_q |= Constants.OUT_OF_RANGE;
               valid = false;
            }

            if(
               (this.offset < Constants.LEAP_SEC_MIN) ||
               (this.offset > Constants.LEAP_SEC_MAX)
            ){
               this.offset = Constants.LEAP_SEC_FILL;
               //this.hkpg_q |= Constants.OUT_OF_RANGE;
               valid = false;
            }

            break;

         case 37:
            this.weeks = (int)this.hkpg;
            if(
               (this.weeks < Constants.WEEKS_MIN) ||
               (this.weeks > Constants.WEEKS_MAX)
            ){
               this.weeks = Constants.WEEKS_FILL;
               //this.hkpg_q |= Constants.OUT_OF_RANGE;
               valid = false;
            }

            break;

         case 38:
            this.termStat = (short)(this.hkpg >> 15);
            this.cmdCnt = (int)(this.hkpg & 32767);

            if(
               (this.termStat < Constants.TERM_STAT_MIN) ||
               (this.termStat > Constants.TERM_STAT_MAX)
            ){
               this.termStat = Constants.TERM_STAT_FILL;
               //this.hkpg_q |= Constants.OUT_OF_RANGE;
               valid = false;
            }
            if(
               (this.cmdCnt < Constants.CMD_CNT_MIN) ||
               (this.cmdCnt > Constants.CMD_CNT_MAX)
            ){
               this.cmdCnt = Constants.CMD_CNT_FILL;
               //this.hkpg_q |= Constants.OUT_OF_RANGE;
               valid = false;
            }
            break;
         case 39:
            this.dcdCnt = (short)(this.hkpg >> 8);
            this.modemCnt = (short)(this.hkpg & 255);
            if(
               (this.dcdCnt < Constants.DCD_CNT_MIN) ||
               (this.dcdCnt > Constants.DCD_CNT_MAX)
            ){
               this.dcdCnt = Constants.DCD_CNT_FILL;
               //this.hkpg_q |= Constants.OUT_OF_RANGE;
               valid = false;
            }
            if(
               (this.modemCnt < Constants.MODEM_CNT_MIN) ||
               (this.modemCnt > Constants.MODEM_CNT_MAX)
            ){
               this.modemCnt = Constants.MODEM_CNT_FILL;
               //this.hkpg_q |= Constants.OUT_OF_RANGE;
               valid = false;
            }
            break;
         default:
            break;
      }

      return valid;
   }

   public boolean setRateCounter(final long r){
      if((this.rcnt < Constants.RCNT_MIN) || (this.rcnt > Constants.RCNT_MAX)){
         this.rcnt = Constants.RCNT_FILL;
         //this.rcnt_q[rec_num_mod4] |= Constants.OUT_OF_RANGE;
         return false;
      } 

      this.rcnt = r;
      return true;
   }

   public boolean setGPS(final int gps){
      boolean valid = true;
      this.gps = gps;
      
      /*
      //check the payload is above the minimum altitude
      if(low_alt){
         if((mod4 == Constants.ALT_I) && ((tmpGPS / 1000000) >= min_alt)){
            low_alt = false;
         }else{return;}
      }else{
         if((mod4 == Constants.ALT_I) && ((tmpGPS / 1000000) < min_alt)){
            low_alt = true;
            return;
         }
      }
      //check the payload is above the minimum altitude
      if(low_alt){
         if((mod4 == Constants.ALT_I) && ((tmpGPS / 1000000) >= min_alt)){
            low_alt = false;
         }else{return;}
      }else{
         if((mod4 == Constants.ALT_I) && ((tmpGPS / 1000000) < min_alt)){
            low_alt = true;
            return;
         }
      }
      */

      //get gps info: 32 bits of mod4 gps data followed by 16 bits of pps data
      switch(this.mod4){
         case Constants.ALT_I:
            if(
               (gps[this.mod4] < Constants.ALT_RAW_MIN) ||
               (gps[this.mod4] > Constants.ALT_RAW_MAX)
            ){
               gps[this.mod4] = Constants.ALT_RAW_FILL;
               //gps_q[rec_num_mod4] |= Constants.OUT_OF_RANGE;  
               valid = false;
            }
            else if(gps[this.mod4] < Constants.MIN_SCI_ALT){
               /*gps_q[rec_num_mod4] |= Constants.LOW_ALT;
               pps_q[rec_num_1Hz] |= Constants.LOW_ALT;
               hkpg_q[rec_num_mod40] |= Constants.LOW_ALT;
               rcnt_q[rec_num_mod4] |= Constants.LOW_ALT;
               mspc_q[rec_num_mod4] |= Constants.LOW_ALT;
               sspc_q[rec_num_mod32] |= Constants.LOW_ALT;
               for(int lc_i = 0; lc_i < 20; lc_i++){
                  fspc_q[rec_num_1Hz + lc_i] |= Constants.LOW_ALT;
               }
               for(int mag_i = 0; mag_i < 4; mag_i++){
                  magn_q[rec_num_1Hz + mag_i] |= Constants.LOW_ALT;
               }*/
               valid = false;
            }
            break;
         case Constants.TIME_I:
            if(
               (gps[this.mod4] < Constants.MS_WEEK_MIN) ||
               (gps[this.mod4] > Constants.MS_WEEK_MAX)
            ){
               gps[this.mod4] = Constants.MS_WEEK_FILL;
               //gps_q[rec_num_mod4] |= Constants.OUT_OF_RANGE;  
               valid = false;
            }
            break;
         case Constants.LAT_I:
         case Constants.LON_I:
            break;
         default:
            valid = false;
            break;
      }
      return valid;
   }

   public boolean setMag(
      final int axis, final int sample, final int mag
   ){
      if((mag < Constants.MAG_MIN) || (mag > Constants.MAG_MAX)){
         this.mag[sample][axis] = Constants.MAG_FILL;
         //magn_q[rec_num_4Hz] |= Constants.OUT_OF_RANGE;
         return false;
      }

      this.mag[sample][axis] = mag;
      return true;
   }

   public int getFrameCounter(){
      return this.fc;
   }

   public short getPayloadID(){
      return this.payId;
   }

   public short getPPS(){
      return this.pps;
   }

   public short getDPUVersion(){
      return this.ver;
   }

   public short getNumSats(){
      return this.sats;
   }

   public short getUTCOffset(){
      return this.offset;
   }

   public short getTermStatus(){
      return this.termStat;
   }

   public short getModemCount(){
      return this.modenCnt;
   }

   public short getDcdCount(){
      return this.dcdCnt;
   }

   public int getWeek(){
      return this.week;
   }

   public int getCommandCounter(){
      return this.cmdCnt;
   }

   public int[] getFSPC(){
      return this.fspc;
   }

   public int[] getMSPC(){
      return this.mspc;
   }

   public int[] getSSPC(){
      return this.sspc;
   }

   public long getHousekeeping(){
      return this.hkpg;
   }

   public int getRateCounter(){
      return this.rcnt;
   }

   public int getGPS(){
      return this.gps;
   }

   public int[][] getMag(){
      return this.mag;
   }
}
