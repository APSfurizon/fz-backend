@import static net.furizon.backend.infrastructure.templating.JteContext.*
@param String link
@param String deadline
@param String fursonaName
@param String fursuitName

@template.layout(title = localize("mail.reminder_fursuit_badge_upload.title"), content = @`
    <h2 class="title">${localize("mail.reminder_fursuit_badge_upload.title")}</h2>
    <p class="main-content">${localize("mail.reminder_fursuit_badge_upload.description", fursonaName, fursuitName, deadline)},</p>
    <a href="${link}" class="link">${localize("mail.reminder_fursuit_badge_upload.upload_badge")}</a>
`)