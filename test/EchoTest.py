import requests

url = 'http://localhost:9000/yamlbackup'
response = requests.get(url)
print response