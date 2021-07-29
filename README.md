App downloads images from Flickr as per the user query or shows the recent images by default.
User can also navigate in the Flickr site within the app using the app's WebView.

Note:
1. Uses AsyncTask for downloading the JSON
2. Uses HandlerThreads and LruCache for image downloading and caching
3. IntentService and AlarmManager for periodic polling (to check for new images if user checks that option) and creating notifications
4. Broadcast Receivers to wake the Service on reboot and for showing notifications only when the user is not using the app.
5. Picasso or Glide could have been used to greatly simplify the image downloading and caching part but since this app is also meant for demonstrating the usage of HandlerThread and Handler classes, I have implemented the required functionalities using the aforementioned classes.
