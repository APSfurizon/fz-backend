@import java.util.List
@param List<String> renderedBadges
<!-- DO NOT MOVE OR RENAME THIS FILE, this is the main template that merges all generated badges -->

<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <title>Confirm your email</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="supported-color-schemes" content="light dark">
        <title>Badges</title>
        <style media="all" type="text/css">
            @page { size: 53.98mm 86.60mm; }
            * { box-sizing: border-box; }
            div.rendered-badge { break-after: page; }
        </style>
    </head>
    <div class="body">
        @for(String data : renderedBadges)
            <div class="rendered-badge">
                $unsafe{data}
            </div>
        @endfor
    </div>
</html>