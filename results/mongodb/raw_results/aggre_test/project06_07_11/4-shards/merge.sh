#!/bin/sh
FILE="aggre_results_mp.csv"
echo "objects,user mentions (ms),hash tags (ms),shared urls (ms)" > $FILE
head -2 aggre_results_1_map_reduce.csv | tail -1 >> $FILE
head -2 aggre_results_2_map_reduce.csv | tail -1 >> $FILE
head -2 aggre_results_3_map_reduce.csv | tail -1 >> $FILE
head -2 aggre_results_4_map_reduce.csv | tail -1 >> $FILE
head -2 aggre_results_5_map_reduce.csv | tail -1 >> $FILE
