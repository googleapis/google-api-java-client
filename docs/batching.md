---
title: Batching
---

# Batching

Each HTTP connection that your client makes results in overhead. To reduce
overhead, you can batch multiple API calls together into a single HTTP request.

The main classes of interest are [BatchRequest][batch-request] and 
[JsonBatchCallback][json-batch-callback]. The following example shows how to use
these classes with service-specific generated libraries:

```java
JsonBatchCallback<Calendar> callback = new JsonBatchCallback<Calendar>() {

  public void onSuccess(Calendar calendar, HttpHeaders responseHeaders) {
    printCalendar(calendar);
    addedCalendarsUsingBatch.add(calendar);
  }

  public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) {
    System.out.println("Error Message: " + e.getMessage());
  }
};

...

Calendar client = Calendar.builder(transport, jsonFactory, credential)
  .setApplicationName("BatchExample/1.0").build();
BatchRequest batch = client.batch();

Calendar entry1 = new Calendar().setSummary("Calendar for Testing 1");
client.calendars().insert(entry1).queue(batch, callback);

Calendar entry2 = new Calendar().setSummary("Calendar for Testing 2");
client.calendars().insert(entry2).queue(batch, callback);

batch.execute();
```

[batch-request]: https://googleapis.dev/java/google-api-client/latest/com/google/api/client/googleapis/batch/BatchRequest.html
[json-batch-callback]: https://googleapis.dev/java/google-api-client/latest/com/google/api/client/googleapis/batch/json/JsonBatchCallback.html