import os
import json
import hashlib
import requests
from requests import Response
from requests.auth import HTTPBasicAuth

BASE_URL = "https://fzbe.furizon.net/"
BASE_URL_API = f"{BASE_URL}api/v1/"

import random
import string

def generate_random_string(length):
    letters = string.ascii_letters + string.digits
    return ''.join(random.choice(letters) for i in range(length))


ACCOUNT_EMAIL = 'zXWsqWIoLS@keysmasher.femboyyyyy.it'
ACCOUNT_PWD = 'A1b2C3d5!'

HEADERS = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:133.0) Gecko/20100101 Firefox/133.0',
    'Accept': '*/*',
    'Accept-Language': 'en-US,en;q=0.8',
    'Referer': 'https://furpanel.furizon.net/',
    'Origin': 'https://furpanel.furizon.net',
    'Connection': 'keep-alive',
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
def uploadFileToGallery(filePath: str, fileName: str, eventId: int) -> int:
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
    
    def uploadChunk(i: int, url: str, chunk: bytes):
        ret = doPut(url, data=chunk)
        etags[i] = ret.headers["etag"]
        md5[i] = hashlib.md5(chunk).digest()
    
    with open(path, 'rb') as f:
        for i, url in enumerate(presignedUrls):
            chunk = f.read(chunkSize)
            try:
                uploadChunk(i, url, chunk)
            except Exception as e:
                print(f"**[WARN] Failed to upload chunk {i} for file '{fileName}': {e}. Retrying...")
                uploadChunk(i, url, chunk)
            
    finalHash = hashlib.md5(b"".join(md5)).hexdigest()
    ret = uploadFileToGallery_complete(reqId, fileName, fileSize, eventId, etags, finalHash)
    return ret.json()["id"]
def getUploadLimits() -> Response:
    return doGet(f'{BASE_URL_API}gallery/upload/limits')

def adminUpdateUpload(uploadIds: list, status: str=None, photographerId: int=None, eventId: int=None) -> Response:
    json = {
        "uploadIds": uploadIds,
        "newStatus": status,
        "newPhotographerUserId": photographerId,
        "newEventId": eventId
    }
    return doPost(f'{BASE_URL_API}gallery/manage/update', json=json)

def getEvents() -> Response:
    return doGet(f'{BASE_URL_API}events/')

eventMap = {}
events = getEvents().json()
for event in events:
    eventMap[event["slug"]] = event["id"]


uploadDirs = [
    {"event": "furizon/riots",      "user": 1,      "path": "C:/Users/Utente/Desktop/riots"},
]


for uploadDir in uploadDirs:
    print(f"**[INFO] Uploading files from '{uploadDir['path']}' to event '{uploadDir['event']}' as user '{uploadDir['user']}'")
    eventId = eventMap[uploadDir["event"]]
    uploadIds = []
    for fileName in os.listdir(uploadDir["path"]):
        print(f"**[FINE] Uploading file '{fileName}'")
        try:
            uploadId = uploadFileToGallery(uploadDir["path"], fileName, eventId)
            print(f"**[FINE] Uploading file '{fileName}': Got upload ID = {uploadId}")
            uploadIds.append(uploadId)
            
            if (len(uploadIds) > 50):
                print(f"**[INFO] Pre-Approving uploads with IDs {uploadIds} and setting photographer to user {uploadDir['user']}")
                adminUpdateUpload(uploadIds, status="APPROVED", photographerId=uploadDir["user"])
                uploadIds = []
        except Exception as e:
            print(f"**[ERROR] Uploading file '{fileName}': {e}")
    print(f"**[INFO] Approving uploads with IDs {uploadIds} and setting photographer to user {uploadDir['user']}")
    adminUpdateUpload(uploadIds, status="APPROVED", photographerId=uploadDir["user"])
    
# python3 uploadToGallery.py 2>&1 | tee -a log.txt
