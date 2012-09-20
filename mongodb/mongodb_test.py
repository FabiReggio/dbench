#!/usr/bin/env python
import json
import time
import csv

import pymongo
""" 
MongoDB Tests 
"""
__author__ = "Chris Choi"
__contact__ = "chutsu at gmail.com"

# global variables
repeat = 5  # number of times to repeat for each test
db_name = "test"
collection_name = "test_collection"
data_file = "../../twitter_data/100meters.json.test"
# data_file = "../../twitter_data/100meters.json"
header = [
    "n",
    "insert (s)",
    "remove (s)"
]


def db_prep(db_name):
    """Prep MongoDB
    Args:
        db_name (str): database name
    Returns:
        collection object (equiv to a table in a RDBMS)
    """
    connection = pymongo.Connection() 
    dbconn = connection[db_name]
    collection = dbconn[collection_name]
    return collection 

def load_data(file):
    """Load JSON Data
    Args:
        file (str): file path
    Returns:
        JSON data
    """
    with open(file, "rb") as f:
        data = json.load(f)
        return data

def init_results(file, header):
    """Initiates a test result file to be written
    Args:
        file (str): file path to write the results
        header (array): list of header objects 
    """
    with open(file, "wb") as csv_file:
        csv_writier = csv.writer(csv_file, delimiter = ",")
        csv_writer.writerow(header)
        csv_file.close()

def log_result(file, row):
    """Log result to the results csv file
    Args:
        file (str): file path to write the results
        row (array): list of objects to write into row
    """
    with open(file, "a") as csv_file:
        csv_writier = csv.writer(csv_file, delimiter = ",")
        csv_writer.writerow(row)

def test_insert(collection, data, repeat = 1):
    """Tests MongoDB's insert performance
    Description:
        the test function simply takes the json data and inserts different
        percentages of the data into the MongoDB. For example if data has
        100 entries, the test function will insert 10% then 20%, 30%, and 
        so on until it reaches 100%

        The time taken for each insert is recorded
    Args:
        collection (collection obj): collection
        data (JSON): JSON data to be inserted
        repeat (int): repeat n times 
    """
    size = len(data)
    for x in range(1, repeat + 1):
        print "Run Number %i:" % (x)
        # insert a range of different sizes
        for i in range(1, 11): # from 1 to 10
            # preparation
            start_time = time.time()
            objs = int(size * (0.1 * i)) # obj to be inserted

            # execution
            collection.insert(data[0: objs])
            time_taken = time.time() - start_time 
            print "Insert Time [n = %i]: %s seconds" % (objs, time_taken)

            # clean up
            test_remove(collection, objs)

def test_remove(collection, objects):
    """Tests MongoDB's remove performance
    Description:
        the test function records the time taken to remove all entries in
        the collection
    Args:
        collection (collection obj): collection
        objects (int): number of objects to be removed
    """
    # preparation
    start_time = time.time()

    # execution
    collection.remove({}) # remove all documents in collection
    time_taken = time.time() - start_time
    print "Remove Time [n = %i]: %s seconds" % (objects, time_taken)

if __name__ == "__main__":
    collection = db_prep(db_name)
    data = load_data(data_file)

    print "Testing MongoDB's I/O performance"
    test_insert(collection, data, repeat) 
    # test_remove is included at end of each insert test 
    
    # for c in collection.find():
    #     print c["_id"]
        

