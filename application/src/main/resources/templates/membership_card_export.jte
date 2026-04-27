@import static net.furizon.backend.infrastructure.templating.JteContext.*
@param String cardIdInYear
@param String lastName
@param String firstName
@param String fiscalCode
@param String deadline
@param String birthday

@template.layout(title = localize("mail.membership_card_export.title", cardIdInYear), content = @`
    <h2 class="title">${localize("mail.membership_card_export.title", cardIdInYear)}</h2>
    <p class="main-content">${localize("mail.membership_card_export.description", deadline)}</p>
    <div style="background-color: #ffffff; border: 0.25em solid #2479b9; margin: 1.25em 0; padding: 2em; box-sizing: border-box; font-family: sans-serif; color: #000000; border-radius: 10px">
        <table width="100%" cellpadding="0" cellspacing="0" border="0" style="margin-bottom: 3.75em;">
            <tr>
                <td style="width: 100%; direction: rtl; text-align: left; vertical-align: top;">
                    <div style="width: 100%; max-width: 200px; text-align: right; vertical-align: top; display: inline-block; direction: ltr; margin-bottom: 1em">
                        <img src="https://furpanel.furizon.net/images/logo-light.png" alt="Logo" style="max-width: 100%; height: auto; display: inline-block; border: 0; vertical-align: top; margin: 0;">
                    </div>
                    <div style="width: 100%; max-width: 250px; text-align: left; display: inline-block; vertical-align: top; direction: ltr;">
                        <div style="font-size: 1.75em; font-weight: bold; color: #000000;">${lastName}</div>
                        <div style="font-size: 1.75em; font-weight: bold; margin-bottom: 0.6em; color: #000000;">${firstName}</div>
                        <div style="font-size: 1em; line-height: 1.5; color: #000000;">${localize("mail.membership_card_export.fiscal_code")}: ${fiscalCode}</div>
                        <div style="font-size: 1em; line-height: 1.5; color: #000000;">${localize("mail.membership_card_export.birthday")}: ${birthday}</div>
                    </div>
                </td>
            </tr>
        </table>
        <div style="color: #000000; font-size: 1.15em">${localize("mail.membership_card_export.expire")}: ${deadline}</div>
        <div style="color: #000000; font-weight: bold; font-size: 1.15em; font-variant-numeric: tabular-nums;">${cardIdInYear}</div>
    </div>
`)