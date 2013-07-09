package edu.ucsc.barrel.cdf_gen;

import gsfc.nssdc.cdf.CDFException;

interface CDFFillerMethods{
   void doGpsCdf(int first, int last, int date) throws CDFException;
   void doPpsCdf(int first, int last, int date) throws CDFException;
   void doMagCdf(int first, int last, int date) throws CDFException;
   void doHkpgCdf(int first, int last, int date) throws CDFException;
   void doFspcCdf(int first, int last, int date) throws CDFException;
   void doMspcCdf(int first, int last, int date) throws CDFException;
   void doSspcCdf(int first, int last, int date) throws CDFException;
   void doRcntCdf(int first, int last, int date) throws CDFException;
}
