@import static net.furizon.backend.infrastructure.templating.JteContext.*

@param String fursonaName
@param String roomTypeName
@param String link

@template.layout(title = localize("mail.room_unconfirmed.title"), content = @`
    <h2 class="title">${localize("mail.room_unconfirmed.title")}</h2>
    <p class="main-content">${localize("mail.room_unconfirmed.description", fursonaName, roomTypeName)}</p>
    <a href="${link}" class="link">${localize("mail.room_unconfirmed.action.go_to_room")}</a>
`)