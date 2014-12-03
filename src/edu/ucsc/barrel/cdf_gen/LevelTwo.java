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
import java.util.Iterator;

public class LevelTwo extends CDFWriter{

   public LevelTwo(
      final String d, final String p, final String f, 
      final String s, final String dir
   ) throws IOException
   {
      super(d, p, f, s, dir, "Level Two");
   }
   
   //Convert the EPHM data and save it to CDF files
   public void doGpsCdf() throws CDFException{
      Calendar cal;
      Logger geo_coord_file;
      String[] 
         mag_coords;
      Integer
         fc;
      BarrelFrame
         frame;
      Iterator<Integer>
         fc_i        = this.fc_list.iterator();
      int
         rec_i,
         year, month, day, day_of_year, hour, min, sec, intVal, mod4, fg,
         numRecords  = CDF_Gen.frames.getNumRecords("mod4");
      double
         sec_of_day  = 0;
      float
         east_lon    = 0;
      float[]
         lat         = new float[numRecords],
         lon         = new float[numRecords],
         alt         = new float[numRecords],
         mlt2        = new float[numRecords],
         mlt6        = new float[numRecords],
         l2          = new float[numRecords],
         l6          = new float[numRecords];
      long[]
         frameGroup  = new long[numRecords],
         q           = new long[numRecords],
         epoch_parts = new long[9],
         gps_time    = new long[numRecords],
         epoch       = new long[numRecords];
      Map<Integer, Boolean> 
         complete_gps= new HashMap<Integer, Boolean>(numRecords);

      //initialize the data arrays with fill value
      Arrays.fill(frameGroup, BarrelCDF.FC_FILL);
      Arrays.fill(epoch,      BarrelCDF.EPOCH_FILL);
//      Arrays.fill(q,          BarrelCDF.QUALITY_FILL);
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
      year = this.working_date / 10000;
      month = (this.working_date - (year * 10000)) / 100;
      day = this.working_date - (year * 10000) - (month * 100);
      cal = Calendar.getInstance();
      cal.set(Calendar.YEAR, year + 2000);
      cal.set(Calendar.MONTH, month - 1);
      cal.set(Calendar.DAY_OF_MONTH, day);
      day_of_year = cal.get(Calendar.DAY_OF_YEAR);
      cal = null;

      //convert lat, lon, and alt values and select values for this date
      rec_i = -1;
      while (fc_i.hasNext()) {
         fc = fc_i.next();
         frame = CDF_Gen.frames.getFrame(fc);

         //calculate the subcom and frameGroup
         mod4 = frame.mod4;
         fg = fc - mod4;

         //figure out if we are still in the same record
         if (rec_i == -1 || frameGroup[rec_i] != fg) {
            rec_i++;
            frameGroup[rec_i] = fg;
         }
         
         //get the epoch of the frameGroup
         epoch[rec_i] = CDF_Gen.barrel_time.getEpoch(fg);

         switch(mod4) {
            //convert mm to km
            case Ephm.ALT_I:
               intVal = frame.getGPS();
               alt[rec_i] = intVal != Ephm.RAW_GPS_FILL ?
                  intVal / 1000000 :
                  Ephm.ALT_FILL;
            break;

            //convert lat and lon to physical units
            case Ephm.LAT_I:
               intVal = frame.getGPS();
               lat[rec_i] = intVal != Ephm.RAW_GPS_FILL ? 
                  (
                     intVal * 
                     Float.intBitsToFloat(
                        Integer.valueOf("33B40000", 16).intValue()
                     )
                  ) :
                  Ephm.LAT_FILL;
            break;
            case Ephm.LON_I:
               intVal = frame.getGPS();
               lon[rec_i] = intVal != Ephm.RAW_GPS_FILL ? 
                  (
                     intVal * 
                     Float.intBitsToFloat(
                        Integer.valueOf("33B40000", 16).intValue()
                     )
                  ) :
                  Ephm.LON_FILL;
            break;
            case Ephm.TIME_I:
               intVal = frame.getGPS();
               //calculate the GPS time
               if(intVal != Ephm.RAW_GPS_FILL){
                  sec = intVal / 1000; //convert ms to sec
                  sec %= 86400; //remove any complete days
                  hour = sec / 3600;
                  sec %= 3600;
                  min = sec / 60;
                  sec %= 60;
                  gps_time[rec_i] = CDFTT2000.compute(
                     (long)(year + 2000), (long)(month - 1), (long)day,
                     (long)hour, (long)min, (long)sec, 0L, 0L, 0L
                  );  
               }
            break;
         }

         //keep track of which frames have complete GPS values
         complete_gps.put(
            fg, 
            (
               (alt[rec_i] != Ephm.ALT_FILL) && 
               (lat[rec_i] != Ephm.LAT_FILL) && 
               (lon[rec_i] != Ephm.LON_FILL)
            )
         );
      }

      //write a text file containing gps coordinates
      geo_coord_file = 
         new Logger("pay" + this.id + "_" + this.working_date + "_gps.txt");
      for (rec_i = 0; rec_i < numRecords; rec_i++) {
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
               frameGroup[rec_i], alt[rec_i], lat[rec_i], east_lon,
               (year + 2000), day_of_year, sec_of_day
            )
         );
      }
      geo_coord_file.close();

      //get the magnetic field info for this location
      try{
         String command = 
            CDF_Gen.getSetting("mag_gen") + " " + 
            "pay" + id + "_" + this.working_date + "_gps.txt";

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
                  "pay" + id + "_" + this.working_date + "_gps_out.txt"
               )
            );
         String line;
         int 
            this_frame = 0,
            last_frame = 0;

         rec_i = 0;
         while((line = mag_coord_file.readLine()) != null){
            line = line.trim();
            mag_coords = line.split("\\s+");

            //check for repeated frame
            last_frame = this_frame;
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

            rec_i++;
         }

         mag_coord_file.close();

         //clean up after ourselves
         geo_coord_file.delete();
         (new File("pay" + id + "_" + this.working_date + "_gps_out.txt")).delete();

      }catch(IOException ex){
         System.out.println("Could not read magnetic coordinate file:");
         System.out.println(ex.getMessage());
      }

      //make sure there is a CDF file to open
      //(copyFile will not clobber an existing file)
      String destName = 
         outputPath + "/" + this.working_date + "/" + "bar_" + id + 
         "_l2_" + "ephm" + "_20" + this.working_date +  "_v" + revNum + ".cdf";
     
      Ephm ephm = new Ephm(destName, "bar_" + id, this.working_date, 2);

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
//      System.out.println("Q");
//      ephm.getCDF().addData("Q", q);

      ephm.close();
   }
   
   //write the misc file, no processing needed
   public void doMiscCdf() throws CDFException{
      Integer
         fc;
      BarrelFrame
         frame;
      Iterator<Integer>
         fc_i       = this.fc_list.iterator();
      int
         rec_i;
      int[]
         version    = new int[this.numFrames],
         payID      = new int[this.numFrames],
         pps_vals   = new int[this.numFrames];
      long[] 
         frameGroup = new long[this.numFrames],
         q          = new long[this.numFrames],
         epoch      = new long[this.numFrames];

      Arrays.fill(frameGroup, BarrelCDF.FC_FILL);
      Arrays.fill(epoch,      BarrelCDF.EPOCH_FILL);
//      Arrays.fill(q,          BarrelCDF.QUALITY_FILL);
      Arrays.fill(version,    Misc.VERSION_FILL);
      Arrays.fill(payID,      Misc.PAYLOADID_FILL);
      Arrays.fill(pps_vals,   Misc.PPS_FILL);

      System.out.println("\nSaving MISC Level Two CDF...");

      rec_i = -1;
      while (fc_i.hasNext()) {
         fc = fc_i.next();
         frame = CDF_Gen.frames.getFrame(fc);
         rec_i++;

         pps_vals[rec_i]   = frame.getPPS();
         version[rec_i]    = frame.getDPUVersion();
         payID[rec_i]      = frame.getPayloadID();
         frameGroup[rec_i] = fc;
         epoch[rec_i]      = CDF_Gen.barrel_time.getEpoch(fc);
      }

      String destName = 
         outputPath  + "/" + this.working_date + "/" + "bar_" + id + 
         "_l2_" + "misc" + "_20" + this.working_date +  "_v" + revNum + ".cdf";
     
      Misc misc = new Misc(destName, "bar_" + id, this.working_date, 2);

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
//      System.out.println("Q");
//      misc.getCDF().addData("Q", q);

      misc.close();
   }
   
   public void doMagCdf() throws CDFException{
      Integer
         fc;
      BarrelFrame
         frame;
      Iterator<Integer>
         fc_i        = this.fc_list.iterator();
      boolean 
         found_fill = false;
      int
         rec_i, sample, offset,
         numRecords  = CDF_Gen.frames.getNumRecords("4Hz");
      int[][]
         raw_mag;
      long
         base_epoch;
      long[] 
         frameGroup = new long[numRecords],
         q          = new long[numRecords],
         epoch      = new long[numRecords];
      float[] 
         magx       = new float[numRecords],
         magy       = new float[numRecords],
         magz       = new float[numRecords],
         magTot     = new float[numRecords];

      Arrays.fill(frameGroup, BarrelCDF.FC_FILL);
      Arrays.fill(epoch,      BarrelCDF.EPOCH_FILL);
//      Arrays.fill(q,          BarrelCDF.QUALITY_FILL);
      Arrays.fill(magx,       Magn.MAG_FILL);
      Arrays.fill(magy,       Magn.MAG_FILL);
      Arrays.fill(magz,       Magn.MAG_FILL);
      Arrays.fill(magTot,     Magn.MAG_FILL);

      System.out.println("\nSaving Magnetometer Level Two CDF...");

      //extract the nominal magnetometer value and calculate |B| for each frame
      rec_i = -1;
      while (fc_i.hasNext()) {
         fc = fc_i.next();
         frame = CDF_Gen.frames.getFrame(fc);
         raw_mag = frame.getMag();

         base_epoch = CDF_Gen.barrel_time.getEpoch(fc);

         //each frame has 4 samples per axis
         for (int sample_i = 0; sample_i < 4; sample_i++) {
            rec_i++;
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
	         
            frameGroup[rec_i] = fc;
	         epoch[rec_i] = base_epoch + offset;
         }
         
         //q[frame_i] = frame.get();
      }

      //store the nominal mag values
      String destName = 
         outputPath + "/" + this.working_date + "/" + "bar_" + id + 
         "_l2_" + "magn" + "_20" + this.working_date +  "_v" + revNum + ".cdf";
     
      Magn magn = new Magn(destName, "bar_" + id, this.working_date, 2);
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
//      System.out.println("Q...");
//      magn.getCDF().addData("Q", q);

      magn.close();
   }
   
   public void doHkpgCdf() throws CDFException{
      Integer
         fc;
      BarrelFrame
         frame;
      Iterator<Integer>
         fc_i        = this.fc_list.iterator();
      int
         rec_i, fg, hkpg_raw, mod40,
         numRecords  = CDF_Gen.frames.getNumRecords("mod40");
      int[]
         sats        = new int[numRecords],
         offset      = new int[numRecords],
         termStat    = new int[numRecords],
         modemCnt    = new int[numRecords],
         dcdCnt      = new int[numRecords],
         cmdCnt      = new int[numRecords],
         weeks       = new int[numRecords];
      long[]
         epoch       = new long[numRecords],
         frameGroup  = new long[numRecords],
         q           = new long[numRecords];
      float [][]
         hkpg_scaled = new float[40][numRecords];

      Arrays.fill(frameGroup, BarrelCDF.FC_FILL);
      Arrays.fill(epoch,      BarrelCDF.EPOCH_FILL);
//      Arrays.fill(q,          BarrelCDF.QUALITY_FILL);
      Arrays.fill(weeks,      HKPG.WEEK_FILL);
      Arrays.fill(cmdCnt,     HKPG.CMD_CNT_FILL);
      Arrays.fill(dcdCnt,     HKPG.DCD_CNT_FILL);
      Arrays.fill(sats,       HKPG.SATS_FILL);
      Arrays.fill(offset,     HKPG.UTC_OFFSET_FILL);
      Arrays.fill(termStat,   HKPG.TERM_STAT_FILL);
      Arrays.fill(modemCnt,   HKPG.MODEM_CNT_FILL);

      for(int var_i = 0; var_i < 36; var_i++){
         Arrays.fill(hkpg_scaled[var_i], HKPG.SENSOR_FILL);
      }
      
      System.out.println("\nSaving HKPG...");

      String destName = 
         outputPath + "/" + this.working_date + "/" + "bar_" + id + 
         "_l2_" + "hkpg" + "_20" + this.working_date +  "_v" + revNum + ".cdf";

      HKPG hkpg = new HKPG(destName, "bar_" + id, this.working_date, 2);

      rec_i = -1;
      while (fc_i.hasNext()) {
         fc = fc_i.next();
         frame = CDF_Gen.frames.getFrame(fc);
         mod40 = frame.mod40;
         fg = fc - mod40;

         if (rec_i == -1 || frameGroup[rec_i] != fg) {
            rec_i++;
            frameGroup[rec_i] = fg;
         }

         //make sure there is a valid housekeeping value to process
         hkpg_raw = frame.getHousekeeping();
         if(hkpg_raw == HKPG.RAW_SENSOR_FILL){continue;}

         //convert the housekeeping data to physical units
         switch (mod40) {
            //for versions 3 and up the T9 and T11 sensors were used for
            //mag statistics rather than solar panel temps
            case HKPG.T9:
               if(this.dpu_ver > 3) {
                  hkpg_scaled[mod40][rec_i] = 
                     ((hkpg_raw - 0x8000) * 0.09094f) - 273.15f;
               } else {
                  hkpg_scaled[mod40][rec_i] = 
                     (hkpg_raw * HKPG.SCALE_FACTORS.get(HKPG.IDS[mod40])) +
                     HKPG.OFFSETS.get(HKPG.IDS[mod40]);
               }
            break;
            case HKPG.T11:
               if(this.dpu_ver > 3) {
                  hkpg_scaled[mod40][rec_i] =  hkpg_raw * 0.0003576f;
               } else {
                  hkpg_scaled[mod40][rec_i] = 
                     (hkpg_raw * HKPG.SCALE_FACTORS.get(HKPG.IDS[mod40])) +
                     HKPG.OFFSETS.get(HKPG.IDS[mod40]);
               }
            break;
            
            case HKPG.SATSOFF:
               sats[rec_i]     = frame.getNumSats();
               offset[rec_i]   = frame.getUTCOffset();
            break;

            case HKPG.WEEK:
               weeks[rec_i]    = frame.getWeek();
            break;

            case HKPG.CMDCNT:
               termStat[rec_i] = frame.getTermStatus();
               cmdCnt[rec_i]   = frame.getCommandCounter();
            break;

            case HKPG.MDMCNT:
               modemCnt[rec_i] = frame.getModemCount();
               dcdCnt[rec_i]   = frame.getDcdCount();
            break;

            default:
               hkpg_scaled[mod40][rec_i] = 
                  (hkpg_raw * HKPG.SCALE_FACTORS.get(HKPG.IDS[mod40])) +
                  HKPG.OFFSETS.get(HKPG.IDS[mod40]);
            break;
         }

         epoch[rec_i] = CDF_Gen.barrel_time.getEpoch(fg);
      }

      for(int var_i = 0; var_i < 36; var_i++){
         String label = HKPG.LABELS.get(HKPG.IDS[var_i]);
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
//      System.out.println("Q...");
//      hkpg.getCDF().addData("Q", q);

      hkpg.close();
   }

   public void doFspcCdf() throws CDFException{
      Integer
         fc;
      BarrelFrame
         frame;
      Iterator<Integer>
         fc_i        = this.fc_list.iterator();
      int
         rec_i, offset, mod40,
         numRecords  = CDF_Gen.frames.getNumRecords("20Hz"),
         numCh = FSPC.getChannels(this.dpu_ver).length;
      int[][] 
         lc_raw     = new int[numCh][numRecords],
         lc_scaled  = new int[numCh][numRecords];
      long
         base_epoch;
      long[]
         frameGroup = new long[numRecords],
         q          = new long[numRecords],
         epoch      = new long[numRecords];
      float 
         scint_temp = 20f,
         dpu_temp   = 20f,
         peak       = -1f;
      float[] 
         old_edges, 
         std_edges  = CDF_Gen.spectra.stdEdges(0, 2.4414f);
      float[][] 
         chan_edges = new float[numRecords][numCh + 1],
         lc_error   = new float[numCh][numRecords];
      String[]
         chan_names = (numCh == 6 ? FSPC.NEW_LABELS : FSPC.OLD_LABELS);

      Arrays.fill(frameGroup, BarrelCDF.FC_FILL);
      Arrays.fill(epoch,      BarrelCDF.EPOCH_FILL);
//      Arrays.fill(q,          BarrelCDF.QUALITY_FILL);
      for (int ch_i = 0; ch_i < numCh; ch_i++) {
         Arrays.fill(lc_scaled[ch_i], FSPC.CNT_FILL);
         Arrays.fill(lc_error[ch_i], FSPC.ERROR_FILL);
      }

      System.out.println("\nSaving FSPC...");
      
      //convert the light curves counts to cnts/sec and 
      //figure out the channel width :)
      rec_i = -1;
      while (fc_i.hasNext()) {
         fc         = fc_i.next();
         frame      = CDF_Gen.frames.getFrame(fc);
         mod40      = frame.mod40;
         lc_raw     = frame.getFSPC();
         base_epoch = CDF_Gen.barrel_time.getEpoch(fc);

         //each frame has 20 samples per channel
         for (int ch_i = 0; ch_i < numCh; ch_i++) {
            for (int sample_i = 0, sample; sample_i < 20; sample_i++) {
               rec_i++;
               offset = sample_i * 50000000;
               sample = lc_raw[ch_i][sample_i];
               if(sample != FSPC.CNT_FILL){
                  lc_scaled[ch_i][rec_i] = sample;
                  lc_error[ch_i][rec_i] =(float)Math.sqrt(sample);
               }

               //get the adjusted bin edges
               chan_edges[rec_i] = CDF_Gen.spectra.createBinEdges(
                  0, CDF_Gen.spectra.getPeakLocation(fc)
               );

               frameGroup[rec_i] = fc;
               epoch[rec_i] = base_epoch + offset;
            }
         }
      }

      String destName = 
         outputPath + "/" + this.working_date + "/" + "bar_" + id + 
         "_l2_" + "fspc" + "_20" + this.working_date +  "_v" + revNum + ".cdf";

      FSPC fspc = 
         new FSPC(destName, "bar_" + id, this.working_date, 2, this.dpu_ver);

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
//      System.out.println("Q");
//      fspc.getCDF().addData("Q", q);

      fspc.close();
   }

   public void doMspcCdf() throws CDFException{
      Integer
         fc;
      BarrelFrame
         frame;
      Iterator<Integer>
         fc_i        = this.fc_list.iterator();
      int 
         rec_i, hkpg_frame, start, stop, mod4, fg,
         offset     = 90,
         numRecords  = CDF_Gen.frames.getNumRecords("mod4");
      int[]
         part_spec,
         raw_spec   = new int[48];
      long[]
         frameGroup = new long[numRecords],
         q          = new long[numRecords],
         epoch      = new long[numRecords];
      float
         scint_temp = 0, 
         dpu_temp   = 0,
         width;
      float[]
         old_edges,
         std_edges  = CDF_Gen.spectra.stdEdges(1, 2.4414f);
      float[][]
         rebin = new float[numRecords][48],
         error = new float[numRecords][48];

      //initialize the data arrays with fill value
      Arrays.fill(frameGroup,  BarrelCDF.FC_FILL);
      Arrays.fill(epoch,       BarrelCDF.EPOCH_FILL);
//      Arrays.fill(q,           BarrelCDF.QUALITY_FILL);
      Arrays.fill(raw_spec,    MSPC.RAW_CNT_FILL);
      for(int i = 0; i < numRecords; i++){
         Arrays.fill(rebin[i], MSPC.CNT_FILL);
         Arrays.fill(error[i], MSPC.ERROR_FILL);
      }
      
      System.out.println("\nSaving MSPC...");

      //get the first frame group
      fc    = this.fc_list.get(0);
      frame = CDF_Gen.frames.getFrame(fc); 
      fg    = fc - frame.mod4;
      frameGroup[0] = fg;

      //rebin the mspc spectra
      rec_i = 0;
      while (fc_i.hasNext()) {
         fc    = fc_i.next();
         frame = CDF_Gen.frames.getFrame(fc);
         mod4  = frame.mod4;
         fg    = fc - mod4;

         //check if we are still in the same frame group 
         //(meaning the same spectrum)
         if (frameGroup[rec_i] != fg) {
            //save the epoch and frameGroup of this spectrum
            epoch[rec_i] = CDF_Gen.barrel_time.getEpoch(frameGroup[rec_i]);

            //get the most recent scintillator temperature value
            scint_temp = getTemp(frame, HKPG.T0);
            dpu_temp   = getTemp(frame, HKPG.T5);

            //get the adjusted bin edges
            old_edges = 
               CDF_Gen.spectra.makeedges(
                  1, scint_temp, dpu_temp, 
                  CDF_Gen.spectra.getPeakLocation(frameGroup[rec_i])
               );

            //rebin the spectrum
            rebin[rec_i] = 
               ExtractSpectrum.rebin(raw_spec, old_edges, std_edges);

            //scale the counts and calculate error
            for(int bin_i = 0; bin_i < 48; bin_i++){
               if(rebin[rec_i][bin_i] != MSPC.CNT_FILL){
                  width = std_edges[bin_i + 1] - std_edges[bin_i];

                  //divide counts by bin width and adjust the time scale
                  rebin[rec_i][bin_i] /= (width * 4f);
                  //get the count error
                  error[rec_i][bin_i] = 
                     (float)Math.sqrt(rebin[rec_i][bin_i]) / (width * 4f);
               }
            }

            //clear the raw spectrum
            Arrays.fill(raw_spec, MSPC.RAW_CNT_FILL);

            //update the record number and frameGroup
            rec_i++;            
            frameGroup[rec_i] = fg;
         }

         //fill part of the raw spectrum
         start = mod4 * 12;
         stop = start + 12;
         part_spec = frame.getMSPC();
         for(
            int spec_i = start, sample_i = 0;
            spec_i < stop;
            sample_i++, spec_i++
         ) {
            raw_spec[spec_i] = part_spec[sample_i];
         }
      }

      String destName = 
         outputPath + "/" + this.working_date + "/" + "bar_" + id +
         "_l2_" + "mspc" + "_20" + this.working_date +  "_v" + revNum + ".cdf";

      MSPC mspc = new MSPC(destName, "bar_" + id, this.working_date, 2);
      System.out.println("mspc");
      mspc.getCDF().addData("MSPC", rebin);
      System.out.println("mspc error");
      mspc.getCDF().addData("cnt_error", error);
      System.out.println("FrameGroup");
      mspc.getCDF().addData("FrameGroup", frameGroup);
      System.out.println("Epoch");
      mspc.getCDF().addData("Epoch", epoch);
//      System.out.println("Q");
//      mspc.getCDF().addData("Q", q);

      mspc.close();
   }

   public void doSspcCdf() throws CDFException{
      Integer
         fc;
      BarrelFrame
         frame;
      Iterator<Integer>
         fc_i        = this.fc_list.iterator();
      float
         scint_temp = 0, 
         dpu_temp   = 0;
      int[]
         part_spec,
         raw_spec   = new int[256];
      int 
         rec_i, hkpg_frame, start, stop, mod32, fg,
         offset     = 90,
         numRecords  = CDF_Gen.frames.getNumRecords("mod32");
      long[]
         frameGroup = new long[numRecords],
         q          = new long[numRecords],
         epoch      = new long[numRecords];
      float
         width;
      float[]
         old_edges,
         std_edges  = CDF_Gen.spectra.stdEdges(2, 2.4414f),
         peak       = new float[numRecords];
      float[][]
         rebin      = new float[numRecords][256],
         error      = new float[numRecords][256];

      //initialize the data arrays with fill value
      Arrays.fill(frameGroup, BarrelCDF.FC_FILL);
      Arrays.fill(epoch,      BarrelCDF.EPOCH_FILL);
//      Arrays.fill(q,          BarrelCDF.QUALITY_FILL);
      Arrays.fill(peak,       SSPC.PEAK_FILL);
      Arrays.fill(raw_spec,   SSPC.RAW_CNT_FILL);
      for (int spec_i = 0; spec_i < 256; spec_i++){
         Arrays.fill(error[spec_i], SSPC.ERROR_FILL);
         Arrays.fill(rebin[spec_i], SSPC.CNT_FILL);
      }

      System.out.println("\nSaving SSPC...");

      //get the first frame group
      fc    = this.fc_list.get(0);
      frame = CDF_Gen.frames.getFrame(fc); 
      fg    = fc - frame.mod32;
      frameGroup[0] = fg;

      //rebin the sspc spectra
      rec_i = 0;
      while (fc_i.hasNext()) {
         fc    = fc_i.next();
         frame = CDF_Gen.frames.getFrame(fc);
         mod32 = frame.mod32;
         fg = fc - mod32;

         //check if we are still in the same frame group 
         //(meaning the same spectrum)
         if (frameGroup[rec_i] != fg) {
            epoch[rec_i] = CDF_Gen.barrel_time.getEpoch(frameGroup[rec_i]);
            peak[rec_i] = CDF_Gen.spectra.getPeakLocation(frameGroup[rec_i]);

            //get the most recent scintillator temperature value
            scint_temp = getTemp(frame, HKPG.T0);
            dpu_temp   = getTemp(frame, HKPG.T5);

            //get the adjusted bin edges
            old_edges = 
               CDF_Gen.spectra.makeedges(2, scint_temp, dpu_temp, peak[rec_i]);

            //rebin the spectrum
            rebin[rec_i] = 
               ExtractSpectrum.rebin(raw_spec, old_edges, std_edges);

            //scale the counts and calculate error
            for(int bin_i = 0; bin_i < 256; bin_i++){
               if(rebin[rec_i][bin_i] != SSPC.CNT_FILL){
                  width = std_edges[bin_i + 1] - std_edges[bin_i];

                  //divide counts by bin width and adjust the time scale
                  rebin[rec_i][bin_i] /= (width * 32f);
                  //get the count error
                  error[rec_i][bin_i] = 
                     (float)Math.sqrt(rebin[rec_i][bin_i]) / (width * 32f);
               }
            }

            //clear the raw spectrum
            Arrays.fill(raw_spec, SSPC.RAW_CNT_FILL);

            //update the record number and frameGroup
            rec_i++;            
            frameGroup[rec_i] = fg;
         }

         //fill part of the raw spectrum
         start = mod32 * 8;
         stop = start + 8;
         part_spec = frame.getSSPC();
         for(
            int spec_i = start, sample_i = 0;
            spec_i < stop;
            sample_i++, spec_i++
         ) {
            raw_spec[spec_i] = part_spec[sample_i];
         }
      }

      String destName =
         outputPath + "/" + this.working_date + "/" + "bar_" + id + 
         "_l2_" + "sspc" + "_20" + this.working_date +  "_v" + revNum + ".cdf";

      SSPC sspc = new SSPC(destName, "bar_" + id, this.working_date, 2);

      System.out.println("sspc");
      sspc.getCDF().addData("SSPC", rebin);
      System.out.println("sspc error");
      sspc.getCDF().addData("cnt_error", error);
      System.out.println("Peak_511");
      sspc.getCDF().addData("Peak_511", peak);
      System.out.println("FrameGroup");
      sspc.getCDF().addData("FrameGroup", frameGroup);
      System.out.println("Epoch");
      sspc.getCDF().addData("Epoch", epoch);
//      System.out.println("Q");
//      sspc.getCDF().addData("Q", q);

      sspc.close();
   }

   public void doRcntCdf() throws CDFException{
      Integer
         fc;
      BarrelFrame
         frame;
      Iterator<Integer>
         fc_i        = this.fc_list.iterator();
      int
         raw, rec_i, mod4, fg,
         numRecords  = CDF_Gen.frames.getNumRecords("mod4");
      long[]
         q          = new long[numRecords],
         frameGroup = new long[numRecords],
         epoch      = new long[numRecords];
      float[][]
         rc         = new float[4][numRecords];
      
      //initialize the data arrays with fill value
      Arrays.fill(frameGroup, BarrelCDF.FC_FILL);
      Arrays.fill(epoch,      BarrelCDF.EPOCH_FILL);
//      Arrays.fill(q,          BarrelCDF.QUALITY_FILL);
      Arrays.fill(rc[0],      RCNT.CNT_FILL);
      Arrays.fill(rc[1],      RCNT.CNT_FILL);
      Arrays.fill(rc[2],      RCNT.CNT_FILL);
      Arrays.fill(rc[3],      RCNT.CNT_FILL);
      
      //change all the units from cnts/4sec to cnts/sec
      rec_i = -1;
      while (fc_i.hasNext()) {
         fc    = fc_i.next();
         frame = CDF_Gen.frames.getFrame(fc);
         mod4 = frame.mod4;
         fg = fc - mod4;

         //figure out if we are still in the same record
         if (rec_i == -1 || frameGroup[rec_i] != fg) {
            rec_i++;
            frameGroup[rec_i] = fg;
         }
         
         //get the epoch the the frameGroup frame
         epoch[rec_i] = CDF_Gen.barrel_time.getEpoch(fg);

         raw = frame.getRateCounter();
         rc[mod4][rec_i] = raw != RCNT.RAW_CNT_FILL ?
            raw / 4f : RCNT.CNT_FILL;
      }
         
      System.out.println("\nSaving RCNT...");

      String destName = 
         outputPath + "/" + this.working_date + "/"  + "bar_" + id +
         "_l2_" + "rcnt" + "_20" + this.working_date +  "_v" + revNum + ".cdf";
      DataProduct rcnt = new RCNT(destName, "bar_" + id, this.working_date, 2);

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
//      System.out.println("Q");
//      rcnt.getCDF().addData("Q", q);

      rcnt.close();
   }

   private float getTemp(BarrelFrame startFrame, int id_number) {
      BarrelFrame frame = null;
      int[] fcRange = CDF_Gen.frames.getFcRange();
      int
         raw_temp,
         //this is the ideal fc: the spectrum's frame offset by the mod40
         //housekeeping index of the temp sensor
         target = 
            (int)startFrame.getFrameCounter() - startFrame.mod40 + id_number,
         prev   = target,
         next   = target,
         //get the bounds of our search
         first  = fcRange[0],
         last   = fcRange[1];

      String
         id = HKPG.IDS[id_number];

      //With each iteration, decrease 'prev' and increase 'next' by 40 frames
      //Since they are started on frames that should contain the correct 
      //temp sensor, each 40 frame offset will should also contain the correct
      //sensor. Keep going until one of them finds a valid temp
      while(prev >= first || next <= last){
         //first check prev
         if (prev > first) { //only proceed if prev is in bounds
            frame = CDF_Gen.frames.getFrame(prev);
            if(frame != null) {//make sure there is a frame for this fc
               raw_temp = frame.getHousekeeping();
               if(raw_temp != HKPG.RAW_SENSOR_FILL){
                  //found a valid temp, we are done
                  return
                     (float) raw_temp * 
                     HKPG.SCALE_FACTORS.get(id) + HKPG.OFFSETS.get(id);
               }
            }
            //nothing found so step back
            prev -= 40;
         }

         //didnt have it for prev, check next
         if (next < last) {
            frame = CDF_Gen.frames.getFrame(next);
            if(frame != null) {
            raw_temp = frame.getHousekeeping();
               if(raw_temp != HKPG.RAW_SENSOR_FILL){
                  return
                     (float) raw_temp * 
                     HKPG.SCALE_FACTORS.get(id) + HKPG.OFFSETS.get(id);
               }
            }
            next += 40;
         }
      }

      //every frame with the correct mod40 offset was checked
      //and no valid temp was found :(
      return 0.0f;
   }
 }
