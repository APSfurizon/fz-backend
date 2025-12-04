@import static net.furizon.backend.infrastructure.templating.JteContext.*
@param String link

@template.layout(title = localize("mail.email_confirm.title"), content = @`
    <h2 class="title">${localize("mail.email_confirm.title")}</h2>
    <p class="main-content">${localize("mail.email_confirm.description")}</p>
    <a href="${link}" class="link">${link}</a>
    <p class="main-content">${localize("mail.email_confirm.advise")}</p>
`)