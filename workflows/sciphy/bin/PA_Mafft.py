#!/usr/bin/python

def validateMafftFile(self, input):
    """
    validateMafftFile - Method for mafft file format testing
                        Responsible for the IsValid tag

    """ 
    filename = input+".mafft"
    arq = open(filename, "r")
    output = ""
    conteudo = arq.readlines()
    seqcont = 0
    output = "True"
    
    if len(conteudo) == 0:
        output = "False"
    for linha in conteudo:
        if linha[0] == ">":
            seqcont = seqcont + 1
        else:
            linha = linha.strip()
            for char in linha:
                if not char.isupper() and not char == "-":
                    output = "False"
    if seqcont < 2:
        output = "False"

    return {"MAFFT_VALID":output} 
