# -*- coding: utf-8 -*-
from psd_tools import PSDImage
from psd_tools.constants import Resource
from psd_tools.psd.image_resources import ResoulutionInfo
import re, argparse
import codecs

parser = argparse.ArgumentParser(description="A script that converts a Photoshop file into HTML/CSS Edit")
parser.add_argument("-f", "--file", required=True)
parser.add_argument("-m", "--mm", action=argparse.BooleanOptionalAction)
args, leftovers = parser.parse_known_args()

# Debug testing:
# class ArgsObj:
# 	def __init__(self, d=None):
# 		if d is not None:
# 			for key, value in d.items():
# 				setattr(self, key, value)
# args = ArgsObj({
# 	"file": "../Furizon/badges/BadgesZenithMerged.psd",
# 	"mm": True
# })

psd: PSDImage = PSDImage.open(args.file)

widthPx = psd.width
heightPx = psd.height
widthCm = heightCm = 0
if (args.mm):
	# Get size in mm. Reference:
	# - https://github.com/GNOME/gimp/blob/e139e016a58da2545119a51fbf49745535ea22e4/plug-ins/file-psd/psd-image-res-load.c#L567
	# - https://github.com/GNOME/gimp/blob/e139e016a58da2545119a51fbf49745535ea22e4/plug-ins/file-psd/psd.h#L281
	resInfo: ResoulutionInfo = psd.image_resources.get_data(Resource.RESOLUTION_INFO)
	ppcVertical = resInfo.vertical / 65536.0
	if resInfo.vertical_unit != 2:
		ppcVertical /= 2.54
	heightCm = heightPx / ppcVertical
	ppcHorizontal = resInfo.horizontal / 65536.0
	if resInfo.horizontal_unit != 2:
		ppcHorizontal /= 2.54
	widthCm = widthPx / ppcHorizontal

# if layer name is already used for an id append _n, where n is smallest available number
def namelayer(checkname: str, i: int):
	if(checkname in elements):
		i += 1
		# remove _n if i higher than 1
		if(i > 1):
			splitstring = checkname.split('_')
			splitstring.pop()
			checkname = ''.join(splitstring)
		return namelayer(f"{checkname}_{i}", i)
	else:
		checkname = re.sub(',','', checkname)
		checkname = re.sub('\\.','', checkname)
		checkname = re.sub('\\s', '-', checkname)
		checkname = re.sub('\\*', '-', checkname)
		checkname = re.sub('#', '-', checkname)
		checkname = re.sub('©', '', checkname)
		return checkname

def psdColorArrToHexStr(arr) -> str:
    def x(c: int) -> str:
        return "%02x" % int(255 * c)
    return f"#{x(arr[1])}{x(arr[2])}{x(arr[3])}{x(arr[0])}"

def apporximateStr(i: int):
	return "%.2f" % i
def getCorrectDimStr(dim, sizePx, sizeCm):
	#dimPx : maxPx = ? : maxCm
	return f"{apporximateStr(((dim * sizeCm) / sizePx) * 10)}mm" if args.mm else f"{dim}px"

zIndex = 289
elements = []
def layerstoimage(layers: PSDImage):
	global zIndex
	global elements
	html = ''
	for layer in reversed(layers):
		if layer.is_group():
			print(layer)
			site = layerstoimage(layer)
			html += site
		else:
			# process name to make unique and strip special characters
			name = namelayer(layer.name, 0)
			elements.append(name)
			print(f"Processing Layer: {name}")

			# create css
			style = ""
			style += f'left: {getCorrectDimStr(layer.bbox[0], widthPx, widthCm)}; '
			style += f'top: {getCorrectDimStr(layer.bbox[1], heightPx, heightCm)}; '
			style += f'position: absolute; '
			style += f'width: {getCorrectDimStr(layer.bbox[2] - layer.bbox[0], widthPx, widthCm)}; '
			style += f'height: {getCorrectDimStr(layer.bbox[3] - layer.bbox[1], heightPx, heightCm)}; '
			style += f'z-index: {zIndex}; '

			zIndex -= 1

			if layer.kind == 'type':
				texts = ""
				# Extract font for each substring in the text.
				text = layer.engine_dict['Editor']['Text'].value
				fontset = layer.resource_dict['FontSet']
				runlength = layer.engine_dict['StyleRun']['RunLengthArray']
				rundata = layer.engine_dict['StyleRun']['RunArray']
				index = 0
				for length, rd in zip(runlength, rundata):
					substring: str = text[index:index + length]
					substring = substring.replace("\n", "").replace("\r", "")
					stylesheet = rd['StyleSheet']['StyleSheetData']
					font = fontset[stylesheet['Font']]
					index += length
					# What we don't support: markdown (EG, bold, italics, etc) + alignment/justification
					textStyle = ""
					textStyle += f'font-family: {font["Name"]}; '
					textStyle += f'font-size: {getCorrectDimStr(stylesheet["FontSize"], widthPx, widthCm)}; '
					textStyle += f'color: {psdColorArrToHexStr(stylesheet["FillColor"]["Values"])}; '
					texts += f'    <span style="{textStyle}">{substring}</span>\n'
				html += f'  <p id="{name}" class="txt-len" style="{style}">\n{texts}  </p>\n'
	 
			else:
				style += f'background-image: url(\'images/{name}.png\'); '
				# create html
				html += f'  <div class="image" id="{name}" style="{style}"></div>\n'
				# save images as images
				layer_image = layer.topil()
				layer_image.save(f"images/{name}.png")

	return html

