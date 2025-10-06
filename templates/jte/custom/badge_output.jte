@import java.util.List
@param List<String> renderedBadges
@param String pageWidth
@param String pageHeight
<!-- DO NOT MOVE OR RENAME THIS FILE, this is the main template that merges all generated badges -->

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <style>
        :root {
            --page-width: ${pageWidth};
            --page-height: ${pageHeight};
            --max-containable-text-width: calc(var(--page-width) * 0.75) /* How much space we horizontally take */
        }

        .name-container { /*This must be put on the P containing the attendee name span*/
            position: absolute;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 1.8em;
            width: 100%;
            font-weight: 700;
            letter-spacing: 2px;
        }
        .text-fit { /*This must be put on the span containing the attendee name*/
            word-wrap: break-word;
            max-width: var(--max-containable-text-width);
            font-size: max(min(calc(var(--max-containable-text-width) / (var(--chars) * 0.85)), 1.7em), 0.8em);
            text-align: center;
            display: block;
            line-clamp: 2;
            -webkit-line-clamp: 2;
        }
        .image {
            background-repeat: no-repeat;
            background-size: contain;
        }

        p {
            margin: 0;
        }
        body {
            width: 100%;
            height: 100%;
            position: absolute;
            margin: 0;
            padding: 0;
        }
        * {
            box-sizing: border-box;
            -moz-box-sizing: border-box;
            color: #000000ff;
        }
        .page {
            position: relative;
            width: var(--page-width);
            min-height: var(--page-height);
        }
        @page {
            size: var(--page-width) var(--page-height);
            margin: 0;
        }
        @media print {
            html, body {
                width: var(--page-width);
                height: var(--page-height);
            }
            .page {
                margin: 0;
                border: initial;
                border-radius: initial;
                width: initial;
                min-height: initial;
                box-shadow: initial;
                background: initial;
                page-break-after: always;
            }
        }


        /* +++ BADGE BACKGROUND URLS +++ */
        /* +++ BADGE BACKGROUND URLS +++ */
        /* +++ BADGE BACKGROUND URLS +++ */
        /* Badge levels are defined in the net.furizon.backend.feature.badge.dto.PrintedBadgeLevel enum */
        .badge-level-staff-main {
            background-image: url('https://furpanel.furizon.net/static/badge-gen/mainStaff.png');
        }
        .badge-level-staff-junior {
            background-image: url('https://furpanel.furizon.net/static/badge-gen/juniorStaff.png');
        }
        .badge-level-daily {
            background-image: url('https://furpanel.furizon.net/static/badge-gen/daily.png');
        }
        .badge-level-sponsor-super {
            background-image: url('https://furpanel.furizon.net/static/badge-gen/sponsorSuper.png');
        }
        .badge-level-sponsor-normal {
            background-image: url('https://furpanel.furizon.net/static/badge-gen/sponsor.png');
        }
        .badge-level-normal {
            background-image: url('https://furpanel.furizon.net/static/badge-gen/attendee.png');
        }

        /* +++ Remember to change the font of the text span!! +++ */
        /* Import text font */
        @font-face {
            font-family: 'attendee-name-otf';
            font-style: normal;
            font-weight: 400;
            src: url('https://furpanel.furizon.net/static/badge-gen/AttendeeName.otf') format('opentype');
        }
        @font-face {
            font-family: 'attendee-name-ttf';
            font-style: normal;
            font-weight: 400;
            src: url('https://furpanel.furizon.net/static/badge-gen/AttendeeName.ttf') format('truetype');
        }
        @font-face {
            font-family: 'badge-number-otf';
            font-style: normal;
            font-weight: 400;
            src: url('https://furpanel.furizon.net/static/badge-gen/BadgeNumber.otf') format('opentype');
        }
        @font-face {
            font-family: 'badge-number-ttf';
            font-style: normal;
            font-weight: 400;
            src: url('https://furpanel.furizon.net/static/badge-gen/BadgeNumber.ttf') format('truetype');
        }
        @font-face {
            font-family: 'extra-text-1-otf';
            font-style: normal;
            font-weight: 400;
            src: url('https://furpanel.furizon.net/static/badge-gen/ExtraText1.otf') format('opentype');
        }
        @font-face {
            font-family: 'extra-text-1-ttf';
            font-style: normal;
            font-weight: 400;
            src: url('https://furpanel.furizon.net/static/badge-gen/ExtraText1.ttf') format('truetype');
        }
        @font-face {
            font-family: 'extra-text-2-otf';
            font-style: normal;
            font-weight: 400;
            src: url('https://furpanel.furizon.net/static/badge-gen/ExtraText2.otf') format('opentype');
        }
        @font-face {
            font-family: 'extra-text-2-ttf';
            font-style: normal;
            font-weight: 400;
            src: url('https://furpanel.furizon.net/static/badge-gen/ExtraText2.ttf') format('truetype');
        }
    </style>
</head>
<body>
@for(String data : renderedBadges)
        $unsafe{data}
@endfor
<script>
    //We must put txt-len over the P containing the span of any text
    var txtLenElements = document.getElementsByClassName("txt-len");
    for (var i = 0; i < txtLenElements.length; i++) {
        var element = txtLenElements[i];
        var text = element.innerText;
        element.style.setProperty("--chars", text.length);
    }
    window.print();
</script>
</body>
</html>