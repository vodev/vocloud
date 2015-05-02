#!/usr/bin/env python
# -*- coding: utf-8 -*-

# $Rev$
# $Date$
# $URL$

import re
import os
import subprocess

data = {}
data["orgfile"] = "korermap.dat"
data["gplot"] = "plotmap.gp"
data["pngfile"] = "plot02map.png"
data["resfile"] = "korel.res"

regex = re.compile(r" *el\( *[0-9]+, *[0-9]+.*=")

res = []
fo = open(data["resfile"], "r")
#res = fo.readlines()[-2:]
for line in fo.readlines():
    if (regex.match(line)):
        res.append(line)
fo.close()

data["xlab"] = res[0].split("=")[0]
data["ylab"] = res[1].split("=")[0]

# el( 0, 5)= .627401E+02 p(1.1): .477401E+02 p(1.21): .477401E+02 p(21,1): .777401E+02
# el( 0, 5)=0.627401E+02 p(1.1):0.477401E+02 p(1.21):0.477401E+02 p(21,1):0.777401E+02
numbers = res[0].split(":")
data["xmin"] = numbers[1].split("p")[0].strip()
data["xmax"] = numbers[-1].strip()

numbers = res[1].split(":")
data["ymin"] = numbers[1].split("p")[0].strip()
data["ymax"] = numbers[2].split("p")[0].strip()

data["xrange"] = float(data["xmax"]) - float(data["xmin"])
data["yrange"] = float(data["ymax"]) - float(data["ymin"])

gplot = """
# gnuplot driver for plotting korermap.dat
set xlabel '%(xlab)s'
set ylabel '%(ylab)s'
x0=%(xmin)s
y0=%(ymin)s
dx=%(xrange)s/20.0
dy=%(yrange)s/20.0
set key off
set autoscale fix
set term png size 840, 600 crop
set output "%(pngfile)s"
set title "Map of (O-C)^2 in parameter space"
set contour both
set cntrparam level 20
#set cntrparam level discrete 620
#unset surface
set table "contour.dat"
#set size square
#set view
#set dgrid3d 100, 100, 5
splot "%(orgfile)s" matrix u (x0+dx*$1):(y0+dy*$2):3
unset table
!awk "NF<2{printf\\"\\n\\"}{print}" <contour.dat >>contour1.dat
reset
set autoscale fix
set term png size 840, 600 crop
set key off
set title "Map of  (O-C)^2  in parameter space"
#set palette gray
##set palette gamma 0.5
set pm3d map
set xlabel '%(xlab)s'
set ylabel '%(ylab)s'
set pm3d explicit
splot "%(orgfile)s" matrix u (x0+dx*$1):(y0+dy*$2):3 w pm3d, "contour1.dat" w line lt -1
#splot [9:11] [9:11] file matrix u (x0+dx*$1):(y0+dy*$2):3
#splot file matrix w lines
#reset
!rm "contour1.dat"
!rm "contour.dat"
reset
""" % (data)

fo = open(data["gplot"], "w")
fo.write(gplot)
fo.close()

subprocess.call(["gnuplot", data["gplot"]])
