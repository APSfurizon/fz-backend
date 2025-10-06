@param String imageUrl
@param String imageMimeType

@param String locales
@param String badgeLevel
@param String sponsorship

@param long userId
@param long serialNo
@param String orderCode

@param String fursonaName

@param Long fursuitId
@param String fursuitName
@param String fursuitSpecies


<div class="page">
    <p id="name" class="txt-len name-container" style="top: 102.45mm; height: 5.59mm; z-index: 288; ">
        <span class="text-fit" style="font-family: 'attendee-name-ttf'; color: #1c1c1bff; ">${fursuitName}</span>
    </p>


    <p id="number" class="txt-len" style="left: 3.35mm; top: 3.85mm; width: 11.77mm; height: 5.16mm; z-index: 289; ">
        <span style="font-family: 'badge-number-ttf'; font-size: 6.06mm; color: #ffffffff; ">${orderCode}</span>
    </p>


    <div class="image" id="background" style="left: 0.00mm; top: 0.00mm; width: 80.01mm; height: 124.80mm; z-index: 287; background-image: url('https://furpanel.furizon.net/static/badge-gen/fursuitBadge.png')"></div>

    <div class="image" id="flag" style="left: 61.81mm; top: 3.39mm; width: 16.26mm; height: 12.28mm; z-index: 280; background-image: url('https://furpanel.furizon.net/static/flags/${locales}.svg'); "></div>

    <div class="image" id="propic" style="left: 18.63mm; top: 53.09mm; width: 42.84mm; height: 42.67mm; z-index: 279; background-image: url('https://furpanel.furizon.net/${imageUrl}'); "></div>

</div>