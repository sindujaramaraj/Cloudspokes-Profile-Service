Cloudspokes Profile Service
------------------------------------------------------------------------------------------

This service is developed using Google Application Engine.
Service can be accessed through both url and get.

URL: https://cloudspokes-profile-service.appspot.com/profile?username=test2&accessurl=true
When param accessurl is specified in the url, authorization is via oauth to obtain the session id.
After obtaining the session id, user is redirected the request url with response
For sample response check sample_json.js
The response contains session-id which you can use for testing.

GET: If you already have the session id then you can directly do a Get request.
index.html has sample implementation.
username is sent as url parameter and set session-id in the header.

Visit https://cloudspokes-profile-service.appspot.com/ for testing the service

Implementation
-----------------------------------------------------------------------------------------
Member and Recommendations are fetched using corresponding service.
To get the recent challenges, there are two approaches.
Challenges with status "Created" are considered as active challenges.

Method 1:
---------
This method used MemberChallenge service.
This service gives all the challenges for the user. There is no filters, limits, order by options are available for this service.
The data fetched contains category information also.
So once complete data is available, we have to sort.
A sorted map based on end date is created.
Then the last three items are returned from the sorted map.
This approach makes only one request. Total challenges count is available.
But lot of data is processed.
This service can added with filters and other options for more efficient solution.

java: ResetServiceHelper.getProcessedChallengesM1


Method 2:
---------
Use Participant service to get the challenges in which the user participated.
This gives challenge information and users status on this challenge.
The query to this service has order by end-date in descending order.
Filter is not available on this service, so some assumptions are made.
Limit is passed as 10. The most recent 10 challenges are returned.
The assumption is that of the 10 challenges, some will be active challenges
and atleast latest 3 past challenges will be contained.
Once this information is obtained, category information is required for the selected challenge.
ChallengeCategory service is used. This makes separate request for each challenge to get the category information.
This service doesn't seem to work with custom fields in query.
Separate request id fired for getting the total challenges.
No of request made in this method is more. But only minimal data is used.

java: ResetServiceHelper.getProcessedChallengesM2

Testing
------------------------------------------------------------------------------------------
See TestProfileService.java

Substitute username and session-id to run the test cases