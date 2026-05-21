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
    <p id="name" class="txt-len name-container" style="top: 51.3mm; height: 6.86mm; z-index: 288; ">
        <span class="text-fit" style="font-family: 'attendee-name-ttf'; color: #353d49ff; ">${fursonaName}</span>
    </p>


    <p id="number" class="txt-len" style="left: 18.88mm; top: 60.54mm; width: 17.27mm; height: 3.98mm; z-index: 289; ">
        <span class="text-fit" style="font-family: 'badge-number-ttf'; font-size: 6.09mm; color: #353d49ff; ">${serialNo}</span>
    </p>

    <div class="image ${badgeLevel}" id="background" style="left: 0.00mm; top: 0.00mm; width: 55.29mm; height: 87.12mm; z-index: 286; "></div>

    <div class="image" id="flag" style="left: 7.96mm; top: 40.98mm; width: 11.68mm; height: 8.89mm; z-index: 279; background-image: url('https://furpanel.furizon.net/static/flags/${locales}.svg'); "></div>

    <div class="image" id="propic" style="left: 8.21mm; top: 4.57mm; width: 38.86mm; height: 38.27mm; z-index: 278; background-image: url('${imageUrl}'); "></div>

</div>