#!/usr/bin/python

def validateRaxmlModels(self, input):
    """
    validateMafftFile - Method for modelgenerator format testing
                        Responsible for the MG_MODEL, MG_MODEL_VALID tags

    """ 
    filename=input+".mg.modelFromMG.txt"
    arq = open(filename, "r")
    output = ""
    conteudo = arq.readlines()
    seqcont = 0
    output = "True"
    modelogerado = ""
    listamodelos = ['DAYHOFF', 'DCMUT', 'JTT', 'MTREV', 'WAG', 'RTREV', 'CPREV',
                   'VT', 'BLOSUM62', 'MTMAM', 'MTART', 'MTZOA', 'LG', 'PMB',
                   'HIVB', 'HIVW', 'JTTDCMUT', 'FLU','GTR']

    if len(conteudo) == 0 or len(conteudo) > 1:
        output = "False"
    else:
        modelogerado = conteudo[0].strip()
        if modelogerado.upper() not in listamodelos:
            print "Teste"
            output = "False"    
        
    return {"MG_MODEL":modelogerado, "MG_MODEL_VALID":output} 
