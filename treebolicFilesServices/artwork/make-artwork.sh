#!/bin/bash

source "../../../make-artwork-lib.sh"

launch="ic_launcher.svg ic_launcher_round.svg"
web="ic_launcher.svg"

icon="ic_treebolic.svg"
splash="ic_splash.svg"

make_mipmap "${launch}" 48
make_app "${web}" 512

make_res "${icon}" 48
make_res "${splash}" 144

