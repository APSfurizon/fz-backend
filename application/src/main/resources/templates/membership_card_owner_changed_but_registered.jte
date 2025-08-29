@param String orderCode
@param String cardId
@param String cardIdInYear
@param String orderPrevOwnerId
@param String orderOwnerId

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Membership card management fatal error</title>
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
            <h2 class="title">Membership card management fatal error</h2>
            <p class="main-content">Order <b>${orderCode}</b> has already generated and REGISTERED the membeship card (<i>${cardId}/${cardIdInYear}</i>), BUT the order's owner has changed (<i>${orderPrevOwnerId} -> ${orderOwnerId}</i>).</p>
            <p class="main-content">Manual fix is needed to get money and re-emit a membership card for the new owner!</p>
        </div>
    </div>
</div>
</html>