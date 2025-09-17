@import static net.furizon.backend.infrastructure.templating.JteContext.*
@param String fursonaName
@param String otherfursonaName

@template.layout(title = localize("mail.invite_refused.title"), content = @`
    <h2 class="title">${localize("mail.invite_refused.title"}</h2>
    <p class="main-content">${localize("mail.invite_refused.description", otherfursonaName, fursonaName)}</p>
`)