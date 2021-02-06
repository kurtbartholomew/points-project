USER_ID=3
POINT_DEDUCTION=5000
LOCALHOST_URI_ACCOUNT="http://localhost:8080/api/v1/account/${USER_ID}"
MAIN_BALANCE_URI="${LOCALHOST_URI_ACCOUNT}/balance"
DEDUCT_BALANCE_URI="${LOCALHOST_URI_ACCOUNT}/payout"
CONTENT_TYPE="Content-Type: application/json"

echo "Adding points to User ${USER_ID}"

echo "Adding 300 points to DANNON"
curl -X POST "${MAIN_BALANCE_URI}" -H "${CONTENT_TYPE}" \
  -d '{"payer":"DANNON","points":300,"timestamp":"2020-10-31T10:00:00.00Z"}'

echo "Adding 200 points to UNILEVER"
curl -X POST "${MAIN_BALANCE_URI}" -H "${CONTENT_TYPE}" \
  -d '{"payer":"UNILEVER","points":200,"timestamp":"2020-10-31T11:00:00.00Z"}'

echo "Adding -200 points to DANNON"
curl -X POST "${MAIN_BALANCE_URI}" -H "${CONTENT_TYPE}" \
  -d '{"payer":"DANNON","points":-200,"timestamp":"2020-10-31T15:00:00.00Z"}'

echo "Adding 10000 points to MILLER COORS"
curl -X POST "${MAIN_BALANCE_URI}" -H "${CONTENT_TYPE}" \
  -d '{"payer":"MILLER COORS","points":10000,"timestamp":"2020-11-01T02:00:00.00Z"}'

echo "Adding 1000 points to DANNON"
curl -X POST "${MAIN_BALANCE_URI}" -H "${CONTENT_TYPE}" \
  -d '{"payer":"DANNON","points":1000,"timestamp":"2020-11-02T14:00:00.00Z"}'

echo "Current balance for User ${USER_ID}"
curl --silent "${MAIN_BALANCE_URI}" -H "${CONTENT_TYPE}" | json_pp

echo "Removing / paying out ${POINT_DEDUCTION} points from User ${USER_ID}"
curl --silent -X POST "${DEDUCT_BALANCE_URI}" -H "${CONTENT_TYPE}" \
  -d "{\"points\":${POINT_DEDUCTION}}" | json_pp

echo "Current points balance for User ${USER_ID}"
curl --silent "${MAIN_BALANCE_URI}" -H "${CONTENT_TYPE}" | json_pp

echo "Removing remaining points script idempotence..."
# THIS NEEDS TO BE UPDATED IF POINT_DEDUCTION IS CHANGED :/
curl --silent -X POST "${DEDUCT_BALANCE_URI}" -H "${CONTENT_TYPE}" \
  -d '{"points":6300}' | json_pp
echo ""

echo "Current balance for User ${USER_ID}"
curl --silent "${MAIN_BALANCE_URI}" -H "${CONTENT_TYPE}" | json_pp