import requests
import json
import sys
import os

if len(sys.argv) != 2:
    print(f"Usage: python {sys.argv[0]} <event_id>")
    sys.exit(1)

event_id = sys.argv[1]

OUTPUT_DIR = "data/public/static/persistentCounts/"
IMAGE_DIR = f"{OUTPUT_DIR}imgs/"
BASE_NEW_URL = "https://fzbe.furizon.net/static/persistentCounts/imgs/"
JSON_FILENAME = f"count_{event_id}.json"

HEADERS = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:133.0) Gecko/20100101 Firefox/133.0',
    'Accept': '*/*',
    'Accept-Language': 'en-US,en;q=0.8,en-US;q=0.5,en;q=0.3',
    'Referer': 'https://furpanel.furizon.net/',
    'Origin': 'https://furpanel.furizon.net',
    'Connection': 'keep-alive',
}

BASE_URL = "http://localhost:9091/"
BASE_URL_API = f"{BASE_URL}api/v1/"

REPLACE_IMG_URL_SOURCE = "https://fzbe.furizon.net"
REPLACE_IMG_URL_TARGET = "http://127.0.0.1"
REPLACE_IMG_URL_HOST = "fzbe.furizon.net"

exportObject = {}

os.makedirs(IMAGE_DIR, exist_ok=True)
savedImages = set([f for f in os.listdir(IMAGE_DIR) if os.path.isfile(os.path.join(IMAGE_DIR, f))])

def handleMediaObject(mediaObject):
    global savedImages
    if (mediaObject is None) or ("mediaUrl" not in mediaObject) or (not mediaObject["mediaUrl"]):
        return
    mediaUrl = mediaObject["mediaUrl"]
    fileName = os.path.basename(mediaUrl)
    if fileName not in savedImages:
        print(f"Downloading {fileName}...")
        
        headers = {**HEADERS}
        if mediaUrl.startswith(REPLACE_IMG_URL_SOURCE):
            mediaUrl = REPLACE_IMG_URL_TARGET + mediaUrl[len(REPLACE_IMG_URL_SOURCE):]
            headers["Host"] = REPLACE_IMG_URL_HOST
        
        img = requests.get(mediaUrl, headers=headers)
        with open(os.path.join(IMAGE_DIR, fileName), 'wb') as f:
            f.write(img.content)
            savedImages.add(fileName)
    mediaObject["mediaUrl"] = f"{BASE_NEW_URL}{fileName}"


print(f"Getting fursuit counts for event ID: {event_id}...")
fursuits = requests.get(f"{BASE_URL_API}counts/fursuit?event-id={event_id}", headers=HEADERS).json()
for fursuit in fursuits["fursuits"]:
    handleMediaObject(fursuit["propic"])
exportObject["fursuits"] = fursuits


print(f"Getting sponsor counts for event ID: {event_id}...")
sponsors = requests.get(f"{BASE_URL_API}counts/sponsors?event-id={event_id}", headers=HEADERS).json()
for users in sponsors["users"].values():
    for user in users:
        handleMediaObject(user["propic"])
exportObject["sponsors"] = sponsors


print(f"Getting admins counts for event ID: {event_id}...")
admins = requests.get(f"{BASE_URL_API}counts/admins?event-id={event_id}", headers=HEADERS).json()
for role in admins["roles"]:
    for user in role["members"]:
        handleMediaObject(user["propic"])
exportObject["admins"] = admins
        

print(f"Getting nose counts for event ID: {event_id}...")
bopos = requests.get(f"{BASE_URL_API}counts/bopos?event-id={event_id}", headers=HEADERS).json()
for hotel in bopos["hotels"]:
    for roomType in hotel["roomTypes"]:
        for room in roomType["rooms"]:
            for guest in room["guests"]:
                user = guest["user"]
                handleMediaObject(user["propic"])
for u in bopos["roomlessFurs"]:
    user = u["user"]
    handleMediaObject(user["propic"])
for date in bopos["dailyFurs"].values():
    for user in date:
        handleMediaObject(user["propic"])
exportObject["bopos"] = bopos

print(f"Storing exported json to {OUTPUT_DIR}{JSON_FILENAME}")
with open(f"{OUTPUT_DIR}{JSON_FILENAME}", 'w') as f:
    f.write(json.dumps(exportObject))