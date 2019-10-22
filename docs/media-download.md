---
title: Media Download
---

# Media Download

## Resumable media downloads

When you download a large media file from a server, use resumable media download
to download the file chunk by chunk. The Google API generated libraries contain
convenience methods for interacting with resumable media download, which was
introduced in the 1.9.0-beta version of the Google API Client Library for Java.
    
The resumable media download protocol is similar to the resumable media upload
protocol, which is described in the
[Google Drive API documentation][google-drive-documentation].

### Implementation details

The main classes of interest are [`MediaHttpDownloader`][media-http-downloader]
and [`MediaHttpDownloaderProgressListener`][media-http-downloader-progress-listener].
Media content is downloaded in chunks, and chunk size is configurable. If a
server error is encountered in a request, then the request is retried.

If methods in the service-specific generated libraries support download in the
Discovery document, then a convenient download method is created for these
methods that takes in an [`OutputStream`][output-stream]. (For more about using
media download with the Google APIs Discovery Service, see
[Media download][media-download].)

For example:

```java
class CustomProgressListener implements MediaHttpDownloaderProgressListener {
  public void progressChanged(MediaHttpDownloader downloader) {
    switch (downloader.getDownloadState()) {
      case MEDIA_IN_PROGRESS:
        System.out.println(downloader.getProgress());
        break;
      case MEDIA_COMPLETE:
        System.out.println("Download is complete!");
    }
  }
}

OutputStream out = new FileOutputStream("/tmp/driveFile.jpg");

DriveFiles.Get request = drive.files().get(fileId);
request.getMediaHttpDownloader().setProgressListener(new CustomProgressListener());
request.executeMediaAndDownloadTo(out);
```

You can also use this feature without service-specific generated libraries.
Here is an example:

```java
OutputStream out = new FileOutputStream("/tmp/Test.jpg");

MediaHttpDownloader downloader = new MediaHttpDownloader(transport, httpRequestInitializer);
downloader.setProgressListener(new CustomProgressListener());
downloader.download(requestUrl, out);
```

## Direct media download

Resumable media download is enabled by default, but you can disable it and use
direct media download instead, for example if you are downloading a small file.
Direct media download was introduced in the 1.9.0-beta version of the Google API
Client Library for Java.

Direct media download downloads the whole media content in one HTTP request, as
opposed to the resumable media download protocol, which can download in multiple
requests. Doing a direct download reduces the number of HTTP requests but
increases the chance of failures (such as connection failures) that can happen
with large downloads.

The usage is the same as what is described above, plus the following call that
tells [`MediaHttpDownloader`][media-http-downloader] to do direct downloads:

```java
mediaHttpDownloader.setDirectDownloadEnabled(true);
```

[google-drive-documentation]: https://developers.google.com/drive/web/manage-uploads#resumable
[media-http-downloader]: https://googleapis.dev/java/google-api-client/latest/com/google/api/client/googleapis/media/MediaHttpDownloader.html
[media-http-downloader-progress-listener]: https://googleapis.dev/java/google-api-client/latest/com/google/api/client/googleapis/media/MediaHttpDownloaderProgressListener.html
[output-stream]: https://docs.oracle.com/javase/7/docs/api/java/io/OutputStream.html
[media-download]: https://developers.google.com/discovery/v1/using#discovery-doc-methods-mediadownload