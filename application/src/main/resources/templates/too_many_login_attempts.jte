@import static net.furizon.backend.infrastructure.templating.JteContext.*

@template.layout(title = localize("mail.too_many_login_attempts.title"), content = @`
    <h2 class="title">${localize("mail.too_many_login_attempts.title")}</h2>
    <p class="main-content">${localize("mail.too_many_login_attempts.description")}</p>
`)