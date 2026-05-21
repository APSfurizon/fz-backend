SELECT orders.user_id FROM orders WHERE orders.event_id = EVENT_ID
AND orders.id NOT IN (SELECT rooms.order_id FROM rooms) -- Doesn't own a created room
AND orders.order_status = 2 -- order is paid
AND orders.user_id NOT IN (
    SELECT room_guests.user_id FROM room_guests
    INNER JOIN rooms ON room_guests.room_id = rooms.room_id
    INNER JOIN orders ON rooms.order_id = orders.id AND orders.event_id = EVENT_ID
) -- user is not in a room
AND orders.order_room_capacity > 0; -- order has a room