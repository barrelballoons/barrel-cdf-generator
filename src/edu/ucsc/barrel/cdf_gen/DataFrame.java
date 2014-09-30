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

public class DataFrame{

   //ISTP defined fill values
   static public final int 
      UINT1_FILL = 255,
      UINT2_FILL = 65535,
      INT1_FILL = -128,
      INT2_FILL = -32768,
      INT4_FILL = -2147483648;
   static public final long
      INT8_FILL = -9223372036854775808L,
      UINT4_FILL = 4294967295L;
   static public final float 
      FLOAT_FILL = -1.0e+31f;
   static public final double 
      DOUBLE_FILL = -1.0e+31;
   
   static public final int 
      LAST_DAY_FC = 2010752,
      FC_OFFSET = 2097152;

   private short  
      pps         = INT2_FILL,
      payID       = INT2_FILL, 
      ver         = INT2_FILL,
      sats        = INT2_FILL,
      offset      = INT2_FILL,
      termStat    = INT2_FILL,
      modemCnt    = INT2_FILL,
      dcdCnt      = INT2_FILL;
   public int 
      fc          = INT4_FILL,
      week        = INT4_FILL,
      cmdCnt      = INT4_FILL,
      mspc        = INT4_FILL,
      sspc        = INT4_FILL,
      gps         = INT4_FILL;
   private long
      epoch       = INT4_FILL,
      hkpg        = INT4_FILL,
      rcnt        = INT4_FILL;
   public float 
      peak511_bin = null;
   public int[]
      mag         = null;
   public int[][]
      fspc        = null;
   private BigInteger
      rawFrame = null;
   boolean valid = true;
 
