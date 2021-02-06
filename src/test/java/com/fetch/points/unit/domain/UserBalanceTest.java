package com.fetch.points.unit.domain;

import com.fetch.points.api.exception.InvalidPointDeductionException;
import com.fetch.points.domain.Payer;
import com.fetch.points.domain.PayerBalance;
import com.fetch.points.domain.UserBalance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class UserBalanceTest {


    UserBalance userBalance;


    @BeforeEach
    public void setUp() {
        userBalance = new UserBalance();
    }

    @Test
    public void shouldAddPositiveBalance() {
        ZonedDateTime datetime = ZonedDateTime.now(ZoneOffset.UTC);
        userBalance.addBalance(100L, datetime, new Payer("DANNON"));
        userBalance.addBalance(100L, datetime.minusDays(3), new Payer("UNILEVER"));
        assertEquals(userBalance.getCurrentBalance(), 200);
    }

    @Test
    public void shouldFailDeductionWhenDeductingInsufficientBalance() {
        ZonedDateTime datetime = ZonedDateTime.now(ZoneOffset.UTC);
        userBalance.addBalance(100L, datetime, new Payer("DANNON"));
        userBalance.addBalance(250L, datetime.minusDays(3), new Payer("UNILEVER"));
        assertThrows(InvalidPointDeductionException.class, () -> userBalance.subtractBalance(400L));
    }

    @Test
    public void shouldFailDeductionWhenDeductingInsufficientPayerBalance() {
        ZonedDateTime datetime = ZonedDateTime.now(ZoneOffset.UTC);
        userBalance.addBalance(100L, datetime, new Payer("DANNON"));
        userBalance.addBalance(100L, datetime, new Payer("UNILEVER"));
        assertThrows(
                InvalidPointDeductionException.class,
                () -> userBalance.subtractBalance(101L, new Payer("DANNON"))
        );
    }

    @Test
    public void shouldMaintainBalanceCreateDateSort() {
        ZonedDateTime datetime = ZonedDateTime.now(ZoneOffset.UTC);
        userBalance.addBalance(100L, datetime.minusDays(2), new Payer("DANNON"));
        userBalance.addBalance(100L, datetime.minusDays(3), new Payer("UNILEVER"));
        userBalance.addBalance(100L, datetime.minusDays(1), new Payer("UNILEVER"));
        userBalance.addBalance(100L, datetime.minusDays(5), new Payer("MILLER COORS"));
        List<PayerBalance> balanceList = userBalance.getBalances();
        assertEquals(
                balanceList.stream().map(PayerBalance::getPayerName).collect(Collectors.toList()),
                List.of("MILLER COORS", "UNILEVER", "DANNON")
        );
        assertEquals(
                balanceList.stream().map(PayerBalance::getPoints).collect(Collectors.toList()),
                List.of(100L, 200L, 100L)
        );
    }

    @Test
    public void shouldMaintainBalanceOrderWhenNotFullyZeroed() {
        ZonedDateTime datetime = ZonedDateTime.now(ZoneOffset.UTC);
        userBalance.addBalance(100L, datetime.minusDays(2), new Payer("DANNON"));
        userBalance.addBalance(300L, datetime.minusDays(3), new Payer("UNILEVER"));
        userBalance.addBalance(50L, datetime.minusDays(4), new Payer("BASKIN ROBBINS"));
        userBalance.addBalance(100L, datetime.minusDays(1), new Payer("UNILEVER"));
        userBalance.addBalance(500L, datetime.minusDays(5), new Payer("MILLER COORS"));
        userBalance.subtractBalance(700);
        List<PayerBalance> balanceList = userBalance.getBalances();
        assertEquals(
                balanceList.stream().map(PayerBalance::getPayerName).collect(Collectors.toList()),
                List.of("UNILEVER","DANNON")
        );
        assertEquals(
                balanceList.stream().map(PayerBalance::getPoints).collect(Collectors.toList()),
                List.of(250L, 100L)
        );
    }

    @Test
    public void shouldResortBalancesWhenUpdatedAfterFullyZeroed() {
        ZonedDateTime datetime = ZonedDateTime.now(ZoneOffset.UTC);
        userBalance.addBalance(100L, datetime.minusDays(2), new Payer("DANNON"));
        userBalance.addBalance(300L, datetime.minusDays(3), new Payer("UNILEVER"));
        userBalance.subtractBalance(350);
        userBalance.addBalance(100L, datetime.minusDays(1), new Payer("UNILEVER"));
        List<PayerBalance> balanceList = userBalance.getBalances();
        assertEquals(
                balanceList.stream().map(PayerBalance::getPayerName).collect(Collectors.toList()),
                List.of("DANNON","UNILEVER")
        );
        assertEquals(
                balanceList.stream().map(PayerBalance::getPoints).collect(Collectors.toList()),
                List.of(50L, 100L)
        );
    }

    @Test
    public void shouldSupportProblemDomain() {
        ZonedDateTime datetime = ZonedDateTime.now(ZoneOffset.UTC);
        userBalance.addBalance(300L, datetime.plusDays(1), new Payer("DANNON"));
        userBalance.addBalance(200L, datetime.plusDays(2), new Payer("UNILEVER"));
        userBalance.subtractBalance(200L, new Payer("DANNON"));
        userBalance.addBalance(10000L, datetime.plusDays(4), new Payer("MILLER COORS"));
        userBalance.addBalance(1000L, datetime.plusDays(5), new Payer("DANNON"));
        userBalance.subtractBalance(5000);
        List<PayerBalance> balanceList = userBalance.getBalances();
        assertEquals(
                balanceList.stream().map(PayerBalance::getPayerName).collect(Collectors.toList()),
                List.of("DANNON","MILLER COORS")
        );
        assertEquals(
                balanceList.stream().map(PayerBalance::getPoints).collect(Collectors.toList()),
                List.of(1000L, 5300L)
        );
    }
}
