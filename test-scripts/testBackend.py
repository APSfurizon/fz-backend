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
    print(f"\n-------- DELETE {url} --------")
    print(response.status_code)
    print(response.cookies.items())
    print(response.headers)
    print(response.text)
    return response

def reloadOrders() -> Response:
    return doPost(f'{BASE_URL_API}cache/pretix/reload-orders')

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
            "phoneNumber": "3331234567",
            "sex":"M",
            "gender":"CisMan"
        }
    }
    return doPost(f'{BASE_URL_API}authentication/register', json=json)

def updateUserInfo() -> Response:
    json = {
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
            "phoneNumber": "3331234567",
            "sex":"M",
            "gender":"CisMan"
    }
    return doPost(f'{BASE_URL_API}membership/update-personal-user-information', json=json) 


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

def markPersonalUserInformationAsUpdated() -> Response:
    return doPost(f'{BASE_URL_API}membership/mark-personal-user-information-as-updated')

def getMe() -> Response:
    return doGet(f'{BASE_URL_API}users/display/me')

def testPermission() -> Response:
    return doGet(f'{BASE_URL_API}authentication/test')

def testInternalAuthorize() -> Response:
    doGet(f'{BASE_URL}internal/orders/ping')
    return doGet(f'{BASE_URL}internal/orders/ping', auth=HTTPBasicAuth('furizon', 'changeit'))

def getOrderLink() -> Response:
    return doGet(f'{BASE_URL_API}orders-workflow/generate-pretix-shop-link')

def linkOrder() -> Response:
    orderCode = "N0FM3"
    orderSecret = "8cog2lc8fb8llvkk"
    json = {
        "orderCode": orderCode,
        "orderSecret": orderSecret,
    }
    return doPost(f'{BASE_URL_API}orders-workflow/link-order', json=json)

def getBadge() -> Response:
    return doGet(f'{BASE_URL_API}badge/')
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

def noseCount() -> Response:
    return doGet(f'{BASE_URL_API}counts/bopos')

def usersAdminView(id: int) -> Response:
    return doGet(f'{BASE_URL_API}users/view/{id}')

def exportHotel() -> Response:
    return doGet(f'{BASE_URL_API}admin/export/hotel-user-list')

def listPermissions() -> Response:
    return doGet(f'{BASE_URL_API}roles/permissions')
def listRoles() -> Response:
    return doGet(f'{BASE_URL_API}roles/')
def crerateRole(roleInternalName: str) -> Response:
    return doPost(f'{BASE_URL_API}roles/', json={"internalName": roleInternalName})
def fetchRole(id: int) -> Response:
    return doGet(f'{BASE_URL_API}roles/{id}')
def updateRole(id: int) -> Response:
    json = {
        "roleInternalName": "testtt",
        "roleDisplayName": None,
        "roleAdmincountPriority": 20,
        "showInAdminCount": True,
        "enabledPermissions": [
            "CAN_MANAGE_USER_PUBLIC_INFO",
            "CAN_MANAGE_ROOMS",
            "CAN_MANAGE_MEMBERSHIP_CARDS",
            #"PRETIX_ADMIN",
            #"CAN_REFRESH_PRETIX_CACHE",
        ],
        "users": [
            {
                "userId": 1,
                "tempRole": True
                #"tempRole": False
            },
            {
                "userId": 2,
                "tempRole": True
                #"tempRole": False
            },
            {
                "userId": 3,
                #"tempRole": True
                "tempRole": False
            },
            {
                "userId": 4,
                "tempRole": True
                #"tempRole": False
            },
        ]
    }
    return doPost(f'{BASE_URL_API}roles/{id}', json=json)
def deleteRole(id: int) -> Response:
    return doDelete(f'{BASE_URL_API}roles/{id}')

def exportBadges() -> Response:
    return doGet(f'{BASE_URL_API}admin/export/badges/user')

def remindRoomNotFull() -> Response:
    return doGet(f'{BASE_URL_API}admin/mail-reminders/room-not-full')

def roomCreate() -> Response:
    json = {
        "name": "Test Room"
    }
    return doPost(f'{BASE_URL_API}room/create', json=json)

def roomDelete() -> Response:
    return doPost(f'{BASE_URL_API}room/delete')

def roomConfirm() -> Response:
    return doPost(f'{BASE_URL_API}room/confirm')

def roomUnconfirm() -> Response:
    return doPost(f'{BASE_URL_API}room/unconfirm')

def roomListing() -> Response:
    return doGet(f'{BASE_URL_API}room/get-room-list-with-quota')

def roomGetInfo() -> Response:
    return doGet(f'{BASE_URL_API}room/info')

#register()
#confirmEmail()
login()
#getMe()
#updateUserInfo()
#markPersonalUserInformationAsUpdated()
#getOrderLink()
#linkOrder()
#reloadOrders()
#testPermission()
#testInternalAuthorize()
#uploadBadge()
#deleteBadge()
#runDeleteDanglingBadges()
#addFursuit()
#addFursuit()
#addFursuit()
#updateBring()
#exportHotel()
#listPermissions()
#listRoles()
#crerateRole("-_____")
#fetchRole(1)
#updateRole(2)
#deleteRole(4)
#exportBadges()
#remindRoomNotFull()
#usersAdminView(1)

#roomCreate()
#roomGetInfo()
#roomConfirm()
#roomGetInfo()
#roomUnconfirm()
#roomGetInfo()
#roomListing()

#uploadBadge()
#deleteBadge()

getBadge()


#countdown()

#fursuitCount()
#noseCount()