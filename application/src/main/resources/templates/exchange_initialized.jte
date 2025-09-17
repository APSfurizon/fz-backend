@import static net.furizon.backend.infrastructure.templating.JteContext.*
@param String fursonaName
@param String otherfursonaName
@param String ownerFursonaName
@param String exchangeActionText
@param String link

@template.layout(title = localize("mail.exchange_initialized.title"), content = @`
    <h2 class="title">${localize("mail.exchange_initialized.title"}</h2>
    <p class="main-content">${localize("mail.exchange_initialized.description", fursonaName, exchangeActionText, ownerFursonaName, otherfursonaName)}</p>
    <a href="${link}" class="link">${link}</a>
`)