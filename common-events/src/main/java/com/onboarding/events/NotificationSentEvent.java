package com.onboarding.events;

public class NotificationSentEvent extends BaseEvent {
    private String notificationType;
    private String deliveryStatus;

    public NotificationSentEvent() {
        super();
    }

    public NotificationSentEvent(String requestId, String notificationType, String deliveryStatus) {
        super(requestId);
        this.notificationType = notificationType;
        this.deliveryStatus = deliveryStatus;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }
}
