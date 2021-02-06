package com.fetch.points.api;

import com.fetch.points.api.resource_dto.DeductUserPointsResource;
import com.fetch.points.api.resource_dto.GetUserPointsResource;
import com.fetch.points.api.resource_dto.ModifyUserPointsResource;
import com.fetch.points.domain.PayerBalance;
import com.fetch.points.api.resource_dto.UserPointsTransactionDTO;
import com.fetch.points.service.PointService;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

@Slf4j
@RestController
@Api(value = "/api/v1/account")
@Validated
@RequestMapping(value="/api/v1/account", produces = MediaType.APPLICATION_JSON_VALUE)
public class BalanceController {

    private final PointService pointService;

    @Autowired
    public BalanceController(PointService pointService) {
        this.pointService = pointService;
    }

    @ApiOperation(value = "Retrieves specified user's current points balance")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Balance has been retrieved successfully"),
            @ApiResponse(code = 400, message = "User Id is invalid"),
            @ApiResponse(code = 500, message = "Server error has occurred")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{userId}/balance")
    public GetUserPointsResource.Response getUserPointsBalance(
            @ApiParam("Numerical ID of the user") @PathVariable("userId") @Positive Long userId
    ) {
        List<PayerBalance> userBalancePerPayer = this.pointService.getPointsBalance(userId);
        return new GetUserPointsResource.Response(userBalancePerPayer);
    }

    @ApiOperation(value = "Modifies (adds to / reduces) specified user's current points balance")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Balance has been updated successfully"),
            @ApiResponse(code = 400, message = "User ID is invalid or body contained an invalid deduction"),
            @ApiResponse(code = 500, message = "Server error has occurred")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/{userId}/balance", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> modifyUserPointsBalance(
            @ApiParam("Numerical ID of the user") @PathVariable("userId") @Positive Long userId,
            @Valid @RequestBody ModifyUserPointsResource.Request modifyUserPointsRequest
    ) {
        this.pointService.modifyPointsBalance(modifyUserPointsRequest.toUserPointsTransaction(userId));
        return ResponseEntity.ok().body(null);
    }

    @ApiOperation(value = "Reduces specified user's current points balance")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Balance has been reduced successfully"),
            @ApiResponse(code = 400, message = "User ID / body is invalid or body contained an invalid deduction"),
            @ApiResponse(code = 500, message = "Server error has occurred")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/{userId}/payout", consumes = MediaType.APPLICATION_JSON_VALUE)
    public DeductUserPointsResource.Response deductUserPointsBalance(
            @ApiParam("Numerical ID of the user") @PathVariable("userId") @Positive Long userId,
            @Valid @RequestBody DeductUserPointsResource.Request deductUserPointsRequest) {
        List<UserPointsTransactionDTO> userPointsTransactions = this.pointService.deductPointsBalance(userId, deductUserPointsRequest.getPoints());
        return new DeductUserPointsResource.Response(userPointsTransactions);
    }
}
