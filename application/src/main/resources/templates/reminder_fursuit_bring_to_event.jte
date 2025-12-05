@import static net.furizon.backend.infrastructure.templating.JteContext.*
@param String link
@param String fursonaName
@param String eventName

@template.layout(title = localize("mail.reminder_fursuit_bring-to-current-event.title"), content = @`
    <h2 class="title">${localize("mail.reminder_fursuit_bring-to-current-event.title")}</h2>
    <p class="main-content">${localize("mail.reminder_fursuit_bring-to-current-event.description", fursonaName, eventName)},</p>
    <a href="${link}" class="link">${localize("mail.reminder_fursuit_bring-to-current-event.email_link")}</a>
`)