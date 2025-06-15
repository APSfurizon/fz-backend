CREATE TABLE public.event_settings (
	event_id int8 GENERATED ALWAYS AS IDENTITY NOT NULL,
	badge_upload_deadline timestamptz NOT NULL,
	room_edit_deadline timestamptz NOT NULL,
	reservation_edit_deadline timestamptz NOT NULL,
	booking_start timestamptz NOT NULL,
	early_booking_start timestamptz NOT NULL,
	CONSTRAINT event_settings_pk PRIMARY KEY (event_id)
);

ALTER TABLE public.event_settings ADD CONSTRAINT event_settings_events_fk FOREIGN KEY (event_id) REFERENCES public.events(id);