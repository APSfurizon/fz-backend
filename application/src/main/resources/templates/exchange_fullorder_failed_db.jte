@import static net.furizon.backend.infrastructure.templating.JteContext.*
@param String fursonaName
@param String otherfursonaName
@param String orderCode

@template.layout(title = localize("mail.exchange_fullorder_failed_db.title"), content = @`
    <h2 class="title">${localize("mail.exchange_fullorder_failed_db.title")}</h2>
    <p class="main-content">${localize("mail.exchange_fullorder_failed_db.description", orderCode, fursonaName, otherfursonaName)}</p>
`)