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
# cd to this directory (bootstrap)
# finally, run ./build.sh



# build bootstrap to target folder "bootstrap"
rm -r bootstrap
make bootstrap

# copy built css to app destination
rm -f -r ../src/main/webapp/css/vendor/bootstrap
mkdir ../src/main/webapp/css/vendor/bootstrap
cp bootstrap/css/*.* ../src/main/webapp/css/vendor/bootstrap

# copy built js to app destination
rm -f -r ../src/main/webapp/js/vendor/bootstrap
mkdir ../src/main/webapp/js/vendor/bootstrap
cp bootstrap/js/*.* ../src/main/webapp/js/vendor/bootstrap
