######################################################
# 
# Sample code to illustrate the use of the OpenStack
# REST API
# 
# TKH 8/23/2014
#
######################################################

############# These libraries are required ###########
import urllib
import urllib2
import json

############### Configuration Section ################
############### Enter your own values ################
# Obtain your OpenStack password from file 
# /root/keystonerc_admin
OSpassword= "c309b26a3ddf4822"


# Obtain your own tenant id for tenant "admin" by running command
#    keystone tenant-list 
# on the OpenStack server
tenantid = "034370b2e765495a8d779ad76ac919b3"
 
# OpenStack server address   
# Can be IP address or DNS name
hostIP="130.35.158.165"

# We are user admin and tenant admin. 
tenantname="admin"
username=  "admin"

############### OpenStack API ports ########
# Make sure that these ports are open in the OpenStack server
# and that VirtualBox Port Forwarding (if used) is properly set
KEYSTONEport     =  "5000"
NOVAport         =  "8774"
NOVA_EC2port     =  "8773"
CINDERport       =  "8776"
CEILOMETERport   =  "8777"
GLANCEport       =  "9292"
NEUTRONport      =  "9696"
KEYSTONE_ADMport = "35357"


################## Sample code logic starts here #######################################
print
print
print"---------- Obtain authorization token from Keystone -----"

#### Build the headers
headers = {
          'Content-Type'   :   'application/json',
          'Accept'         :   'application/json'
#           'X-Auth-Token'   :    mytoken
           }
print "Headers: ",headers
print"--------------------------------------------------------------------------"

#### Build the URL
CMDpath="/v2.0/tokens"
APIport=KEYSTONEport
url="http://"+hostIP+":"+APIport+CMDpath
print "URL:     ",url

#### Build the body
body='{"auth":{"tenantName":"'+tenantname+'","passwordCredentials":{"username":"'+username+'","password":"'+OSpassword+'"}}}'
print "BODY:    ",body

#### Send the  POST request
req = urllib2.Request(url, body, headers)

#### Read the response
response = urllib2.urlopen(req).read()

# Uncomment to see the raw response
# print "RESPONSE:   ",response

#### Convert to JSON format
decoded = json.loads(response.decode('utf8'))
# Uncomment to see response in JSON format
# print "JSON:   ",decoded

#### Make it look pretty and indented
pretty = json.dumps(decoded,sort_keys=True,indent=3)
# Uncomment to see response in pretty JSON format
# print "PRETTY: ",pretty 

#### Parse JSON formatted data for token expiration date
expires = decoded['access']['token']['expires']
# Uncomment to see token expiration date
# print "TOKEN EXPIRES:  ",expires

#### Parse JSON formatted data for the token ID
mytoken = decoded['access']['token']['id']
# Uncomment to the full token ID (can be long!)
# print "TOKEN:",mytoken


############## List the NOVA API-v2 details, just for fun #######
print
print
print "---------------List of NOVA API-v2 details ---------------"
headers = {
          'Content-Type'   :   'application/json',
          'Accept'         :   'application/json',
          'X-Auth-Token'   :    mytoken
           }

#### Build the URL
CMDpath="/v2"
APIport=NOVAport
url="http://"+hostIP+":"+APIport+CMDpath
print "URL:     ",url



#### Send the GET request
# Note that the second parameter which normally carries the body data 
# is "None", making the request a "GET" instead of a "POST"
req = urllib2.Request(url,None, headers)

#### Read the response
response = urllib2.urlopen(req).read()

# Uncomment to see the raw response
# print "RESPONSE:   ",response

#### Convert to JSON format
decoded = json.loads(response.decode('utf8'))
# Uncomment to see response in JSON format
# print "JSON:   ",decoded

#### Make it look pretty and indented
pretty = json.dumps(decoded,sort_keys=True,indent=3)
# Uncomment to see response in pretty JSON format
# print "PRETTY: "
# print pretty





############## Get list of NOVA servers #####################
print
print
print "--------------- List of NOVA servers ---------------"
headers = {
          'Content-Type'   :   'application/json',
          'Accept'         :   'application/json',
          'X-Auth-Token'   :    mytoken
           }


#### Build the URL

APIport=NOVAport
CMDpath="/v2/"+tenantid+"/servers"
url="http://"+hostIP+":"+APIport+CMDpath
print "URL:     ",url


#### Send the GET request
# Note that the second parameter which normally carries the body data
# is "None", making the request a "GET" instead of a "POST"
req = urllib2.Request(url,None, headers)


#### Read the response
response = urllib2.urlopen(req).read()

# Uncomment to see the raw response
# print "RESPONSE:   ",response
print
print
print

#### Convert to JSON format
decoded = json.loads(response.decode('utf8'))
# Uncomment to see response in JSON format
# print "JSON:   ",decoded

#### Make it look pretty and indented
pretty = json.dumps(decoded,sort_keys=True,indent=3)
# Uncomment to see response in pretty JSON format
# print "PRETTY: "
# print pretty




############# STOP here for now ##########################
############ Remove the quit() command during the demo ###
quit()

############## Create a new network  #####################
print
print
print "--------------- Create new network ---------------"
headers = {
          'Content-Type'   :   'application/json',
          'Accept'         :   'application/json',
          'X-Auth-Token'   :    mytoken
           }

#### Build the URL
CMDpath="/v2.0/networks"
APIport=NEUTRONport
url="http://"+hostIP+":"+APIport+CMDpath
print "URL:     ",url

#### Build the body
body='{"network": {"name": "CMPEnet-01","admin_state_up": true}}'
print "BODY:    ",body

#### Send the POST request
req = urllib2.Request(url, body , headers)

#### Read the response
response = urllib2.urlopen(req).read()

# Uncomment to see the raw response
# print "RESPONSE:   ",response

#### Convert to JSON format
decoded = json.loads(response.decode('utf8'))
# Uncomment to see response in JSON format
# print "JSON:   ",decoded

#### Make it look pretty and indented
pretty = json.dumps(decoded,sort_keys=True,indent=3)
# Uncomment to see response in pretty JSON format
print "PRETTY: "
print pretty

