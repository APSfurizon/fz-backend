import os
import json
import exifread
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

def searchByUserId(id: int) -> Response:
    return doGet(f'{BASE_URL_API}users/search/by-user-id?id={id}')


def uploadFileToGallery_complete(reqId: int, fileName: str, fileSize: int, eventId: int, etags: list, hash: str) -> Response:
	json = {
		"uploadReqId": reqId,
		"fileName": fileName,
		"fileSize": fileSize,
		"eventId": eventId,
		"uploadRepostPermissions": "PHOTOGRAPHER_DISCRETION",
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

def getDate(path, file):
	#print(f"**[FINEST] Extracting EXIF data from path '{path}' file '{file}'...")
	try:
		with open(os.path.join(path, file), 'rb') as fh:
			#import inspect
			#print(inspect.getfullargspec(exifread.process_file))
			tags = exifread.process_file(fh, stop_tag="EXIF DateTimeOriginal", extract_thumbnail=False, details=False)
			#tags = exifread.process_file(fh, stop_tag="EXIF DateTimeOriginal", details=False)
			dateTaken = tags["EXIF DateTimeOriginal"]
			return dateTaken
	except KeyError:
		print(f"**[WARN] No EXIF DateTimeOriginal tag found for file '{file}' in path '{path}'.")
	return None

def approveUploads(uploadIds: dict, force: bool=False) -> Response:
	for uploadKey, ids in uploadIds.items():
		if len(ids) > 0 and (len(ids) > 50 or force):
			try:
				photographerId = uploadKey[0]
				reject = uploadKey[1]
				rejectStr = "REJECTED" if reject else "APPROVED"
				print(f"**[INFO] Updating uploads with IDs {ids}: setting photographer to user {photographerId}, reject = {reject}")
				adminUpdateUpload(ids, status=rejectStr, photographerId=photographerId)
				uploadIds[uploadKey] = []
			except Exception as e:
				print(f"**[ERROR] Failed to update uploads with IDs {ids}: {e}")


login()

eventSlugToId = {}
eventIdToSlug = {}
events = getEvents().json()
for event in events["events"]:
	slug = event["slug"]
	eventId = event["id"]
	eventSlugToId[slug] = eventId
	eventIdToSlug[eventId] = slug
 
supportedFormats = getUploadLimits().json()["allowedFileExtensions"]
supportedFormats = [x.lower() for x in supportedFormats]
print(f"**[DEBUG] Supported file formats for upload: {supportedFormats}")


uploadDirs = [
	{"event": "furizon/sideralis",		"user": 96,			"path": "Sideralis/Furizon 2k18/Demon/"}, #2018
	{"event": "furizon/sideralis",		"user": 45,			"path": "Sideralis/Furizon 2k18/Gideon/"},
	# NO ACCOUNT{"event": "furizon/sideralis",		"user": 45,			"path": "Sideralis/Furizon 2k18/Lale/"},
	# NO ACCOUNT{"event": "furizon/sideralis",		"user": 45,			"path": "Sideralis/Furizon 2k18/Ren/"},
	# NO ACCOUNT{"event": "furizon/sideralis",		"user": 45,			"path": "Sideralis/Furizon 2k18/Ryuu/"},
	{"event": "furizon/sideralis",		"user": 346,			"path": "Sideralis/Furizon 2k18/Sherly/"},
	# NO ACCOUNT{"event": "furizon/sideralis",		"user": 346,			"path": "Sideralis/Furizon 2k18/Vesper/"},
	{"event": "furizon/sideralis",		"user": 551,			"path": "Sideralis/Furizon 2k18/Winter Wolf/furizon/"},
	# NO ACCOUNT{"event": "furizon/sideralis",		"user": 551,			"path": "Sideralis/Furizon 2k18/XxXVolpett0-OscuroXxX/"},
 
	{"event": "furizon/sideralis",		"user": 4,				"path": "Sideralis/"}, #kyrill
 
 
	{"event": "furizon/sovereign",		"user": 862,			"path": "Sovereign/Ser Reginaldo (Cerunnus)/"}, #2019
	# NO ACCOUNT{"event": "furizon/sovereign",		"user": 862,			"path": "Sovereign/Shiri/"},
	{"event": "furizon/sovereign",		"user": 669,			"path": "Sovereign/Soonyx/"},
 
 
	{"event": "furizon/river-side-2020",		"user": 20,			"path": "Riverside/Riverside HYPE/"},
	{"event": "furizon/river-side-2020",		"user": 10,			"path": "Riverside/Razor AND soon/razor/"},
 
 
	# NO ACCOUNT{"event": "furizon/river-side-2021",		"user": 76,			"path": "Furizon Riverside 2021/Furizon Riverside 2021 - Uploads/Aiden McLean/"},
	{"event": "furizon/river-side-2021",		"user": 543,		"path": "Furizon Riverside 2021/Furizon Riverside 2021 - Uploads/Fuel/"},
	{"event": "furizon/river-side-2021",		"user": 20,			"path": "Furizon Riverside 2021/Furizon Riverside 2021 - Uploads/Hypewolf/"},
	{"event": "furizon/river-side-2021",		"user": 119,		"path": "Furizon Riverside 2021/Furizon Riverside 2021 - Uploads/Mark/"},
	{"event": "furizon/river-side-2021",		"user": 45,			"path": "Furizon Riverside 2021/Furizon Riverside 2021 - Uploads/Nitro PunkDoggo/"},
	{"event": "furizon/river-side-2021",		"user": 415,		"path": "Furizon Riverside 2021/Furizon Riverside 2021 - Uploads/Ricky_the_apennine_wolf/"},
	{"event": "furizon/river-side-2021",		"user": 346,		"path": "Furizon Riverside 2021/Furizon Riverside 2021 - Uploads/Sherly/"},
	{"event": "furizon/river-side-2021",		"user": 669,		"path": "Furizon Riverside 2021/Furizon Riverside 2021 - Uploads/Soonyx/"},
	{"event": "furizon/river-side-2021",		"user": 488,		"path": "Furizon Riverside 2021/Furizon Riverside 2021 - Uploads/steve_wolf/"},
	{"event": "furizon/river-side-2021",		"user": 1,			"path": "Furizon Riverside 2021/Furizon Riverside 2021 - Uploads/Stranck/"},
	{"event": "furizon/river-side-2021",		"user": 2,			"path": "Furizon Riverside 2021/Furizon Riverside 2021 - Uploads/Sushi/"},
	{"event": "furizon/river-side-2021",		"user": 86,			"path": "Furizon Riverside 2021/Furizon Riverside 2021 - Uploads/Umbra Querciarossa/"},
	{"event": "furizon/river-side-2021",		"user": 37,			"path": "Furizon Riverside 2021/Furizon Riverside 2021 - Uploads/WildWolf/"},
	{"event": "furizon/river-side-2021",		"user": 22,			"path": "Furizon Riverside 2021/Furizon Riverside 2021 - Uploads/Wurki/"},
 
	{"event": "furizon/river-side-2021",		"user": 10,			"path": "Furizon Riverside 2021/OFFICIAL/razor/"},
	{"event": "furizon/river-side-2021",		"user": 669,		"path": "Furizon Riverside 2021/OFFICIAL/soonyx/"},


	{"event": "furizon/riots",		"user": 76,			"path": "Furizon Riots 2022␠/Furizon Riots 2022 - Uploads/Aeritus/"},
	# NO ACCOUNT{"event": "furizon/riots",		"user": 76,			"path": "Furizon Riots 2022␠/Furizon Riots 2022 - Uploads/Aiden/"},
	# NO ACCOUNT{"event": "furizon/riots",		"user": 76,			"path": "Furizon Riots 2022␠/Furizon Riots 2022 - Uploads/Aless The Tiger/"},
	{"event": "furizon/riots",		"user": 14,			"path": "Furizon Riots 2022␠/Furizon Riots 2022 - Uploads/Arven/"},
	# NO ACCOUNT{"event": "furizon/riots",		"user": 14,			"path": "Furizon Riots 2022␠/Furizon Riots 2022 - Uploads/Blichard/"},
	{"event": "furizon/riots",		"user": 228,		"path": "Furizon Riots 2022␠/Furizon Riots 2022 - Uploads/Devon/"},
	# NO ACCOUNT{"event": "furizon/riots",		"user": 228,		"path": "Furizon Riots 2022␠/Furizon Riots 2022 - Uploads/dyed_rotterdam/"},
	# NO ACCOUNT{"event": "furizon/riots",		"user": 228,		"path": "Furizon Riots 2022␠/Furizon Riots 2022 - Uploads/Foxo/"},
	{"event": "furizon/riots",		"user": 68,			"path": "Furizon Riots 2022␠/Furizon Riots 2022 - Uploads/Hazard/"},
	{"event": "furizon/riots",		"user": 584,		"path": "Furizon Riots 2022␠/Furizon Riots 2022 - Uploads/Indie AW/"},
	{"event": "furizon/riots",		"user": 85,			"path": "Furizon Riots 2022␠/Furizon Riots 2022 - Uploads/Jabilo Bearly/"},
	{"event": "furizon/riots",		"user": 25,			"path": "Furizon Riots 2022␠/Furizon Riots 2022 - Uploads/Jan SnowPix/"},
	{"event": "furizon/riots",		"user": 26,			"path": "Furizon Riots 2022␠/Furizon Riots 2022 - Uploads/Kay Squiffy/"},
	{"event": "furizon/riots",		"user": 175,		"path": "Furizon Riots 2022␠/Furizon Riots 2022 - Uploads/Kimbo/"},
	{"event": "furizon/riots",		"user": 39,			"path": "Furizon Riots 2022␠/Furizon Riots 2022 - Uploads/Lakota/"},
	# NO ACCOUNT{"event": "furizon/riots",		"user": 39,			"path": "Furizon Riots 2022␠/Furizon Riots 2022 - Uploads/Lale o conner/"},
	# NO ACCOUNT{"event": "furizon/riots",		"user": 39,			"path": "Furizon Riots 2022␠/Furizon Riots 2022 - Uploads/LeBerto6/"},
	{"event": "furizon/riots",		"user": 158,		"path": "Furizon Riots 2022␠/Furizon Riots 2022 - Uploads/Lion Heroar/"},
	{"event": "furizon/riots",		"user": 168,		"path": "Furizon Riots 2022␠/Furizon Riots 2022 - Uploads/Lollo The Bulf/"},
	{"event": "furizon/riots",		"user": 221,		"path": "Furizon Riots 2022␠/Furizon Riots 2022 - Uploads/Luka/"},
	{"event": "furizon/riots",		"user": 214,		"path": "Furizon Riots 2022␠/Furizon Riots 2022 - Uploads/MaraSabot/"},
	{"event": "furizon/riots",		"user": 119,		"path": "Furizon Riots 2022␠/Furizon Riots 2022 - Uploads/Mark/"},
	{"event": "furizon/riots",		"user": 872,		"path": "Furizon Riots 2022␠/Furizon Riots 2022 - Uploads/Maxetto/"},
	{"event": "furizon/riots",		"user": 534,		"path": "Furizon Riots 2022␠/Furizon Riots 2022 - Uploads/Micole/"},
	{"event": "furizon/riots",		"user": 329,		"path": "Furizon Riots 2022␠/Furizon Riots 2022 - Uploads/Paktani/"},
	{"event": "furizon/riots",		"user": 32,			"path": "Furizon Riots 2022␠/Furizon Riots 2022 - Uploads/Rei Short/"},
	{"event": "furizon/riots",		"user": 152,		"path": "Furizon Riots 2022␠/Furizon Riots 2022 - Uploads/Roll Lee/"},
	{"event": "furizon/riots",		"user": 12,			"path": "Furizon Riots 2022␠/Furizon Riots 2022 - Uploads/SnukTheKobold/"},
	{"event": "furizon/riots",		"user": 669,		"path": "Furizon Riots 2022␠/Furizon Riots 2022 - Uploads/Soonyx/"},
	{"event": "furizon/riots",		"user": 1,			"path": "Furizon Riots 2022␠/Furizon Riots 2022 - Uploads/Stranck/"},
	# NO ACCOUNT{"event": "furizon/riots",		"user": 1,			"path": "Furizon Riots 2022␠/Furizon Riots 2022 - Uploads/Woolier/"},
	{"event": "furizon/riots",		"user": 114,		"path": "Furizon Riots 2022␠/Furizon Riots 2022 - Uploads/Wyle/"},
 
	{"event": "furizon/riots",		"user": 669,		"path": "Furizon Riots 2022␠/OFFICIAL/Furizon_ Riots 2022 - Offical Indoor Photographic Set/"},
	{"event": "furizon/riots",		"user": 10,			"path": "Furizon Riots 2022␠/OFFICIAL/Furizon_ Riots 2022 - Official Furcon Gallery/razor/"},
	{"event": "furizon/riots",		"user": 669,		"path": "Furizon Riots 2022␠/OFFICIAL/Furizon_ Riots 2022 - Official Furcon Gallery/soonyx/"},
	{"event": "furizon/riots",		"user": 669,		"path": "Furizon Riots 2022␠/Soonyx/StuffedTailsShoot/"},
	{"event": "furizon/riots",		"user": 669,		"path": "Furizon Riots 2022␠/Soonyx/Shoot cascate NON TOCCARE/"},
	{"event": "furizon/riots",		"user": 669,		"path": "Furizon Riots 2022␠/Soonyx/Shoot cascate NON TOCCARE/01_Avanzate/"},
	{"event": "furizon/riots",		"user": 669,		"path": "Furizon Riots 2022␠/Soonyx/Shoot cascate NON TOCCARE/02_Nara e Hype/"},
	{"event": "furizon/riots",		"user": 669,		"path": "Furizon Riots 2022␠/Soonyx/Shoot cascate NON TOCCARE/03_Kaomoro/"},
	{"event": "furizon/riots",		"user": 669,		"path": "Furizon Riots 2022␠/Soonyx/Shoot cascate NON TOCCARE/04_Lakota/"},
	{"event": "furizon/riots",		"user": 669,		"path": "Furizon Riots 2022␠/Soonyx/Shoot cascate NON TOCCARE/05_Squiffy/"},
	{"event": "furizon/riots",		"user": 669,		"path": "Furizon Riots 2022␠/Soonyx/Shoot cascate NON TOCCARE/06_Fedix/"},
	{"event": "furizon/riots",		"user": 669,		"path": "Furizon Riots 2022␠/Soonyx/Shoot cascate NON TOCCARE/07_Khajiit/"},
	{"event": "furizon/riots",		"user": 669,		"path": "Furizon Riots 2022␠/Soonyx/Shoot cascate NON TOCCARE/08_Luka/"},
	{"event": "furizon/riots",		"user": 669,		"path": "Furizon Riots 2022␠/Soonyx/Shoot cascate NON TOCCARE/09_Plateon/"},
	{"event": "furizon/riots",		"user": 669,		"path": "Furizon Riots 2022␠/Soonyx/Shoot cascate NON TOCCARE/10_Jan/"},
	{"event": "furizon/riots",		"user": 669,		"path": "Furizon Riots 2022␠/Soonyx/Shoot cascate NON TOCCARE/11_Lio/"},
	{"event": "furizon/riots",		"user": 669,		"path": "Furizon Riots 2022␠/Soonyx/Shoot cascate NON TOCCARE/12_Bryan/"},
	{"event": "furizon/riots",		"user": 669,		"path": "Furizon Riots 2022␠/Soonyx/Shoot cascate NON TOCCARE/13_Aiden e Dustar/"},
	{"event": "furizon/riots",		"user": 669,		"path": "Furizon Riots 2022␠/Soonyx/Shoot cascate NON TOCCARE/14_Pako/"},
	{"event": "furizon/riots",		"user": 669,		"path": "Furizon Riots 2022␠/Soonyx/Shoot cascate NON TOCCARE/15_Firi/"},
	{"event": "furizon/riots",		"user": 669,		"path": "Furizon Riots 2022␠/Soonyx/Shoot cascate NON TOCCARE/16_Sabot e Shandrax/"},
	{"event": "furizon/riots",		"user": 669,		"path": "Furizon Riots 2022␠/Soonyx/Shoot cascate NON TOCCARE/17_David Fox/"},
	{"event": "furizon/riots",		"user": 669,		"path": "Furizon Riots 2022␠/Soonyx/Shoot cascate NON TOCCARE/18_Zylkor/"},
	{"event": "furizon/riots",		"user": 669,		"path": "Furizon Riots 2022␠/Soonyx/Shoot cascate NON TOCCARE/19_Lollo/"},
	{"event": "furizon/riots",		"user": 669,		"path": "Furizon Riots 2022␠/Soonyx/Shoot cascate NON TOCCARE/20_Mark/"},
	{"event": "furizon/riots",		"user": 669,		"path": "Furizon Riots 2022␠/Soonyx/Shoot cascate NON TOCCARE/21_Wyle/"},
	{"event": "furizon/riots",		"user": 669,		"path": "Furizon Riots 2022␠/Soonyx/Shoot cascate NON TOCCARE/22_Pak e Whip/"},
	{"event": "furizon/riots",		"user": 669,		"path": "Furizon Riots 2022␠/Soonyx/Shoot cascate NON TOCCARE/23_Sabrina/"},
	{"event": "furizon/riots",		"user": 669,		"path": "Furizon Riots 2022␠/Soonyx/Shoot cascate NON TOCCARE/24_Pepsy/"},
	{"event": "furizon/riots",		"user": 669,		"path": "Furizon Riots 2022␠/Soonyx/Shoot cascate NON TOCCARE/25_Starlight/"},
	{"event": "furizon/riots",		"user": 669,		"path": "Furizon Riots 2022␠/Soonyx/Shoot cascate NON TOCCARE/26_Tyto/"},
	{"event": "furizon/riots",		"user": 669,		"path": "Furizon Riots 2022␠/Soonyx/Shoot cascate NON TOCCARE/27_Kim/"},
	{"event": "furizon/riots",		"user": 669,		"path": "Furizon Riots 2022␠/Soonyx/Shoot cascate NON TOCCARE/28_Indie/"},
	{"event": "furizon/riots",		"user": 669,		"path": "Furizon Riots 2022␠/Soonyx/Shoot cascate NON TOCCARE/29_Goldye/"},
	{"event": "furizon/riots",		"user": 669,		"path": "Furizon Riots 2022␠/Soonyx/Shoot cascate NON TOCCARE/30_Stranck/"},


	{"event": "furizon/river-side-2022",		"user": 862,		"path": "Furizon Riverside 2022/Furizon Riverside 2022 - Uploads/HadesCave/"},
	{"event": "furizon/river-side-2022",		"user": 221,		"path": "Furizon Riverside 2022/Furizon Riverside 2022 - Uploads/Luka/"},
	{"event": "furizon/river-side-2022",		"user": 119,		"path": "Furizon Riverside 2022/Furizon Riverside 2022 - Uploads/Mark/"},
	{"event": "furizon/river-side-2022",		"user": 195,		"path": "Furizon Riverside 2022/Furizon Riverside 2022 - Uploads/Maverick The Husky/"},
	{"event": "furizon/river-side-2022",		"user": 11,			"path": "Furizon Riverside 2022/Furizon Riverside 2022 - Uploads/Naraiko/"},
	{"event": "furizon/river-side-2022",		"user": 10,			"path": "Furizon Riverside 2022/Furizon Riverside 2022 - Uploads/Razor/"},
	{"event": "furizon/river-side-2022",		"user": 152,		"path": "Furizon Riverside 2022/Furizon Riverside 2022 - Uploads/Roll Lee/"},
	{"event": "furizon/river-side-2022",		"user": 346,		"path": "Furizon Riverside 2022/Furizon Riverside 2022 - Uploads/Sherly/"},
	{"event": "furizon/river-side-2022",		"user": 1,			"path": "Furizon Riverside 2022/Furizon Riverside 2022 - Uploads/Stranck/"},
	{"event": "furizon/river-side-2022",		"user": 114,		"path": "Furizon Riverside 2022/Furizon Riverside 2022 - Uploads/Wyle/"},
 
	{"event": "furizon/river-side-2022",		"user": 862,		"path": "Furizon Riverside 2022/OFFICIAL/Main photos/ari/"},
	{"event": "furizon/river-side-2022",		"user": 20,			"path": "Furizon Riverside 2022/OFFICIAL/Main photos/hype/"},
	{"event": "furizon/river-side-2022",		"user": 10,			"path": "Furizon Riverside 2022/OFFICIAL/Main photos/razor/"},
	{"event": "furizon/river-side-2022",		"user": 669,		"path": "Furizon Riverside 2022/OFFICIAL/Main photos/soonyx/"},
	{"event": "furizon/river-side-2022",		"user": 862,		"path": "Furizon Riverside 2022/OFFICIAL/Halloween/ari/"},
	{"event": "furizon/river-side-2022",		"user": 10,			"path": "Furizon Riverside 2022/OFFICIAL/Halloween/razor/"},
	{"event": "furizon/river-side-2022",		"user": 669,		"path": "Furizon Riverside 2022/OFFICIAL/Halloween/soon/"},


	{"event": "furizon/beyond",		"user": 76,			"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/Aeritus/"},
	{"event": "furizon/beyond",		"user": 254,		"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/Baritz/"},
	{"event": "furizon/beyond",		"user": 155,		"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/Carty/"},
	{"event": "furizon/beyond",		"user": 228,		"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/Castore Krios/"},
	{"event": "furizon/beyond",		"user": 259,		"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/cavallium/"},
	# NO ACCOUNT{"event": "furizon/beyond",		"user": 259,		"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/Ciroz/"},
	{"event": "furizon/beyond",		"user": 6,			"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/Drew/"},
	{"event": "furizon/beyond",		"user": 728,		"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/Fenrir Werewolfe/"},
	{"event": "furizon/beyond",		"user": 164,		"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/Fons/"},
	{"event": "furizon/beyond",		"user": 345,		"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/GazelleIT/"},
	# NO ACCOUNT{"event": "furizon/beyond",		"user": 164,		"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/Glax/"},
	{"event": "furizon/beyond",		"user": 355,		"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/Igen/"},
	# NO ACCOUNT{"event": "furizon/beyond",		"user": 164,		"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/Lale OConner/"},
	# NO ACCOUNT{"event": "furizon/beyond",		"user": 164,		"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/LeBerto6/"},
	{"event": "furizon/beyond",		"user": 221,		"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/Luka/"},
	{"event": "furizon/beyond",		"user": 119,		"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/Mark/"},
	{"event": "furizon/beyond",		"user": 872,		"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/Maxetto/"},
	{"event": "furizon/beyond",		"user": 534,		"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/Micole_Vulpix/"},
	{"event": "furizon/beyond",		"user": 120,		"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/Morkulfr/"},
	{"event": "furizon/beyond",		"user": 200,		"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/Paco folf/"},
	{"event": "furizon/beyond",		"user": 693,		"path": "Furizon Beyond 2023/Furizon Beyond 2023 - User uploads/Pianostrong/compressed/"},
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

	{"event": "furizon/beyond",		"user": 669,		"path": "Furizon Beyond 2023/OFFICIAL/INDOOR Photoshoot - Furizon BEYOND 2023/"}, #soonyx
	{"event": "furizon/beyond",		"user": 862,		"path": "Furizon Beyond 2023/OFFICIAL/Furizon BEYOND - 2023/ari/"},
	{"event": "furizon/beyond",		"user": 11,			"path": "Furizon Beyond 2023/OFFICIAL/Furizon BEYOND - 2023/naraiko/"},
	{"event": "furizon/beyond",		"user": 10,			"path": "Furizon Beyond 2023/OFFICIAL/Furizon BEYOND - 2023/razor/"},
	{"event": "furizon/beyond",		"user": 669,		"path": "Furizon Beyond 2023/OFFICIAL/Furizon BEYOND - 2023/soonyx/"},
	{"event": "furizon/beyond",		"user": 348,		"path": "Furizon Beyond 2023/OFFICIAL/Furizon BEYOND - 2023/zargo/"},


	{"event": "furizon/river-side-2023",		"user": 254,		"path": "Furizon Riverside 2023/Furizon Riverside 2023 - Uploads/Baritz/"},
	{"event": "furizon/river-side-2023",		"user": 120,		"path": "Furizon Riverside 2023/Furizon Riverside 2023 - Uploads/Mork/"},
	{"event": "furizon/river-side-2023",		"user": 152,		"path": "Furizon Riverside 2023/Furizon Riverside 2023 - Uploads/Roll Lee/"},
	{"event": "furizon/river-side-2023",		"user": 669,		"path": "Furizon Riverside 2023/Furizon Riverside 2023 - Uploads/Soonyx/"},
	# NO ACCOUNT{"event": "furizon/river-side-2023",		"user": 669,		"path": "Furizon Riverside 2023/Furizon Riverside 2023 - Uploads/Starlight/"}, #starlight il protogen
	{"event": "furizon/river-side-2023",		"user": 1,			"path": "Furizon Riverside 2023/Furizon Riverside 2023 - Uploads/Stranck/"},
	{"event": "furizon/river-side-2023",		"user": 2,			"path": "Furizon Riverside 2023/Furizon Riverside 2023 - Uploads/Sushi/"},
 
	{"event": "furizon/river-side-2023",		"user": 862,		"path": "Furizon Riverside 2023/OFFICIAL/NON PUBBLICARE - Photoshoot Full Riverside 2023/ari/"},
	{"event": "furizon/river-side-2023",		"user": 10,			"path": "Furizon Riverside 2023/OFFICIAL/NON PUBBLICARE - Photoshoot Full Riverside 2023/razor/"},
	{"event": "furizon/river-side-2023",		"user": 20,			"path": "Furizon Riverside 2023/OFFICIAL/NON PUBBLICARE - Furizon Riverside 2023 Main Photos/hypewolf/"},
	{"event": "furizon/river-side-2023",		"user": 11,			"path": "Furizon Riverside 2023/OFFICIAL/NON PUBBLICARE - Furizon Riverside 2023 Main Photos/nara/"},
	

	{"event": "furizon/overlord",	"user": 135,		"path": "Furizon Overlord 2024/Furizon Overlord 2024 - User uploads/60 foto nome NightFall/"},
	{"event": "furizon/overlord",	"user": 14,			"path": "Furizon Overlord 2024/Furizon Overlord 2024 - User uploads/Arven/"},
	{"event": "furizon/overlord",	"user": 254,		"path": "Furizon Overlord 2024/Furizon Overlord 2024 - User uploads/Baritz/"},
	{"event": "furizon/overlord",	"user": 873,		"path": "Furizon Overlord 2024/Furizon Overlord 2024 - User uploads/Caballino/"},
	{"event": "furizon/overlord",	"user": 155,		"path": "Furizon Overlord 2024/Furizon Overlord 2024 - User uploads/Carty/"},
	{"event": "furizon/overlord",	"user": 259,		"path": "Furizon Overlord 2024/Furizon Overlord 2024 - User uploads/Cavallium/"},
	# NO ACCOUNT{"event": "furizon/overlord",	"user": 259,		"path": "Furizon Overlord 2024/Furizon Overlord 2024 - User uploads/Endiu Tealber/"},
	{"event": "furizon/overlord",	"user": 164,		"path": "Furizon Overlord 2024/Furizon Overlord 2024 - User uploads/Fons The Bun/"},
	# NO ACCOUNT{"event": "furizon/overlord",	"user": 259,		"path": "Furizon Overlord 2024/Furizon Overlord 2024 - User uploads/Glax/"},
	{"event": "furizon/overlord",	"user": 85,			"path": "Furizon Overlord 2024/Furizon Overlord 2024 - User uploads/Jabilo Bearly/"},
	{"event": "furizon/overlord",	"user": 70,			"path": "Furizon Overlord 2024/Furizon Overlord 2024 - User uploads/Kinae/"},
	{"event": "furizon/overlord",	"user": 872,			"path": "Furizon Overlord 2024/Furizon Overlord 2024 - User uploads/Maxetto/"},
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
 
	{"event": "furizon/overlord",	"user": 254,		"rejected": True,		"path": "Furizon Overlord 2024/REJECTED User uploads␠/Baritz/"},
	{"event": "furizon/overlord",	"user": 85,			"rejected": True,		"path": "Furizon Overlord 2024/REJECTED User uploads␠/Jabilo Bearly/"},
	{"event": "furizon/overlord",	"user": 70,			"rejected": True,		"path": "Furizon Overlord 2024/REJECTED User uploads␠/Kinae/"},
	{"event": "furizon/overlord",	"user": 495,		"rejected": True,		"path": "Furizon Overlord 2024/REJECTED User uploads␠/rizapon/"},
	{"event": "furizon/overlord",	"user": 488,		"rejected": True,		"path": "Furizon Overlord 2024/REJECTED User uploads␠/Steve Wolf/"},
 
	{"event": "furizon/overlord",	"user": 20,			"path": "Furizon Overlord 2024/OFFICIAL/FURIZON OVERLORD - 2024/hypewolf/"},
	{"event": "furizon/overlord",	"user": 10,			"path": "Furizon Overlord 2024/OFFICIAL/FURIZON OVERLORD - 2024/razor/"},
 
 
	{"event": "furizon/zenith",	"user": 100,			"path": "Furizon Zenith 2025/Furizon Zenith 2025 - User uploads/Akoro/"},
	{"event": "furizon/zenith",	"user": 31,				"path": "Furizon Zenith 2025/Furizon Zenith 2025 - User uploads/ArMeyk/"},
	{"event": "furizon/zenith",	"user": 57,				"path": "Furizon Zenith 2025/Furizon Zenith 2025 - User uploads/Axl/Compressed/"},
	{"event": "furizon/zenith",	"user": 254,			"path": "Furizon Zenith 2025/Furizon Zenith 2025 - User uploads/Baritz/"},
	{"event": "furizon/zenith",	"user": 295,			"path": "Furizon Zenith 2025/Furizon Zenith 2025 - User uploads/Brusa/"},
	{"event": "furizon/zenith",	"user": 219,			"path": "Furizon Zenith 2025/Furizon Zenith 2025 - User uploads/Buck The Owl/"},
	{"event": "furizon/zenith",	"user": 111,			"path": "Furizon Zenith 2025/Furizon Zenith 2025 - User uploads/CescoCat/"},
	{"event": "furizon/zenith",	"user": 184,			"path": "Furizon Zenith 2025/Furizon Zenith 2025 - User uploads/draco892/"},
	{"event": "furizon/zenith",	"user": 109,			"path": "Furizon Zenith 2025/Furizon Zenith 2025 - User uploads/Egnur/"},
	{"event": "furizon/zenith",	"user": 106,			"path": "Furizon Zenith 2025/Furizon Zenith 2025 - User uploads/Fedix/"},
	{"event": "furizon/zenith",	"user": 211,			"path": "Furizon Zenith 2025/Furizon Zenith 2025 - User uploads/Frank Turri/"},
	{"event": "furizon/zenith",	"user": 138,			"path": "Furizon Zenith 2025/Furizon Zenith 2025 - User uploads/furryalpower/"},
	{"event": "furizon/zenith",	"user": 340,			"path": "Furizon Zenith 2025/Furizon Zenith 2025 - User uploads/Hinoki/"},
	{"event": "furizon/zenith",	"user": 216,			"path": "Furizon Zenith 2025/Furizon Zenith 2025 - User uploads/jessy999/"},
	{"event": "furizon/zenith",	"user": 70,				"path": "Furizon Zenith 2025/Furizon Zenith 2025 - User uploads/Kinae/"},
	{"event": "furizon/zenith",	"user": 428,			"path": "Furizon Zenith 2025/Furizon Zenith 2025 - User uploads/Koru/"},
	{"event": "furizon/zenith",	"user": 284,			"path": "Furizon Zenith 2025/Furizon Zenith 2025 - User uploads/Lasky/"},
	{"event": "furizon/zenith",	"user": 507,			"path": "Furizon Zenith 2025/Furizon Zenith 2025 - User uploads/LazyChemist/"},
	{"event": "furizon/zenith",	"user": 281,			"path": "Furizon Zenith 2025/Furizon Zenith 2025 - User uploads/luca/"},
	{"event": "furizon/zenith",	"user": 431,			"path": "Furizon Zenith 2025/Furizon Zenith 2025 - User uploads/Matsu/"},
	{"event": "furizon/zenith",	"user": 221,			"path": "Furizon Zenith 2025/Furizon Zenith 2025 - User uploads/montagutiluca/"},
	{"event": "furizon/zenith",	"user": 173,			"path": "Furizon Zenith 2025/Furizon Zenith 2025 - User uploads/Murley/"},
	{"event": "furizon/zenith",	"user": 45,				"path": "Furizon Zenith 2025/Furizon Zenith 2025 - User uploads/NiTRO PunkShep/"},
	{"event": "furizon/zenith",	"user": 60,				"path": "Furizon Zenith 2025/Furizon Zenith 2025 - User uploads/Rigel/"},
	{"event": "furizon/zenith",	"user": 361,			"path": "Furizon Zenith 2025/Furizon Zenith 2025 - User uploads/Riven Katharos/"},
	{"event": "furizon/zenith",	"user": 176,			"path": "Furizon Zenith 2025/Furizon Zenith 2025 - User uploads/Sodawn/"},
	{"event": "furizon/zenith",	"user": 465,			"path": "Furizon Zenith 2025/Furizon Zenith 2025 - User uploads/Stephen_Fang/"},
	{"event": "furizon/zenith",	"user": 1,				"path": "Furizon Zenith 2025/Furizon Zenith 2025 - User uploads/Stranck/"},
	{"event": "furizon/zenith",	"user": 2,				"path": "Furizon Zenith 2025/Furizon Zenith 2025 - User uploads/Sushi/"},
	{"event": "furizon/zenith",	"user": 380,			"path": "Furizon Zenith 2025/Furizon Zenith 2025 - User uploads/The Doctor Drake/"},
	{"event": "furizon/zenith",	"user": 429,			"path": "Furizon Zenith 2025/Furizon Zenith 2025 - User uploads/Tudorso/"},
	{"event": "furizon/zenith",	"user": 377,			"path": "Furizon Zenith 2025/Furizon Zenith 2025 - User uploads/UndyBadger/"},
	{"event": "furizon/zenith",	"user": 319,			"path": "Furizon Zenith 2025/Furizon Zenith 2025 - User uploads/Windows 11/"},
	{"event": "furizon/zenith",	"user": 84,				"path": "Furizon Zenith 2025/Furizon Zenith 2025 - User uploads/Xandor/"},
	{"event": "furizon/zenith",	"user": 317,			"path": "Furizon Zenith 2025/Furizon Zenith 2025 - User uploads/Xen/"},
	{"event": "furizon/zenith",	"user": 71,				"path": "Furizon Zenith 2025/Furizon Zenith 2025 - User uploads/Zamy/"},
 
 	{"event": "furizon/zenith",	"user": 70,			"rejected": True,		"path": "Furizon Zenith 2025/Furizon Zenith 2025 - User uploads - REJECTED/Kinae/"},
 	{"event": "furizon/zenith",	"user": 281,		"rejected": True,		"path": "Furizon Zenith 2025/Furizon Zenith 2025 - User uploads - REJECTED/luca/"},
 	{"event": "furizon/zenith",	"user": 221,		"rejected": True,		"path": "Furizon Zenith 2025/Furizon Zenith 2025 - User uploads - REJECTED/montegatiluca/"},
 	{"event": "furizon/zenith",	"user": 319,		"rejected": True,		"path": "Furizon Zenith 2025/Furizon Zenith 2025 - User uploads - REJECTED/Windows 11/"},
 
	{"event": "furizon/zenith",	"user": 20,				"path": "Furizon Zenith 2025/OFFICIAL/hype/"},
	{"event": "furizon/zenith",	"user": 10,				"path": "Furizon Zenith 2025/OFFICIAL/razor/"},


	{"event": "furizon/river-side-2025",		"user": 221,		"path": "Furizon Riverside 2025/Furizon Riverside 2025 - User Uploads/montagutiluca/"},
	{"event": "furizon/river-side-2025",		"user": 299,		"path": "Furizon Riverside 2025/Furizon Riverside 2025 - User Uploads/Reyven/"},
	{"event": "furizon/river-side-2025",		"user": 87,			"path": "Furizon Riverside 2025/Furizon Riverside 2025 - User Uploads/Spectre/"},
	{"event": "furizon/river-side-2025",		"user": 1,			"path": "Furizon Riverside 2025/Furizon Riverside 2025 - User Uploads/stranck/"},
	{"event": "furizon/river-side-2025",		"user": 2,			"path": "Furizon Riverside 2025/Furizon Riverside 2025 - User Uploads/Sushi/"},
	{"event": "furizon/river-side-2025",		"user": 585,		"path": "Furizon Riverside 2025/Furizon Riverside 2025 - User Uploads/TomTheSnep/"},
	{"event": "furizon/river-side-2025",		"user": 8,			"path": "Furizon Riverside 2025/Furizon Riverside 2025 - User Uploads/Yeeno/"},
 
	{"event": "furizon/river-side-2025",		"user": 10,			"path": "Furizon Riverside 2025/OFFICIAL/razor/"},
	{"event": "furizon/river-side-2025",		"user": 30,			"path": "Furizon Riverside 2025/OFFICIAL/shado/"},
	{"event": "furizon/river-side-2025",		"user": 1,			"path": "Furizon Riverside 2025/OFFICIAL/stranck/"},
]


# DONE add rejected uploads
# DONE add dry run printing event name + id; user name obtained from backend + id; number of photos; path
# DONE add method to verify if upload format is supported. Add print if unsupported
# DONE load all photos first and then, for each event, order them by shot date and then filename

eventToUploads = {}

for uploadDir in uploadDirs:
	print(f"**[DEBUG] Processing upload directory: {uploadDir}")
	eventName = uploadDir["event"]
	eventId = eventSlugToId[eventName]
	if (eventId not in eventToUploads):
		eventToUploads[eventId] = []
	event: list = eventToUploads[eventId]
	path = uploadDir["path"]
	photographerId = uploadDir["user"]
	rejected = uploadDir.get("rejected", False)
	filesFound = 0
	for fileName in os.listdir(path):
		if (not os.path.isfile(os.path.join(path, fileName))):
			continue
		if fileName == ".noempty":
			continue
		_, file_extension = os.path.splitext(fileName)
		if file_extension.lower()[1:] not in supportedFormats:
			print(f"**[DEBUG] Unsupported file format '{file_extension}' for file '{fileName}' in path '{path}'. Skipping.")
			continue

		dateTaken = getDate(path, fileName)
		dateTaken = str(dateTaken) if dateTaken else f"---{photographerId}-{fileName}" 
		event.append({
			"fileName": fileName,
			"path": path,
			"photographerId": photographerId,
			"rejected": rejected,
			"dateTaken": dateTaken
		})
		filesFound += 1
	photographerName: str = None
	try:
		photographerName = searchByUserId(photographerId).json()["users"][0]["description"]
	except Exception as e:
		pass
	attentionStr = "!!!!!!!!!!!!" if filesFound < 5 else ""
	print(f"**[DEBUG] Event ({eventId}) '{eventName}' - User ({("%03d" % photographerId)}) '{photographerName.ljust(40)}' - Found {("%04d" % filesFound)} photos - Rejected: {rejected} - Path: '{path}' {attentionStr}")

for eventId, uploads in eventToUploads.items():
	uploads.sort(key=lambda x: x["dateTaken"], reverse=False)
	#print(uploads)

print(json.dumps(eventToUploads))

uploadIds = {}
for eventId, uploads in eventToUploads.items():
	for upload in uploads:
		fileName = upload["fileName"]
		try:
			print(f"**[FINE] Uploading file '{fileName}'")
			uploadId = uploadFileToGallery(upload["path"], fileName, eventId)
			uploadKey = (upload["photographerId"], rejected)
			print(f"**[FINE] Uploading file '{fileName}': Got upload ID = {uploadId}. Storing with key {uploadKey}")
			if (uploadKey not in uploadIds):
				uploadIds[uploadKey] = []
			personalUploadIds: list = uploadIds[uploadKey]
			personalUploadIds.append(uploadId)
			approveUploads(uploadIds)
		except Exception as e:
			print(f"**[ERROR] Uploading file '{fileName}': {e}")
approveUploads(uploadIds, force=True)

# python3 uploadToGallery.py 2>&1 | tee -a log.txt
# grep -e "^\*\*" log.txt > log2.txt