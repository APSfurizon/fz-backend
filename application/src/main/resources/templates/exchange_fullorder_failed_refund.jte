@param String fursonaName
@param String otherfursonaName
@param String eventName
@param String orderCode
@param String refundMoney


<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Full order exchange REFUND FAILED!!</title>
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
            <h2 class="title">Full order exchange REFUND FAILED!!</h2>
            <p class="main-content">During the full order transfer <i>${fursonaName} -> ${otherfursonaName}</i> on event ${eventName}, one or more error happened while refunding order ${orderCode}</p>
            <p class="main-content"><i>We "fake refund" (Without sending money back) all money of an order and create a new manual payment to stop accidental real refunds</i></p>
            <p class="main-content">Amount left to refund: <b>${refundMoney}</b></p>
        </div>
    </div>
</div>
</html>