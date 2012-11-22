#!/bin/sh
URL=http://apache.mirror.rbftpnetworks.com/lucene/solr/4.0.0/apache-solr-4.0.0.tgz

wget $URL
tar -xzvf apache-solr-4.0.0.tgz

# There really isn't any need to install Solr, you just need to execute the 
# start.jar file within Solr.
