from pymongo import MongoClient

#connect to database
connection = MongoClient('localhost', 27017)

db = connection.test

#handle to names collections
names = db.names

item = names.find_one()

if item:
    print (item['name'])
else:
    print "No item found!"