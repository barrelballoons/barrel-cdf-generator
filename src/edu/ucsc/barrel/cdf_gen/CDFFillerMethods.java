package edu.ucsc.barrel.cdf_gen;

import gsfc.nssdc.cdf.CDFException;

interface CDFFillerMethods{
   void doGpsCdf () throws CDFException;
   void doMiscCdf() throws CDFException;
   void doMagCdf () throws CDFException;
   void doHkpgCdf() throws CDFException;
   void doFspcCdf() throws CDFException;
   void doMspcCdf() throws CDFException;
   void doSspcCdf() throws CDFException;
   void doRcntCdf() throws CDFException;
}
