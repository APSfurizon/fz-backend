@import static net.furizon.backend.infrastructure.templating.JteContext.*
@param String orderCode


@template.layout(title = localize("mail.room_upgade_failed_position.title"), content = @`
    <h2 class="title">${localize("mail.room_upgade_failed_position.title")}</h2>
    <p class="main-content">${localize("mail.room_upgade_failed_position.description", orderCode)}</p>
`)