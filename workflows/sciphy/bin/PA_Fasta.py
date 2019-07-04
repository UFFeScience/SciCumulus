#!/usr/bin/python

def validateFastaFile(self, input):
    """
    validateMafftFile - Method for mafft file format testing
                        Responsible for the IsValid tag

    """ 
    arq = open(input, "r")
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
                if not char.isupper():
                    output = "False"
                    
    if seqcont < 2:
        output = "False"

    return {"FASTA_VALID":output}

def extractMultiFasta(self, input):

    number = 10
    arq = open(input, "r")
    lines = arq.readlines()
    arq.close()

    tags = []
    #output = []
    output = {}
    strtemp = ""
    cont = 0
    for x in lines:
        if ">" in x:
            desc = ""
            strtemp = ""
            cont = cont + 1
            tagtitle = "FASTA"+str(cont)
              
            temp = x.split()
            temp2 = temp[0]
            
            output.update({tagtitle+"_"+"ID":temp2.split("|")[0][1:]})
            output.update({tagtitle+"_"+"ORIGIN_ID":temp2.split("|")[1].split(".")[0]})
            output.update({tagtitle+"_"+"VAL_ID":temp2.split("|")[1].split(".")[1]})
            for y in temp[1:]:
                desc = desc + y + " "
            output.update({tagtitle+"_"+"DESC":desc[:-1]})            
        else:
            if strtemp == "":
                strtemp = x.strip()
                output.update({tagtitle+"_"+"SEQUENCE":strtemp})
            else:
                strtemp = x.strip()
                output.update({tagtitle+"_SEQUENCE":output[tagtitle+"_SEQUENCE"]+strtemp})
                
    return output
