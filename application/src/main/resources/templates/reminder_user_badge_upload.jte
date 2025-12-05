@import static net.furizon.backend.infrastructure.templating.JteContext.*

@param String fursonaName
@param String deadline
@param String link

@template.layout(title = localize("mail.reminder_user_badge_upload.title"), content = @`
    <h2 class="title">${localize("mail.reminder_user_badge_upload.title")}</h2>
    <p class="main-content">${localize("mail.reminder_user_badge_upload.description", fursonaName, deadline)}</p>
    <a href="${link}" class="link">${localize("mail.reminder_user_badge_upload.action.email_link")}</a>
`)