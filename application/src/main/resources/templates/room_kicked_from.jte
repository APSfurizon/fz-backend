@import static net.furizon.backend.infrastructure.templating.JteContext.*

@param String fursonaName
@param String ownerFursonaName
@param String roomName
@param String link

@template.layout(title = localize("mail.room_kicked_from.title"), content = @`
    <h2 class="title">${localize("mail.room_kicked_from.title")}</h2>
    <p class="main-content">${localize("mail.room_kicked_from.description", fursonaName, ownerFursonaName, roomName)}</p>
    <a href="${link}" class="link">${localize("mail.room_kicked_from.action.go_to_room")}</a>
`)