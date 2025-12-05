@import static net.furizon.backend.infrastructure.templating.JteContext.*
@param String fursonaName
@param String eventName
@param String orderCode
@param String duplicateOrderCode
@param String link

@template.layout(title = localize("mail.order_duplicate_detected.title"), content = @`
    <h2 class="title">${localize("mail.order_duplicate_detected.title")}</h2>
    <p class="main-content">${localize("mail.order_duplicate_detected.description", fursonaName, eventName, orderCode, duplicateOrderCode)}</p>
    <a href="${link}" class="link">${localize("mail.order_duplicate_detected.action.email_link")}</a>
`)