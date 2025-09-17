@import static net.furizon.backend.infrastructure.templating.JteContext.*
@param String link
@param String deadline
@param String fursonaName

@template.layout(title = localize("mail.reminder_id_expired.title"), content = @`
    <h2 class="title">${localize("mail.reminder_id_expired.title")}</h2>
    <p class="main-content">${localize("mail.reminder_id_expired.description", fursonaName, deadline)},</p>
    <a href="${link}" class="link">${localize("mail.reminder_id_expired.action.update")}</a>
`)