   public DataFrame(final BigInteger frame, final int dpuId){
      //Breakdown frame counter words: 
      //save the frame counter parts as temp variables,
      //they will be written to the main structure once rec_num is calculated.
      //First 5 bits are version, next 6 are id, last 21 are FC
      this.valid = this.setVersion(
         frame.shiftRight(1691).and(BigInteger.valueOf(31)).shortValue()
      );

      //make sure this frame belongs to this payload
      this.valid = this.setPayID(
         frame.shiftRight(1685).and(BigInteger.valueOf(63)).shortValue(), dpuId
      );
      if(!this.valid){
         CDF_Gen.log.writeln(
            "Found frame from dpu " + this.payId + 
            " should be dpu " + dpuId
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
      this.valid = setPulsePerSecond([rec_num_1Hz] = 
         frame.shiftRight(1616).and(BigInteger.valueOf(65535)).shortValue());

      //mag data 4 sets of xyz vectors. 24 bits/component
      for(int i = 0; i < 4; i++){
         this.valid = this.setMagnetometer(
            Magnetometer.X_AXIS,
            frame.shiftRight(1592 - (72 * i)).
            and(BigInteger.valueOf(16777215)).intValue(),
         );
         this.valid = this.setMag(
            Magnetometer.Y_AXIS,
            frame.shiftRight(1568 - (72 * i)).
            and(BigInteger.valueOf(16777215)).intValue();
         );
         this.valid = this.setMag(
            Magnetometer.Z_AXIS,
            frame.shiftRight(1544 - (72 * i)).
            and(BigInteger.valueOf(16777215)).intValue();
         );
         
      }
      
      //mod40 housekeeping data: 16bits
      this.setHousekeeping(
         frame.shiftRight(1312).and(BigInteger.valueOf(65535)).longValue()
      );
      
      //fast spectra: 20Hz data
      for(int sample = 0; sample < 20; sample++){
         this.setFSPC(
            sample, 
            frame.shiftRight(1264 - sample * 48).
            and(BigInteger.valueOf(281474976710656));
         );
      }
       
      //medium spectra: 12 channels per frame, 16 bits/channels
      for(int chan_i = 0; chan_i < 12; chan_i++){
         this.setMspc(
            chan_i,
            frame.shiftRight(336 - (16 * chan_i)).
            and(BigInteger.valueOf(65535)).intValue()
         );
      }

      //slow spectra: 8 channels per frame, 16 bits/channels
      for(int chan_i = 0; chan_i < 8; chan_i++){
         chan_i = (mod32 * 8) + sspc_i;
         this.setSspc(
            chan_i,
            frame.shiftRight(144 - (16 * sspc_i))
               .and(BigInteger.valueOf(65535)).intValue()
         );
      }
      //add to the frame count
      sspc_frames++;
      
      //rate counter: mod4 data, 16bits
      rcnt[mod4][rec_num_mod4] = 
         frame.shiftRight(16).and(BigInteger.valueOf(65535)).longValue();
      if(
         (rcnt[mod4][rec_num_mod4] < Constants.RCNT_MIN) ||
         (rcnt[mod4][rec_num_mod4] > Constants.RCNT_MAX)
      ){
         rcnt[mod4][rec_num_mod4] = Constants.RCNT_FILL;
         rcnt_q[rec_num_mod4] |= Constants.OUT_OF_RANGE;
      }
   }
   public boolean setFrameCounter(final int fc){
   
      this.fc = fc;

      //validate frame number
      if(tmpFC <= Constants.FC_MIN || tmpFC > Constants.FC_MAX){return;}

      //check for fc rollover
      if(fc_rollover){
         tmpFC += Constants.FC_OFFSET;
      }else{
         if((last_fc - tmpFC) > Constants.LAST_DAY_FC){
            //rollover detected
            fc_rollover = true;
           
         CDF_Gen.log.writeln(
               "Payload " + payload + " rolled over after fc = " + last_fc 
            );

            //offset fc
            tmpFC += Constants.FC_OFFSET;

            //create an empty file to indicate rollover
            (new Logger("fc_rollovers/" + payload)).close();
         }else{
            last_fc = tmpFC;
         }
      }

      //if there was a rollover, flag the data
      if(fc_rollover){
         gps_q[rec_num_mod4] |= Constants.FC_ROLL;
         pps_q[rec_num_1Hz] |= Constants.FC_ROLL;
         hkpg_q[rec_num_mod40] |= Constants.FC_ROLL;
         rcnt_q[rec_num_mod4] |= Constants.FC_ROLL;
         mspc_q[rec_num_mod4] |= Constants.FC_ROLL;
         sspc_q[rec_num_mod32] |= Constants.FC_ROLL;
         for(int lc_i = 0; lc_i < 20; lc_i++){
            fspc_q[rec_num_1Hz + lc_i] |= Constants.FC_ROLL;
         }
         for(int mag_i = 0; mag_i < 4; mag_i++){
            magn_q[rec_num_1Hz + mag_i] |= Constants.FC_ROLL;
         }
      }
      //get multiplex info
      this.mod4 = (int)fc % 4;
      this.mod32 = (int)fc % 32;
      this.mod40 = (int)fc % 40;

   }

   public void setPayloadId(final short payID){
      this.payId = payId;
   }

   public void setPulsePerSecond(final short pps){
      this.pps = pps;
      if(
         (pps[rec_num_1Hz] < Constants.PPS_MIN) ||
         (pps[rec_num_1Hz] > Constants.PPS_MAX)
      ){
         //make sure the value is not out of range because of an early pps
         if(pps[rec_num_1Hz] != 65535){
            pps[rec_num_1Hz] = Constants.PPS_FILL;
            pps_q[rec_num_1Hz] |= Constants.OUT_OF_RANGE;
         }
      }

      //flag potentially bad gps and pps records

      if(
         mod4 > 0 && // make sure there is a previous record to compare to
         pps[rec_num_1Hz] == 65535 && //possible bad pps
         ms_of_week[rec_num_mod4] == ms_of_week[rec_num_mod4 - 1] //gps repeat
      ){
         pps_q[rec_num_1Hz] |= Constants.NO_GPS;
         gps_q[rec_num_mod4] |= Constants.NO_GPS;
      }
   }

   public void setDPUVersion(final short ver){
      this.ver = ver;
   }

   public void setNumSats(final short sats){
      this.sats = sats;
   }

   public void setUTCOffset(final short offset){
      this.offset = offset;
   }

   public void setTermStatus(final short termStat){
      this.termStat = termStat;
   }

   public void setModemCount(final short modemCnt){
      this.modemCnt = modemCnt;
   }

   public void setDcdCount(final short dcdCnt){
      this.dcdCnt = dcdCnt;
   }

   public void setWeek(final int week){
      this.week = week;
   }

   public void setCommandCounter(final int cmdCnt){
      this.cmdCnt = cmdCnt;
   }

   public void setFSPC(final int sample, final BigIntege raw){
      int channels = FSPC.getChannels(this.version);
      int[] fspc = new int[channels.length]
      int value;

      for(int ch_i = 0; ch_i < channels.length; ch_i++){
          value = 
             raw.shiftRight(channels[ch_i].start)
             .and(BigInteger.valueOf(channels[ch_i].width)).intValue();
         if(
            (value < Constants.FSPC_RAW_MIN) ||
            (value > Constants.FSPC_RAW_MAX)
         ){
            fspc[ch_i] = Constants.FSPC_RAW_FILL;
            fspc_q[ch_i] |= Constants.OUT_OF_RANGE;
         } else {
            fspc[ch_i] = value;
         }
      }
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

   public void setMSPC(final int chan_i, final int mspc){
      this.mspc[chan_i] = mspc;
      if(
         (this.mspc[chan_i] < Constants.MSPC_RAW_MIN) ||
         (this.mspc[chan_i] > Constants.MSPC_RAW_MAX)
      ){
         this.mspc[chan_i] = Constants.MSPC_RAW_FILL;
         this.mspc_q |= Constants.OUT_OF_RANGE;
      }
   }

   public void setSSPC(final int chan_i, final int sspc){
      this.sspc[chan_i] = sspc;
         if(
            (sspc[chan_i] < Constants.SSPC_RAW_MIN) ||
            (sspc[chan_i] > Constants.SSPC_RAW_MAX)
         ){
            sspc[chan_i] = Constants.SSPC_RAW_FILL;
            sspc_q |= Constants.OUT_OF_RANGE;
         }
   }

   public void setEpoch(final long epoch){
      this.epoch = epoch;
   }

   public void setHousekeeping(final long hkpg){
      if(
         (this.hkpg < Constants.HKPG_MIN) ||
         (this.hkpg > Constants.HKPG_MAX)
      ){
         this.hkpg = Constants.HKPG_FILL;
         this.hkpg_q |= Constants.OUT_OF_RANGE;

         return;
      } else {
         this.hkpg = hkpg;
      }

      switch(this.mod40){
         case 36:
            this.sats = (short)(this.hkpg >> 8);
            this.offset = (short)(this.hkpg & 255);

            if(
               (this.sats < Constants.SATS_MIN) ||
               (this.sats > Constants.SATS_MAX)
            ){
               this.sats = Constants.SATS_FILL;
               this.hkpg_q |= Constants.OUT_OF_RANGE;
            }

            if(
               (this.offset < Constants.LEAP_SEC_MIN) ||
               (this.offset > Constants.LEAP_SEC_MAX)
            ){
               this.offset = Constants.LEAP_SEC_FILL;
               this.hkpg_q |= Constants.OUT_OF_RANGE;
            }

            break;

         case 37:
            this.weeks = (int)this.hkpg;
            if(
               (this.weeks < Constants.WEEKS_MIN) ||
               (this.weeks > Constants.WEEKS_MAX)
            ){
               this.weeks = Constants.WEEKS_FILL;
               this.hkpg_q |= Constants.OUT_OF_RANGE;
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
               this.hkpg_q |= Constants.OUT_OF_RANGE;
            }
            if(
               (this.cmdCnt < Constants.CMD_CNT_MIN) ||
               (this.cmdCnt > Constants.CMD_CNT_MAX)
            ){
               this.cmdCnt = Constants.CMD_CNT_FILL;
               this.hkpg_q |= Constants.OUT_OF_RANGE;
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
               this.hkpg_q |= Constants.OUT_OF_RANGE;
            }
            if(
               (this.modemCnt < Constants.MODEM_CNT_MIN) ||
               (this.modemCnt > Constants.MODEM_CNT_MAX)
            ){
               this.modemCnt = Constants.MODEM_CNT_FILL;
               this.hkpg_q |= Constants.OUT_OF_RANGE;
            }
            break;
         default:
            break;
      }

      return this.hkpg == 0 ? true : false;
   }

   public void setRateCounter(final long rcnt){
      this.rcnt = rcnt;
   }

   public void setGPS(final int gps){
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


      //get gps info: 32 bits of mod4 gps data followed by 16 bits of pps data

      switch(mod4){
         case Constants.ALT_I: 
            if(
               (gps[mod4][rec_num_mod4] < Constants.ALT_RAW_MIN) ||
               (gps[mod4][rec_num_mod4] > Constants.ALT_RAW_MAX)
            ){
               gps[mod4][rec_num_mod4] = Constants.ALT_RAW_FILL;
               gps_q[rec_num_mod4] |= Constants.OUT_OF_RANGE;  
            }
            else if(gps[mod4][rec_num_mod4] < Constants.MIN_SCI_ALT){
               gps_q[rec_num_mod4] |= Constants.LOW_ALT;
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
               }
            }
            break;
         case Constants.TIME_I: 
            if(
               (gps[mod4][rec_num_mod4] < Constants.MS_WEEK_MIN) ||
               (gps[mod4][rec_num_mod4] > Constants.MS_WEEK_MAX)
            ){
               gps[mod4][rec_num_mod4] = Constants.MS_WEEK_FILL;
               gps_q[rec_num_mod4] |= Constants.OUT_OF_RANGE;  
            }
*/
   }

   public void setMagnetometer(final int axis, final int sample, final int mag){
      if((mag < Constants.MAG_MIN) || (mag > Constants.MAG_MAX)){
         this.mag[axis][sample] = Constants.MAG_FILL;
         magn_q[rec_num_4Hz] |= Constants.OUT_OF_RANGE;
         return;
      }

      this.mag[axis][sample] = mag;
   }

   public void setPeak511Line(final float peak511_bin){
      this.peak511_bin;
   }

   public int getFrameCounter(){
      return this.fc;
   }

   public short getPayloadId(){
      return this.payId;
   }

   public short getPulsePerSecond(){
      return this.pps;
   }

   public short getDPUVersion(){
      return this.ver;
   }

   public short getNumSats(){
      return this.sats;
   }

   public short getUTCOffget(){
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

   public int getMSPC(){
      return this.mspc;
   }

   public int getSSPC(){
      return this.sspc;
   }

   public long getEpoch(){
      return this.epoch;
   }

   public long getHousekeeping(){
      return this.hkpg;
   }

   public long getRateCounter(){
      return this.rcnt;
   }

   public int getGPS(){
      return this.gps;
   }

   public int[] getMagnetometer(){
      return this.mag;
   }

   public float getPeak511Line(){
      return this.peak511_bin;
   }
