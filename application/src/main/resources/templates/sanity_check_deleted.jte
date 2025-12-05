@import static net.furizon.backend.infrastructure.templating.JteContext.*
@param String fursonaName
@param String roomName
@param String sanityCheckReason
@param String link

@template.layout(title = localize("mail.sanity_check_deleted.title"), content = @`
    <h2 class="title">${localize("mail.sanity_check_deleted.title")}</h2>
    <p class="main-content">${localize("mail.sanity_check_deleted.description", fursonaName, roomName, sanityCheckReason)}</p>
    <a href="${link}" class="link">${localize("mail.sanity_check_deleted.action.email_link")}</a>
`)