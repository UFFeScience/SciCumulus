#!/bin/bash
java -jar %=WFDIR%/bin/readseq.jar -all -f=12 %=MAFFT_FILE% -o %=FASTA_FILE%.phylip
