#!/bin/bash

# prereqs to build... npm, etc  (on mac)
# =======================================
# brew install node
# make sure make is installed - mac instructions: http://stackoverflow.com/questions/10265742/how-to-install-make-and-gcc-on-a-mac
# git clone http://github.com/isaacs/npm.git
# cd npm
# sudo make install
# now you have node package manager (npm)
# sudo npm install less -g
# sudo npm install uglify-js -g
# sudo npm install recess -g
# ... also npm install grunt and jslint
# install ruby gem and then gem install hekyll

# cd to this directory (bootstrap)
# finally, run ./build.sh

# build bootstrap to target folder "bootstrap"
npm install
rm -i -r -f dist
mkdir dist
grunt dist

# copy built css to app destination
rm -f -r ../ui/css/vendor/bootstrap
mkdir ../ui/css/vendor/bootstrap
cp dist/css/*.* ../ui/css/vendor/bootstrap

# copy built js to app destination
rm -f -r ../ui/js/vendor/bootstrap
mkdir ../ui/js/vendor/bootstrap
cp dist/js/*.* ../ui/js/vendor/bootstrap

# copy fonts to app destination
rm -f -r ../ui/fonts
mkdir ../ui/fonts
cp fonts/*.* ../ui/fonts
