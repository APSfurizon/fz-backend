@import static net.furizon.backend.infrastructure.templating.JteContext.*
@param String link
@param String orderCode

@template.layout(title = localize("mail.reminder_order_link.title"), content = @`
    <h2 class="title">${localize("mail.reminder_order_link.title")}</h2>
    <p class="main-content">${localize("mail.reminder_order_link.description", orderCode)}</p>
    <a href="${link}" class="link">${link}</a>
`)