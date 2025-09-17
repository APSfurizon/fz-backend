@import static net.furizon.backend.infrastructure.templating.JteContext.*

@param String fursonaName
@param String roomTypeName
@param String roomCapacity
@param String roomGuestNo
@param String deadline
@param String link

@template.layout(title = localize("mail.reminder_room_not_full.title"), content = @`
    <h2 class="title">${localize("mail.reminder_room_not_full.title")}</h2>
    <p class="main-content">${localize("mail.reminder_room_not_full.description", fursonaName, roomTypeName, roomCapacity, roomGuestNo, deadline)}</p>
    <a href="${link}" class="link">${localize("mail.reminder_room_not_full.action.go_to_room")}</a>
`)