@import static net.furizon.backend.infrastructure.templating.JteContext.*
@param String fursonaName
@param String otherfursonaName
@param String eventName
@param String orderCode
@param String refundMoney

@template.layout(title = localize("mail.exchange_fullorder_failed_refund.title"), content = @`
    <h2 class="title">${localize("mail.exchange_fullorder_failed_refund.title")}</h2>
    <p class="main-content">${localize("mail.exchange_fullorder_failed_refund.description", orderCode, fursonaName, otherfursonaName, eventName, refundMoney)}</p>
`)