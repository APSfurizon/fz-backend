@param String link
@param String deadline
@param String fursonaName
@param String roomTypeName
@param String roomCapacity
@param String roomGuestNo

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>You have not filled your room yet!</title>
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
            <h2 class="title">You have not filled your room yet!</h2>
            <p class="main-content">Dear ${fursonaName},</p>
            <p class="main-content">We have noticed you have bought a <i>${roomTypeName}</i>, but you haven't completely filled it yet!</p>
            <p class="main-content">Your room holds up to ${roomCapacity} people, but currently there are only ${roomGuestNo}.</p>
            <p class="main-content">If this is intentional, you can just ignore this email. However, if there's someone who still needs to be added to your room, please, make sure to do it before the ${deadline}, so we can communicate to the Hotel's staff the attendees who are going to stay in the hotel by night. Incomplete rooms may not be managed correctly by the hotel during check-in and cause delays or inconvenience in the room assignments</p>
            <p class="main-content">You can manage your room here:</p>
            <a href="${link}" class="link">${link}</a>
        </div>
    </div>
</div>
</html>