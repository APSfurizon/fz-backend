@import static net.furizon.backend.infrastructure.templating.JteContext.*

@param String fursonaName
@param String otherfursonaName
@param String link

@template.layout(title = localize("mail.room_user_left.title", otherfursonaName), content = @`
    <h2 class="title">${localize("mail.room_user_left.title", otherfursonaName)}</h2>
    <p class="main-content">${localize("mail.room_user_left.description", fursonaName, otherfursonaName)}</p>
    <a href="${link}" class="link">${localize("mail.room_user_left.action.go_to_room")}</a>
`)