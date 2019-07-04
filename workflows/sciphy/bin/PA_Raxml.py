#!/usr/bin/python
import os

def validatePhylogeneticTreeFile(self, input):
    """
    validatePhylogeneticTreeFile - Method for tree file verification
                                   Responsible for the PHYLOGENETIC_TREE_VALID tag

    """
    
    output = "True"
    nomearq = input.split("/")[-1]
    arquivo = "RAxML_bipartitions."+nomearq+".phylip_tree3.BS_TREE"
    if os.path.isfile(arquivo) == False:
        output = "False"
    else:
        arq = open(arquivo, "r")
        conteudo = arq.readlines()    
        if len(conteudo) == 0:
            output = "False"        

    return {"PHYLOGENETIC_TREE_VALID":output}
