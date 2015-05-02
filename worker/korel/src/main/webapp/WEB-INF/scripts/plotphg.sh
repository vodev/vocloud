#! /usr/bin/env bash

KOREL_BIN="$(dirname $(readlink -f ${BASH_SOURCE[0]}))"

#
# Author: Petr Skoda <skoda@sunstel.asu.cas.cz>
#
sed -i '/end/d' phg.ps
convert phg.ps plot01phg.png
