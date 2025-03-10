@param String nickname
@param String mainOverlayImageUrl
@param String profileImageUrl
@param String staffRole

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="supported-color-schemes" content="light dark">
    <style media="all" type="text/css">
        :root {
            --width: 53.98mm;
            --height: 85.60mm;
        }

        * { color: #bbc6ce; box-sizing: border-box; }
        html,
        body {
            padding: 0px;
            margin: 0px;
            max-width: var(--width);
            width: var(--width);
            height: var(--height);
        }

        body {
            overflow: hidden;
        }

        div.content {
            page-break-after: always;
        }

        div.badge {
            width: var(--width);
            height: var(--height);
            background-color: white;
            overflow: hidden;
            background: #1095c1;
            background-size: contain;

            img.profile {
                position: fixed;
                z-index: -1;
                width: calc(var(--width) - 2mm);
                height: calc(var(--width) - 2mm);
                top: 8mm;
                left: 1mm;
            }

            div.nickname-holder,
            div.logo-holder {
                position: fixed;
                display: flex;
                justify-content: center;
                top: 0px;
                left: 2mm;
                width: calc(var(--width) - 4mm);
                flex-wrap: wrap;
            }

            div.nickname-holder {
                top: 56mm;

                p.nickname {
                    white-space: wrap;
                    word-wrap: break-word;
                    max-width: 100%;
                    text-align: center;
                }
            }

            div.logo-holder {
                top: 2mm;
                img.convention-logo {
                    width: calc(var(--width) - 24mm);
                    height: auto;
                }
            }
        }
    </style>
</head>
<body class="body">
<div class="badge">
    <img class="overlay">
    <img class="profile" src="${profileImageUrl}">
    <div class="nickname-holder">
        <p class="nickname">${nickname}</p>
    </div>
    <div class="logo-holder">
        <img class="convention-logo" src="https://furizon.net/wp-content/uploads/2022/08/Logo_base_no_sfondoo_Furizon_1-1.png">
    </div>
</div>
</body>
</html>