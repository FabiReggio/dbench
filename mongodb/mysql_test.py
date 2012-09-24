#!/usr/bin/env python
import json
import time
import csv

import MySQLdb as mysql 
""" 
MySQL Tests
"""
__author__ = "Chris Choi"
__contact__ = "chutsu at gmail.com"

# global variables
repeat = 5  # number of times to repeat for each test

db_host = "localhost"
db_user = ""
db_pass = ""
db_name = "test"
table_name = "test_table"

# data_file = "../../twitter_data/100meters.json.test"
data_file = "../../twitter_data/100meters.json"
io_results_file = "./io_results_mysql.dat"
header = [
    "n",
    "insert (s)",
    "remove (s)"
]


def db_prep(db_host, db_user, db_pass, db_name, test_table):
    """Prep MySQL 
    Args:
        db_host (str): database host
        db_user (str): username
        db_pass (str): password
        db_name (str): database name
        test_table (str): table name
    Returns:
        database connection object
    """
    dbconn = ""
    try:
        dbconn = mysql.connect(db_host, db_user, db_pass, db_name)
    except mysql.Error, e:
        sys.stderr.write("Error: cannot connect to database!\n")
        sys.stderr.write(str(e) + "\n")
    return dbconn 

def db_close(dbconn):
    """Close database connection
    Args:
        dbconn (db connection obj): database connection
    """
    dbconn.close()

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
        csv_writer = csv.writer(csv_file, delimiter = ",")
        csv_writer.writerow(header)
        csv_file.close()

def log_result(file, row):
    """Log result to the results csv file
    Args:
        file (str): file path to write the results
        row (array): list of objects to write into row
    """
    with open(file, "a") as csv_file:
        csv_writer = csv.writer(csv_file, delimiter = ",")
        csv_writer.writerow(row)
        csv_file.close()

def test_insert(collection, data):
    """Tests MongoDB's insert performance
    Description:
        this test function records the time taken to insert entries in
        the collection
    Args:
        dbconn (database connection obj): database connection
        data (JSON): JSON data to be inserted
    """
    start_time = time.time()
    collection.insert(data)
    time_taken = time.time() - start_time 
    print "Insert Time: %s seconds" % (time_taken)
    return time_taken

def test_remove(collection, objects):
    """Tests MongoDB's remove performance
    Description:
        this test function records the time taken to remove all entries in
        the collection
    Args:
        dbconn (database connection obj): database connection
        objects (int): number of objects to be removed
    """
    start_time = time.time()
    collection.remove({}) # remove all documents in collection
    time_taken = time.time() - start_time
    print "Remove Time: %s seconds" % (time_taken)
    return time_taken

def test_find(collection, objects):
    """Tests MongoDB's find performance
    Description:
        this test function records the time taken to find entries in
        the collection
    Args:
        dbconn (database connection obj): database connection
        objects (int): number of objects to be removed
    """

def test_io_runner(dbconn, data, repeat, results_file):
    """Test Runner
    Args:
        dbconn (database connection obj): database connection
        data (JSON): JSON data to be inserted
        repeat (int): repeat n times 
        results_file (string): file path to the results file
    """
    size = len(data)
    for x in range(1, repeat + 1):
        print "Run Number %i:" % (x)
        # insert a range of different sizes
        for i in range(1, 11): # from 1 to 10
            # prep 
            objs = int(size * (0.1 * i)) # obj to be inserted

            # tests
            insert_time = test_insert(collection, data[0: objs]) 
            remove_time = test_remove(collection, data[0: objs])

            # clean up 
            row = [objs, insert_time, remove_time]
            log_result(results_file, row)

if __name__ == "__main__":
    dbconn = db_prep(db_name)
    data = load_data(data_file)

    print "Testing MongoDB's I/O performance"
    init_results(io_results_file, header)
    test_io_runner(dbconn, data, repeat, io_results_file)
