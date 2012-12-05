#!/bin/sh
FILE="aggre_results_mp.csv"
echo "objects,user mentions (ms),hash tags (ms),shared urls (ms)" > $FILE
head -2 aggre_results_1_mp.csv | tail -1 >> $FILE
head -2 aggre_results_2_mp.csv | tail -1 >> $FILE
head -2 aggre_results_3_mp.csv | tail -1 >> $FILE
head -2 aggre_results_4_mp.csv | tail -1 >> $FILE
head -2 aggre_results_5_mp.csv | tail -1 >> $FILE
