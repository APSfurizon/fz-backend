import requests
from requests import Response
from requests.auth import HTTPBasicAuth

BASE_URL = "http://localhost:8081/"
BASE_URL_API = f"{BASE_URL}api/v1/"

import random
import string

def generate_random_string(length):
    letters = string.ascii_letters + string.digits
    return ''.join(random.choice(letters) for i in range(length))

RANDOM_MAIL = True
ACCOUNT_EMAIL = (generate_random_string(10) if RANDOM_MAIL else "dkopasdkopsadosa") + "@keysmasher.femboyyyyy.it"
ACCOUNT_PWD = "A1b2C3d5!"

print(f"ACCOUNT_EMAIL = '{ACCOUNT_EMAIL}'")
print(f"ACCOUNT_PWD = '{ACCOUNT_PWD}'")
print()

ACCOUNT_EMAIL = 'zXWsqWIoLS@keysmasher.femboyyyyy.it'
ACCOUNT_PWD = 'A1b2C3d5!'

HEADERS = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:133.0) Gecko/20100101 Firefox/133.0',
    'Accept': '*/*',
    'Accept-Language': 'it-IT,it;q=0.8,en-US;q=0.5,en;q=0.3',
    'Referer': 'http://localhost:3000/',
    #'content-type': 'application/json',
    'Origin': 'http://localhost:3000',
    'Connection': 'keep-alive'
}

session = requests.session()

def doPost(url, json=None, data=None, auth=None, files=None) -> Response:
    global session
    global HEADERS
    response = session.post(url, headers=HEADERS, json=json, data=data, allow_redirects=False, auth=auth, files=files)
    print(f"\n-------- POST {url} --------")
    print(response.status_code)
    print(response.cookies.items())
    print(response.headers)
    print(response.text)
    return response
def doGet(url, auth=None) -> Response:
    global session
    global HEADERS
    response = session.get(url, headers=HEADERS, allow_redirects=False, auth=auth)
    print(f"\n-------- GET {url} --------")
    print(response.status_code)
    print(response.cookies.items())
    print(response.headers)
    print(response.text)
    return response
def doDelete(url) -> Response:
    global session
    global HEADERS
    response = session.delete(url, headers=HEADERS, allow_redirects=False)
    print(f"\n-------- GET {url} --------")
    print(response.status_code)
    print(response.cookies.items())
    print(response.headers)
    print(response.text)
    return response

def register() -> Response:
    json = {
        "email": ACCOUNT_EMAIL,
        "password": ACCOUNT_PWD,
        "fursonaName": "Pisnello",
        "personalUserInformation": {
            "firstName": "Luca",
            "lastName": "Stracc",
            "birthCity": "Rome",
            "birthRegion": "RM",
            "birthCountry": "IT",
            "birthday": "2002-06-12",
            "residenceAddress": "via Borgo Pio",
            "residenceZipCode": "00100",
            "residenceCity": "Rome",
            "residenceRegion": "RM",
            "residenceCountry": "IT",
            "prefixPhoneNumber": "+39",
            "phoneNumber": "3331234567"
        }   
    }
    return doPost(f'{BASE_URL_API}authentication/register', json=json)

def confirmEmail() -> Response:
    uuid = input("Confirmation uuid: ")
    return doGet(f'{BASE_URL_API}authentication/confirm-mail?id={uuid}')

def login() -> Response:
    global HEADERS
    json = {
        "email": ACCOUNT_EMAIL,
        "password": ACCOUNT_PWD
    }
    req = doPost(f'{BASE_URL_API}authentication/login', json=json)
    if (req.status_code == 200):
        token = req.json()["accessToken"]
        val = f"Bearer {token}"
        HEADERS["Authorization"] = val
        session.cookies.set("fz-token", val)
        print(HEADERS)
    
    return req

def getMe() -> Response:
    doGet(f'{BASE_URL_API}users/display/me')

def testPermission() -> Response:
    doGet(f'{BASE_URL_API}authentication/test')

def testInternalAuthorize() -> Response:
    doGet(f'{BASE_URL}internal/orders/ping')
    doGet(f'{BASE_URL}internal/orders/ping', auth=HTTPBasicAuth('furizon', 'changeit'))

def uploadBadge() -> Response:
    #imageName = 'testImage2.png'
    imageName = 'testImageSmall.jpg'
    files = {
        'image': (imageName, open(imageName, 'rb')),
    }
    return doPost(f'{BASE_URL_API}badge/user/upload', files=files)
def deleteBadge() -> Response:
    return doDelete(f'{BASE_URL_API}badge/user/')
def runDeleteDanglingBadges() -> Response:
    #doGet(f'{BASE_URL_API}admin/ping')
    return doPost(f'{BASE_URL_API}admin/media/run-delete-media-cronjob')

def addFursuit() -> Response:
    json = {
        "name": "redwing " + generate_random_string(5),
        "species": "Lucario",
        "bringToCurrentEvent": True,
        "showInFursuitCount": True
    }
    return doPost(f'{BASE_URL_API}fursuits/', json=json)
def updateBring() -> Response:
    json = {
        "bringFursuitToCurrentEvent": False,
        "userId": 2
    }
    fursuitID = 1
    return doPost(f'{BASE_URL_API}fursuits/{fursuitID}/bringToEvent', json=json)

def countdown() -> Response:
    return doGet(f'{BASE_URL_API}admin/countdown')

def fursuitCount() -> Response:
    return doGet(f'{BASE_URL_API}counts/fursuit')

def usersAdminView(id: int) -> Response:
    return doGet(f'{BASE_URL_API}users/view/{id}')

#register()
#confirmEmail()
login()
#getMe()
#testPermission()
#testInternalAuthorize()
#uploadBadge()
#deleteBadge()
#runDeleteDanglingBadges()
#addFursuit()
#addFursuit()
#addFursuit()
#updateBring()

usersAdminView(1)

#countdown()

#fursuitCount()