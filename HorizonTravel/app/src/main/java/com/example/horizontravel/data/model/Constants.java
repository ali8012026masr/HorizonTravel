package com.example.horizontravel.data.model;

public final class Constants {
    private Constants() {}

    public static final class Role {
        public static final String TOURIST = "TOURIST";
        public static final String TOUR_GUIDE = "TOUR_GUIDE";
        public static final String SERVICE_PROVIDER = "SERVICE_PROVIDER";
        public static final String ADMIN = "ADMIN";
    }

    public static final class UserStatus {
        public static final String ACTIVE = "ACTIVE";
        public static final String PENDING_VERIFICATION = "PENDING_VERIFICATION";
        public static final String SUSPENDED = "SUSPENDED";
        public static final String REVOKED = "REVOKED";
    }

    public static final class VerificationStatus {
        public static final String PENDING = "PENDING";
        public static final String APPROVED = "APPROVED";
        public static final String REJECTED = "REJECTED";
    }

    public static final class BookingStatus {
        public static final String PENDING = "PENDING";
        public static final String CONFIRMED = "CONFIRMED";
        public static final String CANCELLED = "CANCELLED";
        public static final String COMPLETED = "COMPLETED";
    }

    public static final class PaymentStatus {
        public static final String UNPAID = "UNPAID";
        public static final String PAID = "PAID";
        public static final String REFUNDED = "REFUNDED";
    }

    public static final class PaymentTxStatus {
        public static final String PENDING = "PENDING";
        public static final String SUCCESS = "SUCCESS";
        public static final String FAILED = "FAILED";
        public static final String REFUNDED = "REFUNDED";
    }

    public static final class PaymentMethod {
        public static final String CARD = "CARD";
        public static final String MOBILE_BANKING = "MOBILE_BANKING";
        public static final String CASH = "CASH";
    }

    public static final class GuideBookingStatus {
        public static final String REQUESTED = "REQUESTED";
        public static final String ACCEPTED = "ACCEPTED";
        public static final String DECLINED = "DECLINED";
        public static final String COMPLETED = "COMPLETED";
    }

    public static final class ServiceCategory {
        public static final String BUS = "BUS";
        public static final String MICROBUS = "MICROBUS";
        public static final String LAUNCH = "LAUNCH";
        public static final String TRAIN = "TRAIN";
        public static final String AIRPLANE = "AIRPLANE";
        public static final String SHIP = "SHIP";
        public static final String HOTEL = "HOTEL";
        public static final String RESORT = "RESORT";
        public static final String CONVENTION_CENTER = "CONVENTION_CENTER";
        public static final String BUFFET = "BUFFET";
        public static final String AMUSEMENT_PARK = "AMUSEMENT_PARK";
    }

    public static final class SlotStatus {
        public static final String OPEN = "OPEN";
        public static final String FULL = "FULL";
        public static final String CLOSED = "CLOSED";
        public static final String CANCELLED = "CANCELLED";
    }

    public static final class AvailabilityStatus {
        public static final String OPEN = "OPEN";
        public static final String BOOKED = "BOOKED";
        public static final String CANCELLED = "CANCELLED";
    }

    public static final class GroupMemberStatus {
        public static final String INVITED = "INVITED";
        public static final String JOINED = "JOINED";
        public static final String DECLINED = "DECLINED";
    }

    public static final class SettlementStatus {
        public static final String PENDING = "PENDING";
        public static final String SETTLED = "SETTLED";
    }

    public static final class AiActionType {
        public static final String FILTER_SLOTS = "FILTER_SLOTS";
        public static final String RECOMMEND_BUNDLE = "RECOMMEND_BUNDLE";
        public static final String RECOMMEND_GROUP_BUNDLE = "RECOMMEND_GROUP_BUNDLE";
        public static final String NONE = "NONE";
    }
}
