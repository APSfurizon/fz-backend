@import static net.furizon.backend.infrastructure.templating.JteContext.*
@param String orderCode
@param String cardId
@param String cardIdInYear

@template.layout(title = localize("mail.membership_card_already_registered.title"), content = @`
    <h2 class="title">${localize("mail.membership_card_already_registered.title")}</h2>
    <p class="main-content">${localize("mail.membership_card_already_registered.description", orderCode, cardId, cardIdInYear)}</p>
`)