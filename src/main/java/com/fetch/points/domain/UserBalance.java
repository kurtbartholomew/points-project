package com.fetch.points.domain;


import com.fetch.points.api.exception.InvalidPointDeductionException;
import com.fetch.points.api.resource_dto.UserPointsTransactionDTO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Data
@Slf4j
public class UserBalance {
    final Comparator<PayerBalance> BALANCE_CREATE_DATE_ORDERING = Comparator.comparing(PayerBalance::getCreatedDate);

    @NotNull
    long currentBalance = 0;

    final SortedSet<PayerBalance> payerBalanceRecords = new TreeSet<>(BALANCE_CREATE_DATE_ORDERING);
    final Map<Payer, PayerBalance> payerToOverallBalance = new HashMap<>();

    /**
     * Tests whether a user has enough points for deduction (either overall or specific payer)
     * @param points Reward points!
     * @param payer A brand / point redeemer?
     * @return Whether a user has enough points!
     */
    private boolean isBalanceSufficient(final long points, final Payer payer) {
        if (payer == null) {
            return points <= currentBalance;
        } else if (this.payerToOverallBalance.containsKey(payer)) {
            return points <= payerToOverallBalance.get(payer).getPoints();
        }
        return true;
    }

    /**
     * Adds a record record to the ordered structure of point balances for a user.
     * Also tabulates and updates the current point total for the payer who is associated with them.
     * @param points Points being added to an account.
     * @param datetime The datetime the points were added to the account.
     * @param payer The payer / brand associated with the points being added.
     */
    public void addBalance(final long points, final ZonedDateTime datetime, final Payer payer) {
        final boolean balancePresent = payerToOverallBalance.containsKey(payer);
        final PayerBalance balanceRecord = new PayerBalance(payer.getPayerName(), points, datetime);
        final PayerBalance overallBalanceRecord;
        if (balancePresent) {
            final PayerBalance oldOverallBalance = payerToOverallBalance.get(payer);
            overallBalanceRecord = new PayerBalance(oldOverallBalance, oldOverallBalance.getPoints() + points);
        } else {
            overallBalanceRecord = balanceRecord;
        }
        payerBalanceRecords.add(balanceRecord);
        log.debug(String.format("%s balance increased by %d", payer.getPayerName(), points));
        payerToOverallBalance.put(payer, overallBalanceRecord);
        currentBalance += points;
        log.debug(String.format("Overall balance increased by %d to %d", points, currentBalance));
    }

    /**
     * Reduces or removes point balances associated with a specific payer / brand
     * @param recordPayer Payer associcated with the points
     * @param pointsToRemove Points being deducted from a total
     */
    private void updatePayerOverallBalanceMap(final Payer recordPayer, final long pointsToRemove) {
        PayerBalance overallBalance = payerToOverallBalance.get(recordPayer);
        if (overallBalance.getPoints() <= pointsToRemove) {
            payerToOverallBalance.remove(recordPayer);

        } else {
            payerToOverallBalance.computeIfPresent(recordPayer,
                    (k,v) -> new PayerBalance(v, v.getPoints() - pointsToRemove)
            );
        }
        log.debug(String.format("Balance of %s reduced by %d", recordPayer.getPayerName(), pointsToRemove));
    }

    /**
     * Removes points from user's account starting with oldest points first.
     * @param points Points to be removed from user's account balance
     * @param payer (potentially absent) name of a payer to target for point deduction
     * @return A list of point deduction transactions
     */
    public List<UserPointsTransactionDTO> subtractBalance(final long points, @Nullable final Payer payer) {
        final long deduction = (points < 0) ? -points : points;
        if (!isBalanceSufficient(points, payer)) {
            String errorMsg = String.format(
                    "User has insufficient points (%d) for deduction (%d).",
                    currentBalance,
                    deduction
            );
            log.error(errorMsg);
            throw new InvalidPointDeductionException(errorMsg);
        }

        Iterator<PayerBalance> iterator = payerBalanceRecords.iterator();

        final Map<String, Long> payerToTotalDeduction = new LinkedHashMap<>();
        PayerBalance reducedBalance = null;
        final List<PayerBalance> balancesToRemove = new ArrayList<>();
        long pointsUnredeemed = points;
        long totalPointsRemoved = 0;

        while (iterator.hasNext() && pointsUnredeemed > 0) {
            PayerBalance balanceRecord = iterator.next();

            final String recordPayerName = balanceRecord.getPayerName();
            final Payer recordPayer = new Payer(recordPayerName);
            final long recordPoints = balanceRecord.getPoints();

            if (payer != null && !payer.getPayerName().equals(recordPayerName)) {
                continue;
            }

            final Long pointsToRemove;
            if (recordPoints <= pointsUnredeemed) {
                pointsToRemove = recordPoints;
            } else {
                pointsToRemove = pointsUnredeemed;
                reducedBalance = new PayerBalance(balanceRecord, recordPoints - pointsToRemove);
            }
            pointsUnredeemed -= pointsToRemove;
            balancesToRemove.add(balanceRecord);
            // updates updates overall deductions by payer and balances by payer
            payerToTotalDeduction.compute(recordPayerName, (name, deductionPoints) ->
                    (deductionPoints == null) ? -pointsToRemove : deductionPoints - pointsToRemove
            );
            updatePayerOverallBalanceMap(recordPayer, pointsToRemove);
            totalPointsRemoved += pointsToRemove;
        }
        payerBalanceRecords.removeAll(balancesToRemove);
        if (reducedBalance != null) { payerBalanceRecords.add(reducedBalance); }
        currentBalance -= points;
        log.debug(String.format("Balance reduced by %d to %d", totalPointsRemoved, currentBalance));
        return payerToTotalDeduction.entrySet().stream()
                .map(e -> new UserPointsTransactionDTO(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Alternate signature deduction method for removing points regardless of payer
     * @param points points to be deducted
     * @return List of deduction transactions
     */
    public List<UserPointsTransactionDTO> subtractBalance(final long points) {
        return subtractBalance(points, null);
    }

    /**
     * Returns a sorted list version of the overall balances by payer.
     * @return A sorted list of balances
     */
    public List<PayerBalance> getBalances() {
        return payerToOverallBalance.values()
                .stream()
                .sorted(BALANCE_CREATE_DATE_ORDERING)
                .collect(Collectors.toList());
    }
}


