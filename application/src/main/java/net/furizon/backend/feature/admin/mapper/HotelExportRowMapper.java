package net.furizon.backend.feature.admin.mapper;

import net.furizon.backend.feature.admin.dto.HotelExportRow;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.jooq.generated.tables.Orders;
import org.jooq.Record;

import static net.furizon.jooq.generated.Tables.AUTHENTICATIONS;
import static net.furizon.jooq.generated.Tables.MEMBERSHIP_INFO;
import static net.furizon.jooq.generated.Tables.ORDERS;
import static net.furizon.jooq.generated.Tables.ROOMS;
import static net.furizon.jooq.generated.Tables.USERS;

public class HotelExportRowMapper {
    public static HotelExportRow map(Record record, Orders roomOwnerOrder, PretixInformation pretixInformation) {
        String region = record.get(MEMBERSHIP_INFO.INFO_REGION);
        String birthRegion = record.get(MEMBERSHIP_INFO.INFO_BIRTH_REGION);
        return HotelExportRow.builder()
                .roomTypeName(pretixInformation.getRoomNamesFromRoomPretixItemId(
                    record.get(roomOwnerOrder.ORDER_ROOM_PRETIX_ITEM_ID)).entrySet().iterator().next().getValue()
                )
                .roomId(record.get(ROOMS.ROOM_ID))
                .firstName(record.get(MEMBERSHIP_INFO.INFO_FIRST_NAME))
                .lastName(record.get(MEMBERSHIP_INFO.INFO_LAST_NAME))
                .birthday(record.get(MEMBERSHIP_INFO.INFO_BIRTHDAY))
                .birthLocation(
                    record.get(MEMBERSHIP_INFO.INFO_CITY) + " "
                    + (birthRegion == null ? "" : "(" + birthRegion + ") ")
                    + "- " + record.get(MEMBERSHIP_INFO.INFO_BIRTH_COUNTRY)
                )
                .fullResidenceAddress(
                    record.get(MEMBERSHIP_INFO.INFO_ADDRESS) + " - "
                    + record.get(MEMBERSHIP_INFO.INFO_CITY)
                    + (region == null ? " " : " (" + region + ") ")
                    + record.get(MEMBERSHIP_INFO.INFO_ZIP) + " - "
                    + record.get(MEMBERSHIP_INFO.INFO_COUNTRY)
                )
                .email(record.get(AUTHENTICATIONS.AUTHENTICATION_EMAIL))
                .phone(
                    record.get(MEMBERSHIP_INFO.INFO_PHONE_PREFIX) + " "
                    + record.get(MEMBERSHIP_INFO.INFO_PHONE)
                )
                .userId(record.get(USERS.USER_ID))
                .userName(record.get(USERS.USER_FURSONA_NAME))
                .orderCode(record.get(ORDERS.ORDER_CODE))
                .roomOwnerOrderCode(record.get(roomOwnerOrder.ORDER_CODE))
                .requiresAttention(record.get(ORDERS.ORDER_REQUIRES_ATTENTION))
                .comment(record.get(ORDERS.ORDER_INTERNAL_COMMENT))
            .build();
    }
}
