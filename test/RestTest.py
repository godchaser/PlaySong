import json
import requests
from pprint import pprint

with open('jsondata') as data_file:
    jsonobject = json.load(data_file)
pprint(jsonobject)
url = 'http://localhost:9000/generatesongbook'
payload = jsonobject
headers = {'content-type': 'application/json'}

response = requests.post(url, data=json.dumps(payload), headers=headers)
print response
