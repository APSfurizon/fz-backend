@param String link
@param String deadline
@param String fursonaName

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Please, update your id document</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="supported-color-schemes" content="light dark">
    <style media="all" type="text/css">
        * { color: #bbc6ce; }
        .body { width: 100%; margin: 0; background-color: #11191f; }
        .container { max-width: 40em; padding: 1em; margin: 0 auto; }
        .title { font-size: 1.75em; margin-bottom: 1.2em; color: #e1e6eb; margin-top: 0; font-family: sans-serif; }
        .main-content { margin-top: 0; font-style: normal; font-weight: 400; font-family: sans-serif;}
        .con-logo { height:3em;}
        .link { color: #fff; font-weight: 600; font-size: medium; background-color: #00000000 }
    </style>
</head>
<div class="body">
    <div class="container">
        <img src="https://fzbe.furizon.net/res/furizon.png" alt="con_logo" title="con_logo" class="con-logo">
        <div>
            <h2 class="title">Please, update your id document</h2>
            <p class="main-content">Dear ${fursonaName},</p>
            <p class="main-content">We have noticed your identification document has expired at the following date: ${deadline}</p>

            <p class="main-content">You can update your document data from the user section of our website:</p>
            <a href="${link}" class="link">Update my data</a>

            <p class="main-content">Keep in mind that to participate in an event you need to present a valid document in the check-in face for identification, and that your document will be transmitted to the host hotel for legal reasons</p>
        </div>
    </div>
</div>
</html>