#! /usr/bin/env bash

KOREL_BIN="$(dirname $(readlink -f ${BASH_SOURCE[0]}))"

#
# Author: Petr Skoda <skoda@sunstel.asu.cas.cz>
#
gnuplot ${KOREL_BIN}/plotrv.gp 
