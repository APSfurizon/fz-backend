@import static net.furizon.backend.infrastructure.templating.JteContext.*
@param String link

@template.layout(title = localize("mail.password_reset.title"), content = @`
    <h2 class="title">${localize("mail.password_reset.title")}</h2>
    <p class="main-content">${localize("mail.password_reset.description")}</p>
    <a href="${link}" class="link">${link}</a>
    <p class="main-content">${localize("mail.password_reset.advise")}</p>
`)