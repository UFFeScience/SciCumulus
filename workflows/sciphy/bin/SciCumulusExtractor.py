# -*- coding: cp1252 -*-
#! /usr/bin/env python
import re

"""
SciCumulusExtractor.py - Abstract class for a generic SciCumulus Extractor

@author: Joao Carlos de A. R. Gonçalves
@version: 0.1

"""

class SciCumulusExtractor:
    """
    SciCumulusExtractor - Class template for SciCumulus extractor
    
    """

    def __init__(self, infile, extfile, outfile='ERelation.txt'):
        if extfile.endswith(".py"):
            extfile = extfile.rstrip(".py")
            
        self.infilename = infile
        self.infile = infile
        self.extfile = extfile
        self.outfile = outfile

    def buildOutputFile(self, tags, provdata):
        """
        buildOutputFile - Method for building the output extraction file

        """
        outcontent = []
        str = ""
        for cat in tags:
                cat = cat.replace(";","")
                str = str + cat + ";"
        str = str[:-1]
        str = str + "\n"
        outcontent.append(str)

        str = ""
        for data in provdata:
                data = data.replace(";","")
                str = str + data + ";"
        str = str[:-1]
        str = str + "\n"   
        outcontent.append(str)
              
        return outcontent

    def extraction(self):
        """
        extraction - Main method for the extractor class

        """
    #TODO - 
        listtags = []
        resultvalues = []
        modname = self.extfile
        methodlist = []
    #Load each extractor code
        #Case 1: Extractor = External library with methods and standard parameters - inputfile
        #Import external library
        mod = __import__(modname)
        dirlist = dir(mod)
        for met in dirlist:
            if "__" not in met and met not in ['re','os']:
                methodlist.append(met)
        for method in methodlist:
            result = ""
            #Load each desired method from external module
            loading = getattr(mod, method)
            #Get the result from method invocation using infile as standard parameter
            result = loading(self, self.infile)
            #Test if result is a dictionary
            if type(result) == dict:
                #Adds each subtuple to the tags and results list
                tags = result.keys()
                tags.sort()
                #Add the new tags
                listtags.extend(tags)
                #Add the new values
                for tag in tags:
                    if (result[tag]) == str:
                        resultvalues.append(result[tag])
                    else:
                        resultvalues.append(str(result[tag]))
            else:
                #Converts unknown format to string
                result = str(result)
                
                #Adds method name to listtags and result to resultvalues
                listtags.append(method)
                resultvalues.append(result)
        #Case 2: Extractor = Legacy code to be executed by shell
            #TODO
        #Case 3: Extractor = python class
            #TODO    
        #Standard extraction output
        
#         begin-vitor
        if self.extfile == "PA_Readseq":
        	listtags.append("PHYLIP")
        	filename = self.infile.split("/")[len(self.infile.split("/"))-1]
        	resultvalues.append(filename + ".phylip")
        elif self.extfile == "PA_Mafft":
        	listtags.append("MAFFT_FILE")
        	filename = self.infile.split("/")[len(self.infile.split("/"))-1]
        	resultvalues.append(filename + ".mafft")
        elif self.extfile == "PA_Modelgenerator":
        	listtags.append("MG")
        	filename = self.infile.split("/")[len(self.infile.split("/"))-1]
        	resultvalues.append(filename + ".mg.modelFromMG.txt")
        elif self.extfile == "PA_Raxml":
        	listtags.append("RAXML")
        	filename = self.infile.split("/")[len(self.infile.split("/"))-1]
        	resultvalues.append("RAxML_bipartitions." + filename + ".phylip_tree3")
#         end-vitor
        	
        	
        outlist = self.buildOutputFile(tags=listtags, provdata = resultvalues)
        output = open(self.outfile, "w")
        for line in outlist:
            output.write(line)

