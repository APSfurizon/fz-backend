@import static net.furizon.backend.infrastructure.templating.JteContext.*

@param String fursonaName
@param String ownerFursonaName
@param String link

@template.layout(title = localize("mail.room_has_new_owner.title"), content = @`
    <h2 class="title">${localize("mail.room_has_new_owner.title")}</h2>
    <p class="main-content">${localize("mail.room_has_new_owner.description", fursonaName, ownerFursonaName)}</p>
    <a href="${link}" class="link">${localize("mail.room_has_new_owner.action.go_to_room")}</a>
`)