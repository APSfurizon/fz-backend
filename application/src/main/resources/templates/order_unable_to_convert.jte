@import static net.furizon.backend.infrastructure.templating.JteContext.*
@param String orderCode

@template.layout(title = localize("mail.order_unable_to_convert.title"), content = @`
    <h2 class="title">${localize("mail.order_unable_to_convert.title")}</h2>
    <p class="main-content">${localize("mail.order_unable_to_convert.description", orderCode)}</p>
`)