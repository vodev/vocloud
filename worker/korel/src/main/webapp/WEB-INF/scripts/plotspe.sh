#! /usr/bin/env bash

KOREL_BIN="$(dirname $(readlink -f ${BASH_SOURCE[0]}))"

#
# Author: Petr Skoda <skoda@sunstel.asu.cas.cz>
#

cat "korel.spe" | awk \
'{print $0;count=NF}\
END {print "# number of spectra is", count-1}' >"dat"
#
nsp=$(awk '/spectra/ {print $6}'< "dat")
for i in $(seq 1 $nsp) 
do
echo "component $i"
(echo "set title \"Disentangled spectrum of component $i\""
echo 'set xlabel "Wavelength [A]"'
echo 'set ylabel "relative intensity (offset from continuum) "'
echo 'set term png'
echo 'set key off'
echo "set output \"component$i.png\""
i=$[i+1]
echo "plot \"dat\"  using 1:$i with lines lt 2 lw 3") >temp.gp
gnuplot temp.gp
done

rm temp.gp
rm dat
