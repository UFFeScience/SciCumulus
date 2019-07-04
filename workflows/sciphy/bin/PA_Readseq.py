#!/usr/bin/python

import re

def validatePhylipFile(self, input):
    """
    validatePhylipFile - Method for Phylip file format testing
                        Responsible for the PHYLIP_VALID tag

    """ 
    filename = input+".phylip"
    arq = open(filename, "r")
    output = ""
    conteudo = arq.readlines()
    seqcont = 999
    output = "True"
    
    if len(conteudo) == 0:
        output = "False"

    regexp = re.match(r'\s[0-9]+\s[0-9]+', conteudo[0])
    
    if regexp == None:
        output = "False"
    else:
        seqcont = regexp.group(0).split()[0]
        
    if seqcont < 2:
        output = "False"

    return {"PHYLIP_VALID":output}
