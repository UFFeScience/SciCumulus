# -*- coding: cp1252 -*-
#! /usr/bin/env python

import sys
from SciCumulusExtractor import SciCumulusExtractor

def main():
        
    if (len(sys.argv) != 4):
        print "Wrong number of arguments"
        print "Usage: extractor.py <inputfilepath> <PAfile> <outputfile>" 
    else:
        infile = sys.argv[1]
        extfile = sys.argv[2]
        outfile = sys.argv[3]
        extractor = SciCumulusExtractor(infile=infile, extfile=extfile, outfile=outfile)
        extractor.extraction()
              

if __name__ == "__main__":
    main()    
