@param String orderCode
@param String cardId
@param String cardIdInYear

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Membership card management warning</title>
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
            <h2 class="title">Membership card management warning</h2>
            <p class="main-content">Order <b>${orderCode}</b> has already generated the membeship card (<i>${cardId}/${cardIdInYear}</i>), BUT it was later deleted from the order</p>
            <p class="main-content">However, a membership card after this one was already registered, so I cannot delete it and shift the enumerations of the other to not leave "holes"</p>
        </div>
    </div>
</div>
</html>