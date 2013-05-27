/*
Logger.java

Description:
   Writes output log files
   
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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Logger{
   private BufferedWriter bw;

   public Logger(final String filename){
      //Open the Log file for writing

      try{
         File file = new File(filename);
         // if file doesnt exists, then create it
         if (!file.exists()) {
            file.createNewFile();
         }

         FileWriter fw = new FileWriter(file.getAbsoluteFile());
         bw = new BufferedWriter(fw);
      }catch(IOException e){
         System.out.println("Could not open log file for writing:");
         System.out.println(e.getMessage());
      }
   }  
   
   public void write(String content){
      try{
         bw.write(content);
      }catch(IOException e){
         System.out.println(e.getMessage());
      }
   }
   public void writeln(String content){
      try{
         bw.write(content);
         bw.newLine();
      }catch(IOException e){
         System.out.println(e.getMessage());
      }
   }
   public void newLine(){
      try{
         bw.newLine();
      }catch(IOException e){
         System.out.println(e.getMessage());
      }
   }



   public void close(){
      try{
         bw.close();
      }catch(IOException e){
         System.out.println("Failed to close log file!");
         System.out.println(e.getMessage());
      }
   }
}
