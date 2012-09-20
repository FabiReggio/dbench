#!/bin/bash
# This script installs mongodb in the /usr/local/bin directory
# It should be ran with ROOT PERMISSION!
wget http://fastdl.mongodb.org/linux/mongodb-linux-x86_64-2.2.0.tgz
tar -xzvf mongodb-linux-x86_64-2.2.0.tgz
cp mongodb-linux-x86_64-2.2.0/bin/* /usr/local/bin/

# delete files and folders
rm mongodb-linux-x86_64-2.2.0.tgz 
rm -rf mongodb-linux-x86_64-2.2.0

