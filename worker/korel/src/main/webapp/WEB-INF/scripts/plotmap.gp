set term png
set output "plot02map.png"
file="korermap.dat"
dx=1.5
dy=0.02
x0=47.74
y0=0.267
set key off
set contour both
set cntrparam level 20
#set cntrparam level discrete 620
#unset surface
set table "contour.dat"
#set size square
#set view 
#set dgrid3d 100, 100, 5
splot file matrix u (x0+dx*$1):(y0+dy*$2):3 
unset table
!awk "NF<2{printf\"\n\"}{print}" <contour.dat >contour1.dat
reset
set key off
set title "Map of  (O-C)^2  in parameter space"
#set palette gray
##set palette gamma 0.5
set pm3d map
set pm3d explicit
set xlabel "el( 0, 5)"
set ylabel "el( 0, 6)"
splot file matrix u (x0+dx*$1):(y0+dy*$2):3 w pm3d, 'contour1.dat' w line lt\
-1
#splot [9:11] [9:11] file matrix u (x0+dx*$1):(y0+dy*$2):3 
#splot file matrix w lines
#reset 

