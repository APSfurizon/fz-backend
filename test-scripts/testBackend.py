import os
import json
import hashlib
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
    'Connection': 'keep-alive',
    #"x-forwarded-for": "123456789abcdefghijklmnopqrstuvwxyz, 192.168.1.1"
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
def doGet(url, auth=None, json=None, data=None) -> Response:
    global session
    global HEADERS
    response = session.get(url, headers=HEADERS, json=json, data=data, allow_redirects=False, auth=auth)
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
def doPut(url, data=None, extraHeaders=None) -> Response:
    global session
    global HEADERS
    extraHeaders = HEADERS if extraHeaders is None else {**HEADERS, **extraHeaders}
    response = session.put(url, headers=extraHeaders, allow_redirects=False, data=data)
    print(f"\n-------- PUT {url} --------")
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
            "gender":"CisMan",
            "shirtSize": "m"
    }
    return doPost(f'{BASE_URL_API}membership/update-personal-user-information', json=json) 


def confirmEmail() -> Response:
    uuid = input("Confirmation uuid: ")
    return doGet(f'{BASE_URL_API}authentication/confirm-mail?id={uuid}')

def login() -> Response:
    global HEADERS
    json = {
        "email": ACCOUNT_EMAIL,
        #"password": ACCOUNT_PWD + "staminchia"
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

def getSponsorshipNames() -> Response:
    return doGet(f'{BASE_URL_API}events/get-sponsorship-names/1')

def getAttendedEvents() -> Response:
    return doGet(f'{BASE_URL_API}events/attended')

def getMeAuth() -> Response:
    return doGet(f'{BASE_URL_API}users/me')

def getMe() -> Response:
    return doGet(f'{BASE_URL_API}users/display/me')

def testPermission() -> Response:
    return doGet(f'{BASE_URL_API}authentication/test')

def testInternalAuthorize() -> Response:
    doGet(f'{BASE_URL}internal/orders/ping')
    return doGet(f'{BASE_URL}internal/orders/ping', auth=HTTPBasicAuth('furizon', 'changeit'))

def pretixWebhook(orderCode: str, event: str, orga: str) -> Response:
    json = {
        "notificationId": 0,
        "organizer": orga,
        "event": event,
        #"code": orderCode + generate_random_string(4),
        "code": orderCode,
        "action": "a"
    }
    return doPost(f'{BASE_URL}internal/orders/webhook', json=json, auth=HTTPBasicAuth('furizon', 'changeit'))

def getOrderFullStatus() -> Response:
    return doGet(f'{BASE_URL_API}orders-workflow/get-full-status')

def getOrderLink() -> Response:
    return doGet(f'{BASE_URL_API}orders-workflow/generate-pretix-shop-link')

def linkOrder() -> Response:
    orderCode = "P0SBM"
    orderSecret = "rslmm34hdqawqddi"
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

def reminderFursuitBringToEvent() -> Response:
    return doGet(f'{BASE_URL_API}admin/mail-reminders/fursuit-bring-to-event')

def roomCreate() -> Response:
    json = {
        "name": "Test Room"
    }
    return doPost(f'{BASE_URL_API}room/create', json=json)

def roomDelete() -> Response:
    json = {}
    return doPost(f'{BASE_URL_API}room/delete')

def roomConfirm() -> Response:
    return doPost(f'{BASE_URL_API}room/confirm')

def roomUnconfirm() -> Response:
    return doPost(f'{BASE_URL_API}room/unconfirm')

def roomListing() -> Response:
    return doGet(f'{BASE_URL_API}room/get-room-list-with-quota')

def buyOrUpgradeRoom() -> Response:
    json = {
        "roomPretixItemId": 335
    }
    return doPost(f'{BASE_URL_API}room/buy-or-upgrade-room', json=json)

def roomGetInfo() -> Response:
    return doGet(f'{BASE_URL_API}room/info')

def exchangeInit() -> Response:
    json = {
        "destUserId": 2,
        "action": "room"
        #"action": "order"
    }
    return doPost(f'{BASE_URL_API}room/exchange/init', json=json)

def exchangeUpdate() -> Response:
    # INSERT INTO exchange_confirmation_status(exchange_id, target_user_id, source_user_id, target_confirmed, source_confirmed, event_id, expires_on, action_type) VALUES (1, 3, 1, true, false, 10, 999999999999999999, 0); 0 for room, 1 for order
    json = {
        "exchangeId": 1,
        "confirm": True
    }
    return doPost(f'{BASE_URL_API}room/exchange/update', json=json)
    
def exception() -> Response:
    return doGet(f'{BASE_URL_API}admin/exception')

def searchByFursuitId(id: int) -> Response:
    return doGet(f'{BASE_URL_API}users/search/by-fursuit-id?id={id}')
def searchByMemberShipNumber(id: str) -> Response:
    return doGet(f'{BASE_URL_API}users/search/by-membership-number?number={id}')
def searchByMemberShipDbid(id: int) -> Response:
    return doGet(f'{BASE_URL_API}users/search/by-membership-dbid?id={id}')
def searchByUserId(id: int) -> Response:
    return doGet(f'{BASE_URL_API}users/search/by-user-id?id={id}')
def searchByOrderSerial(id: int) -> Response:
    return doGet(f'{BASE_URL_API}users/search/current-event/by-order-serial?orders={id}')
def searchByOrderCode(id: str) -> Response:
    return doGet(f'{BASE_URL_API}users/search/current-event/by-order-code?orders={id}')

def addUserToRole(userId: int, roleId: int, temp: bool) -> Response:
    json = {
        "userId": userId,
        "tempRole": temp
    }
    return doPost(f'{BASE_URL_API}roles/{roleId}/add-user', json=json)
def removeUserFromRole(userId: int, roleId: int) -> Response:
    json = {
        "userId": userId
    }
    return doPost(f'{BASE_URL_API}roles/{roleId}/remove-user', json=json)
    
    
def uploadFileToGallery_complete(reqId: int, fileName: str, fileSize: int, eventId: int, etags: list, hash: str) -> Response:
    json = {
        "uploadReqId": reqId,
        "fileName": fileName,
        "fileSize": fileSize,
        "eventId": eventId,
        "uploadRepostPermissions": "CC_BY_NC_ND",
        "etags": etags,
        "md5Hash": hash
    }
    return doPost(f'{BASE_URL_API}gallery/upload/complete', json=json)
def uploadFileToGallery_listParts(reqId: int) -> Response:
    return doGet(f'{BASE_URL_API}gallery/upload/status/{reqId}')
def uploadFileToGallery_abort(reqId: int) -> Response:
    json = {
        "uploadReqId": reqId
    }
    return doPost(f'{BASE_URL_API}gallery/upload/abort', json=json)
def uploadFileToGallery(filePath: str, fileName: str, eventId: int) -> Response:
    path = filePath + "/" + fileName
    fileSize = os.path.getsize(path)
    json = {
        "fileName": fileName,
        "fileSize": fileSize,
        "eventId": eventId
    }
    ret = doPost(f'{BASE_URL_API}gallery/upload', json=json)
    
    ret = ret.json()
    reqId = ret["uploadReqId"]
    ret = ret["multipartCreationResponse"]
    chunkSize = ret["chunkSize"]
    presignedUrls = ret["presignedUrls"]
    
    md5 = [0] * len(presignedUrls)
    etags = [0] * len(presignedUrls)
    
    with open(path, 'rb') as f:
        for i, url in enumerate(presignedUrls):
            chunk = f.read(chunkSize)
            ret = doPut(url, data=chunk)
            etags[i] = ret.headers["etag"]
            md5[i] = hashlib.md5(chunk).digest()
            
    finalHash = hashlib.md5(b"".join(md5)).hexdigest()
    
    #uploadFileToGallery_listParts(reqId)
    #uploadFileToGallery_abort(reqId)
    uploadFileToGallery_complete(reqId, fileName, fileSize, eventId, etags, finalHash)
def getUploadLimits() -> Response:
    return doGet(f'{BASE_URL_API}gallery/upload/limits')
def galleryProcessorWebhook() -> Response:
    data = {
        'id': 7,
        'file': '34a97c44-ecf3-41a8-965c-3d82989f31e2.MOV',
        'status': 'DONE',
        'type': 'VIDEO',
        'result': {
            'resolutionWidth': 1920,
            'resolutionHeight': 1440,
            'shotTimestamp': "2026-02-23T22:59:52Z",
            'fileSize': 47154973,
            'mimeType': 'video/quicktime',
            'extraMediaMimeType': 'image/webp',
            'thumbnailMediaName': 'thumb_34a97c44-ecf3-41a8-965c-3d82989f31e2.webp',
            'renderedMediaName': 'rend_34a97c44-ecf3-41a8-965c-3d82989f31e2.webp',
            'photoMetadata': None,
            'videoMetadata': {
                'audioFrequency': '48.000 kHz',
                'videoCodec': 'hevc',
                'audioCodec': 'pcm_s16le',
                'framerate': '29,97 fps',
                'duration': 20385
            }
        }
    }
    return doPost(f'{BASE_URL_API}gallery/job/completed', json=data)

def getUpload(uploadId: int) -> Response:
    return doGet(f'{BASE_URL_API}gallery/pub/{uploadId}')
def myUploads() -> Response:
    return doGet(f'{BASE_URL_API}gallery/my-uploads')
def listUploads(fromId: int = None, photographerId: int = None, eventId: int = None) -> Response:
    url = f'{BASE_URL_API}gallery/pub/list?'
    if fromId is not None:
        url += f"fromUploadId={fromId}&"
    if photographerId is not None:
        url += f"photographerUserId={photographerId}&"
    if eventId is not None:
        url += f"eventId={eventId}&"
    url += "a=0"
    return doGet(url)
def deleteUpload(uploadId: int) -> Response:
    return doDelete(f'{BASE_URL_API}gallery/manage/{uploadId}')
def getGalleryEvents() -> Response:
    #return doGet(f'{BASE_URL_API}gallery/pub/events')
    return doGet(f'{BASE_URL_API}gallery/pub/events?photographerUserId=2')
def getGalleryEvent() -> Response:
    #return doGet(f'{BASE_URL_API}gallery/pub/events/10')
    return doGet(f'{BASE_URL_API}gallery/pub/events/10?photographerUserId=2')
def getGalleryPhotographers() -> Response:
    return doGet(f'{BASE_URL_API}gallery/pub/photographers')
    #return doGet(f'{BASE_URL_API}gallery/pub/photographers?eventId=10')
def getGalleryPhotographer() -> Response:
    #return doGet(f'{BASE_URL_API}gallery/pub/photographers/1')
    return doGet(f'{BASE_URL_API}gallery/pub/photographers/1?eventId=9')
def adminUpdateUpload(uploadIds: list, status: str=None, photographerId: int=None, eventId: int=None) -> Response:
    json = {
        "uploadIds": uploadIds,
        "newStatus": status,
        "newPhotographerUserId": photographerId,
        "newEventId": eventId
    }
    return doPost(f'{BASE_URL_API}gallery/manage/update', json=json)

#register()
#confirmEmail()
login()
#getMeAuth()
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
#exception()
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
#searchByFursuitId(4)
#searchByMemberShipNumber("2526001")
#searchByMemberShipDbid(10)
#searchByUserId(1)
#searchByOrderSerial(1)
#searchByOrderCode("T07EZ")
#addUserToRole(5, 1, True)
#removeUserFromRole(5, 1)
#testInternalAuthorize()
#uploadFileToGallery("C:/Users/Stran/Desktop/Furizon", "RZR07368.ARW", 10)
#galleryProcessorWebhook()
#getUpload(13)
#listUploads(
    #fromId = 0,
    #photographerId = 1,
    #eventId = 10
#)
#myUploads()
#deleteUpload(16)
#getGalleryEvents()
#getGalleryEvent()
#getGalleryPhotographers()
#getGalleryPhotographer()
#adminUpdateUpload(
#    uploadIds=[13],
#    #status="APPROVED",
#    #photographerId=23,
#    eventId=91
#)
#getAttendedEvents()
getUploadLimits()

#getOrderFullStatus()


#roomDelete()
#roomCreate()
#roomGetInfo()
#roomConfirm()
#roomGetInfo()
#roomUnconfirm()
#roomGetInfo()
#roomListing()
#buyOrUpgradeRoom()
#exchangeInit()
#exchangeUpdate()
#getSponsorshipNames()
#reminderFursuitBringToEvent()


#uploadBadge()
#deleteBadge()

#usersAdminView(1)
# getBadge()


#countdown()

#fursuitCount()
#noseCount()

#pretixWebhook("STOCAZZO", "polaris", "furizon")