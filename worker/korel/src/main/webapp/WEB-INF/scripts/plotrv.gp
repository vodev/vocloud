set output "plot03rv.png"
set title "RV solution from korel.rv"
set term png
plot "korel.rv" u 2:3 w p,"" u 2:5 w p,"" u 2:7 w p
