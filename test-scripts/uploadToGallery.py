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
	ret = doPost(f'{BASE_URL_API}gallery/upload/', json=json)
	
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
	{"event": "furizon/beyond",		"user": 76,			"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/Aeritus/"},
	{"event": "furizon/beyond",		"user": 254,		"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/Baritz/"},
	{"event": "furizon/beyond",		"user": 155,		"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/Carty/"},
	{"event": "furizon/beyond",		"user": 228,		"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/Castore Krios/"},
	{"event": "furizon/beyond",		"user": 259,		"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/cavallium/"},
	# NO ACCOUNT{"event": "furizon/beyond",		"user": 259,		"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/Ciroz/"},
	{"event": "furizon/beyond",		"user": 6,			"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/Drew/"},
	{"event": "furizon/beyond",		"user": 728,		"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/Fenrir Werewolfe/"},
	{"event": "furizon/beyond",		"user": 164,		"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/Fons/"},
	# NO ACCOUNT{"event": "furizon/beyond",		"user": 164,		"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/GazelleIT/"},
	# NO ACCOUNT{"event": "furizon/beyond",		"user": 164,		"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/Glax/"},
	{"event": "furizon/beyond",		"user": 355,		"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/Igen/"},
	# NO ACCOUNT{"event": "furizon/beyond",		"user": 164,		"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/Lale OConner/"},
	# NO ACCOUNT{"event": "furizon/beyond",		"user": 164,		"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/LeBerto6/"},
	{"event": "furizon/beyond",		"user": 221,		"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/Luka/"},
	{"event": "furizon/beyond",		"user": 119,		"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/Mark/"},
	# NO ACCOUNT{"event": "furizon/beyond",		"user": 164,		"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/Maxetto/"},
	{"event": "furizon/beyond",		"user": 120,		"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/Morkulfr/"},
	{"event": "furizon/beyond",		"user": 200,		"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/Paco folf/"},
	{"event": "furizon/beyond",		"user": 693,		"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/Pianostrong/"},
	{"event": "furizon/beyond",		"user": 152,		"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/Roll Lee/"},
	{"event": "furizon/beyond",		"user": 276,		"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/Saika/"},
	{"event": "furizon/beyond",		"user": 30,			"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/Shado + others/"},
	{"event": "furizon/beyond",		"user": 346,		"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/Sherly/"},
	{"event": "furizon/beyond",		"user": 1,			"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/Stranck/"},
	{"event": "furizon/beyond",		"user": 2,			"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/Sushi/"},
	{"event": "furizon/beyond",		"user": 377,		"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/UndyBadger/"},
	{"event": "furizon/beyond",		"user": 114,		"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/Wyle/"},
	{"event": "furizon/beyond",		"user": 71,			"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/Zamy/"},
	# NO ACCOUNT{"event": "furizon/beyond",		"user": 71,			"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/ZellDragon6/"},
	{"event": "furizon/beyond",		"user": 453,		"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/Zeus/"},
 
 
	{"event": "furizon/overlord",	"user": 135,		"path": "Furizon Overlord 2024/Furizon Overlord 2024 - User uploads/60 foto nome NightFall/"},
	{"event": "furizon/overlord",	"user": 14,			"path": "Furizon Overlord 2024/Furizon Overlord 2024 - User uploads/Arven/"},
	{"event": "furizon/overlord",	"user": 254,		"path": "Furizon Overlord 2024/Furizon Overlord 2024 - User uploads/Baritz/"},
	# NO ACCOUNT{"event": "furizon/overlord",	"user": 254,		"path": "Furizon Overlord 2024/Furizon Overlord 2024 - User uploads/Caballino/"},
	{"event": "furizon/overlord",	"user": 155,		"path": "Furizon Overlord 2024/Furizon Overlord 2024 - User uploads/Carty/"},
	{"event": "furizon/overlord",	"user": 259,		"path": "Furizon Overlord 2024/Furizon Overlord 2024 - User uploads/Cavallium/"},
	# NO ACCOUNT{"event": "furizon/overlord",	"user": 259,		"path": "Furizon Overlord 2024/Furizon Overlord 2024 - User uploads/Endiu Tealber/"},
	{"event": "furizon/overlord",	"user": 164,		"path": "Furizon Overlord 2024/Furizon Overlord 2024 - User uploads/Fons The Bun/"},
	# NO ACCOUNT{"event": "furizon/overlord",	"user": 259,		"path": "Furizon Overlord 2024/Furizon Overlord 2024 - User uploads/Glax/"},
	{"event": "furizon/overlord",	"user": 85,			"path": "Furizon Overlord 2024/Furizon Overlord 2024 - User uploads/Jabilo Bearly/"},
	{"event": "furizon/overlord",	"user": 70,			"path": "Furizon Overlord 2024/Furizon Overlord 2024 - User uploads/Kinae/"},
	# NO ACCOUNT{"event": "furizon/overlord",	"user": 70,			"path": "Furizon Overlord 2024/Furizon Overlord 2024 - User uploads/Maxetto/"},
	{"event": "furizon/overlord",	"user": 221,		"path": "Furizon Overlord 2024/Furizon Overlord 2024 - User uploads/montagutiluca/"},
	{"event": "furizon/overlord",	"user": 80,			"path": "Furizon Overlord 2024/Furizon Overlord 2024 - User uploads/MoodyDog/"},
	{"event": "furizon/overlord",	"user": 120,		"path": "Furizon Overlord 2024/Furizon Overlord 2024 - User uploads/Morkulfr/"},
	{"event": "furizon/overlord",	"user": 495,		"path": "Furizon Overlord 2024/Furizon Overlord 2024 - User uploads/rizapon/"},
	{"event": "furizon/overlord",	"user": 276,		"path": "Furizon Overlord 2024/Furizon Overlord 2024 - User uploads/Saika/"},
	{"event": "furizon/overlord",	"user": 117,		"path": "Furizon Overlord 2024/Furizon Overlord 2024 - User uploads/Seishin/"},
	{"event": "furizon/overlord",	"user": 346,		"path": "Furizon Overlord 2024/Furizon Overlord 2024 - User uploads/Sherly/"},
	# NO ACCOUNT{"event": "furizon/overlord",	"user": 346,		"path": "Furizon Overlord 2024/Furizon Overlord 2024 - User uploads/SithDragon18/"},
	{"event": "furizon/overlord",	"user": 669,		"path": "Furizon Overlord 2024/Furizon Overlord 2024 - User uploads/Soonyx/"},
	{"event": "furizon/overlord",	"user": 87,			"path": "Furizon Overlord 2024/Furizon Overlord 2024 - User uploads/Spectre/"},
	# NO ACCOUNT{"event": "furizon/overlord",	"user": 87,			"path": "Furizon Overlord 2024/Furizon Overlord 2024 - User uploads/Stejf/"},
	{"event": "furizon/overlord",	"user": 465,		"path": "Furizon Overlord 2024/Furizon Overlord 2024 - User uploads/Stephen fang/"},
	{"event": "furizon/overlord",	"user": 488,		"path": "Furizon Overlord 2024/Furizon Overlord 2024 - User uploads/Steve Wolf/"},
	{"event": "furizon/overlord",	"user": 1,			"path": "Furizon Overlord 2024/Furizon Overlord 2024 - User uploads/Stranck/"},
	{"event": "furizon/overlord",	"user": 2,			"path": "Furizon Overlord 2024/Furizon Overlord 2024 - User uploads/Sushi/"},
	{"event": "furizon/overlord",	"user": 293,		"path": "Furizon Overlord 2024/Furizon Overlord 2024 - User uploads/Taru/"},
	{"event": "furizon/overlord",	"user": 86,			"path": "Furizon Overlord 2024/Furizon Overlord 2024 - User uploads/Umbra Querciarossa/"},
	{"event": "furizon/overlord",	"user": 377,		"path": "Furizon Overlord 2024/Furizon Overlord 2024 - User uploads/UndyBadger/"},
	{"event": "furizon/overlord",	"user": 136,		"path": "Furizon Overlord 2024/Furizon Overlord 2024 - User uploads/Vlcak/"},
	{"event": "furizon/overlord",	"user": 22,			"path": "Furizon Overlord 2024/Furizon Overlord 2024 - User uploads/Wurki/"},
	{"event": "furizon/overlord",	"user": 71,			"path": "Furizon Overlord 2024/Furizon Overlord 2024 - User uploads/Zamy/"},
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
