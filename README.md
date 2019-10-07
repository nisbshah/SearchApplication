# Retirement plan Search Application

This project contains source code and supporting files for a serverless application to search the Retirement plans based on the Plan Name, Sponsor Name and State. The application is divided in to the following 2 parts,

1) Loading all the data into the Elastic Search
2) Expose an API using Api gateway and Lambda to search on Elastic Search

### Loading all the data into the Elastic Search
1) Upload CSV file manually to S3
2) Above event triggers `ElasticSearchIndexerFunction`  Lambda 
3) `ElasticSearchIndexerFunction` Lambda does the following,
    - reads the file and converts each CSV record into JSON format
    - Use Elastic Search SDK bulk API to load records into Elastic Search

### Expose an API using Api gateway and Lambda to search on Elastic Search
1) Create an API using Api gateway which interfaces with `SearchFunction` Lambda
2) `SearchFunction` Lambda will use Elastic Search SDK to send the search query to Elastic Search and get the response.
3) Search should be allowed by Plan name, Sponsor name and Sponsor State which refers to the following fields in the input data
    PLAN_NAME
    SPONSOR_DFE_NAME
    SPONS_DFE_MAIL_US_STATE


### Sample calls to the API

#### Search by Plan Name
```bash
curl -X GET \
  'https://13n0tkqquk.execute-api.us-west-2.amazonaws.com/Prod/search?key=planname&value=PREMIUM%20INSPECTION%20& TESTING, INC. 401K PLAN 401K PLAN=&%20TESTING,%20INC.%20401K%20PLAN%20401K%20PLAN='
```

#### Search by Sponsor Name
```bash
curl -X GET \
  'https://13n0tkqquk.execute-api.us-west-2.amazonaws.com/Prod/search?key=sponsorname&value=INNOVATIVE%20HEALTH%20MANAGEMENT%20PARTNER%20LLC'
```

#### Search by State
```bash
curl -X GET \
  'https://13n0tkqquk.execute-api.us-west-2.amazonaws.com/Prod/search?key=state&value=ca'
```
