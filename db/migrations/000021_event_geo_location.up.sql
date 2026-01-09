BEGIN;

ALTER TABLE IF EXISTS events ADD COLUMN IF NOT EXISTS event_geo_lat FLOAT NULL DEFAULT NULL;
ALTER TABLE IF EXISTS events ADD COLUMN IF NOT EXISTS event_geo_lon FLOAT NULL DEFAULT NULL;

ALTER TABLE events ADD CONSTRAINT event_geo_lat_check CHECK (((event_geo_lat >= -90)  AND (event_geo_lat <= 90)));
ALTER TABLE events ADD CONSTRAINT event_geo_lon_check CHECK (((event_geo_lon >= -180) AND (event_geo_lon <= 180)));

COMMIT;

