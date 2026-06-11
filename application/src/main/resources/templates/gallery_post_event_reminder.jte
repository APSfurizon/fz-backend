@import static net.furizon.backend.infrastructure.templating.JteContext.*
@param String link
@param String eventName
@param String fursonaName

@template.layout(title = localize("mail.gallery_post_event_reminder.title"), content = @`
    <h2 class="title">${localize("mail.gallery_post_event_reminder.title")}</h2>
    <p class="main-content">${localize("mail.gallery_post_event_reminder.description", fursonaName, eventName)}</p>
    <a href="${link}" class="link">${link}</a>
`)