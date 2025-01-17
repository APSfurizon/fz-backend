package net.furizon.backend.feature.pretix.ordersworkflow;

public enum OrderWorkflowErrorCode {
    SERVER_ERROR, //An error occurred while computing the request
    MEMBERSHIP_MULTIPLE_DONE, //An user owns multiple membership cards within an year
    MEMBERSHIP_MISSING, //An user has an order for an event, but no membership card in that year
    ORDER_NOT_FOUND, //An user is trying to link an order, however we were unable to retrieve it
    ORDER_MULTIPLE_DONE, //An user owns multiple orders for an event
    ORDER_SECRET_NOT_MATCH, //The provided secret does not match the one of the order
    ORDER_ALREADY_OWNED_BY_SOMEBODY_ELSE, //The provided order is already owned by someone else who owns only 1 order
    SHOP_NOT_OPENED_YET, //Tried to generate a shop url but he's not a staffer and the registrations aren't opened yet
    ORDER_EDITS_CLOSED //Tried to generate a edit order url, but order editing is closed
}
