package com.example.horizontravel.data.api;

import com.example.horizontravel.data.model.AdminUser;
import com.example.horizontravel.data.model.AiChatRequest;
import com.example.horizontravel.data.model.AiChatResponse;
import com.example.horizontravel.data.model.Booking;
import com.example.horizontravel.data.model.BookingRequest;
import com.example.horizontravel.data.model.BookingsSummary;
import com.example.horizontravel.data.model.ChangePasswordRequest;
import com.example.horizontravel.data.model.Commission;
import com.example.horizontravel.data.model.CommissionSummary;
import com.example.horizontravel.data.model.CountResponse;
import com.example.horizontravel.data.model.CreateGuideAvailabilityRequest;
import com.example.horizontravel.data.model.CreateServiceSlotRequest;
import com.example.horizontravel.data.model.ForgotPasswordRequest;
import com.example.horizontravel.data.model.Group;
import com.example.horizontravel.data.model.GroupBookingRequest;
import com.example.horizontravel.data.model.GroupCreateRequest;
import com.example.horizontravel.data.model.GroupMember;
import com.example.horizontravel.data.model.GroupMessage;
import com.example.horizontravel.data.model.Guide;
import com.example.horizontravel.data.model.GuideAvailability;
import com.example.horizontravel.data.model.GuideBooking;
import com.example.horizontravel.data.model.GuideBookingRequest;
import com.example.horizontravel.data.model.GuideRating;
import com.example.horizontravel.data.model.GuideRatingRequest;
import com.example.horizontravel.data.model.JoinGroupRequest;
import com.example.horizontravel.data.model.Location;
import com.example.horizontravel.data.model.LoginRequest;
import com.example.horizontravel.data.model.LoginResponse;
import com.example.horizontravel.data.model.MessageRequest;
import com.example.horizontravel.data.model.MessageResponse;
import com.example.horizontravel.data.model.Notification;
import com.example.horizontravel.data.model.Payment;
import com.example.horizontravel.data.model.PaymentRequest;
import com.example.horizontravel.data.model.PendingRegistrationsResponse;
import com.example.horizontravel.data.model.Provider;
import com.example.horizontravel.data.model.RegisterGuideRequest;
import com.example.horizontravel.data.model.RegisterProviderRequest;
import com.example.horizontravel.data.model.RegisterTouristRequest;
import com.example.horizontravel.data.model.ResetPasswordRequest;
import com.example.horizontravel.data.model.RevenueSummary;
import com.example.horizontravel.data.model.ServiceRating;
import com.example.horizontravel.data.model.ServiceRatingRequest;
import com.example.horizontravel.data.model.ServiceSlot;
import com.example.horizontravel.data.model.StatusRequest;
import com.example.horizontravel.data.model.Tourist;
import com.example.horizontravel.data.model.UpdateGuideAvailabilityRequest;
import com.example.horizontravel.data.model.UpdateGuideBookingStatusRequest;
import com.example.horizontravel.data.model.UpdateGuideRequest;
import com.example.horizontravel.data.model.UpdateLocationRequest;
import com.example.horizontravel.data.model.UpdateProviderRequest;
import com.example.horizontravel.data.model.UpdateServiceSlotRequest;
import com.example.horizontravel.data.model.UpdateTouristRequest;
import com.example.horizontravel.data.model.UpdateUserStatusRequest;
import com.example.horizontravel.data.model.VerifyRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // ==========================================
    // Public Auth & Search Endpoints
    // ==========================================

    @POST("auth/register/tourist")
    Call<Tourist> registerTourist(@Body RegisterTouristRequest request);

    @POST("auth/register/guide")
    Call<Guide> registerGuide(@Body RegisterGuideRequest request);

    @POST("auth/register/provider")
    Call<Provider> registerProvider(@Body RegisterProviderRequest request);

    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("auth/forgot-password")
    Call<MessageResponse> forgotPassword(@Body ForgotPasswordRequest request);

    @POST("auth/reset-password")
    Call<MessageResponse> resetPassword(@Body ResetPasswordRequest request);

    @GET("slots/search")
    Call<List<ServiceSlot>> searchSlots(
            @Query("category") String category,
            @Query("origin") String origin,
            @Query("destination") String destination,
            @Query("locationName") String locationName,
            @Query("dateFrom") String dateFrom,
            @Query("dateTo") String dateTo,
            @Query("minPrice") Double minPrice,
            @Query("maxPrice") Double maxPrice
    );

    @GET("slots/{id}")
    Call<ServiceSlot> getSlotById(@Path("id") long id);

    @GET("guide-availability/search")
    Call<List<GuideAvailability>> searchGuideAvailability(
            @Query("dateFrom") String dateFrom,
            @Query("dateTo") String dateTo,
            @Query("location") String location
    );

    @GET("guide-availability/guide/{guideId}")
    Call<List<GuideAvailability>> getGuideAvailabilityByGuideId(@Path("guideId") long guideId);

    @GET("guides")
    Call<List<Guide>> getGuides(@Query("available") Boolean available);

    @GET("guides/{id}")
    Call<Guide> getGuideById(@Path("id") long id);

    @GET("ratings/guide/{guideId}")
    Call<List<GuideRating>> getRatingsForGuide(@Path("guideId") long guideId);

    @GET("ratings/provider/{providerId}")
    Call<List<ServiceRating>> getRatingsForProvider(@Path("providerId") long providerId);

    // ==========================================
    // Generic Authenticated Endpoints (All Roles)
    // ==========================================

    @PUT("auth/change-password")
    Call<MessageResponse> changePassword(@Body ChangePasswordRequest request);

    @GET("notifications/user/{userId}")
    Call<List<Notification>> getNotifications(@Path("userId") long userId);

    @GET("notifications/user/{userId}/unread-count")
    Call<CountResponse> getUnreadNotificationCount(@Path("userId") long userId);

    @PUT("notifications/{id}/read")
    Call<Notification> markNotificationAsRead(@Path("id") long id);

    // ==========================================
    // TOURIST Endpoints
    // ==========================================

    @GET("tourists/{id}")
    Call<Tourist> getTouristProfile(@Path("id") long id);

    @PUT("tourists/{id}")
    Call<Tourist> updateTouristProfile(@Path("id") long id, @Body UpdateTouristRequest request);

    @DELETE("tourists/{id}")
    Call<Void> deleteTouristAccount(@Path("id") long id);

    @POST("bookings")
    Call<Booking> createBooking(@Body BookingRequest request);

    @GET("bookings/tourist/{touristId}")
    Call<List<Booking>> getBookingsForTourist(@Path("touristId") long touristId);

    @PUT("bookings/{id}/status")
    Call<Booking> updateBookingStatus(@Path("id") long id, @Body StatusRequest request);

    @POST("payments")
    Call<Payment> createPayment(@Body PaymentRequest request);

    @GET("payments/{id}")
    Call<Payment> getPaymentById(@Path("id") long id);

    @GET("payments/booking/{bookingId}")
    Call<List<Payment>> getPaymentsForBooking(@Path("bookingId") long bookingId);

    @POST("guide-bookings")
    Call<GuideBooking> createGuideBooking(@Body GuideBookingRequest request);

    @GET("guide-bookings/tourist/{touristId}")
    Call<List<GuideBooking>> getGuideBookingsForTourist(@Path("touristId") long touristId);

    @POST("ratings/guide")
    Call<GuideRating> submitGuideRating(@Body GuideRatingRequest request);

    @POST("ratings/service")
    Call<ServiceRating> submitServiceRating(@Body ServiceRatingRequest request);

    @POST("groups")
    Call<Group> createGroup(@Body GroupCreateRequest request);

    @POST("groups/join")
    Call<Group> joinGroup(@Body JoinGroupRequest request);

    @GET("groups/{id}")
    Call<Group> getGroupById(@Path("id") long id);

    @GET("groups/tourist/{touristId}")
    Call<List<Group>> getGroupsForTourist(@Path("touristId") long touristId);

    @GET("groups/{id}/members")
    Call<List<GroupMember>> getGroupMembers(@Path("id") long id);

    @DELETE("groups/{id}/members/{touristId}")
    Call<Void> leaveOrRemoveGroupMember(@Path("id") long id, @Path("touristId") long touristId);

    @POST("groups/{id}/booking")
    Call<Group> linkGroupBooking(@Path("id") long id, @Body GroupBookingRequest request);

    @PUT("groups/{id}/members/{touristId}/paid")
    Call<GroupMember> markGroupMemberPaid(@Path("id") long id, @Path("touristId") long touristId);

    @GET("groups/{id}/messages")
    Call<List<GroupMessage>> getGroupMessages(@Path("id") long id);

    @POST("groups/{id}/messages")
    Call<GroupMessage> sendGroupMessage(@Path("id") long id, @Body MessageRequest request);

    @GET("guides/{id}/location")
    Call<Location> getGuideLocation(@Path("id") long id);

    @POST("ai/chat")
    Call<AiChatResponse> sendAiChatMessage(@Body AiChatRequest request);

    // ==========================================
    // TOUR_GUIDE Endpoints
    // ==========================================

    @PUT("guides/{id}")
    Call<Guide> updateGuideProfile(@Path("id") long id, @Body UpdateGuideRequest request);

    @DELETE("guides/{id}")
    Call<Void> deleteGuideAccount(@Path("id") long id);

    @POST("guide-availability")
    Call<GuideAvailability> createGuideAvailability(@Body CreateGuideAvailabilityRequest request);

    @PUT("guide-availability/{id}")
    Call<GuideAvailability> updateGuideAvailability(@Path("id") long id, @Body UpdateGuideAvailabilityRequest request);

    @DELETE("guide-availability/{id}")
    Call<Void> deleteGuideAvailability(@Path("id") long id);

    @PUT("guides/{id}/location")
    Call<Location> updateGuideLocation(@Path("id") long id, @Body UpdateLocationRequest request);

    @GET("guide-bookings/guide/{guideId}")
    Call<List<GuideBooking>> getGuideBookingsForGuide(@Path("guideId") long guideId);

    @PUT("guide-bookings/{id}/status")
    Call<GuideBooking> updateGuideBookingStatus(@Path("id") long id, @Body UpdateGuideBookingStatusRequest request);

    @PUT("guide-bookings/{id}/payment-received")
    Call<GuideBooking> markGuideBookingPaymentReceived(@Path("id") long id);

    // ==========================================
    // SERVICE_PROVIDER Endpoints
    // ==========================================

    @GET("providers/{id}")
    Call<Provider> getProviderById(@Path("id") long id);

    @PUT("providers/{id}")
    Call<Provider> updateProviderProfile(@Path("id") long id, @Body UpdateProviderRequest request);

    @DELETE("providers/{id}")
    Call<Void> deleteProviderAccount(@Path("id") long id);

    @GET("providers/{id}/commissions")
    Call<List<Commission>> getCommissionsForProvider(@Path("id") long id);

    @GET("providers/{id}/payments")
    Call<List<Payment>> getPaymentsForProvider(
            @Path("id") long id,
            @Query("dateFrom") String dateFrom,
            @Query("dateTo") String dateTo,
            @Query("status") String status
    );

    @POST("slots")
    Call<ServiceSlot> createSlot(@Body CreateServiceSlotRequest request);

    @PUT("slots/{id}")
    Call<ServiceSlot> updateSlot(@Path("id") long id, @Body UpdateServiceSlotRequest request);

    @DELETE("slots/{id}")
    Call<Void> deleteSlot(@Path("id") long id);

    @GET("slots/provider/{providerId}")
    Call<List<ServiceSlot>> getSlotsForProvider(@Path("providerId") long providerId);



    // ==========================================
    // ADMIN Endpoints
    // ==========================================

    @GET("admin/pending-registrations")
    Call<PendingRegistrationsResponse> getPendingRegistrations();

    @PUT("admin/providers/{id}/verify")
    Call<Provider> verifyProvider(@Path("id") long id, @Body VerifyRequest request);

    @PUT("admin/guides/{id}/verify")
    Call<Guide> verifyGuide(@Path("id") long id, @Body VerifyRequest request);

    @GET("admin/users")
    Call<List<AdminUser>> getAdminUsers(
            @Query("role") String role,
            @Query("status") String status
    );

    @PUT("admin/users/{id}/status")
    Call<AdminUser> updateUserStatus(@Path("id") long id, @Body UpdateUserStatusRequest request);

    @GET("admin/payments")
    Call<List<Payment>> getAdminPayments(
            @Query("dateFrom") String dateFrom,
            @Query("dateTo") String dateTo,
            @Query("status") String status,
            @Query("method") String method
    );

    @GET("admin/commissions")
    Call<List<Commission>> getAdminCommissions(
            @Query("dateFrom") String dateFrom,
            @Query("dateTo") String dateTo,
            @Query("settlementStatus") String settlementStatus
    );

    @PUT("admin/commissions/{id}/settle")
    Call<Commission> settleCommission(@Path("id") long id);

    @GET("admin/reports/revenue-summary")
    Call<RevenueSummary> getRevenueSummary(
            @Query("dateFrom") String dateFrom,
            @Query("dateTo") String dateTo
    );

    @GET("admin/reports/bookings-summary")
    Call<BookingsSummary> getBookingsSummary(
            @Query("dateFrom") String dateFrom,
            @Query("dateTo") String dateTo
    );

    @GET("admin/reports/commission-summary")
    Call<CommissionSummary> getCommissionSummary(
            @Query("dateFrom") String dateFrom,
            @Query("dateTo") String dateTo
    );
}
