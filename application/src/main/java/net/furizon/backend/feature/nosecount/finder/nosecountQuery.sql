SELECT
    users.user_id,
    users.user_fursona_name,
    --users.user_locale--,
    --media.media_path,--
    --media.media_type,--
    media.media_id,
    orders.order_daily_days,
    orders.order_sponsorship_type,

    rooms.room_id,
    rooms.room_name,
    roomOwnerOrder.order_room_capacity,
    roomOwnerOrder.order_room_pretix_item_id,
    roomOwnerOrder.order_room_internal_name,
    roomOwnerOrder.order_hotel_internal_name
FROM users
INNER JOIN orders
ON users.user_id = orders.user_id AND orders.event_id = 6 AND users.show_in_nosecount = true

LEFT JOIN room_guests
ON users.user_id = room_guests.user_id
LEFT JOIN rooms
ON room_guests.room_id = rooms.room_id --AND rooms.show_in_nosecount = true--
LEFT JOIN orders roomOwnerOrder
ON roomOwnerOrder.id = rooms.order_id

LEFT JOIN media
ON users.media_id_propic = media.media_id


ORDER BY rooms.room_id;