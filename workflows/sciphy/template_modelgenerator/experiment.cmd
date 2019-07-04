#!/bin/bash
java -jar %=WFDIR%/bin/modelgenerator.jar %=FASTA_FILE%.phylip 6 > %=FASTA_FILE%.mg
python %=WFDIR%/bin/clean_modelgenerator.py %=FASTA_FILE%.mg
