@param String imageUrl
@param String locales
@param String badgeLevel
@param String fursonaName
@param long fursuitId

<div class="page">
    <p id="name" class="txt-len name-container" style="top: 56.40mm; z-index: 289; --chars: 12">
        <span class="text-fit" id="nameValue">${fursonaName}</span>
    </p>

    <p id="number" class="txt-len" style="top: 64mm; position: absolute; width: 100%; height: 3.30mm; z-index: 288; text-align: center;">
        <span id="numberValue" style="font-family: 'main-text-font'; font-size: 4.94mm; color: #000000ff; ">${fursuitId}</span>
    </p>

    <div id="background" class="${badgeLevel}" style="left: 0.00mm; top: 0.00mm; position: absolute; width: 56.90mm; height: 88.90mm; z-index: 287; background-repeat: no-repeat; background-size: contain"></div>

    <div id="flag" style="left: 22.44mm; top: 53.26mm; position: absolute; width: 12.11mm; height: 9.06mm; z-index: 282; background-repeat: no-repeat; background-size: contain; background-image: url('static/flags/${locales}.svg'); "></div>

    <div id="propic" style="left: 6.77mm; top: 3.47mm; position: absolute; width: 43.35mm; height: 43.35mm; z-index: 281; background-repeat: no-repeat; background-size: contain; background-image: url('${imageUrl}'); "></div>

</div>