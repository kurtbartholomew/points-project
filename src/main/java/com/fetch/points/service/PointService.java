package com.fetch.points.service;

import com.fetch.points.api.exception.InvalidPointDeductionException;
import com.fetch.points.api.resource_dto.UserPointsTransactionDTO;
import com.fetch.points.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Service
public class PointService {

    private final static ConcurrentMap<User, UserBalance> usersToUserBalanceMap = new ConcurrentHashMap<>();

    /**
     * Public testing util for balance confirmation
     * @param userId the id used to construct a user
     * @return True/False depending on if User has at any point had a balance
     */
    public boolean isUserBalancePresent(final long userId) {
        final User user = new User(userId);
        return usersToUserBalanceMap.containsKey(user);
    }


    /**
     * Public testing util to prevent integration tests from stampeding each other
     */
    public void resetUserData() {
        usersToUserBalanceMap.clear();
    }

    public UserBalance getOrCreateUserBalance(final User user) {
        return getOrCreateUserBalance(user, null);
    }

    private UserBalance getOrCreateUserBalance(final User user, final Long points) {
        // TODO: Figure out if I should just ignore the initial points and
        //       eagerly create a user in the name of simplicity.
        boolean userIsPresent = usersToUserBalanceMap.containsKey(user);
        if (userIsPresent) {
            return usersToUserBalanceMap.get(user);
        } else {
            if (points != null && points < 0) {
                String errorMsg = "Unable to deduct points. User balance does not exist.";
                log.error(errorMsg);
                throw new InvalidPointDeductionException(errorMsg);
            }
            return usersToUserBalanceMap.compute(user, (k,v) -> new UserBalance());
        }
    }

    public void modifyPointsBalance(final UserPointsTransactionDTO pointsTransaction) {
        final long points = pointsTransaction.getPoints();
        // TODO: Remember to throw a reminder at the response level if 0
        if (points == 0) { return; }
        final User user = new User(pointsTransaction.getUserId());
        final UserBalance userBalance = getOrCreateUserBalance(user, points);

        synchronized (usersToUserBalanceMap) {
            Payer payer = new Payer(pointsTransaction.getPayerName());
            ZonedDateTime datetime = pointsTransaction.getTransactionDate();
            if (points > 0) {
                userBalance.addBalance(points, datetime, payer);
            } else {
                userBalance.subtractBalance(-points, payer);
            }
        }
    }

    public List<UserPointsTransactionDTO> deductPointsBalance(final long userId, final long points) {
        if (points == 0) { return Collections.emptyList(); }
        final User user = new User(userId);
        List<UserPointsTransactionDTO> transactions;
        synchronized (usersToUserBalanceMap) {
            transactions = getOrCreateUserBalance(user, points).subtractBalance(points);
        }
        return transactions;
    }

    public List<PayerBalance> getPointsBalance(long userId) {
        final User user = new User(userId);
        return getOrCreateUserBalance(user).getBalances();
    }
}
