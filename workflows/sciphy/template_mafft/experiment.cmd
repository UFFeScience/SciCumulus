#!/bin/bash
perl %=WFDIR%/bin/numberFasta.pl %=FASTA_FILE% > `basename %=FASTA_FILE%`.fastaNumbered
/usr/local/bin/mafft `basename %=FASTA_FILE%`.fastaNumbered > `basename %=FASTA_FILE%`.mafft

