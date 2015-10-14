import json
import pprint
import requests
from requests.auth import HTTPBasicAuth

"""
Global Constants
"""
PASSWORD = '6a00b426-1243-4d80-a059-a1973e3482fe'
USER = 'watsh.rajneesh@sjsu.edu'
HTTPS = 'false'

"""
OpenStack keystonerc-admin constants
"""
OS_AUTH_URL="http://localhost:5000/v2.0"
OS_TENANT_NAME="admin"
OS_PASSWORD="61f23b78184d4b92"
OS_USERNAME="admin"



def replace_value_with_definition(current_dict, key_to_find, definition):
    """
    This method is used to substitute the default values read from .json file
    by a value that the user specified on CLI.

    :param current_dict:
    :param key_to_find:
    :param definition:
    :return:
    """
    for key in current_dict.keys():
        if key == key_to_find:
            current_dict[key] = definition


def get_http_scheme():
    """
    Gets the http/https protocol scheme prefix for url.w
    :return:
    """
    if HTTPS.lower() == 'true':
        return "https"
    else:
        return "http"


def pretty_print_json(response):
    """
    Pretty print the response.
    """
    print("Response Headers:%s" % (response.headers))
    print("Reason:%s" % (response.reason))
    try:
        res = json.dumps(response.json(), sort_keys=True, indent=4, separators=(',', ':'))
        print("[HTTPCode:%s]JSON:" % (response.status_code))
        print(res)
    except:
        if response is not None:
            print("[HTTPCode:%s]Text:[%s]" % (response.status_code, response.text))
        else:
            print("Noresponse.")


#################### HTTP method wrapper functions ################

def get_request(url, headers=None):
    """
    GETrequest.
    :paramurl:
    :return:
    """
    print('GET ' + str(url))
    if headers is not None:
        print('Request Headers:' + str(headers))
        r = requests.get(url, auth=HTTPBasicAuth(USER, PASSWORD), headers=headers)
    else:
        r = requests.get(url, auth=HTTPBasicAuth(USER, PASSWORD))
        pretty_print_json(r)
    return r


def put_request(headers, payload, url):
    """
    PUT request.
    :param headers:
    :param payload:
    :param url:
    :return:
    """
    print('PUT ' + str(url))
    print('RequestHeaders:' + str(headers))
    pp = pprint.PrettyPrinter(indent=2)
    pp.pprint(payload)
    r = requests.put(url, auth=HTTPBasicAuth(USER, PASSWORD), data=json.dumps(payload), headers=headers)
    pretty_print_json(r)
    return r


def post_request(url, payload=None, headers=None):
    """
    POST request.
    :param url:
    :param payload:
    :param headers:
    :return:
    """
    print('POST ' + str(url))
    if payload is None and headers is None:
        r = requests.post(url, auth=HTTPBasicAuth(USER, PASSWORD))
    else:
        print('RequestHeaders:' + str(headers))
    pp = pprint.PrettyPrinter(indent=2)
    pp.pprint(payload)
    r = requests.post(url, auth=HTTPBasicAuth(USER, PASSWORD), data=json.dumps(payload), headers=headers)
    pretty_print_json(r)
    return r


def delete_request(url):
    """
    DELETE request.
    :param url:
    :return:
    """
    print('DELETE ' + str(url))
    r = requests.delete(url, auth=HTTPBasicAuth(USER, PASSWORD))
    pretty_print_json(r)
    return r


############################### REST Client functions ############################