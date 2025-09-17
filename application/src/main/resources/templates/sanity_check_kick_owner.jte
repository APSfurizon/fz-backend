@import static net.furizon.backend.infrastructure.templating.JteContext.*
@param String fursonaName
@param String otherfursonaName
@param String roomName
@param String sanityCheckReason

@template.layout(title = localize("mail.sanity_check_kick_owner.title"), content = @`
    <h2 class="title">${localize("mail.too_many_login_attempts.title")}</h2>
    <p class="main-content">${localize("mail.sanity_check_kick_owner.description", fursonaName, otherfursonaName, roomName, sanityCheckReason)}</p>
`)