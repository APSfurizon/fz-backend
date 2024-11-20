package net.furizon.backend.feature.pretix.ordersworkflow;

public enum OrderWorkflowErrorCode {
    ORDER_ALREADY_DONE,
    SERVER_ERROR,
    ORDER_SECRET_NOT_MATCH,
    ORDER_ALREADY_OWNED_BY_SOMEBODY_ELSE
}
