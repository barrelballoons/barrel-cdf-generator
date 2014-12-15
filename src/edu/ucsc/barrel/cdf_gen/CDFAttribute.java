/*
CDFAttribute.java

Description:
   CDFComponent subclass that impements  
   methods for reading and writing CDF variables.

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

import gsfc.nssdc.cdf.CDF;
import gsfc.nssdc.cdf.CDFException;
import gsfc.nssdc.cdf.CDFConstants;
import gsfc.nssdc.cdf.Variable;
import gsfc.nssdc.cdf.Attribute;
import gsfc.nssdc.cdf.Entry;

public class CDFAttribute implements CDFComponent{
   private CDF cdf;
   private long type = -1;
   private String name;
   private Object value;
   private Attribute attr;
   private CDFComponent owner;
   
   //create and set the attribute all at once
   public CDFAttribute(
      final CDFComponent o, final String n, final Object v, long t
   ){
      this.owner = o;
      this.cdf = o.getCDF();
      this.name = n;
      this.type = t;
      
      create(n);
      setValue(v);
   }
   public CDFAttribute(final CDFComponent o, final String n, final Object v){
      this.owner = o;
      this.cdf = o.getCDF();
      this.name = n; 

      create(n);
      setValue(v);
   }
   //overloaded constructor to just create the attribute without setting it
   public CDFAttribute(final CDFComponent o, final String n){
      this.owner = o;
      this.cdf = o.getCDF(); 
      this.name = n;

      create(n);
   }
   
   //public getters and setters
   public CDF getCDF(){return this.cdf;}
   public long getID(){return this.attr.getID();}
   public String getName(){return this.name;}
   public long getNumEntries(){return this.attr.getNumEntries();}

   public long getType(){return this.type;}
   public void setType(long t){this.type = t;}

   public Object getValue(){return this.value;}
   public void setValue(final Object v, long id){
      //set this value and attribute id in the CDFAttribute object
      this.value = v;

      if(this.type == -1){guessType();}

      try{
         Entry.create(this.attr, id, this.type, this.value);
      }catch(CDFException e){
         System.out.println(
            "Could not create entry '" + this.value + 
            "' for attribute '" + this.name + ":"
         );
         System.out.println(e.getMessage());
      }

   }
   //overload the setValue() function with the option to not provide an id
   public void setValue(final Object v){
      long id;

      //figure out the attribute entry id
      if(this.owner.getType() == CDFConstants.CDF_){
         //use the number of entries for this attribute as the entry id
         id = this.getNumEntries();

         //use 0 as the entry number for global variables
         //id = 0L;
      }else{
         //use the variable id as the entry id
         id = this.owner.getID();
      }

      setValue(v, id);
   }

   //try to create an attribute for the owner. If an attribute with that name
   //already exists, get its reference instead.
   public void create(String name){
      long scope, id;
     
      //determine the scope based on the owner type
      scope = (this.owner.getType() == CDFConstants.CDF_) ?
         CDFConstants.GLOBAL_SCOPE : CDFConstants.VARIABLE_SCOPE;

      try{
         //figure out what the id number should be for this attribute
         id = cdf.getAttributeID(this.name);
         //either create or get the attribute
         this.attr = (id != -1L) ? 
            this.cdf.getAttribute(this.name) : 
            Attribute.create(this.cdf, this.name, scope);
      }catch(CDFException e){
         attr = null;
         System.out.println("Could not get attribute: ");
         System.out.println(e.getMessage());
      }
   }
   
   private void guessType(){
      //no type was set try to determine it from value and type of owner
      if(this.value instanceof String){
         //string values must be CDF_CHAR
         this.type = CDFConstants.CDF_CHAR;
      }
      else if(owner.getType() == CDFConstants.CDF_){
         //this is a global, we want globals to be strings. make it so.
         this.type = CDFConstants.CDF_CHAR;
         this.value = this.value.toString();
      }else{
         //Only other option is a numeric variable attribute.
         //Numerical variables will have attribute types that are the same 
         //as the variable type
         this.type = this.owner.getType();
      }
   }
}
