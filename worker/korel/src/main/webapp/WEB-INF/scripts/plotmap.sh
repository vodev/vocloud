#! /usr/bin/env bash

KOREL_BIN="$(dirname $(readlink -f ${BASH_SOURCE[0]}))"

# Plotting the VO-KOREL output files
# Author: Petr Skoda <skoda@sunstel.asu.cas.cz>>
# 13.7.2011


#orgfile="korermap.dat"
#procfile="proc_korermap.dat"
#gplot="plotmap.gp"
#pngfile="plot02map.png"
#resfile="korel.res"
#
#cat "$resfile" |awk \
#'/\ *el\(\ *[0-9]+,\ *[0-9]+.*=/' > "korermap.par"
#xlab=$(head -n1 korermap.par|awk 'BEGIN{FS="="};{print $1}')
#ylab=$(tail -n1 korermap.par|awk 'BEGIN{FS="="};{print $1}')
#xmin=$(head -n1 korermap.par|awk '{print $6}')
#xmax=$(head -n1 korermap.par|awk '{print $10}')
#ymin=$(tail -n1 korermap.par|awk '{print $6}')
#ymax=$(tail -n1 korermap.par|awk '{print $8}')
#echo "# gnuplot driver for plotting korermap.dat">"$gplot"
#echo "set xlabel '$xlab'" >> "$gplot"
#echo "set ylabel '$ylab'" >> "$gplot"
#echo "x0=$xmin" >> "$gplot"
#echo "y0=$ymin" >> "$gplot"
#echo "dx=($xmax-$xmin)/20.0" >> "$gplot"
#echo "dy=($ymax-$ymin)/20.0" >> "$gplot"
#echo "set key off" >> "$gplot"
#echo "set autoscale fix" >> "$gplot"
#echo "set term png size 840, 600 crop" >> "$gplot"
#echo "set output \"$pngfile\"" >> "$gplot"
#echo 'set title "Map of (O-C)^2 in parameter space"'>>"$gplot"
#echo 'set contour both'>>"$gplot"
#echo 'set cntrparam level 20'>>"$gplot"
#echo '#set cntrparam level discrete 620'>>"$gplot"
#echo '#unset surface'>>"$gplot"
#echo 'set table "contour.dat"'>>"$gplot"
#echo '#set size square'>>"$gplot"
#echo '#set view '>>"$gplot"
#echo '#set dgrid3d 100, 100, 5'>>"$gplot"
#echo "splot \"$orgfile\" matrix u (x0+dx*\$1):(y0+dy*\$2):3 ">>"$gplot"
#echo 'unset table'>>"$gplot"
#echo '!awk "NF<2{printf\"\n\"}{print}" <contour.dat >>contour1.dat'>>"$gplot"
#echo 'reset'>>"$gplot"
#echo "set autoscale fix" >> "$gplot"
#echo "set term png size 840, 600 crop" >> "$gplot"
#echo 'set key off'>>"$gplot"
#echo 'set title "Map of  (O-C)^2  in parameter space"'>>"$gplot"
#echo '#set palette gray'>>"$gplot"
#echo '##set palette gamma 0.5'>>"$gplot"
#echo 'set pm3d map'>>"$gplot"
#echo "set xlabel '$xlab'" >> "$gplot"
#echo "set ylabel '$ylab'" >> "$gplot"
#echo 'set pm3d explicit'>>"$gplot"
#echo "splot \"$orgfile\" matrix u (x0+dx*\$1):(y0+dy*\$2):3 w pm3d, \"contour1.dat\" \
#w line lt -1">>"$gplot"
#echo '#splot [9:11] [9:11] file matrix u (x0+dx*$1):(y0+dy*$2):3 '>>"$gplot"
#echo '#splot file matrix w lines'>>"$gplot"
#echo '#reset '>>"$gplot"
#
#echo '!rm "contour1.dat"' >> "$gplot"
#echo '!rm "contour.dat"' >> "$gplot"
#echo 'reset' >> "$gplot" 
#gnuplot "$gplot"
#rm "korermap.par"

${KOREL_BIN}/plotmap.py
