@import static net.furizon.backend.infrastructure.templating.JteContext.*
@param String fursonaName

@template.layout(title = localize("mail.password_changed.title"), content = @`
    <h2 class="title">${localize("mail.password_changed.title"}</h2>
    <p class="main-content">${localize("mail.password_changed.description", fursonaName )}</p>
`)