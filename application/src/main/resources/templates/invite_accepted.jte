@import static net.furizon.backend.infrastructure.templating.JteContext.*
@param String fursonaName
@param String otherfursonaName

@template.layout(title = localize("mail.invite_accepted.title", otherfursonaName), content = @`
    <h2 class="title">${localize("mail.invite_accepted.title", otherfursonaName)}</h2>
    <p class="main-content">${localize("mail.invite_accepted.description", otherfursonaName, fursonaName)}</p>
`)