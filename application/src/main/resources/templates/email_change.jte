@import static net.furizon.backend.infrastructure.templating.JteContext.*
@param String email

@template.layout(title = localize("mail.email_change.title"), content = @`
    <h2 class="title">${localize("mail.email_change.title")}</h2>
    <p class="main-content">${localize("mail.email_change.description", email)}</p>
`)