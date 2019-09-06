#!/bin/bash

source "./lib-artwork.sh"

launch="ic_launcher.svg ic_launcher_round.svg"
app="ic_launcher.svg"
logo="logo_app.svg"

icon="ic_treebolic.svg"
splash="ic_splash.svg"
action="ic_action_*.svg"

make_mipmap "${launch}" 48
make_app "${launch}" 512
make_res "${logo}" 48

make_res "${icon}" 48
make_res "${splash}" 144
make_res "${action}" 24

check_dir "${dirres}"

