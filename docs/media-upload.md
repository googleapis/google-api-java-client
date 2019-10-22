---
title: Media Upload
---

# Media Upload

## Resumable media upload

When you upload a large media file to a server, use resumable media upload to
send the file chunk by chunk. The Google API generated libraries contain
convenience methods for interacting with resumable media upload, which was
introduced in the 1.7.0-beta version of the Google API Client Library for Java.

The resumable media upload protocol is similar to the resumable media upload
protocol described in the
[Google Drive API documentation][google-drive-documentation].

### Protocol design

The following sequence diagram shows how the resumable media upload protocol
works: ![Resumable Media Upload Protocol Diagram][resumable-media-upload-protocol-diagram]

### Implementation details

The main classes of interest are [MediaHttpUploader][media-http-uploader] and 
[MediaHttpProgressListener][media-http-progress-listener].

If methods in the service-specific generated libraries contain the `mediaUpload`
parameter in the Discovery document, then a convenience method is created for
these methods that takes an [InputStreamContent][input-stream-content] as a 
parameter. (For more about using media upload with the Google APIs Discovery
Service, see [Media upload][media-upload].)

For example, the `insert` method of the Drive API supports `mediaUpload`, and
you can use the following code to upload a file:

```java
class CustomProgressListener implements MediaHttpUploaderProgressListener {
  public void progressChanged(MediaHttpUploader uploader) throws IOException {
    switch (uploader.getUploadState()) {
      case INITIATION_STARTED:
        System.out.println("Initiation has started!");
        break;
      case INITIATION_COMPLETE:
        System.out.println("Initiation is complete!");
        break;
      case MEDIA_IN_PROGRESS:
        System.out.println(uploader.getProgress());
        break;
      case MEDIA_COMPLETE:
        System.out.println("Upload is complete!");
    }
  }
}

File mediaFile = new File("/tmp/driveFile.jpg");
InputStreamContent mediaContent =
    new InputStreamContent("image/jpeg",
        new BufferedInputStream(new FileInputStream(mediaFile)));
mediaContent.setLength(mediaFile.length());

Drive.Files.Insert request = drive.files().insert(fileMetadata, mediaContent);
request.getMediaHttpUploader().setProgressListener(new CustomProgressListener());
request.execute();
```

You can also use the resumable media upload feature without the service-specific
generated libraries. Here is an example:

```java
File mediaFile = new File("/tmp/Test.jpg");
InputStreamContent mediaContent =
    new InputStreamContent("image/jpeg",
        new BufferedInputStream(new FileInputStream(mediaFile)));
mediaContent.setLength(mediaFile.length());


MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, transport, httpRequestInitializer);
uploader.setProgressListener(new CustomProgressListener());
HttpResponse response = uploader.upload(requestUrl);
if (!response.isSuccessStatusCode()) {
  throw GoogleJsonResponseException(jsonFactory, response);
}
```

## Direct media upload

Resumable media upload is enabled by default, but you can disable it and use
direct media upload instead, for example if you are uploading a small file.
Direct media upload was introduced in the 1.9.0-beta version of the Google API
Client Library for Java.

Direct media upload uploads the whole file in one HTTP request, as opposed to
the resumable media upload protocol, which uploads the file in multiple
requests. Doing a direct upload reduces the number of HTTP requests but
increases the chance of failures (such as connection failures) that can happen
with large uploads.

The usage for direct media upload is the same as what is described above for
resumable media upload, plus the following call that tells
[MediaHttpUploader][media-http-uploader] to only do direct uploads:

```java
mediaHttpUploader.setDirectUploadEnabled(true);
```

[google-drive-documentation]: https://developers.google.com/drive/web/manage-uploads#resumable
[media-http-uploader]: https://googleapis.dev/java/google-api-client/latest/com/google/api/client/googleapis/media/MediaHttpUploader.html
[media-http-progress-listener]: https://googleapis.dev/java/google-api-client/latest/com/google/api/client/googleapis/media/MediaHttpUploaderProgressListener.html
[input-stream-content]: https://googleapis.dev/java/google-http-client/latest/com/google/api/client/http/InputStreamContent.html
[media-upload]: https://developers.google.com/discovery/v1/using#discovery-doc-methods-mediaupload
[resumable-media-upload-protocol-diagram]: https://github.com/googleapis/google-api-java-client/raw/master/Resumable-Media-Upload-Sequence-Diagram.png