cssWidthStr = f"{apporximateStr(widthCm * 10)}mm" if args.mm else f"{psd.width}px"
cssHeightStr = f"{apporximateStr(heightCm * 10)}mm" if args.mm else f"{psd.height}px"

html = f'''
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <style>
        :root {{
            --page-width: {cssWidthStr};
            --page-height: {cssHeightStr};
            --max-containable-text-width: calc(var(--page-width) * 0.75) /* How much space we horizontally take */
        }}

        .name-container {{ /*This must be put on the P containing the attendee name span*/
            position: absolute;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 1.8em;
            width: 100%;
            font-weight: 700;
            letter-spacing: 2px;
        }}
        .text-fit {{ /*This must be put on the span containing the attendee name*/
            word-wrap: break-word;
            max-width: var(--max-containable-text-width);
            font-size: max(min(calc(var(--max-containable-text-width) / (var(--chars) * 0.85)), 1.7em), 0.8em);
            text-align: center;
            display: block;
            line-clamp: 2;
            -webkit-line-clamp: 2;
        }}
        .image {{
            background-repeat: no-repeat;
            background-size: contain;
        }}

        p {{
            margin: 0;
        }}
        body {{
            width: 100%;
            height: 100%;
            position: absolute;
            margin: 0;
            padding: 0;
        }}
        * {{
            box-sizing: border-box;
            -moz-box-sizing: border-box;
            color: #000000ff;
        }}
        .page {{
            position: relative;
            width: var(--page-width);
            min-height: var(--page-height);
        }}
        @page {{
            size: var(--page-width) var(--page-height);
            margin: 0;
        }}
        @media print {{
            html, body {{
                width: var(--page-width);
                height: var(--page-height);
            }}
            .page {{
                margin: 0;
                border: initial;
                border-radius: initial;
                width: initial;
                min-height: initial;
                box-shadow: initial;
                background: initial;
                page-break-after: always;
            }}
        }}


        /* +++ BADGE BACKGROUND URLS +++ */
        /* +++ BADGE BACKGROUND URLS +++ */
        /* +++ BADGE BACKGROUND URLS +++ */
        /* Badge levels are defined in the net.furizon.backend.feature.badge.dto.PrintedBadgeLevel enum */
        .badge-level-staff-main {{
            background-image: url('https://furpanel.furizon.net/static/badge-gen/mainStaff.png');
        }}
        .badge-level-staff-junior {{
            background-image: url('https://furpanel.furizon.net/static/badge-gen/juniorStaff.png');
        }}
        .badge-level-daily {{
            background-image: url('https://furpanel.furizon.net/static/badge-gen/daily.png');
        }}
        .badge-level-sponsor-super {{
            background-image: url('https://furpanel.furizon.net/static/badge-gen/sponsorSuper.png');
        }}
        .badge-level-sponsor-normal {{
            background-image: url('https://furpanel.furizon.net/static/badge-gen/sponsor.png');
        }}
        .badge-level-normal {{
            background-image: url('https://furpanel.furizon.net/static/badge-gen/attendee.png');
        }}

        /* +++ Remember to change the font of the text span!! +++ */
        /* Import text font */
        @font-face {{
            font-family: 'attendee-name-otf';
            font-style: normal;
            font-weight: 400;
            src: url('https://furpanel.furizon.net/static/badge-gen/AttendeeName.otf') format('opentype');
        }}
        @font-face {{
            font-family: 'attendee-name-ttf';
            font-style: normal;
            font-weight: 400;
            src: url('https://furpanel.furizon.net/static/badge-gen/AttendeeName.ttf') format('truetype');
        }}
        @font-face {{
            font-family: 'badge-number-otf';
            font-style: normal;
            font-weight: 400;
            src: url('https://furpanel.furizon.net/static/badge-gen/BadgeNumber.otf') format('opentype');
        }}
        @font-face {{
            font-family: 'badge-number-ttf';
            font-style: normal;
            font-weight: 400;
            src: url('https://furpanel.furizon.net/static/badge-gen/BadgeNumber.ttf') format('truetype');
        }}
        @font-face {{
            font-family: 'extra-text-1-otf';
            font-style: normal;
            font-weight: 400;
            src: url('https://furpanel.furizon.net/static/badge-gen/ExtraText1.otf') format('opentype');
        }}
        @font-face {{
            font-family: 'extra-text-1-ttf';
            font-style: normal;
            font-weight: 400;
            src: url('https://furpanel.furizon.net/static/badge-gen/ExtraText1.ttf') format('truetype');
        }}
        @font-face {{
            font-family: 'extra-text-2-otf';
            font-style: normal;
            font-weight: 400;
            src: url('https://furpanel.furizon.net/static/badge-gen/ExtraText2.otf') format('opentype');
        }}
        @font-face {{
            font-family: 'extra-text-2-ttf';
            font-style: normal;
            font-weight: 400;
            src: url('https://furpanel.furizon.net/static/badge-gen/ExtraText2.ttf') format('truetype');
        }}
    </style>
</head>
<body>
  <div class="page">
{layerstoimage(psd)}
  </div>
<script>
    var txtLenElements = document.getElementsByClassName("txt-len");
    for (var i = 0; i < txtLenElements.length; i++) {{
        var element = txtLenElements[i];
        var text = element.innerText;
        element.style.setProperty("--chars", text.length);
    }}
    window.print();
</script>
</body>
</html>
'''

f = codecs.open('index.html','w', "utf-8")
f.write(html)
f.close()
