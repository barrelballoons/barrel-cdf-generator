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
   private String payload;
   private int dpuId;
   private Map<Integer, BarrelFrame> frames;
   
   //variables to keep track of valid altitude range
   private float min_alt;
   private boolean low_alt = true;
   
   //variables to  signal frame counter rollover
   private boolean fc_rollover = false;
   private Integer last_fc = 0;

   public FrameHolder(final String p, final int id){
      this.frame = new HashMap<String, BarrelFrame>();
      this.payload = (p.split(","))[0];
      this.dpuId = id;
      
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
        this.fc_rollover = true; 
      }
   }

   public int getVersion(){
      return this.version;
   }

   public void addFrame(BigInteger rawFrame){
      BarrelFrame frame = new BarrelFrame(rawFrame, dpuId);
      int fc = frame.getFrameCounter();

      //check for fc rollover
      if(this.fc_rollover){
         frame.setFrameCounter(fc + Constants.FC_OFFSET);
      }else{
         if((this.last_fc - fc) > Constants.LAST_DAY_FC){
            //rollover detected
            this.fc_rollover = true;
           
         CDF_Gen.log.writeln(
               "Payload " + payload + " rolled over after fc = " + last_fc 
            );

            //offset fc
            frame.setFrameCounter(fc + Constants.FC_OFFSET);

            //create an empty file to indicate rollover
            (new Logger("fc_rollovers/" + payload)).close();
         }else{
            last_fc = tmpFC;
         }
      }

      //add the frame to the map
      this.frames.put(fc, frame);
   }
   
   public 
}
