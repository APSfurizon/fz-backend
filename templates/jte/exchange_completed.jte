@param String fursonaName
@param String otherfursonaName
@param String ownerFursonaName
@param String exchangeActionText

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>The exchange is complete!</title>
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
        .link { text-decoration: none; background-color: #1095c1; color: #fff; padding: 1em; border-radius: 5px; font-weight: 600; }
    </style>
</head>
<div class="body">
    <div class="container">
        <img src="https://fzbe.furizon.net/res/furizon.png" alt="con_logo" title="con_logo" class="con-logo">
        <div>
            <h2 class="title">The exchange is complete!</h2>
            <p class="main-content">Dear ${fursonaName},</p>
            <p class="main-content">You're receiving this email because the <b>${exchangeActionText}</b> between you and ${otherfursonaName} has been completed!</p>
        </div>
    </div>
</div>
</html>