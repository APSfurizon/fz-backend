@import static net.furizon.backend.infrastructure.templating.JteContext.*
@param String fursonaName
@param String roomName
@param String sanityCheckReason

@template.layout(title = localize("mail.sanity_check_kick_user.title"), content = @`
    <h2 class="title">${localize("mail.sanity_check_kick_user.title")}</h2>
    <p class="main-content">${localize("mail.sanity_check_kick_user.description", fursonaName, roomName, sanityCheckReason)}</p>
`)