@import static net.furizon.backend.infrastructure.templating.JteContext.*
@param String orderCode
@param String cardId
@param String cardIdInYear
@param String orderPrevOwnerId
@param String orderOwnerId

@template.layout(title = localize("mail.membership_card_owner_changed_but_registered.title"), content = @`
    <h2 class="title">${localize("mail.membership_card_owner_changed_but_registered.title")}</h2>
    <p class="main-content">${localize("mail.membership_card_owner_changed_but_registered.description", orderCode, cardId, cardIdInYear, orderPrevOwnerId, orderOwnerId)}</p>
`)