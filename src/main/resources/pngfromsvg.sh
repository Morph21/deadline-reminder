#!/bin/bash

set -ex

svg=$1

size=(16 24 32 48 64 72 96 128 144 152 192 196 256 512 1024 2048)

folder=temp

out="$(rm $folder -f -R)"
out="$(mkdir $folder)"

out=$folder

echo Making bitmaps from your svg...

for i in ${size[@]}; do
  inkscape $svg -e="$out/$i.png" -w$i -h$i --without-gui
done

echo done
