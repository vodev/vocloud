#! /usr/bin/env bash

# Plotting the VO-KOREL output files
# Author: Petr Skoda <skoda@sunstel.asu.cas.cz>
# 13.7.2011


orgfile="korel.o-c"
procfile="proc_korel.o-c"
gplot="ploto-c.gp"
pngfile="plot06o-c.png"

echo "# gnuplot driver for plotting korel.dat">$gplot
echo "set key off" >> $gplot
echo "set autoscale fix" >> $gplot
echo "set term png" >> $gplot
echo "set output \"$pngfile\"" >> $gplot
echo 'set title "Residual spectra from korel.o-c"'>>$gplot
echo 'set xlabel "Wavelength (A)"'>>$gplot
echo 'set ylabel "Intensity"'>>$gplot
echo 'offset=0.05' >> $gplot
echo 'scale=2' >> $gplot


# isolates the line with jd,lam0,deltarv,weight,numpoint
# remembers variable, prepares converted file and gnuplot script
# separates spectra by double newline as gnuplot requires
cat $orgfile | awk \
'/\ *[0-9]*\.[0-9]*\ +[0-9]*\.[0-9]*\ +[0-9]*\.[0-9]*\ +[0-9]*\.[0-9]*\
\ +[0-9]*\ *$/ {jd=$1;lam0=$2;dv=$3;\
print "lam0=",lam0,"\n",\
"dv=",dv >> "'$gplot'";\
print "\n\n#"$0;next}{ for (i=1; i<=NF; i++) print $i}'>"$procfile"
# {print$0}'
#> "$procfile"
echo "plot \"$procfile\" u \
((1+dv/299792.4562)**\$0*lam0):(scale*\$1-offset*column(-2)) w l \
lt rgb \"red\" ">>"$gplot"
gnuplot "$gplot"
rm "$gplot"
rm "$procfile"
