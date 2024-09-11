# Location Reminder App

## Setup Instructions

To run this application, you need to add two configuration files: `secrets.properties` and `google-services.json`.

### 1. `secrets.properties`

Create a file named `secrets.properties` in the root directory of the project. This file should contain the following line:

`MAPS_API_KEY=GOOGLE_MAP_SECRET_KEY`

You can obtain the `GOOGLE_MAP_SECRET_KEY` by following the instructions at [Google Maps API Key Documentation](https://developers.google.com/maps/documentation/android-sdk/get-api-key).

### 2. `google-services.json`

Add the `google-services.json` file to the `app` directory. This file is necessary for Firebase Authentication.

You can obtain the `google-services.json` file from the Firebase Console in the Authentication section.

## Additional Information

Make sure to follow the setup instructions carefully to ensure the application works correctly.