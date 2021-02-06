## Fetch Rewards Points Take-home Project

### Environment Preparation for Use

If you want to run natively, you'll have to install:
- `Maven`
- `Java 11`+.

Install however you want.
- I use `brew` (Mac OS) for Maven and a version manager for Java.

If you want to run it in a container, you'll need to install `Docker`.

#### Testing Natively

1. Install the required dependencies and build the project.
  - `mvn install`
2. Run the jar with the spring boot app server bundled in. (Uses port 8080)
  - `mvn spring-boot:run`

#### Testing Via Docker

1. Install and have docker running.
2. Build and run the image with the following command:
```
docker run -it --rm --name kurt-points-project \
       -v "$(pwd)":/usr/src/points \
       -w /usr/src/points \
       -p "8080:8080" \
       maven:3.6.3-openjdk-11 mvn test spring-boot:run
```

### Design Anatomy

#### Included Features

- REST api with 3 endpoints
  - (get points balance): GET `/api/v1/account/:user_id/balance`
  - (add to point balance): POST `/api/v1/account/:user_id/balance`
  - (deduct from points balance): POST `/api/v1/account/:user_id/payout`
- Home web page with link to api docs (to prevent improper navigation) (no favicon shame...)
- Swagger/OpenAPI REST endpoints *general* documentation (*swagger autodoc parses static inner models oddly, beware :(* )
- Importable Postman Collection (with solution examples)
- Example Curl Requests shell script to run against local app (idempotent)
- Main data storage in concurrent data structure in service (ConcurrentHashMap)
- Unit Tests for:
  - Resource DTO's
  - Custom Date JSON Serializer
  - Main Domain Object (contains basic test for design problem)
  - Main View Controller
- Integration tests  
  - Main Api Controller (contains full REST test for design problem)
- Dockerfile for running app via image
- Javadoc for domain object with most complex logic

#### Design Considerations
- Design doc asks for `list all positive points per payer` and displays a balance of `UNILEVER, 0 points`
  - Opted to `list all positive points per payer` for better times on read complexity
  - Likely in a production application, it is better retain the zero balance for data integrity
- Intended for good compromise between reads and writes
  - Reads are time complexity O(k) (k is distinct payers for a user)
  - Writes are between time complexity O(log n) and O(n log n) (n is distinct balance records for a user)
- Intended for moderate memory conservation
  - Doesn't eagerly create users (waits for positive modification)
  - Removes entries of payer totals when total for that user is zeroed out
    - Normally these entries would be retained in service of returning all balances (including 0)
- Use of concurrent main data storage structure and `synchronized` modification of contents
- Most constructs are largely final and treated as immutable.
- Record keys are largely Strings wrapped in objects that would be DB Entities (User, Payer)
- Chose POST instead of DELETE method for point payout/deduction since it does not remove the resource

#### Data Structures and Time/Space Complexity for User Flows

`NOTE: All Time/Space Complexity (T)/(S where relevant) is upper bound only. These are my estimates so please forgive me. ;)`

For reference, `n` is # of `distinct balance records` and `k` is # of `distinct payers`
- Major Data Structures
  - ConcurrentHashmap for mapping `User` to their points collection `UserBalance`
  - `UserBalance` houses a TreeSet for tracking temporal records of points `PayerBalance`
    - Balance records could also have been called `UserPayerBalance` more accurately
    - TreeSet ordered by Comparator for creation date
  - `UserBalance` also uses a HashMap to map `Payer` to `User`'s balance for that `Payer`
- Get Balance of Points: (T)O(k log k) / (S)O(k)
  - Lookup of `UserBalance` by `UserId`: (T)O(1) lookup in HashMap (assuming normal dist of keys)
  - Traverse `User`'s payerToOverallBalance map: (T)O(k) read
  - Collect into List and Sort: (T)O(k) / (S)O(k) create, (T)O(k log k or k based on sort type)
- Add/Modification of Points: (T)O(log n)
  - Lookup of `UserBalance` by `UserId`: (T)O(1) lookup in HashMap
  - Create a user if needed: (T)O(1) insert in HashMap
  - Lookup of `User`'s current total balance: (T)O(1) lookup in UserBalance object
  - Insert into `payerBalanceRecords` TreeSet: Amortized (T)O(log n)
  - Add/Update user's payerOverallBalance map: (T)O(1)
  - Special Case: `Modification includes negative points` - See `Deduction of Points`
- Deduction of Points: [Average] (T)O(log n) - [Worst] (T)O(n log n) / (S)O(n)
  - Lookup of `UserBalance` by `UserId`: (T)O(1) lookup in HashMap
  - Iterate through (potentially all) balance records in TreeSet: (T)O(n)
  - Update overall balance for payer in map: (T)O(n)
  - Insert into deduction by payer linked hashmap: (T)O(n) (S)O(n)  
  - Place (potentially all) balances in List: (T)O(1) (S)O(n)
  - Remove (potentially all) balance nodes in TreeSet: (T)O(n log n) (S)O(n)
  - Add final modified balance (if any left) to TreeSet: (T)O(log n)
  - Traverse deduction by payer map: (T)O(k)
  - Collect into List (ordered due to TreeSet ordering and insertion time): (T)O(k) (S)O(k)

#### Places for Improvement
- Improve Swagger Configuration since inner static models are parsed out of the box :(
- Expand surface area of resource DTO validation tests beyond basic implementation
- Optimize for reads or writes depending on usage patterns
- Surgically shrink synchronized blocks to reduce latency
- Based on need, choose memory-appropriate default size for in memory collections
- Use a higher precision method of datetime storage to avoid ordering collisions
- Support more datetime formats and even dates or time formats for timestamps
- Additional instrumentation for profiling and application health
- Additional uniform logging for debugging and error visibility
- Add whatever flavor of durable data store!