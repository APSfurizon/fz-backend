@import static net.furizon.backend.infrastructure.templating.JteContext.*
@param String orderCode
@param String refundMoney


@template.layout(title = localize("mail.room_upgade_failed_price.title"), content = @`
    <h2 class="title">${localize("mail.room_upgade_failed_price.title")}</h2>
    <p class="main-content">${localize("mail.room_upgade_failed_price.description", orderCode, refundMoney)}</p>
`)