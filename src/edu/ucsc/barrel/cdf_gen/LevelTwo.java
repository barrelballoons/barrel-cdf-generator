/*
LevelTwo.java

Description:
   LevelTwo.java pulls data from the DataHolder.java object and processes it 
   into physical units (when needed) and outputs CDF files in the L2 directory.

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

import gsfc.nssdc.cdf.CDFException;
import gsfc.nssdc.cdf.CDFConstants;
import gsfc.nssdc.cdf.util.CDFTT2000;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Calendar;
import java.util.Vector;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

public class LevelTwo extends CDFWriter{
   private BarrelFrame[] frames;
   private int date, dpu_ver;
   public LevelTwo(
      final BarrelFrame[] frames, final int date,
      final String d, final String p, final String f, 
      final String s, final String dir, final ver String
   ) throws IOException
   {
      super(d, p, f, s, dir, "Level Two");
      this.frames = frames;
      this.numFrames = this.frames.length;
      this.date = date;
      this.dpu_ver = Integer.valueOf(ver);
   }
   
   //Convert the EPHM data and save it to CDF files
   public void doGpsCdf() throws CDFException{
      Calendar cal;
      Logger geo_coord_file =
         new Logger("pay" + this.id + "_" + this.date + "_gps.txt");
      int
         year, month, day, day_of_year, hour, min, sec, intVal, mod4, fc, fg,
         numRecords  = (int)Math.ceil(this.numFrames / 4.0);
      double
         sec_of_day  = 0;
      float
         east_lon    = 0;
      String[] 
         mag_coords;
      float[]
         lat         = new float[numRecords],
         lon         = new float[numRecords],
         alt         = new float[numRecords],
         mlt2        = new float[numRecords],
         mlt6        = new float[numRecords],
         l2          = new float[numRecords],
         l6          = new float[numRecords];
      int[]
         frameGroup  = new int[numRecords],
         q           = new int[numRecords];
      long[]
         epoch_parts = new long[9],
         gps_time    = new long[numRecords],
         epoch       = new long[numRecords];
      Map<Integer, Boolean> 
         complete_gps= new HashMap<Integer, Boolean>(numRecords);

      //initialize the data arrays with fill value
      Arrays.fill(frameGroup, Ephm.FC_FILL);
      Arrays.fill(epoch,      Ephm.EPOCH_FILL);
      Arrays.fill(q,          Ephm.QUALITY_FILL);
      //Arrays.fill(gps_time,   Ephm.GPSTIME_FILL);
      Arrays.fill(lat,        Ephm.LAT_FILL);
      Arrays.fill(lon,        Ephm.LON_FILL);
      Arrays.fill(alt,        Ephm.ALT_FILL);
      Arrays.fill(mlt2,       Ephm.MLT2_FILL);
      Arrays.fill(mlt6,       Ephm.MLT6_FILL);
      Arrays.fill(l2,         Ephm.L2_FILL);
      Arrays.fill(l6,         Ephm.L6_FILL);

      System.out.println("\nSaving EPHM Level Two CDF...");

      //calculate the day of year
      year = this.date / 10000;
      month = (this.date - (year * 10000)) / 100;
      day = this.date - (year * 10000) - (month * 100);
      cal = Calendar.getInstance();
      cal.set(Calendar.YEAR, year + 2000);
      cal.set(Calendar.MONTH, month - 1);
      cal.set(Calendar.DAY_OF_MONTH, day);
      day_of_year = cal.get(Calendar.DAY_OF_YEAR);
      cal = null;

      //convert lat, lon, and alt values and select values for this date
      for(int frame_i = 0, rec_i = -1; frame_i < this.numFrames; frame_i++){

         //make sure we have a valid frame counter
         fc = this.frames[frame_i].getFrameCounter();
         if(fc == null || fc == BarrelFrame.INT4_FILL){
            continue;
         }

         //calculate the subcom and frameGroup
         mod4 = this.frames[frame_i].mod4;
         fg = fc - mod4;

         //figure out if we are still in the same record
         if (frameGroup[rec_i] != fg) {
            rec_i++;
            frameGroup[rec_i] = fg;
         }
         
         //get the epoch the the frameGroup frame
         epoch[rec_i] = CDF_Gen.barrel_time.getEpoch(frameGroup[rec_i]);

         switch(this.frames[frame_i].mod4) {
            //convert mm to km
            case Ephm.ALT_I:
               intVal = this.frames[frame_i].getGps();
               alt[rec_i] = intVal != Ephm.RAW_ALT_FILL ?
                  intVal / 1000000 :
                  Ephm.ALT_FILL;
            break;

            //convert lat and lon to physical units
            case Ephm.LAT_I:
               intVal = this.frames[frame_i].getGps();
               lat[rec_i] = intVal != Ephm.RAW_LAT_FILL ? 
                  (
                     intVal * 
                     Float.intBitsToFloat(
                        Integer.valueOf("33B40000", 16).intValue()
                     )
                  ) :
                  Ephm.LAT_FILL;
            break;
            case Ephm.LON_I:
               intVal = this.frames[frame_i].getGps();
               lon[rec_i] = intVal != Ephm.RAW_LON_FILL ? 
                  (
                     intVal * 
                     Float.intBitsToFloat(
                        Integer.valueOf("33B40000", 16).intValue()
                     )
                  ) :
                  Ephm.LON_FILL;
            break;
            case Ephm.TIME_I:
               intVal = this.frames[frame_i].getGps();
               //calculate the GPS time
               if(intVal != Ephm.MS_OF_WEEK_FILL){
                  sec = intVal / 1000; //convert ms to sec
                  sec %= 86400; //remove any complete days
                  hour = sec / 3600;
                  sec %= 3600;
                  min = sec / 60;
                  sec %= 60;
                  gps_time[rec_i] = CDFTT2000.compute(
                     (long)(year + 2000), (long)(month - 1), (long)day, (long)hour, 
                     (long)min, (long)sec, 0L, 0L, 0L
                  );  
               }
            break;
         }

         //keep track of which frames have complete GPS values
         complete_gps.put(
            frameGroup[rec_i], 
            (
               (alt[rec_i] != Ephm.ALT_FILL) && 
               (lat[rec_i] != Ephm.LAT_FILL) && 
               (lon[rec_i] != Ephm.LON_FILL)
            )
         );

         //calculate the current time in seconds of day
         epoch_parts = CDFTT2000.breakdown(epoch[rec_i]);
         sec_of_day = 
            (epoch_parts[3] * 3600) + // hours
            (epoch_parts[4] * 60) + //minutes
            epoch_parts[5] + //seconds
            (epoch_parts[6] * 0.001) + //ms
            (epoch_parts[7] * 0.000001) + //us
            (epoch_parts[8] * 0.000000001); //ns
         //convert signed longitude to east longitude
         east_lon = (lon[rec_i] > 0) ? lon[rec_i] : lon[rec_i] + 360;

         geo_coord_file.writeln(
            String.format(
               "%07d %02.6f %03.6f %03.6f %04d %03d %02.3f", 
               frameGroup[rec_i], alt[rec_i], lat[rec_i], lon[rec_i],
               (year + 2000), day_of_year, sec_of_day
            )
         );
      }
      geo_coord_file.close();

      //get the magnetic field info for this location
      try{
         String command = 
            CDF_Gen.getSetting("mag_gen") + " " + 
            "pay" + id + "_" + date + "_gps.txt";

         Process p = Runtime.getRuntime().exec(command);
         BufferedReader input =
            new BufferedReader(new InputStreamReader(p.getInputStream()));
         System.out.println(input.readLine()); 
      }catch(IOException ex){
         System.out.println("Could not read gps coordinate file:");
         System.out.println(ex.getMessage());
      }
      
      //Read the magnetic coordinates into a set of arrays
      try{
         BufferedReader mag_coord_file = 
            new BufferedReader(
               new FileReader(
                  "pay" + id + "_" + date + "_gps_out.txt"
               )
            );
         String line;
         int 
            rec_i = 0,
            this_frame = 0,
            last_frame = 0;
         
         while((line = mag_coord_file.readLine()) != null){
            line = line.trim();
            mag_coords = line.split("\\s+");

            //check for repeated frame
            this_frame = Integer.parseInt(mag_coords[0]);
            if(
               (this_frame != last_frame) && 
               (complete_gps.get(this_frame) == true)
            ){
               //make sure the mag coordinates were calculated correctly
               if(mag_coords[8].indexOf("*") == -1){
                  l2[rec_i] = Math.abs(Float.parseFloat(mag_coords[8]));
               }else{
                  l2[rec_i] = 9999;
               }
               if(mag_coords[9].indexOf("*") == -1){
                  mlt2[rec_i] = Float.parseFloat(mag_coords[9]);
               }else{
                  mlt2[rec_i] = 9999;
               }
               if(mag_coords[11].indexOf("*") == -1){
                  l6[rec_i] = Math.abs(Float.parseFloat(mag_coords[11]));
               }else{
                  l6[rec_i] = 9999;
               }
               if(mag_coords[12].indexOf("*") == -1){
                  mlt6[rec_i] = Float.parseFloat(mag_coords[12]);
               }else{
                  mlt6[rec_i] = 9999;
               }
            }

            last_frame = this_frame;
            rec_i++;
         }

         mag_coord_file.close();

         //clean up after ourselves
         geo_coord_file.delete();
         (new File("pay" + id + "_" + date + "_gps_out.txt")).delete();

      }catch(IOException ex){
         System.out.println("Could not read magnetic coordinate file:");
         System.out.println(ex.getMessage());
      }

      //make sure there is a CDF file to open
      //(copyFile will not clobber an existing file)
      String destName = 
         outputPath + "/" + date + "/" + "bar_" + id + 
         "_l2_" + "ephm" + "_20" + date +  "_v" + revNum + ".cdf";
     
      Ephm ephm = new Ephm(destName, "bar_" + id, date, 2);

      System.out.println("GPS_Alt");
      ephm.getCDF().addData("GPS_Alt", alt);
      System.out.println("GPS_Lon");
      ephm.getCDF().addData("GPS_Lon", lon);
      System.out.println("GPS_Lat");
      ephm.getCDF().addData("GPS_Lat", lat);
      System.out.println("MLT_Kp2");
      ephm.getCDF().addData("MLT_Kp2", mlt2);
      System.out.println("MLT_Kp6");
      ephm.getCDF().addData("MLT_Kp6", mlt6);
      System.out.println("L_Kp2");
      ephm.getCDF().addData("L_Kp2", l2);
      System.out.println("L_Kp6");
      ephm.getCDF().addData("L_Kp6", l6);
      System.out.println("FrameGroup");
      ephm.getCDF().addData("FrameGroup", frameGroup);
      System.out.println("Epoch");
      ephm.getCDF().addData("Epoch", epoch);
      System.out.println("Q");
      ephm.getCDF().addData("Q", q);

      ephm.close();
   }
   
   //write the misc file, no processing needed
   public void doMiscCdf() throws CDFException{
      short[] 
         version    = new short[this.numFrames],
         payID      = new short[this.numFrames],
         pps_vals   = new short[this.numFrames];
      int[] 
         frameGroup = new int[this.numFrames],
         q          = new int[this.numFrames];
      long[] 
         epoch      = new long[this.numFrames];

      Arrays.fill(frameGroup, Misc.FC_FILL);
      Arrays.fill(epoch,      Misc.EPOCH_FILL);
      Arrays.fill(q,          Misc.QUALITY_FILL);
      Arrays.fill(version,    Misc.VERSION_FILL);
      Arrays.fill(payID,      Misc.PAYLOADID_FILL);
      Arrays.fill(pps_vals,   Misc.PPS_FILL);

      System.out.println("\nSaving MISC Level Two CDF...");

      for(int frame_i = 0; frame_i < this.numFrames; frame_i++){
         pps_vals[frame_i]   = this.frames.getPPS();
         version[frame_i]    = this.frames.getDPUVersion();
         payID[frame_i]      = this.frames.getPayloadID();
         frameGroup[frame_i] = this.frames.getFrameCounter();
         epoch[frame_i] = CDF_Gen.barrel_time.getEpoch(frameGroup[frame_i]);
      }

      String destName = 
         outputPath  + "/" + date + "/" + "bar_" + id + 
         "_l2_" + "misc" + "_20" + date +  "_v" + revNum + ".cdf";
     
      Misc misc = new Misc(destName, "bar_" + id, date, 2);

      System.out.println("GPS_PPS");
      misc.getCDF().addData("GPS_PPS", pps_vals);
      System.out.println("Version");
      misc.getCDF().addData("Version", version);
      System.out.println("Payload_ID");
      misc.getCDF().addData("Payload_ID", payID);
      System.out.println("FrameGroup");
      misc.getCDF().addData("FrameGroup", frameGroup);
      System.out.println("Epoch");
      misc.getCDF().addData("Epoch", epoch);
      System.out.println("Q");
      misc.getCDF().addData("Q", q);

      misc.close();
   }
   
   public void doMagCdf() throws CDFException{
      boolean 
         found_fill = false;
      int
         rec_i, sample, offset,
         numRecords = this.numFrames * 4;
      int[]
         frameGroup = new int[numRecords],
         q          = new int[numRecords];
      int[][]
         raw_mag;
      long
         base_epoch;
      long[] 
         epoch      = new long[numRecords];
      float[] 
         magx       = new float[numRecords],
         magy       = new float[numRecords],
         magz       = new float[numRecords],
         magTot     = new float[numRecords];

      Arrays.fill(frameGroup, Magn.FC_FILL);
      Arrays.fill(epoch,      Magn.EPOCH_FILL);
      Arrays.fill(q,          Magn.QUALITY_FILL);
      Arrays.fill(magx,       Magn.MAG_FILL);
      Arrays.fill(magy,       Magn.MAG_FILL);
      Arrays.fill(magz,       Magn.MAG_FILL);
      Arrays.fill(magTot,     Magn.MAG_FILL);

      System.out.println("\nSaving Magnetometer Level Two CDF...");

      //extract the nominal magnetometer value and calculate |B| for each frame
      for (int frame_i = 0; frame_i < numFrames; frame_i++) {
         raw_mag = this.frames[frame_i].getMag();

         base_epoch = CDF_Gen.barrel_time.getEpoch(frameGroup[rec_i]);

         //each frame has 4 samples
         for (int sample_i = 0; sample_i < 4; sample_i++) {
            rec_i = sample_i + (frame_i * 4);
            offset = sample_i * 250000000;
            sample = raw_mag[sample_i][Magn.X_AXIS];
            if(sample != Magn.RAW_MAG_FILL){
               magx[rec_i] = (sample - 8388608.0f) / 83886.070f;
            } else {
               found_fill = true;
            }

            sample = raw_mag[sample_i][Magn.Y_AXIS];
            if(sample != Magn.RAW_MAG_FILL){
               magy[rec_i] = (sample - 8388608.0f) / 83886.070f;
            } else {
               found_fill = true;
            }
            
            sample = raw_mag[sample_i][Magn.Z_AXIS];
            if(sample != Magn.RAW_MAG_FILL){
               magz[rec_i] = (sample - 8388608.0f) / 83886.070f;
            } else {
               found_fill = true;
            }
            
            if(!found_fill){
               magTot[rec_i] = 
                  (float)Math.sqrt(
                     (magx[rec_i] * magx[rec_i]) + 
                     (magy[rec_i] * magy[rec_i]) +
                     (magz[rec_i] * magz[rec_i]) 
                  );
                  found_fill = false;
            }
	         
            frameGroup[rec_i] = this.frames[frame_i].getFrameCounter();
	         epoch[rec_i] = base_epoch + offset;
         }
         
         q[frame_i] = this.frames[frane_i].get();
      }

      //store the nominal mag values
      String destName = 
         outputPath + "/" + date + "/" + "bar_" + id + 
         "_l2_" + "magn" + "_20" + date +  "_v" + revNum + ".cdf";
     
      Magn magn = new Magn(destName, "bar_" + id, date, 2);
      System.out.println("MAG_X");
      magn.getCDF().addData("MAG_X", magx);
      System.out.println("MAG_Y...");
      magn.getCDF().addData("MAG_Y", magy);
      System.out.println("MAG_Z...");
      magn.getCDF().addData("MAG_Z", magz);
      System.out.println("Total...");
      magn.getCDF().addData("Total", magTot);
      System.out.println("FrameGroup...");
      magn.getCDF().addData("FrameGroup", frameGroup);
      System.out.println("Epoch...");
      magn.getCDF().addData("Epoch", epoch);
      System.out.println("Q...");
      magn.getCDF().addData("Q", q);

      magn.close();
   }
   
   public void doHkpgCdf() throws CDFException{
      int
         fc, fg, hkpg_raw,
         numRecords  = (int) Math.ceil(this.numFrames / 40.0);
      short []
         sats        = new short[numRecords],
         offset      = new short[numRecords],
         termStat    = new short[numRecords],
         modemCnt    = new short[numRecords],
         dcdCnt      = new short[numRecords];
      int[] 
         frameGroup  = new int[numRecords],
         q           = new int[numRecords],
         cmdCnt      = new int[numRecords],
         weeks       = new int[numRecords];
      long[]
         epoch       = new long[numRecords];
      float [][]
         hkpg_scaled = new float[40][numRecords];

      Arrays.fill(frameGroup, HKPG.FC_FILL);
      Arrays.fill(epoch,      HKPG.EPOCH_FILL);
      Arrays.fill(q,          HKPG.QUALITY_FILL);
      Arrays.fill(weeks,      HKPG.WEEKS_FILL);
      Arrays.fill(cmdCnt,     HKPG.CMDCNT_FILL);
      Arrays.fill(dcdCnt,     HKPG.DCDCNT_FILL);
      Arrays.fill(sats,       HKPG.SATS_FILL);
      Arrays.fill(offset,     HKPG.OFFSET_FILL);
      Arrays.fill(termStat,   HKPG.TERMSTAT_FILL);
      Arrays.fill(modemCnt,   HKPG.MODEMCNT_FILL);

      for(int var_i = 0; var_i < 36; var_i++){
         Arrays.fill(hkpg_scaled[var_i], HKPG.SENSOR_FILL);
      }
      
      System.out.println("\nSaving HKPG...");

      String destName = 
         outputPath + "/" + date + "/" + "bar_" + id + 
         "_l2_" + "hkpg" + "_20" + date +  "_v" + revNum + ".cdf";

      HKPG hkpg = new HKPG(destName, "bar_" + id, date, 2);

      for(int frame_i = 0, rec_i = -1; frame_i < this.numFrames; frame_i++){

         fc = this.frames[frame_i].getFrameCounter();
         if(fc == null || fc == HKPG.FC_FILL){
            continue;
         }

         mod40 = this.frames[frame_i].mod40;
         
         if (frameGroup[rec_i] != fg) {
            rec_i++;
            frameGroup[rec_i] = fg;
         }

         //make sure there is a valid housekeeping value to process
         hkpg_raw = this.frames[frame_i].getHousekeeping();
         if(hkpg_raw == HKPGHKPG.RAW_SENSOR_FILL){continue;}

         //convert the housekeeping data to physical units
         if(this.dpu_ver > 3) {
            //for versions 3 and up the T9 and T11 sensors were used for
            //mag statistics rather than solar panel temps
            switch (mod40) {
               case HKPGHKPGHKPG.T9:
                  hkpg_scaled[mod40][frame_i] = 
                     ((hkpg_raw - 0x8000) * 0.09094f) - 273.15f;
               break;
               case HKPGHKPGHKPGHKPG.T11:
                  hkpg_scaled[mod40][frame_i] =  hkpg_raw * 0.0003576f;
               break;
               default:
                  hkpg_scaled[mod40][frame_i] = 
                     (hkpk_raw * HKPG.SCALE_FACTORS.get(mod40)) +
                     HKPG.OFFSETS.get(mod40);
               break;
            }
         } else {
            hkpg_scaled[mod40][frame_i] = 
               (hkpk_raw * HKPG.SCALE_FACTORS.get(mod40)) +
               HKPG.OFFSETS.get(mod40);
         }

         sats[rec_i]     = this.frames[frame_i].getNumSats();
         offset[rec_i]   = this.frames[frame_i].getUTCOffset();
         termStat[rec_i] = this.frames[frame_i].getTermStat();
         modemCnt[rec_i] = this.frames[frame_i].getModemCount();
         dcdCnt[rec_i]   = this.frames[frame_i].getDcdCount();
         cmdCnt[rec_i]   = this.frames[frame_i].getCommandCounter();
         weeks[rec_i]    = this.frames[frame_i].getWeeks();
         epoch[frame_i]  = CDF_Gen.barrel_time.getEpoch(frameGroup[frame_i]);
      }

      for(int var_i = 0; var_i < 36; var_i++){
         String label = HKPG.LABELS.get(var_i);
         System.out.println(label + "...");
         hkpg.getCDF().addData(label, hkpg_scaled[var_i]);
      }

      System.out.println("numOfSats...");
      hkpg.getCDF().addData("numOfSats", sats);
      System.out.println("timeOffset...");
      hkpg.getCDF().addData("timeOffset", offset);
      System.out.println("termStatus...");
      hkpg.getCDF().addData("termStatus", termStat);
      System.out.println("cmdCounter...");
      hkpg.getCDF().addData("cmdCounter", cmdCnt);
      System.out.println("modemCounter...");
      hkpg.getCDF().addData("modemCounter", modemCnt);
      System.out.println("dcdCounter...");
      hkpg.getCDF().addData("dcdCounter", dcdCnt);
      System.out.println("weeks...");
      hkpg.getCDF().addData("weeks", weeks);
      System.out.println("FrameGroup...");
      hkpg.getCDF().addData("FrameGroup", frameGroup);
      System.out.println("Epoch...");
      hkpg.getCDF().addData("Epoch", epoch);
      System.out.println("Q...");
      hkpg.getCDF().addData("Q", q);

      hkpg.close();
   }

   public void doFspcCdf() throws CDFException{
      int
         rec_i, offset,
         numRecords = this.frames * 20,
         numCh = FSPC.getChannels(this.dpu_ver).length;
      int[] 
         lc_raw     = new int[numRecords],
         frameGroup = new int[numRecords],
         q          = new int[numRecords];
      int[][] 
         lc_scaled  = new int[numCh][numRecords];
      long
         base_epoch;
      long[]
         epoch      = new long[numRecords];
      float 
         scint_temp = 20f,
         dpu_temp   = 20f,
         peak       = -1f;
      float[] 
         old_edges, 
         std_edges  = SpectrumExtract.stdEdges(0, 2.4414f);
      float[][] 
         chan_edges = new float[numRecords][numCh + 1],
         lc_error   = new float[numCh][numRecords];
      String[]
         chan_names = (numCh == 6 ? FSPC.NEW_LABELS : FSPC.OLD_LABELS);

      Arrays.fill(frameGroup, BarrelFrame.INT4_FILL);
      Arrays.fill(epoch,      BarrelFrame.INT8_FILL);
      Arrays.fill(q,          BarrelFrame.INT4_FILL);
      for (int ch_i = 0; ch_i < numCh; ch_i++) {
         Arrays.fill(lc_scaled[chi_i], BarrelFrame.INT4_FILL);
         Arrays.fill(lc_error[chi_i], BarrelFrame.FLOAT_FILL);
      }

      System.out.println("\nSaving FSPC...");
      
      //convert the light curves counts to cnts/sec and 
      //figure out the channel width :)
      for(int frame_i = 0; frame_i < this.numFrames; frame_i++){

         lc_raw = this.frames[frame_i].getFSPC();

         base_epoch = CDF_Gen.barrel_time.getEpoch(frameGroup[rec_i]);

         //each frame has 20 samples per channel
         for (int ch_i = 0; ch_i < numCh; ch_i++) {
            for (int sample_i = 0; sample_i < 20; sample_i++) {
               rec_i  = sample_i + (frame_i * 20);
               offset = sample_i * 50000000;
               sample = lc_raw[ch_i][sample_i];
               if(sample != BarrelFrame.INT4_FILL){
                  lc_scaled[ch_i][rec_i] = (sample - 8388608.0f) / 83886.070f;
                  lc_error[ch_i][rec_i] =(float)Math.sqrt(sample);
               }

               //get the adjusted bin edges
               chan_edges[rec_i] = SpectrumExtract.createBinEdges(
                  0, this.frames[frame_i].getPeak511()
               );

               frameGroup[rec_i] = this.frames[frame_i].getFrameCounter();
               epoch[rec_i] = base_epoch + offset ;
            }
         }
      }

      String destName = 
         outputPath + "/" + date + "/" + "bar_" + id + 
         "_l2_" + "fspc" + "_20" + date +  "_v" + revNum + ".cdf";

      FSPC fspc = 
         new FSPC(destName, "bar_" + id, date, 2, this.dpu_ver);

      for (int ch_i = 0; ch_i < numCh; ch_i++) {

         System.out.println("FSPC" + chan_names[ch_i]);
         fspc.getCDF().addData("FSPC" + chan_names[ch_i], lc_scaled[ch_i]);
         fspc.getCDF().addData("cnt_error" + chan_names[ch_i], lc_error[ch_i]);
      }
      System.out.println("FSPC_Edges");
      fspc.getCDF().addData("FSPC_Edges", chan_edges);
      System.out.println("FrameGroup");
      fspc.getCDF().addData("FrameGroup", frameGroup);
      System.out.println("Epoch");
      fspc.getCDF().addData("Epoch", epoch);
      System.out.println("Q");
      fspc.getCDF().addData("Q", q);

      fspc.close();
   }

   public void doMspcCdf() throws CDFException{
      float 
         peak       = -1, 
         scint_temp = 0, 
         dpu_temp   = 0;
      int 
         hkpg_frame, start, stop, mod4, fg, frame_i,
         fc         = null,
         offset     = 90,
         numRecords = (int)Math.ceil(this.numFrames / 4);
      int[]
         part_spec,
         raw_spec   = new int[48],
         frameGroup = new int[numRecords],
         q          = new int[numRecords];
      long[]
         epoch      = new long[numRecords];

      float
         width;
      float[]
         old_edges,
         std_edges  = SpectrumExtract.stdEdges(1, 2.4414f);
      float[][]
         rebin = new float[numRecords][48],
         error = new float[numRecords][48];

      //initialize the data arrays with fill value
      Arrays.fill(frameGroup, BarrelFrame.INT4_FILL);
      Arrays.fill(epoch,      BarrelFrame.INT8_FILL);
      Arrays.fill(q,          BarrelFrame.INT4_FILL);
      Arrays.fill(raw_spec,   BarrelFrame.FLOAT_FILL);
      Arrays.fill(rebin, raw_spec);
      Arrays.fill(error, raw_spec);

      System.out.println("\nSaving MSPC...");

      //get the first valid frame counter
      frame_i = 0;
      while (frame_i < this.frames.length) {
         fc = this.frames[frame_i].getFrameCounter();
         if(fc != null && fc != BarrelFrame.INT4_FILL){
            break;
         }
         frame_i++;
      }

      //caluclate the first frame group
      mod4 = this.frames[frame_i].mod4;
      frameGroup[0] = fc - mod4; 

      //rebin the mspc spectra
      for(frame_i = 0, rec_i = 0; frame_i < this.numFrames; frame_i++){
          
         fc = this.frames[frame_i].getFrameCounter();
         if(fc == null || fc == BarrelFrame.INT4_FILL){
            continue;
         }

         mod4 = this.frames[frame_i].mod4;
         fg = fc - mod4;

         //check if we are still in the same frame group 
         //(meaning the same spectrum)
         if (frameGroup[rec_i] != fg) {
            //save the epoch and frameGroup of this spectrum
            epoch[rec_i] = CDF_Gen.barrel_time.getEpoch(frameGroup[rec_i]);
            

            //get the most recent scintillator temperature value
            scint_temp = getTemp(frame_i, HKPG.T0);
            dpu_temp = getTemp(frame_i, HKPG.T5);

            //get the adjusted bin edges
            old_edges = 
               SpectrumExtract.makeedges(
                  1, scint_temp, dpu_temp, 
                  CDF_Gen.spectra.getPeakLocation(frameGroup[rec_i])
               );

            //rebin the spectrum
            rebin[rec_i] = 
               SpectrumExtract.rebin(raw_spec, old_edges, std_edges);

            //scale the counts and calculate error
            for(int bin_i = 0; bin_i < 48; bin_i++){
               if(rebin[rec_i][bin_i] != BarrelFrame.FLOAT_FILL){
                  width = std_edges[bin_i + 1] - std_edges[bin_i];

                  //divide counts by bin width and adjust the time scale
                  rebin[rec_i][bin_i] /= (width * 4f);
                  //get the count error
                  error[rec_i][bin_i] = 
                     (float)Math.sqrt(rebin[rec_i][bin_i]) / (width * 4f);
               }
            }

            //clear the raw spectrum
            Arrays.fill(raw_spec, BarrelFrame.FLOAT_FILL);

            //update the record number and frameGroup
            frameGroup[rec_i] = fg;
            rec_i++;
         }

         //fill part of the raw spectrum
         start = mod4 * 4;
         stop = start + 4;
         part_spec = this.frames[frame_i].getMspc();
         for(
            int spec_i = start, sample_i = 0;
            sample_i < stop;
            sample_i++, spec_i++
         ) {
            raw_spec[spec_i] = part_spec[sample_i];
         }
      }

      String destName = 
         outputPath + "/" + date + "/" + "bar_" + id +
         "_l2_" + "mspc" + "_20" + date +  "_v" + revNum + ".cdf";

      MSPC mspc = new MSPC(destName, "bar_" + id, date, 2);
      System.out.println("mspc");
      mspc.getCDF().addData("MSPC", mspc_rebin);
      System.out.println("mspc error");
      mspc.getCDF().addData("cnt_error", mspc_error);
      System.out.println("FrameGroup");
      mspc.getCDF().addData("FrameGroup", frameGroup);
      System.out.println("Epoch");
      mspc.getCDF().addData("Epoch", epoch);
      System.out.println("Q");
      mspc.getCDF().addData("Q", q);

      mspc.close();
   }

   public void doSspcCdf() throws CDFException{
      float
         scint_temp = 0, 
         dpu_temp   = 0;
      int 
         hkpg_frame, start, stop, mod32, fg, frame_i,
         fc         = null,
         offset     = 90,
         numRecords = (int)Math.ceil(this.numFrames / 32);
      int[]
         part_spec,
         raw_spec   = new int[256],
         frameGroup = new int[numRecords],
         q          = new int[numRecords];
      long[]
         epoch      = new long[numRecords];
      float
         width;
      float[]
         old_edges,
         std_edges  = SpectrumExtract.stdEdges(2, 2.4414f);
         peak       = new float[numRecords];
      float[][]
         rebin = new float[numRecords][256],
         error = new float[numRecords][256];

      //initialize the data arrays with fill value
      Arrays.fill(frameGroup, BarrelFrame.INT4_FILL);
      Arrays.fill(epoch,      BarrelFrame.INT8_FILL);
      Arrays.fill(q,          BarrelFrame.INT4_FILL);
      Arrays.fill(peak,       BarrelFrame.FLOAT_FILL);
      Arrays.fill(raw_spec,   BarrelFrame.INT4_FILL);
      for (int spec_i = 0; spec_i < 256; spec_i++){
         Arrays.fill(error[spec_i], BarrelFrame.FLOAT_FILL);
         Arrays.fill(rebin[spec_i], BarrelFrame.FLOAT_FILL);
      }
      

      //get the first valid frame counter
      frame_i = 0;
      while (frame_i < this.frames.length) {
         fc = this.frames[frame_i].getFrameCounter();
         if(fc != null && fc != BarrelFrame.INT4_FILL){
            break;
         }
         frame_i++;
      }

      //caluclate the first frame group
      mod32 = this.frames[frame_i].mod32;
      frameGroup[0] = fc - mod32; 

      System.out.println("\nSaving SSPC...");

      //rebin the sspc spectra
      for (frame_i = 0, rec_i = 0; frame_i < this.numFrames; frame_i++) {
         fc = this.frames[frame_i].getFrameCounter();
         if(fc == null || fc == BarrelFrame.INT4_FILL){
            continue;
         }

         mod32 = this.frames[frame_i].mod32;
         fg = fc - mod32;

         //check if we are still in the same frame group 
         //(meaning the same spectrum)
         if (frameGroup[rec_i] != fg) {
            epoch[rec_i] = CDF_Gen.barrel_time.getEpoch(frameGroup[rec_i]);
            peak[rec_i] = CDF_Gen.spectra.getPeakLocation(frameGroup[rec_i]);

            //get the most recent scintillator temperature value
            scint_temp = getTemp(frame_i, HKPG.T0);
            dpu_temp = getTemp(frame_i, HKPG.T5);

            //get the adjusted bin edges
            old_edges = 
               ExtractSpectrum.makeedges(2, scint_temp, dpu_temp, peak[rec_i]);

            //rebin the spectrum
            rebin[rec_i] = 
               ExtractSpectrum.rebin(raw_spec, old_edges, std_edges);

            //scale the counts and calculate error
            for(int bin_i = 0; bin_i < 256; bin_i++){
               if(rebin[rec_i][bin_i] != BarrelFrame.FLOAT_FILL){
                  width = std_edges[bin_i + 1] - std_edges[bin_i];

                  //divide counts by bin width and adjust the time scale
                  rebin[rec_i][bin_i] /= (width * 32f);
                  //get the count error
                  error[rec_i][bin_i] = 
                     (float)Math.sqrt(rebin[rec_i][bin_i]) / (width * 32f);
               }
            }

            //clear the raw spectrum
            Arrays.fill(raw_spec, BarrelFrame.INT4_FILL);

            //update the record number and frameGroup
            frameGroup[rec_i] = fg;
            rec_i++;            
         }

         //fill part of the raw spectrum
         start = mod32 * 32;
         stop = start + 32;
         part_spec = this.frames[frame_i].getSspc();
         for(
            int spec_i = start, sample_i = 0;
            sample_i < stop;
            sample_i++, spec_i++
         ) {
            raw_spec[spec_i] = part_spec[sample_i];
         }
      }

      String destName =
         outputPath + "/" + date + "/" + "bar_" + id + 
         "_l2_" + "sspc" + "_20" + date +  "_v" + revNum + ".cdf";

      SSPC sspc = new SSPC(destName, "bar_" + id, date, 2);

      System.out.println("sspc");
      sspc.getCDF().addData("SSPC", sspc_rebin);
      System.out.println("sspc error");
      sspc.getCDF().addData("cnt_error", sspc_error);
      System.out.println("Peak_511");
      sspc.getCDF().addData("Peak_511", peak);
      System.out.println("FrameGroup");
      sspc.getCDF().addData("FrameGroup", frameGroup);
      System.out.println("Epoch");
      sspc.getCDF().addData("Epoch", epoch);
      System.out.println("Q");
      sspc.getCDF().addData("Q", q);

      sspc.close();
   }

   public void doRcntCdf() throws CDFException{
      int
         numRecords = (int)Math.ceil(this.numFrames / 4),
         raw;
      int[]
         frameGroup = new int[numRecords],
         q = new int[numRecords];
      float[][] rc = new float[4][numRecords];
      
      long[] epoch = new long[numRecords];
      
      //initialize the data arrays with fill value
      Arrays.fill(frameGroup,       BarrelFrame.INT4_FILL);
      Arrays.fill(epoch,            BarrelFrame.INT8_FILL);
      Arrays.fill(q,                BarrelFrame.INT4_FILL);
      Arrays.fill(rc[0], BarrelFrame.FLOAT_FILL);
      Arrays.fill(rc[1], BarrelFrame.FLOAT_FILL);
      Arrays.fill(rc[2], BarrelFrame.FLOAT_FILL);
      Arrays.fill(rc[3], BarrelFrame.FLOAT_FILL);
      
      //change all the units from cnts/4sec to cnts/sec
      for(int frame_i = 0, rec_i = -1; frame_i < this.numFrames; frame_i++){

         //make sure we have a valid frame counter
         fc = this.frames[frame_i].getFrameCounter();
         if(fc == null || fc == BarrelFrame.INT4_FILL){
            continue;
         }

         //calculate the subcom and frameGroup
         mod4 = this.frames[frame_i].mod4;
         fg = fc - mod4;

         //figure out if we are still in the same record
         if (frameGroup[rec_i] != fg) {
            rec_i++;
            frameGroup[rec_i] = fg;
         }
         
         //get the epoch the the frameGroup frame
         epoch[rec_i] = CDF_Gen.barrel_time.getEpoch(frameGroup[rec_i]);

         raw = this.frames[frame_i].getRateCounter();
         rc[rec_i] = raw != BarrelFrame.INT4_FILL ?
            raw / 4f : BarrelFrame.FLOAT_FILL;
      }
         
      System.out.println("\nSaving RCNT...");

      String destName = 
         outputPath + "/" + date + "/"  + "bar_" + id +
         "_l2_" + "rcnt" + "_20" + date +  "_v" + revNum + ".cdf";
      DataProduct rcnt = new RCNT(destName, "bar_" + id, date, 2);

      System.out.println("Interrupt");
      rcnt.getCDF().addData("Interrupt", rc[0]);
      System.out.println("LowLevel");
      rcnt.getCDF().addData("LowLevel",  rc[1]);
      System.out.println("HighLevel");
      rcnt.getCDF().addData("HighLevel", rc[3]);
      System.out.println("PeakDet");
      rcnt.getCDF().addData("PeakDet",   rc[2]);
      System.out.println("FrameGroup");
      rcnt.getCDF().addData("FrameGroup", frameGroup);
      System.out.println("Epoch");
      rcnt.getCDF().addData("Epoch", epoch);
      System.out.println("Q");
      rcnt.getCDF().addData("Q", q);

      rcnt.close();
   }

   private float getTemp(int start, int id_number) {
      int
         raw_temp  = null,
         frame_i   = start,
         mod40     = this.frames[startFrame].mod40,
         fc        = this.frames[startFrame].getFrameCounter();
      String
         id = HKPG.IDS[id_number];

      frame_i = fc - mod40 + tempID;
      while(frame_i > 0){
         raw_temp = this.frames[frame_i].getHousekeeping();

         if(raw_temp != null && raw_temp != BarrelFrame.INT4_FILL){
            return
               (float) raw_temp * 
               HKPG.SCALE_FACTORS.get(id) + HKPG.OFFSETS.get(id);
         }
         
         frame_i--;
      }

      return 0.0f;
   }
 }
