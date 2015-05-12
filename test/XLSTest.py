import requests

url = 'http://localhost:9000/empty'
response = requests.get(url)
print response

url = 'http://localhost:9000/init'
response = requests.get(url)
print response

url = 'http://localhost:9000/update'
response = requests.get(url)
print response