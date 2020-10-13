#!/bin/bash

set -ex

svg=$1

size=(16 24 32 48 64 72 96 128 144 152 192 196 256 512)

folder=temp

out="$(rm $folder -f -R)"
out="$(mkdir $folder)"

out=$folder

echo Making bitmaps from your svg...

for i in ${size[@]}; do
  inkscape $svg -e="$out/$i.png" -w$i -h$i --without-gui
done

echo Compressing...

## Replace with your favorite (e.g. pngquant)
optipng -o7 temp/*.png
#pngquant -f --ext .png $out"/*.png" --posterize 4 --speed 1

echo Converting to favicon.ico...

convert temp/*.png favicon.ico

# Clean-up
rm temp -R

echo Done
