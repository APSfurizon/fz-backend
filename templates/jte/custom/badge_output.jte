@import java.util.List
@param List<String> renderedBadges
<!-- DO NOT MOVE OR RENAME THIS FILE, this is the main template that merges all generated badges -->

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <script>
        var txtLenElements = document.getElementsByClassName("txt-len");
        for (var i = 0; i < txtLenElements.length; i++) {
            var element = txtLenElements[i];
            var text = element.innerText;
            element.style.setProperty("--chars", text.length);
        }
        window.print();
    </script>
    <style>
        :root {
            --page-width: 56.90mm;
            --page-height: 88.90mm;
            --max-containable-text-width: calc(var(--page-width) * 0.70)
        }

        .name-container {
            position: absolute;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 1.8em;
            width: 100%;
            overflow: hidden;
            font-weight: 700;
            letter-spacing: 2px;
        }

        .text-fit {
            font-family: 'main-text-font';
            font-size: 0.8em;
            word-wrap: break-word;
            max-width: var(--max-containable-text-width);
            font-size: max(min(calc(var(--max-containable-text-width) / (var(--chars) * 0.85)), 1.7em), 0.8em);
            text-align: center;
            display: block;
            line-clamp: 2;
            -webkit-line-clamp: 2;
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
            width: 56.90mm;
            min-height: 88.90mm;
        }
        @page {
            size: 56.90mm 88.90mm;
            margin: 0;
        }
        @media print {
            html, body {
                width: 56.90mm;
                height: 88.90mm;
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
        .badge-level-sponsor-normal {
            background-image: url('https://furpanel.furizon.net/static/badge-gen/sponsor.png');
        }
        .badge-level-sponsor-super {
            background-image: url('https://furpanel.furizon.net/static/badge-gen/sponsorSuper.png');
        }
        .badge-level-normal {
            background-image: url('https://furpanel.furizon.net/static/badge-gen/attendee.png');
        }

        /* Import text font */
        @font-face {
            font-family: 'main-text-font';
            font-style: normal;
            font-weight: 400;
            src: url('https://furpanel.furizon.net/static/badge-gen/RoundedEleganceRegular.otf') format('opentype');
        }
    </style>
</head>
<body>
@for(String data : renderedBadges)
        $unsafe{data}
@endfor
</body>
</html>