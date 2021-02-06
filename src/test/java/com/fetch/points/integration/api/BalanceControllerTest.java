package com.fetch.points.integration.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fetch.points.api.resource_dto.UserPointsTransactionDTO;
import com.fetch.points.domain.User;
import com.fetch.points.service.PointService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BalanceControllerTest {

    private final String POINTS_BALANCE_URL = "/api/v1/account/{userId}/balance";
    private final String POINTS_DEDUCTION_URL = "/api/v1/account/{userId}/payout";

    private final Long VALID_USER_ID = 3L;

    @Data
    @AllArgsConstructor
    private static class AddPointsRequest{
        String payer;
        long points;
        String timestamp;

        AddPointsRequest(String payer, long points) {
            this.payer = payer;
            this.points = points;
        }
    }

    @Value
    private static class DeductPointsRequest{
        long points;
    }

    @Value
    private static class DeductPointsResponse{
        List<Deduction> deductions;

        @Value
        private static class Deduction {
            String payer;
            long points;
            String timestamp;
        }
    }

    @Value
    private static class BalancePointsResponse{
        List<Balance> balance;

        @Value
        private static class Balance {
            String payer;
            long points;
        }
    }

    private final List<AddPointsRequest> PROBLEM_DOMAIN_ADD_POINTS_REQUESTS = List.of(
            new AddPointsRequest("DANNON", 300, "2020-10-31T10:00:00.00Z"),
            new AddPointsRequest("UNILEVER", 200, "2020-10-31T11:00:00.00Z"),
            new AddPointsRequest("DANNON", -200, "2020-10-31T15:00:00.00Z"),
            new AddPointsRequest("MILLER COORS", 10000, "2020-11-01T02:00:00.00Z"),
            new AddPointsRequest("DANNON", 1000, "2020-11-02T14:00:00.00Z")
    );
    private final DeductPointsRequest PROBLEM_DOMAIN_DEDUCT_REQUEST = new DeductPointsRequest(5000);
    private final DeductPointsResponse PROBLEM_DOMAIN_DEDUCT_RESPONSE = new DeductPointsResponse(
            List.of(new DeductPointsResponse.Deduction("DANNON", -100, "now"),
                    new DeductPointsResponse.Deduction("UNILEVER", -200, "now"),
                    new DeductPointsResponse.Deduction("MILLER COORS", -4700, "now"))
    );
    private final BalancePointsResponse PROBLEM_DOMAIN_BALANCE_RESPONSE_JSON = new BalancePointsResponse(
            List.of(new BalancePointsResponse.Balance("DANNON", 1000),
                    new BalancePointsResponse.Balance("MILLER COORS", 5300))
    );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PointService pointService;

    @BeforeEach
    public void setUp() {
        this.pointService.resetUserData();
    }


    private String toJSON(final Object jsonObj) throws Exception {
        return objectMapper.writeValueAsString(jsonObj);
    }

    @Test
    void GetBalanceShouldReturnEmptyBalanceListIfNoData() throws Exception {
        final BalancePointsResponse resp = new BalancePointsResponse(List.of());
        performPointsBalanceRequest(VALID_USER_ID.toString())
                .andExpect(status().isOk())
                .andExpect(content().json(toJSON(resp)));
    }

    @Test
    void GetBalanceShouldReturn400IfUserIdInvalid() throws Exception {
        for(String invalidUserId : List.of("a","-3")) {
            performPointsBalanceRequest(invalidUserId)
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void GetBalanceShouldRetrieveValidUserBalanceSuccessfully() throws Exception {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime oneDayEarlier = now.minusDays(1);
        pointService.modifyPointsBalance(new UserPointsTransactionDTO(VALID_USER_ID, "UNILEVER", 1000L, now));
        pointService.modifyPointsBalance(new UserPointsTransactionDTO(VALID_USER_ID, "DANNON", 100L, oneDayEarlier));

        final BalancePointsResponse resp = new BalancePointsResponse(
                List.of(new BalancePointsResponse.Balance("DANNON", 100),
                        new BalancePointsResponse.Balance("UNILEVER", 1000))
        );
        performPointsBalanceRequest(VALID_USER_ID.toString())
                .andExpect(status().isOk())
                .andExpect(content().json(toJSON(resp)));
    }

    @Test
    void AddBalanceShouldReturn400IfDateTimeIsInFuture() throws Exception {
        AddPointsRequest future = new AddPointsRequest("DANNON", 300, "2999-10-31T10:00:00.00Z");
        performPointsModificationRequest(VALID_USER_ID.toString(), toJSON(future))
                .andExpect(status().isBadRequest());
    }

    @Test
    void AddBalanceShouldModifyPointsBalanceSuccessfully() throws Exception {
        long totalPointsBalanceChange = 0;
        for (AddPointsRequest body : PROBLEM_DOMAIN_ADD_POINTS_REQUESTS) {
            totalPointsBalanceChange += body.getPoints();
            performPointsModificationRequest(VALID_USER_ID.toString(), toJSON(body))
                    .andExpect(status().isOk());
        }
        assertEquals(
                pointService.getOrCreateUserBalance(new User(VALID_USER_ID)).getCurrentBalance(),
                totalPointsBalanceChange
        );
    }

    @Test
    void AddBalanceShouldReturn200IfAddHasNoTimestamp() throws Exception {
        AddPointsRequest req = new AddPointsRequest("DANNON", 200);
        performPointsModificationRequest(VALID_USER_ID.toString(), toJSON(req))
                .andExpect(status().isOk());
        this.pointService.getOrCreateUserBalance(new User(3));
    }

    @Test
    void AddBalanceShouldReturn400IfAddMakesBalanceNegative() throws Exception {
        AddPointsRequest req = new AddPointsRequest("DANNON", -200, "2020-10-31T15:00:00.00Z");
        performPointsModificationRequest(VALID_USER_ID.toString(), toJSON(req))
                .andExpect(status().isBadRequest());
    }

    @Test
    void DeductBalanceShouldReturnEmptyDeductions() throws Exception {
        DeductPointsRequest req = new DeductPointsRequest(0);

        DeductPointsResponse resp = new DeductPointsResponse(List.of());
        performPointsDeductionRequest(VALID_USER_ID.toString(), toJSON(req))
                .andExpect(status().isOk())
                .andExpect(content().json(toJSON(resp)));
    }

    @Test
    void DeductBalanceShouldDeductPointsBalanceSuccessfully() throws Exception {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime oneDayEarlier = now.minusDays(1);
        pointService.modifyPointsBalance(new UserPointsTransactionDTO(VALID_USER_ID, "UNILEVER", 1000L, now));
        pointService.modifyPointsBalance(new UserPointsTransactionDTO(VALID_USER_ID, "DANNON", 100L, oneDayEarlier));

        DeductPointsRequest req = new DeductPointsRequest(1100);

        DeductPointsResponse resp = new DeductPointsResponse(
                List.of(
                        new DeductPointsResponse.Deduction("DANNON", -100, "now"),
                        new DeductPointsResponse.Deduction("UNILEVER", -1000, "now")
                )
        );
        performPointsDeductionRequest(VALID_USER_ID.toString(), toJSON(req))
                .andExpect(status().isOk())
                .andExpect(content().json(toJSON(resp)));
    }

    @Test
    void ProblemDomainShouldBeSatisfied() throws Exception {
        for (AddPointsRequest body : PROBLEM_DOMAIN_ADD_POINTS_REQUESTS) {
            performPointsModificationRequest(VALID_USER_ID.toString(), toJSON(body))
                    .andExpect(status().isOk());
        }
        performPointsDeductionRequest(VALID_USER_ID.toString(), toJSON(PROBLEM_DOMAIN_DEDUCT_REQUEST))
                .andExpect(status().isOk())
                .andExpect(content().json(toJSON(PROBLEM_DOMAIN_DEDUCT_RESPONSE)));

        performPointsBalanceRequest(VALID_USER_ID.toString())
                .andExpect(status().isOk())
                .andExpect(content().json(toJSON(PROBLEM_DOMAIN_BALANCE_RESPONSE_JSON)));
    }

    private ResultActions performPointsBalanceRequest(final String userId) throws Exception {
        return mockMvc.perform(get(POINTS_BALANCE_URL, userId)
                .contentType(MediaType.APPLICATION_JSON));
    }

    private ResultActions performPointsDeductionRequest(final String userId, final String deductionJSON) throws Exception {
        return mockMvc.perform(post(POINTS_DEDUCTION_URL, userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(deductionJSON));
    }

    private ResultActions performPointsModificationRequest(final String userId, final String modificationJSON) throws Exception {
        return mockMvc.perform(post(POINTS_BALANCE_URL, userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(modificationJSON));
    }

}