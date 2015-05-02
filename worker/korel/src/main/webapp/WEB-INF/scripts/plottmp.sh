#! /usr/bin/env bash

# Plotting the VO-KOREL output files
# Author: Petr Skoda <skoda@sunstel.asu.cas.cz>
# 13.7.2011


orgfile="korel.tmp"
procfile="proc_korel.tmp"
gplot="plottmp.gp"
pngfile="plot05tmp.png"

if [ $(stat --printf=%s $orgfile) -eq 0 ]; then
    echo "Ignoring empty $orgfile"
    exit
fi

echo "# gnuplot driver for plotting korel.dat">$gplot
echo "set key off" >> $gplot
echo "set autoscale fix" >> $gplot
echo "set term png" >> $gplot
echo "set output \"$pngfile\"" >> $gplot
echo 'set title "Input template from korel.tmp"'>>$gplot
echo 'set xlabel "Wavelength (A)"'>>$gplot
echo 'set ylabel "Normalized intensity"'>>$gplot
echo 'offset=0.05' >> $gplot
echo 'scale=1' >> $gplot


# isolates the line with jd,lam0,deltarv,weight,numpoint
# remembers variable, prepares converted file and gnuplot script
# separates spectra by double newline as gnuplot requires
cat $orgfile | awk \
'/\ *[0-9]*\.[0-9]*\ +[0-9]*\.[0-9]*\ +[0-9]*\.*[0-9]*\
\ *$/ {lam0=$1;dv=$2;\
print "lam0=",lam0,"\n",\
"dv=",dv >> "'$gplot'";\
print "#"$0;next}{print $0}'>"$procfile"
# {print$0}'
#> "$procfile"
echo "plot \"$procfile\" u \
((1+dv/299792.4562)**\$0*lam0):(\$1) w l lw 2 lt rgb \
\"blue\" ">>"$gplot"
gnuplot "$gplot"
rm "$gplot"
rm "$procfile"
